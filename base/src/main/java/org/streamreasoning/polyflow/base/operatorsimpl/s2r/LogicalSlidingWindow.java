package org.streamreasoning.polyflow.base.operatorsimpl.s2r;

import org.streamreasoning.polyflow.api.enums.Tick;
import org.streamreasoning.polyflow.api.exceptions.OutOfOrderElementException;
import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.Window;
import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.WindowImpl;
import org.streamreasoning.polyflow.api.sds.timevarying.TimeVarying;
import org.streamreasoning.polyflow.api.secret.content.Content;
import org.streamreasoning.polyflow.api.secret.content.ContentFactory;
import org.streamreasoning.polyflow.api.secret.report.Report;
import org.streamreasoning.polyflow.api.secret.time.Time;
import org.streamreasoning.polyflow.api.secret.time.TimeInstant;
import org.streamreasoning.polyflow.base.sds.TimeVaryingObject;

import java.util.List;

public class LogicalSlidingWindow<I, W, R extends Iterable<?>> implements StreamToRelationOperator<I, W, R> {

    protected final Time time;
    protected final String name;
    protected final ContentFactory<I, W, R> cf;
    protected Report report;
    private final long width;
    private Window activeWindow;
    private Content<I, W, R> activeContent;
    private long t0;
    private long toi;

    public LogicalSlidingWindow(Time time, String name, ContentFactory<I, W, R> cf, Report report, long width){

        this.time = time;
        this.name = name;
        this.cf = cf;
        this.report = report;
        this.width = width;
        this.t0 = time.getScope();
        this.activeWindow = new WindowImpl(0, width);
        this.activeContent = cf.create();
        this.toi = 0;
    }

    @Override
    public Report report() {
        return report;
    }

    @Override
    public Tick tick() {
        return null;
    }

    @Override
    public Time time() {
        return time;
    }

    @Override
    public Content<I, W, R> content(long l) {
        return activeContent;
    }

    @Override
    public List<Content<I, W, R>> getContents(long l) {
        return List.of(activeContent);
    }

    @Override
    public TimeVarying<R> get() {
        return new TimeVaryingObject<>(this, this.name);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void compute(I i, long l) {
        if(l < time.getAppTime()){
            throw new OutOfOrderElementException("Out of order not supported");
        }
        if(l > activeWindow.getC()){
            activeWindow = new WindowImpl(l-width, l);
        }
        activeContent.add(i);
        this.time.setAppTime(l);
        if(report.report(activeWindow, activeContent, l, System.currentTimeMillis())){
            time.addEvaluationTimeInstants(new TimeInstant(l));
        }

    }

    @Override
    public void evict() {

    }

    @Override
    public void evict(long l) {

    }
}
