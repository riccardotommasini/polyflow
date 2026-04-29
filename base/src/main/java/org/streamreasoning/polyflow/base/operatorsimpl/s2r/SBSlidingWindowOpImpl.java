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
import org.streamreasoning.polyflow.base.operatorsimpl.s2r.segment.ListSegmentFactory;
import org.streamreasoning.polyflow.base.operatorsimpl.s2r.state.ListSingleBufferState;
import org.streamreasoning.polyflow.base.sds.TimeVaryingObject;

import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;

public class SBSlidingWindowOpImpl<I, R extends Iterable<?>> implements StreamToRelationOperator<I, R> {

    private static final Logger log = Logger.getLogger(SBSlidingWindowOpImpl.class);

    protected final Ticker ticker;
    protected Tick tick;
    protected final Time time;
    protected final String name;
    protected final SingleBufferState<I, R> state;
    protected Report report;
    private final long width;
    private Window reportedWindow;

    public SBSlidingWindowOpImpl(Tick tick, Time time, String name, SingleBufferState<I, R> state, Report report, long width) {
        this.tick = tick;
        this.time = time;
        this.name = name;
        this.state = state;
        this.report = report;
        this.width = width;
        this.ticker = TickerFactory.tick(tick, this);
        Logger.getRootLogger().setLevel(Level.OFF);
    }

    public SBSlidingWindowOpImpl(Tick tick, Time time, String name, ListSegmentFactory<I, R> segmentFactory,
                                 BiPredicate<I, Long> expiresBefore, Report report, long width) {
        this(tick, time, name,
                new ListSingleBufferState<>(segmentFactory.create(), segmentFactory.createEmpty(), expiresBefore),
                report,
                width);
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
        return state.getWindow() != null ? state.segment() : state.emptySegment();
    }

    @Override
    public List<Segment<I, R>> getContents(long t_e) {
        return state.getWindow() != null ? Collections.singletonList(state.segment()) : Collections.singletonList(state.emptySegment());
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

    @Override
    public void compute(I arg, long ts) {
        log.debug("Received element (" + arg + "," + ts + ")");

        if (time.getAppTime() > ts) {
            log.error("OUT OF ORDER NOT HANDLED");
            throw new OutOfOrderElementException("(" + arg + "," + ts + ")");
        }

        long open = ts - width;
        Window current = new WindowImpl(open, ts);
        state.append(arg);

        state.setWindow(current);
        state.evict(open);

        if (ticker.tick(ts) && report.report(current, state.segment(), ts, System.currentTimeMillis())) {
            reportedWindow = current;
            time.addEvaluationTimeInstants(new TimeInstant(ts));
        }

        time.setAppTime(ts);
    }

    @Override
    public void evict() {
        reportedWindow = null;
    }

    @Override
    public void evict(long ts) {
        if (state.getWindow() != null && state.getWindow().getC() < ts) {
            state.clear();
        }
        evict();
    }
}
