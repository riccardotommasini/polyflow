package org.streamreasoning.polyflow.base.operatorsimpl.s2r;

import org.apache.log4j.Logger;
import org.streamreasoning.polyflow.api.enums.AggregationFunction;
import org.streamreasoning.polyflow.api.enums.FrameType;
import org.streamreasoning.polyflow.api.enums.Tick;
import org.streamreasoning.polyflow.api.exceptions.OutOfOrderElementException;
import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.Window;
import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.WindowImpl;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.SegmentFactory;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.SingleBufferState;
import org.streamreasoning.polyflow.api.sds.timevarying.TimeVarying;
import org.streamreasoning.polyflow.api.secret.report.Report;
import org.streamreasoning.polyflow.api.secret.tick.Ticker;
import org.streamreasoning.polyflow.api.secret.tick.secret.TickerFactory;
import org.streamreasoning.polyflow.api.secret.time.Time;
import org.streamreasoning.polyflow.api.secret.time.TimeInstant;
import org.streamreasoning.polyflow.base.operatorsimpl.s2r.state.ListSingleBufferState;
import org.streamreasoning.polyflow.base.sds.TimeVaryingObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

public class FrameOp<I, R extends Iterable<?>> implements StreamToRelationOperator<I, R> {

    private static final Logger log = Logger.getLogger(FrameOp.class);

    protected final Ticker ticker;
    protected final Tick tick;
    protected final Time time;
    protected final String name;
    protected final SingleBufferState<I, R> state;
    protected final Report report;
    protected final FrameType frameType;
    protected final double frameParameter;
    protected final AggregationFunction aggregationFunction;
    protected final ToDoubleFunction<I> valueExtractor;
    protected final Comparator<Double> comparator;

    private Segment<I, R> reportedContent;
    private final Context context = new Context();

    public FrameOp(Tick tick, Time time, String name, SingleBufferState<I, R> state,
                   Report report, FrameType frameType, double frameParameter,
                   AggregationFunction aggregationFunction, ToDoubleFunction<I> valueExtractor,
                   Comparator<Double> comparator) {
        this.tick = tick;
        this.time = time;
        this.name = name;
        this.state = state;
        this.report = report;
        this.frameType = frameType;
        this.frameParameter = frameParameter;
        this.aggregationFunction = aggregationFunction;
        this.valueExtractor = valueExtractor;
        this.comparator = comparator;
        validateConfig();
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
    public Segment<I, R> content(long t_e) {
        return reportedContent != null ? reportedContent : state.emptySegment();
    }

    @Override
    public List<Segment<I, R>> getContents(long t_e) {
        return reportedContent != null
                ? Collections.singletonList(reportedContent)
                : Collections.singletonList(state.emptySegment());
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

        boolean added = false;

        if (hasOpenFrame() && closePred(arg, ts)) {
            close(arg, ts);
            closeFrame(ts);
        }

        if (hasOpenFrame() && updatePred(arg, ts)) {
            added = true;
            state.append(arg);
            update(arg, ts);
        }

        if (openPred(arg, ts)) {
            added = true;
            open(arg, ts);
            state.setWindow(new WindowImpl(ts, -1));
            state.append(arg);
        }

        if (!added) {
            discard(arg, ts);
        }

        time.setAppTime(ts);
    }

    @Override
    public void evict() {
        reportedContent = null;
    }

    @Override
    public void evict(long ts) {
        evict();
    }

    protected void open(I arg, long ts) {
        switch (frameType) {
            case THRESHOLD:
                context.count = 1;
                context.start = true;
                break;
            case DELTA:
                context.v = value(arg);
                context.start = true;
                break;
            case AGGREGATE:
                context.aggregateCount = 1;
                context.aggregateSum = value(arg);
                context.v = context.aggregateSum;
                context.start = true;
                break;
            case SESSION:
                context.currentTimestamp = ts;
                context.start = true;
                break;
        }
    }

    protected void update(I arg, long ts) {
        switch (frameType) {
            case THRESHOLD:
                context.count++;
                break;
            case DELTA:
                context.currentValue = value(arg);
                break;
            case AGGREGATE:
                context.aggregateCount++;
                context.aggregateSum += value(arg);
                context.v = aggregateValue();
                break;
            case SESSION:
                context.currentTimestamp = ts;
                break;
        }
    }

    protected void close(I arg, long ts) {
        switch (frameType) {
            case THRESHOLD:
                context.count = 0;
                context.start = false;
                break;
            case DELTA:
            case AGGREGATE:
                context.start = false;
                context.aggregateCount = 0;
                context.aggregateSum = 0;
                break;
            case SESSION:
                context.start = false;
                break;
        }
    }

    protected boolean openPred(I arg, long ts) {
        return switch (frameType) {
            case THRESHOLD -> compare(value(arg), frameParameter) > 0 && !context.start;
            case DELTA, AGGREGATE, SESSION -> !context.start;
        };
    }

    protected boolean updatePred(I arg, long ts) {
        return switch (frameType) {
            case THRESHOLD -> compare(value(arg), frameParameter) > 0 && context.start;
            case DELTA -> compare(Math.abs(context.v - value(arg)), frameParameter) > 0 && context.start;
            case AGGREGATE -> compare(context.v, frameParameter) > 0 && context.start;
            case SESSION -> ts - context.currentTimestamp <= frameParameter && context.start;
        };
    }

    protected boolean closePred(I arg, long ts) {
        return switch (frameType) {
            case THRESHOLD -> compare(value(arg), frameParameter) < 0 && context.start;
            case DELTA -> compare(Math.abs(context.v - value(arg)), frameParameter) < 0 && context.start;
            case AGGREGATE -> compare(context.v, frameParameter) < 0 && context.start;
            case SESSION -> ts - context.currentTimestamp > frameParameter && context.start;
        };
    }

    protected void discard(I arg, long ts) {
        // no-op by default
    }

    protected boolean hasOpenFrame() {
        return state.getWindow() != null;
    }

    protected void closeFrame(long ts) {
        Window current = state.getWindow();
        if (current == null) {
            return;
        }
        current.setC(ts);
        reportedContent = state.segment();
        if (ticker.tick(ts)) {
            time.addEvaluationTimeInstants(new TimeInstant(ts));
        }
        state.clear();
    }

    private void validateConfig() {
        if (frameType != FrameType.SESSION) {
            Objects.requireNonNull(valueExtractor, "valueExtractor is required for non-session frames");
            Objects.requireNonNull(comparator, "comparator is required for non-session frames");
        }
        if (frameType == FrameType.AGGREGATE) {
            Objects.requireNonNull(aggregationFunction, "aggregationFunction is required for aggregate frames");
        }
    }

    private double value(I arg) {
        return valueExtractor.applyAsDouble(arg);
    }

    private int compare(double left, double right) {
        return comparator.compare(left, right);
    }

    private double aggregateValue() {
        if (aggregationFunction == AggregationFunction.AVG) {
            return context.aggregateCount == 0 ? 0 : context.aggregateSum / context.aggregateCount;
        }
        return context.aggregateSum;
    }

    private static class Context {
        double v;
        double currentValue;
        int count;
        boolean start;
        long currentTimestamp;
        double aggregateSum;
        int aggregateCount;
    }
}
