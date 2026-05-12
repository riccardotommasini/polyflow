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
import org.streamreasoning.polyflow.base.sds.TimeVaryingObject;

import java.util.Collections;
import java.util.List;

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
    private Window reportedWindow;
    private final long t0;

    public SBHoppingWindowOpImpl(Tick tick, Time time, String name, SingleBufferState<I, R> state, Report report, long width, long slide) {
        this.tick = tick;
        this.time = time;
        this.name = name;
        this.state = state;
        this.report = report;
        this.width = width;
        this.slide = slide;
        this.ticker = TickerFactory.tick(tick, this);
        this.t0 = time.getScope();
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
    public Segment<I, R> content(long t_e) {
        return state.segment(reportedWindow);
    }

    @Override
    public List<Segment<I, R>> getContents(long t_e) {
        return state.getWindow() != null ? Collections.singletonList(state.segment(reportedWindow)) : Collections.singletonList(state.emptySegment());
    }

    @Override
    public TimeVarying<R> get() {
        return new TimeVaryingObject<>(this, name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean named() {
        return !name.isEmpty();
    }

    private Window scope(long t_e){
        long c_sup = (long) Math.ceil(((double) Math.abs(t_e - t0) / (double) slide)) * slide;
        long o_i = c_sup - width;
        log.debug("Calculating the Window to Open. Opens at [" + o_i + "] and closes at [" + (o_i + width) +"]");
        state.setWindow(new WindowImpl(o_i, o_i + width));
        return new WindowImpl(o_i, o_i + width);
    }

    @Override
    public void compute(I arg, long ts) {
        log.debug("Received element (" + arg + "," + ts + ")");

        if (time.getAppTime() > ts) {
            log.error("OUT OF ORDER NOT HANDLED");
            throw new OutOfOrderElementException("(" + arg + "," + ts + ")");
        }

        state.append(arg);

        if (ticker.tick(ts)) {
            Window win = scope(ts);
            if (report.report(win, state.segment(win), ts, System.currentTimeMillis())) {
                reportedWindow = win;
                time.addEvaluationTimeInstants(new TimeInstant(ts));
            }
        }
        time.setAppTime(ts);
    }

    @Override
    public void evict() {
        reportedWindow = null;
    }

    @Override
    public void evict(long ts) {
        state.evict(state.getWindow().getO());
        evict();
    }
}
