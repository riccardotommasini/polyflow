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
    private Window reportedWindow;
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
        // A policy may not have selected a window yet, or eviction may have cleared it.
        return reportedWindow != null ? state.segment(reportedWindow) : state.emptySegment();
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

        // Reporting decides when to evaluate; extraction is measured during materialization.
        if (ticker.tick(ts)) {
            scope(ts);
            Window oldestStillActive = activeWindows.peekFirst();
            if (oldestStillActive != null && report.report(oldestStillActive, state.segment(oldestStillActive), ts, System.currentTimeMillis())) {
                reportedWindow = oldestStillActive;
                time.addEvaluationTimeInstants(new TimeInstant(ts));
            }
        }
        time.setAppTime(ts);
    }

    private void updateActiveWindow(long ts) {
        while (!activeWindows.isEmpty() && activeWindows.peekFirst().getC() < ts) {
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
        reportedWindow = null;
    }

    @Override
    public void evict(long ts) {
        benchmark("evict", ts, () -> {
            updateActiveWindow(ts);
            state.evict(evictionTs);
            evict();
        });
    }

    private void benchmark(String operation, long ts, Runnable runnable) {
        bench.measure(operation, "SBSlidingWindow", "SingleBuffer", state.toString(), ts, runnable);
    }
}
