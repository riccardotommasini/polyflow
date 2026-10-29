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
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Reporting, reads and eviction select their scope from the evaluation timestamp so
 * independently arriving inputs can share an evaluation clock without a prior report.
 */
public class SBHoppingWindowOpImpl<I, R extends Iterable<?>> implements StreamToRelationOperator<I, R> {

    private static final Logger log = Logger.getLogger(SBHoppingWindowOpImpl.class);

    protected final Ticker ticker;
    protected Tick tick;
    protected final Time time;
    protected final String name;
    protected final SingleBufferState<I, R> state;
    protected Report report;
    private final long width;
    private final long slide;
    private final long t0;
    private final Benchmark bench;

    /** Reject invalid scopes and missing collaborators before initializing the operator. */
    public SBHoppingWindowOpImpl(Tick tick, Time time, String name, SingleBufferState<I, R> state, Report report, long width, long slide) {
        if (width <= 0 || slide <= 0) {
            throw new IllegalArgumentException("Window width and slide must be positive");
        }
        this.tick = Objects.requireNonNull(tick, "tick");
        this.time = Objects.requireNonNull(time, "time");
        this.name = Objects.requireNonNull(name, "name");
        this.state = Objects.requireNonNull(state, "state");
        this.report = Objects.requireNonNull(report, "report");
        this.width = width;
        this.slide = slide;
        this.ticker = TickerFactory.tick(tick, this);
        this.t0 = time.getScope();
        Logger.getRootLogger().setLevel(Level.OFF);
        bench = new Benchmark(Path.of(System.getProperty(
                "polyflow.bench.csv",
                "target/bench/window-measurements-SBHoppingWindow-"+width+".csv"
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
        // A read selects its own scope rather than depending on this input having reported.
        return state.segment(scope(t_e));
    }

    @Override
    public List<Segment<I, R>> getContents(long t_e) {
        return List.of(content(t_e));
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

    /**
     * Select [close-width, close) and make it the state's current window.
     * Integer arithmetic preserves the ceiling calculation for epoch millisecond timestamps.
     */
    private Window scope(long t_e) {
        long offset = Math.abs(Math.subtractExact(t_e, t0));
        long periods = offset / slide + (offset % slide == 0 ? 0 : 1);
        long close = Math.multiplyExact(periods, slide);
        Window window = new WindowImpl(Math.subtractExact(close, width), close);
        log.debug("Calculating the Window to Open. Opens at [" + window.getO()
                + "] and closes at [" + window.getC() + "]");
        state.setWindow(window);
        return window;
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
            Window win = scope(ts);
            if (report.report(win, state.segment(win), ts, System.currentTimeMillis())) {
                time.addEvaluationTimeInstants(new TimeInstant(ts));
            }
        }
        time.setAppTime(ts);
    }

    @Override
    public void evict() {
        // Calls without an explicit evaluation time use the shared application clock.
        evict(time.getAppTime());
    }

    @Override
    public void evict(long timestamp) {
        // Idle inputs expire at the requested evaluation scope, even with no previous report.
        Window window = scope(timestamp);
        benchmark("evict", timestamp, () -> state.evict(window.getO()));
    }

    private void benchmark(String operation, long ts, Runnable runnable) {
        bench.measure(operation, "SBHoppingWindow", "SingleBuffer", state.toString(), ts, runnable);
    }
}
