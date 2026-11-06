package org.streamreasoning.polyflow.base.operatorsimpl.s2r;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.streamreasoning.polyflow.api.enums.Tick;
import org.streamreasoning.polyflow.api.exceptions.OutOfOrderElementException;
import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.Window;
import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.WindowImpl;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.MultiBufferState;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;
import org.streamreasoning.polyflow.api.sds.timevarying.TimeVarying;
import org.streamreasoning.polyflow.api.secret.report.Report;
import org.streamreasoning.polyflow.api.secret.tick.Ticker;
import org.streamreasoning.polyflow.api.secret.tick.secret.TickerFactory;
import org.streamreasoning.polyflow.api.secret.time.Time;
import org.streamreasoning.polyflow.api.secret.time.TimeInstant;
import org.streamreasoning.polyflow.base.benchmark.Benchmark;
import org.streamreasoning.polyflow.base.sds.TimeVaryingObject;

import java.nio.file.Path;
import java.util.*;

public class MBSlidingWindowOpImpl<I, R extends Iterable<?>> implements StreamToRelationOperator<I, R> {

    private static final Logger log = Logger.getLogger(MBSlidingWindowOpImpl.class);
    protected final Ticker ticker;
    protected Tick tick;
    protected final Time time;
    protected final String name;
    protected final MultiBufferState<I, R> state;
    protected Report report;
    private final long width;
    private Set<Window> to_evict;
    private Map<I, Long> r_stream;

    private final Benchmark bench;

    public MBSlidingWindowOpImpl(Tick tick, Time time, String name, MultiBufferState<I, R> state, Report report, long width) {
        this.tick = tick;
        this.time = time;
        this.name = name;
        this.state = state;
        this.report = report;
        this.width = width;
        this.to_evict = new HashSet<>();
        this.r_stream = new HashMap<>();
        this.ticker = TickerFactory.tick(tick, this);
        Logger.getRootLogger().setLevel(Level.OFF);
        bench = new Benchmark(Path.of(System.getProperty(
                "polyflow.bench.csv",
                "target/bench/window-measurements-MBSlidingWindow-"+width+".csv"
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
    public String getName() {
        return name;
    }


    @Override
    public TimeVarying<R> get() {
        return new TimeVaryingObject<>(this, name) {
            @Override
            public void materialize(long ts) {
                // Retrieve the maintained window segment and include coalesce() in extraction.
                benchmark("extract", ts, () -> super.materialize(ts));
            }
        };
    }


    /** Select the same arrival-created, half-open scope as single-buffer sliding. */
    private Optional<Window> oldestActiveWindow(long ts) {
        return stream(state.windows())
                .filter(w -> w.getO() <= ts && ts < w.getC())
                .min(Comparator.comparingLong(Window::getO));
    }

    @Override
    public Segment<I, R> content(long ts) {
        // Reading an idle input must not depend on whether it triggered the report.
        return oldestActiveWindow(ts).map(this::getWindowContent).orElseGet(state::emptySegment);
    }

    @Override
    public List<Segment<I, R>> getContents(long ts) {
        // Both read APIs expose one identical relation, including the empty case.
        return Collections.singletonList(content(ts));
    }


    private Window scope(long t_e) {
        log.debug("Calculating the Windows to Open. First one opens at [" + t_e + "] and closes at [" + (t_e+width) + "]");
        return new WindowImpl(t_e, t_e + width);
    }

    @Override
    public void compute(I arg, long ts) {
        log.debug("Received element (" + arg + "," + ts + ")");

        if (time.getAppTime() > ts) {
            log.error("OUT OF ORDER NOT HANDLED");
            throw new OutOfOrderElementException("(" + arg + "," + ts + ")");
        }

        Window active = scope(ts);

        // Charge maintenance of the per-window segments to insertion.
        benchmark("insert", ts, () -> {
            Segment<I, R> content = state.get(active);
            boolean newWindow = content == null;
            if (newWindow) {
                log.debug("Computing Window [" + active.getO() + "," + active.getC() + "] if absent");
                content = state.create(active);
                r_stream.entrySet().stream()
                        .filter(ee -> active.getO() <= ee.getValue() && ee.getValue() < active.getC())
                        .map(Map.Entry::getKey)
                        .forEach(content::add);
            }

            r_stream.entrySet().removeIf(ee -> ee.getValue() < active.getO());
            r_stream.put(arg, ts);

            // Use inclusive-open/exclusive-close membership, matching SB extraction.
            stream(state.windows()).forEach(w -> {
                if (w.getO() <= ts && ts < w.getC()) {
                    log.debug("Adding element [" + arg + "] to Window [" + w.getO() + "," + w.getC() + "]");
                    state.get(w).add(arg);
                }
                if (w.getC() <= ts) {
                    log.debug("Scheduling for Eviction [" + w.getO() + "," + w.getC() + "]");
                    to_evict.add(w);
                }
            });
        });

        // Time-only reporting skips segment construction; content-based checks request it lazily.
        // Extraction remains measured during materialization.
        if (ticker.tick(ts)) {
            // Select first: Periodic mutates its deadline, so probing every window
            // makes reporting depend on state iteration order and can consume a stale report.
            oldestActiveWindow(ts).ifPresent(window -> {
                if (report.reportLazy(window, () -> getWindowContent(window), ts, System.currentTimeMillis())) {
                    time.addEvaluationTimeInstants(new TimeInstant(ts));
                }
            });
        }
        time.setAppTime(ts);
    }

    private Segment<I, R> getWindowContent(Window w) {
        Segment<I, R> segment = state.get(w);
        return segment != null ? segment : state.emptySegment();
    }

    @Override
    public void evict() {
        // Another input can advance the shared clock while this input has no arrivals.
        evict(time.getAppTime());
    }

    private void evictWindows() {
        to_evict.forEach(state::evict);
        to_evict.clear();
    }

    @Override
    public void evict(long ts) {
        benchmark("evict", ts, () -> {
            stream(state.windows()).forEach(w -> {
                if (w.getC() <= ts) to_evict.add(w);
            });
            evictWindows();
        });
    }

    private void benchmark(String operation, long ts, Runnable runnable) {
        // Snapshot the incrementally maintained count before the operation timer starts.
        bench.measure(operation, "MBSlidingWindow", state.toString(), state.getSegmentName(), ts, -1, state.size(), runnable);
    }

    private java.util.stream.Stream<Window> stream(Iterable<Window> windows) {
        return java.util.stream.StreamSupport.stream(windows.spliterator(), false);
    }
}
