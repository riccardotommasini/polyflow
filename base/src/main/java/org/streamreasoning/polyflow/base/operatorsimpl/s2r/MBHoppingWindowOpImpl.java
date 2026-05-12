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
import org.streamreasoning.polyflow.base.sds.TimeVaryingObject;

import java.util.*;
import java.util.stream.Collectors;

public class MBHoppingWindowOpImpl<I, R extends Iterable<?>> implements StreamToRelationOperator<I, R> {

    protected final String name;
    private static final Logger log = Logger.getLogger(MBHoppingWindowOpImpl.class);

    protected final Ticker ticker;
    protected Tick tick;
    protected final Time time;

    protected final MultiBufferState<I, R> state;
    private long t0;
    private final long width, slide;

    protected Report report;
    private Window to_report;
    private List<Window> reported_windows;

    private Set<Window> to_evict;
    private long toi;

    public MBHoppingWindowOpImpl(Tick tick, Time time, String name, MultiBufferState<I, R> state, Report report,
                                 long width, long slide) {
        this.tick = tick;
        this.time = time;
        this.name = name;
        this.state = state;
        this.report = report;
        this.width = width;
        this.slide = slide;
        this.reported_windows = new ArrayList<>();
        this.to_evict = new HashSet<>();
        this.t0 = time.getScope();
        this.toi = 0;
        this.ticker = TickerFactory.tick(tick, this);
        Logger.getRootLogger().setLevel(Level.OFF);
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
    public boolean named() {
        return !name.isEmpty();
    }

    /**
     * Returns the content of the last window closed before time t_e. If no such window exists, returns an empty content
     */
    @Override
    public Segment<I, R> content(long t_e) {
        // If some windows matched the report clause, return the last one that did so
        if (!reported_windows.isEmpty()) {
            return reported_windows.stream()
                    .max(Comparator.comparingLong(Window::getC))
                    .map(state::get).orElse(state.emptySegment());
        }
        // return state.emptySegment();
        // Else return the last window closed
        else {
            Optional<Window> max = stream(state.windows())
                    .filter(w -> w.getO() < t_e && w.getC() < t_e)
                    .max(Comparator.comparingLong(Window::getC));

            if (max.isPresent())
                return state.get(max.get());

            return state.emptySegment();
        }
    }

    /**
     * Returns the content of all the windows closed before time t_e as a list of contents. If no such windows exist, returns an empty list of contents
     */
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

    /**
     * Creates all the windows that can possibly contain the given timestamp
     */
    private void scope(long t_e) {
        long c_sup = (long) Math.ceil(((double) Math.abs(t_e - t0) / (double) slide)) * slide;
        long o_i = c_sup - width;
        log.debug("Calculating the Windows to Open. First one opens at [" + o_i + "] and closes at [" + c_sup + "]");

        do {
            log.debug("Computing Window [" + o_i + "," + (o_i + width) + ") if absent");
            state.create(new WindowImpl(o_i, o_i + width));
            o_i += slide;

        } while (o_i <= t_e);
    }


    @Override
    public void compute(I arg, long ts) {

        log.debug("Received element (" + arg + "," + ts + ")");

        if (time.getAppTime() > ts) {
            log.error("OUT OF ORDER NOT HANDLED");
            throw new OutOfOrderElementException("(" + arg + "," + ts + ")");
        }

        scope(ts);

        stream(state.windows()).forEach(
                w -> {
                    log.debug("Processing Window [" + w.getO() + "," + w.getC() + ") for element (" + arg + "," + ts + ")");
                    if (w.getO() <= ts && ts < w.getC()) {
                        log.debug("Adding element [" + arg + "] to Window [" + w.getO() + "," + w.getC() + ")");
                        state.get(w).add(arg);
                    }
                    if (ts >= w.getC()) {
                        log.debug("Scheduling for Eviction [" + w.getO() + "," + w.getC() + ")");
                        to_evict.add(w);
                    }
                });

        if (ticker.tick(ts)) {
            stream(state.windows())
                    .filter(w -> report.report(w, getWindowContent(w), ts, System.currentTimeMillis()))
                    .max(Comparator.comparingLong(Window::getC))
                    .ifPresent(window -> {
                        reported_windows.add(window);
                        time.addEvaluationTimeInstants(new TimeInstant(ts));
                    });
        }
        time.setAppTime(ts);
    }

    @Override
    public TimeVarying<R> get() {
        return new TimeVaryingObject<>(this, name);
    }

    private Segment<I, R> getWindowContent(Window w) {
        Segment<I, R> segment = state.get(w);
        return segment != null ? segment : state.emptySegment();
    }

    @Override
    public void evict() {
        to_evict.forEach(w -> {
            log.debug("Evicting [" + w.getO() + "," + w.getC() + ")");
            state.evict(w);
            if (toi < w.getC())
                toi = w.getC() + slide;
        });
        to_evict.clear();
        reported_windows = new ArrayList<>();
    }

    @Override
    public void evict(long ts) {
        stream(state.windows()).forEach(w -> {if (w.getC() < ts) to_evict.add(w);});
        evict();
    }

    private java.util.stream.Stream<Window> stream(Iterable<Window> windows) {
        return java.util.stream.StreamSupport.stream(windows.spliterator(), false);
    }

}
