package org.streamreasoning.polyflow.base.operatorsimpl.s2r;

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

import java.util.Collections;
import java.util.List;

public class FrameOp<I, W, R extends Iterable<?>> implements StreamToRelationOperator<I, W, R> {


    private static final Logger log = Logger.getLogger(HoppingWindowOpImpl.class);
    protected final Ticker ticker;
    protected Tick tick;
    protected final Time time;
    protected final String name;
    protected final ContentFactory<I, W, R> cf;
    protected Report report;
    private Window active_window;
    private Content<I, W, R> active_content;
    private Window reported_window;
    private Content<I, W, R> reported_content;
    private boolean toReport;

    public FrameOp(Tick tick, Time time, String name, ContentFactory<I, W, R> cf, Report report) {
        this.tick = tick;
        this.time = time;
        this.name = name;
        this.cf = cf;
        this.report = report;
        this.ticker = TickerFactory.tick(tick, this);
        this.toReport = false;
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
    public Content<I, W, R> content(long t_e) {
        if (toReport)
            return reported_content;
        else return cf.createEmpty();
    }

    @Override
    public List<Content<I, W, R>> getContents(long t_e) {
        if (toReport)
            return Collections.singletonList(reported_content);
        else return Collections.singletonList(cf.createEmpty());
    }

    @Override
    public void compute(I arg, long ts) {
        log.debug("Received element (" + arg + "," + ts + ")");

        if (time.getAppTime() > ts) {
            log.error("OUT OF ORDER NOT HANDLED");
            throw new OutOfOrderElementException("(" + arg + "," + ts + ")");
        }

        if (active_window != null) {
            active_content.add(arg);
        } else {
            active_window = new WindowImpl(ts, -1);
            active_content = cf.create();
            active_content.add(arg);
        }
        toReport = report.report(active_window, active_content, ts, System.currentTimeMillis());
        if (toReport) {
            time.addEvaluationTimeInstants(new TimeInstant(ts));
            active_window.setC(ts); //If a frame needs to be reported, then it must also be closed
            reported_window = active_window;
            reported_content = active_content;
            active_window = new WindowImpl(ts, -1);
            active_content = cf.create();
            active_content.add(arg);
        }

        time.setAppTime(ts);
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
    public void evict() {
        if (toReport) {
            toReport = false;
            reported_window = null;
            reported_content = null;
        }
    }

    @Override
    public void evict(long ts) {
        evict();
    }
}
