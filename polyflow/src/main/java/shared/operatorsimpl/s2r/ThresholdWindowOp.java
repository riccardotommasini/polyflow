package shared.operatorsimpl.s2r;

import org.apache.log4j.Logger;
import org.streamreasoning.rsp4j.api.enums.ReportGrain;
import org.streamreasoning.rsp4j.api.enums.Tick;
import org.streamreasoning.rsp4j.api.exceptions.OutOfOrderElementException;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.instance.Window;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.instance.WindowImpl;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVaryingFactory;
import org.streamreasoning.rsp4j.api.secret.content.Content;
import org.streamreasoning.rsp4j.api.secret.content.ContentFactory;
import org.streamreasoning.rsp4j.api.secret.report.Report;
import org.streamreasoning.rsp4j.api.secret.tick.Ticker;
import org.streamreasoning.rsp4j.api.secret.tick.secret.TickerFactory;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.secret.time.TimeInstant;

import java.util.*;
import java.util.function.Predicate;

public class ThresholdWindowOp<I, W, R extends Iterable<?>> implements StreamToRelationOperator<I, W, R> {


    private static final Logger log = Logger.getLogger(CSPARQLStreamToRelationOpImpl.class);
    protected final Ticker ticker;
    protected Tick tick;
    protected final Time time;
    protected final String name;
    protected final ContentFactory<I, W, R> cf;
    protected final TimeVaryingFactory<R> tvFactory;
    protected ReportGrain grain;
    protected Report report;
    private final long min_size;
    private Map<Window, Content<I, W, R>> active_windows;
    private List<Window> reported_windows;
    private Set<Window> to_evict;
    private Predicate<I> threshold;

    public ThresholdWindowOp(Tick tick, Time time, String name, ContentFactory<I, W, R> cf, TimeVaryingFactory<R> tvFactory, ReportGrain grain, Report report,
                             long min_size, Predicate<I> threshold){
        this.tick = tick;
        this.time = time;
        this.name = name;
        this.cf = cf;
        this.tvFactory = tvFactory;
        this.grain = grain;
        this.report = report;
        this.threshold = threshold;
        this.min_size = min_size;
        this.reported_windows = new ArrayList<>();
        this.to_evict = new HashSet<>();
        this.ticker = TickerFactory.tick(tick, this);

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
    public ReportGrain grain() {
        return grain;
    }

    @Override
    public Content<I, W, R> content(long t_e) {
        if(!reported_windows.isEmpty()){
            return active_windows.get(reported_windows.get(0));
        }
        return cf.createEmpty();

    }

    @Override
    public List<Content<I, W, R>> getContents(long t_e) {
        if(!reported_windows.isEmpty()){
            return Collections.singletonList(active_windows.get(reported_windows.get(0)));
        }
        return Collections.singletonList(cf.createEmpty());
    }

    @Override
    public void windowing(I arg, long ts) {
        log.debug("Received element (" + arg + "," + ts + ")");
        long t_e = ts;

        if (time.getAppTime() > t_e) {
            log.error("OUT OF ORDER NOT HANDLED");
            throw new OutOfOrderElementException("(" + arg + "," + ts + ")");
        }

        if(threshold.test(arg)){
            if(!active_windows.isEmpty()){
                active_windows.values().stream().findFirst().get().add(arg);
            }
            else{
                Window w = new WindowImpl(ts, -1); //Closing time is not known at priori
                Content<I, W, R> content = cf.create();
                content.add(arg);
                active_windows.put(w, content);
            }
        }
        else{
            if(!active_windows.isEmpty()){
                Window to_close = active_windows.keySet().stream().findFirst().get();
                to_close.setC(t_e); //When an event does not match the threshold, we close the current window
                reported_windows.add(to_close);
                to_evict.add(to_close);
                time.addEvaluationTimeInstants(new TimeInstant(t_e));
                time.setAppTime(t_e);
            }
        }
    }

    @Override
    public TimeVarying<R> get() {
        return tvFactory.create(this, name);
    }

    @Override
    public String getName() {
        return this.name;
    }



    @Override
    public void evict() {
        to_evict.forEach(w -> {
            log.debug("Evicting [" + w.getO() + "," + w.getC() + ")");
            active_windows.remove(w);
        });
        to_evict.clear();
        reported_windows = new ArrayList<>();
    }

    @Override
    public void evict(long ts) {
        active_windows.keySet().forEach(w->{if(w.getC() != -1)to_evict.add(w);});
        evict();
    }
}
