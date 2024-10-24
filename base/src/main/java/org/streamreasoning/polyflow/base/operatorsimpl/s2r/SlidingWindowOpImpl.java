package org.streamreasoning.polyflow.base.operatorsimpl.s2r;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.streamreasoning.polyflow.api.enums.Tick;
import org.streamreasoning.polyflow.api.exceptions.OutOfOrderElementException;
import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.Window;
import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.WindowImpl;
import org.streamreasoning.polyflow.api.sds.timevarying.TimeVarying;
import org.streamreasoning.polyflow.api.secret.content.Content;
import org.streamreasoning.polyflow.api.secret.content.ContentFactory;
import org.streamreasoning.polyflow.api.secret.report.Report;
import org.streamreasoning.polyflow.api.secret.tick.Ticker;
import org.streamreasoning.polyflow.api.secret.tick.secret.TickerFactory;
import org.streamreasoning.polyflow.api.secret.time.Time;
import org.streamreasoning.polyflow.api.secret.time.TimeInstant;
import org.streamreasoning.polyflow.base.sds.TimeVaryingObject;

import java.util.*;
import java.util.stream.Collectors;

public class SlidingWindowOpImpl<I, W, R extends Iterable<?>> implements StreamToRelationOperator<I, W, R> {

    private static final Logger log = Logger.getLogger(HoppingWindowOpImpl.class);
    protected final Ticker ticker;
    protected Tick tick;
    protected final Time time;
    protected final String name;
    protected final ContentFactory<I, W, R> cf;
    protected Report report;
    private final long a;
    private Map<Window, Content<I, W, R>> active_windows;
    private List<Window> reported_windows;
    private Set<Window> to_evict;
    private Map<I, Long> r_stream;
    private Map<I, Long> d_stream;


    public SlidingWindowOpImpl(Tick tick, Time time, String name, ContentFactory<I, W, R> cf, Report report,
                               long a) {

        this.tick = tick;
        this.time = time;
        this.name = name;
        this.cf = cf;
        this.report = report;
        this.a = a;
        this.active_windows = new HashMap<>();
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
    public Content<I, W, R> content(long t_e) {
        // If some windows matched the report clause, return the last one that did so
        if (!reported_windows.isEmpty()) {
            return reported_windows.stream()
                    .max(Comparator.comparingLong(Window::getC))
                    .map(w -> (active_windows.get(w))).get();
        }
        //Else return the last window closed
        else {
            Optional<Window> max = active_windows.keySet().stream()
                    .filter(w -> w.getO() < t_e && w.getC() < t_e)
                    .max(Comparator.comparingLong(Window::getC));

            if (max.isPresent())
                return active_windows.get(max.get());

            return cf.createEmpty();
        }
    }

    @Override
    public List<Content<I, W, R>> getContents(long t_e) {
        if (!reported_windows.isEmpty()) {
            return reported_windows.stream()
                    .max(Comparator.comparingLong(Window::getC))
                    .map(w -> Collections.singletonList(active_windows.get(w))).get();
        } else
            return active_windows.keySet().stream()
                    .filter(w -> w.getO() < t_e && t_e < w.getC())
                    .map(active_windows::get).collect(Collectors.toList());
    }


    private Window scope(long t_e) {
        long o_i = t_e - a;
        log.debug("Calculating the Windows to Open. First one opens at [" + o_i + "] and closes at [" + t_e + "]");
        log.debug("Computing Window [" + o_i + "," + (o_i + a) + ") if absent");

        WindowImpl active = new WindowImpl(o_i, t_e);
        active_windows.computeIfAbsent(active, window -> cf.create());
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
        Content<I, W, R> content = active_windows.get(active);

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

            active_windows.forEach((window, content1) -> {
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
        to_evict.forEach(active_windows::remove);
        to_evict.clear();
        reported_windows = new ArrayList<>();
    }

    @Override
    public void evict(long ts) {
        active_windows.keySet().forEach(w -> {if (w.getC() < ts) to_evict.add(w);});
        evict();
    }
}
