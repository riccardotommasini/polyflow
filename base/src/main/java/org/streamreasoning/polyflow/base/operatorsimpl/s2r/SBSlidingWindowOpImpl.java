package org.streamreasoning.polyflow.base.operatorsimpl.s2r;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.streamreasoning.polyflow.api.enums.Tick;
import org.streamreasoning.polyflow.api.exceptions.OutOfOrderElementException;
import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.Window;
import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.WindowImpl;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.SingleBufferState;
import org.streamreasoning.polyflow.api.sds.timevarying.TimeVarying;
import org.streamreasoning.polyflow.api.secret.report.Report;
import org.streamreasoning.polyflow.api.secret.report.strategies.OnWindowClose;
import org.streamreasoning.polyflow.api.secret.tick.Ticker;
import org.streamreasoning.polyflow.api.secret.tick.secret.TickerFactory;
import org.streamreasoning.polyflow.api.secret.time.Time;
import org.streamreasoning.polyflow.api.secret.time.TimeInstant;
import org.streamreasoning.polyflow.base.benchmark.Benchmark;
import org.streamreasoning.polyflow.base.sds.TimeVaryingObject;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class SBSlidingWindowOpImpl<I, R extends Iterable<?>> implements StreamToRelationOperator<I, R> {

    private static final Logger log = Logger.getLogger(SBSlidingWindowOpImpl.class);

    protected final Ticker ticker;
    protected Tick tick;
    protected final Time time;
    protected final String name;
    protected final SingleBufferState<I, R> state;
    protected Report report;
    private final long width;
    // A close-triggered evaluation reads its completed scope until the task evicts it.
    private Window completedReportWindow;
    private long completedReportTime;
    private final Benchmark bench;
    private final Deque<Window> activeWindows = new ArrayDeque<>();
    private long evictionTs = Long.MIN_VALUE;

    public SBSlidingWindowOpImpl(Tick tick, Time time, String name, SingleBufferState<I, R> state, Report report, long width) {
        this.tick = tick;
        this.time = time;
        this.name = name;
        this.state = state;
        this.report = report;
        this.width = width;
        this.ticker = TickerFactory.tick(tick, this);
        Logger.getRootLogger().setLevel(Level.OFF);
        bench = new Benchmark(Path.of(System.getProperty(
                "polyflow.bench.csv",
                "target/bench/window-measurements-SBSlidingWindow-"+width+".csv"
        )));
    }

    @Override
    public Report report() {
        return report;
    }

    @Override
    public Tick tick() {
        return tick;
    }

    @Override
    public Time time() {
        return time;
    }

    @Override
    public Segment<I, R> content(long t_e) {
        // Every input must be readable when another input triggers a shared-clock evaluation.
        // Reads select an arrival-created scope without changing the clock or scheduling reports.
        Window selected = completedReportWindow != null && completedReportTime == t_e
                ? completedReportWindow : oldestActiveWindow(t_e);
        return selected != null ? state.segment(selected) : state.emptySegment();
    }

    @Override
    public List<Segment<I, R>> getContents(long t_e) {
        return Collections.singletonList(content(t_e));
    }

    @Override
    public TimeVarying<R> get() {
        return new TimeVaryingObject<>(this, name) {
            @Override
            public void materialize(long ts) {
                // Include shared-buffer selection, segment construction, and coalesce().
                benchmark("extract", ts, () -> super.materialize(ts));
            }
        };
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean named() {
        return !name.isEmpty();
    }

    /** A conjunction containing OnWindowClose must inspect completed scopes. */
    private boolean reportsOnClose() {
        return java.util.Arrays.stream(report.strategies()).anyMatch(OnWindowClose.class::isInstance);
    }

    private void scope(long ts) {
        Window w = new WindowImpl(ts, ts + width);
        activeWindows.addLast(w);
        state.setWindow(w);
    }

    @Override
    public void compute(I arg, long ts) {
        log.debug("Received element (" + arg + "," + ts + ")");

        if (time.getAppTime() > ts) {
            log.error("OUT OF ORDER NOT HANDLED");
            throw new OutOfOrderElementException("(" + arg + "," + ts + ")");
        }

        // Insertion stores the element in the shared buffer; retrieval selects its window.
        benchmark("insert", ts, () -> state.append(arg));

        // Time-only reporting skips segment construction; content-based checks request it lazily.
        // Extraction remains measured during materialization.
        if (ticker.tick(ts)) {
            scope(ts);
            boolean onClose = reportsOnClose();
            // Periodic reads active windows; close reports must retain completed scopes
            // until evaluation. OnWindowClose itself preserves its strict close < ts rule.
            if (!onClose) updateActiveWindow(ts);
            Window oldest = activeWindows.peekFirst();
            Window candidate = onClose
                    ? (oldest != null && oldest.getC() < ts ? oldest : null)
                    : oldestActiveWindow(ts);
            if (candidate != null && report.reportLazy(candidate, () -> state.segment(candidate), ts, System.currentTimeMillis())) {
                if (onClose) {
                    completedReportWindow = candidate;
                    completedReportTime = ts;
                }
                time.addEvaluationTimeInstants(new TimeInstant(ts));
            }
        }
        time.setAppTime(ts);
    }

    /** Select the oldest retained arrival-created window containing the requested time. */
    private Window oldestActiveWindow(long ts) {
        for (Window window : activeWindows) {
            if (window.getO() > ts) return null;
            if (ts < window.getC()) return window;
        }
        return null;
    }

    private void updateActiveWindow(long ts) {
        while (!activeWindows.isEmpty() && activeWindows.peekFirst().getC() <= ts) {
            activeWindows.removeFirst();
        }

        if (!activeWindows.isEmpty()) {
            evictionTs = activeWindows.peekFirst().getO();
        } else {
            evictionTs = ts;
        }
    }

    @Override
    public void evict() {
        // An idle input expires against the shared clock, not its last local report.
        evict(time.getAppTime());
    }

    @Override
    public void evict(long ts) {
        benchmark("evict", ts, () -> {
            updateActiveWindow(ts);
            state.evict(evictionTs);
            completedReportWindow = null;
            // Remaining descriptors keep valid buffered data readable after evaluation.
        });
    }

    private void benchmark(String operation, long ts, Runnable runnable) {
        // Snapshot the incrementally maintained count before the operation timer starts.
        bench.measure(operation, "SBSlidingWindow", "SingleBuffer", state.toString(), ts, state.size(), -1, runnable);
    }
}
