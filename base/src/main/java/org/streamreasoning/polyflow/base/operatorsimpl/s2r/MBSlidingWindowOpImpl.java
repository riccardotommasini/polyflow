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
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.SegmentFactory;
import org.streamreasoning.polyflow.api.sds.timevarying.TimeVarying;
import org.streamreasoning.polyflow.api.secret.report.Report;
import org.streamreasoning.polyflow.api.secret.tick.Ticker;
import org.streamreasoning.polyflow.api.secret.tick.secret.TickerFactory;
import org.streamreasoning.polyflow.api.secret.time.Time;
import org.streamreasoning.polyflow.api.secret.time.TimeInstant;
import org.streamreasoning.polyflow.base.benchmark.Benchmark;
import org.streamreasoning.polyflow.base.operatorsimpl.s2r.state.MapMultiBufferState;
import org.streamreasoning.polyflow.base.sds.TimeVaryingObject;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class MBSlidingWindowOpImpl<I, R extends Iterable<?>> implements StreamToRelationOperator<I, R> {

    private static final Logger log = Logger.getLogger(MBSlidingWindowOpImpl.class);
    protected final Ticker ticker;
    protected Tick tick;
    protected final Time time;
    protected final String name;
    protected final MultiBufferState<I, R> state;
    protected Report report;
    private final long width;
    private List<Window> reported_windows;
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
        this.reported_windows = new ArrayList<>();
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
        return new TimeVaryingObject<>(this, name);
    }


    @Override
    public Segment<I, R> content(long t_e) {
        // If some windows matched the report clause, return the last one that did so
        if (!reported_windows.isEmpty()) {
            return reported_windows.stream()
                    .max(Comparator.comparingLong(Window::getC))
                    .map(state::get).orElse(state.emptySegment());
        }
        //Else return the last window closed
        else {
            Optional<Window> max = stream(state.windows())
                    .filter(w -> w.getO() < t_e && w.getC() < t_e)
                    .max(Comparator.comparingLong(Window::getC));

            if (max.isPresent())
                return state.get(max.get());

            return state.emptySegment();
        }
    }

    @Override
    public List<Segment<I, R>> getContents(long t_e) {
        if (!reported_windows.isEmpty()) {
            return reported_windows.stream()
                    .max(Comparator.comparingLong(Window::getC))
                    .map(w -> Collections.singletonList(state.get(w))).orElseGet(Collections::emptyList);
        } else
            return stream(state.windows())
                    .filter(w -> w.getO() < t_e && t_e < w.getC())
                    .map(state::get).collect(Collectors.toList());
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

        benchmark("add", ts, () -> {
            Segment<I, R> content = state.get(active);
            boolean newWindow = content == null;
            if (newWindow) {
                log.debug("Computing Window [" + active.getO() + "," + active.getC() + "] if absent");
                content = state.create(active);
                r_stream.entrySet().stream()
                        .filter(ee -> active.getO() <= ee.getValue() && ee.getValue() <= active.getC())
                        .map(Map.Entry::getKey)
                        .forEach(content::add);
            }

            r_stream.entrySet().removeIf(ee -> ee.getValue() < active.getO());
            r_stream.put(arg, ts);

            stream(state.windows()).forEach(w -> {
                if (w.getO() <= ts && ts <= w.getC()) {
                    log.debug("Adding element [" + arg + "] to Window [" + w.getO() + "," + w.getC() + "]");
                    state.get(w).add(arg);
                }
                if (w.getC() < ts) {
                    log.debug("Scheduling for Eviction [" + w.getO() + "," + w.getC() + "]");
                    to_evict.add(w);
                }
            });
        });

        benchmark("report", ts, () -> {
        if (ticker.tick(ts)) {
            stream(state.windows())
                    .filter(w -> report.report(w, getWindowContent(w), ts, System.currentTimeMillis()))
                    .max(Comparator.comparingLong(Window::getC))
                    .ifPresent(window -> {
                        reported_windows.add(window);
                        time.addEvaluationTimeInstants(new TimeInstant(ts));
                    });
        }});
        time.setAppTime(ts);
    }

    private Segment<I, R> getWindowContent(Window w) {
        Segment<I, R> segment = state.get(w);
        return segment != null ? segment : state.emptySegment();
    }

    @Override
    public void evict() {
        to_evict.forEach(state::evict);
        to_evict.clear();
        reported_windows = new ArrayList<>();
    }

    @Override
    public void evict(long ts) {
        benchmark("evict", ts, () -> {
            stream(state.windows()).forEach(w -> {
                if (w.getC() < ts) to_evict.add(w);
            });
            evict();
        });
    }

    private void benchmark(String operation, long ts, Runnable runnable) {
        bench.measure(operation, "MBSlidingWindow", state.toString(), state.getSegmentName(), ts, runnable);
    }

    private java.util.stream.Stream<Window> stream(Iterable<Window> windows) {
        return java.util.stream.StreamSupport.stream(windows.spliterator(), false);
    }
}
