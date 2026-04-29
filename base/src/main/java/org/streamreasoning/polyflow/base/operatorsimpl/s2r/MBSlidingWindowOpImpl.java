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
import org.streamreasoning.polyflow.base.operatorsimpl.s2r.state.MapMultiBufferState;
import org.streamreasoning.polyflow.base.sds.TimeVaryingObject;

import java.util.*;
import java.util.stream.Collectors;

public class MBSlidingWindowOpImpl<I, W, R extends Iterable<?>> implements StreamToRelationOperator<I, R> {

    private static final Logger log = Logger.getLogger(MBHoppingWindowOpImpl.class);
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
    private Map<I, Long> d_stream;

    public MBSlidingWindowOpImpl(Tick tick, Time time, String name, SegmentFactory<I, R> sf, Report report,
                                 long width) {
        this(tick, time, name, new MapMultiBufferState<>(sf), report, width);
    }

    public MBSlidingWindowOpImpl(Tick tick, Time time, String name, MultiBufferState<I, R> state, Report report,
                                 long width) {
        this.tick = tick;
        this.time = time;
        this.name = name;
        this.state = state;
        this.report = report;
        this.width = width;
        this.reported_windows = new ArrayList<>();
        this.to_evict = new HashSet<>();
        this.r_stream = new HashMap<>();
        this.d_stream = new HashMap<>();
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
        long o_i = t_e - width;
        log.debug("Calculating the Windows to Open. First one opens at [" + o_i + "] and closes at [" + t_e + "]");
        log.debug("Computing Window [" + o_i + "," + (o_i + width) + ") if absent");

        WindowImpl active = new WindowImpl(o_i, t_e);
        state.create(active);
        return active;
    }

    @Override
    public void compute(I arg, long ts) {
        log.debug("Received element (" + arg + "," + ts + ")");

        if (time.getAppTime() > ts) {
            log.error("OUT OF ORDER NOT HANDLED");
            throw new OutOfOrderElementException("(" + arg + "," + ts + ")");
        }

        Window active = scope(ts);
        Segment<I, R> content = state.get(active);

        r_stream.entrySet().stream().filter(ee -> ee.getValue() < active.getO()).forEach(ee -> d_stream.put(ee.getKey(), ee.getValue()));

        r_stream.entrySet().stream().filter(ee -> ee.getValue() >= active.getO()).map(Map.Entry::getKey).forEach(content::add);

        r_stream.put(arg, ts);
        content.add(arg);

        if (ticker.tick(ts)) {
            if (report.report(active, content, ts, System.currentTimeMillis())) {
                reported_windows.add(active);
                time.addEvaluationTimeInstants(new TimeInstant(ts));
            }
        }
        time.setAppTime(ts);


        //REMOVE ALL THE WINDOWS THAT CONTAIN DSTREAM ELEMENTS
        //Theoretically active window has always size 1
        d_stream.entrySet().forEach(ee -> {
            log.debug("Evicting [" + ee + "]");

            stream(state.windows()).forEach(window -> {
                if (window.getO() <= ee.getValue() && window.getC() < ee.getValue())
                    schedule_for_eviction(window);
            });
            r_stream.remove(ee);
        });

    }

    private void schedule_for_eviction(Window w) {
        to_evict.add(w);
    }

    @Override
    public void evict() {
        to_evict.forEach(state::evict);
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
