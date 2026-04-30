package org.streamreasoning.polyflow.base.operatorsimpl.s2r;

import org.apache.log4j.Logger;
import org.streamreasoning.polyflow.api.enums.AggregationFunction;
import org.streamreasoning.polyflow.api.enums.FrameClosingCondition;
import org.streamreasoning.polyflow.api.enums.FrameType;
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
import java.util.Objects;
import java.util.function.ToDoubleFunction;

/**
 * Single-buffer stream-to-relation operator for frame windows.
 * <p>
 * A frame keeps exactly one current buffer. Each incoming element is evaluated
 * against the configured frame lifecycle:
 * <ol>
 *   <li>close the current frame when the closing condition is met;</li>
 *   <li>update the current frame while the closing condition is not met;</li>
 *   <li>open a new frame when no frame is currently open and the frame type
 *       allows the incoming element to start one.</li>
 * </ol>
 * The operator is data-model agnostic. The caller supplies a
 * {@link SingleBufferState} to store frame content and a {@code valueExtractor}
 * for frame types that compare numeric values.
 * <p>
 * Frame types:
 * <ul>
 *   <li>{@link FrameType#THRESHOLD}: compares the current element value with
 *       {@code frameParameter}.</li>
 *   <li>{@link FrameType#DELTA}: compares {@code abs(firstValue - currentValue)}
 *       with {@code frameParameter}.</li>
 *   <li>{@link FrameType#AGGREGATE}: compares the running aggregate with
 *       {@code frameParameter}. The aggregate is selected with
 *       {@link AggregationFunction#SUM} or {@link AggregationFunction#AVG}.</li>
 *   <li>{@link FrameType#SESSION}: uses {@code frameParameter} as the session
 *       gap in the same time unit used by event timestamps.</li>
 * </ul>
 * For non-session frames, {@code closingCondition} says when the frame closes:
 * {@code GREATER_THAN} means close when the observed value is greater than the
 * parameter, {@code LESS_OR_EQUAL} means close when it is less than or equal to
 * the parameter, and so on. The previous comparator-style API is intentionally
 * replaced by {@link FrameClosingCondition}. For session frames, the frame
 * closes when {@code ts - lastTimestamp > frameParameter}.
 */
public class SBFramesWindowOpImpl<I, R extends Iterable<?>> implements StreamToRelationOperator<I, R> {

    private static final Logger log = Logger.getLogger(SBFramesWindowOpImpl.class);

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
    protected final FrameClosingCondition closingCondition;

    private Segment<I, R> reportedContent;
    private final Context context = new Context();

    /**
     * Creates a frame operator.
     *
     * @param state single-buffer state used to hold the current frame content
     * @param frameType frame semantics: threshold, delta, aggregate, or session
     * @param frameParameter threshold/delta/aggregate limit, or session gap for
     *                       {@link FrameType#SESSION}
     * @param aggregationFunction aggregate function for
     *                            {@link FrameType#AGGREGATE}; ignored otherwise
     * @param valueExtractor extracts the numeric value used by non-session
     *                       frames; ignored for {@link FrameType#SESSION}
     * @param closingCondition comparison that closes non-session frames; for
     *                         example {@link FrameClosingCondition#GREATER_THAN}
     *                         closes when the observed value is greater than
     *                         {@code frameParameter}
     */
    public SBFramesWindowOpImpl(Tick tick, Time time, String name, SingleBufferState<I, R> state, Report report,
                                FrameType frameType, double frameParameter,
                                AggregationFunction aggregationFunction, ToDoubleFunction<I> valueExtractor,
                                FrameClosingCondition closingCondition) {
        this.tick = tick;
        this.time = time;
        this.name = name;
        this.state = state;
        this.report = report;
        this.frameType = frameType;
        this.frameParameter = frameParameter;
        this.aggregationFunction = aggregationFunction;
        this.valueExtractor = valueExtractor;
        this.closingCondition = closingCondition;
        validateConfig();
        this.ticker = TickerFactory.tick(tick, this);
    }

    /**
     * Convenience constructor accepting symbols {@code >}, {@code <},
     * {@code <=}, {@code >=}, {@code =}, and {@code ==}.
     */
    public SBFramesWindowOpImpl(Tick tick, Time time, String name, SingleBufferState<I, R> state, Report report,
                                FrameType frameType, double frameParameter,
                                AggregationFunction aggregationFunction, ToDoubleFunction<I> valueExtractor,
                                String closingCondition) {
        this(tick, time, name, state, report, frameType, frameParameter, aggregationFunction,
                valueExtractor, FrameClosingCondition.fromSymbol(closingCondition));
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
                context.start = false;
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
            case THRESHOLD -> keepOpen(value(arg), frameParameter) && !context.start;
            case DELTA, AGGREGATE, SESSION -> !context.start;
        };
    }

    protected boolean updatePred(I arg, long ts) {
        return switch (frameType) {
            case THRESHOLD -> keepOpen(value(arg), frameParameter) && context.start;
            case DELTA -> keepOpen(Math.abs(context.v - value(arg)), frameParameter) && context.start;
            case AGGREGATE -> keepOpen(context.v, frameParameter) && context.start;
            case SESSION -> ts - context.currentTimestamp <= frameParameter && context.start;
        };
    }

    protected boolean closePred(I arg, long ts) {
        return switch (frameType) {
            case THRESHOLD -> shouldClose(value(arg), frameParameter) && context.start;
            case DELTA -> shouldClose(Math.abs(context.v - value(arg)), frameParameter) && context.start;
            case AGGREGATE -> shouldClose(context.v, frameParameter) && context.start;
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
            Objects.requireNonNull(closingCondition, "closingCondition is required for non-session frames");
        }
        if (frameType == FrameType.AGGREGATE) {
            Objects.requireNonNull(aggregationFunction, "aggregationFunction is required for aggregate frames");
        }
    }

    private double value(I arg) {
        return valueExtractor.applyAsDouble(arg);
    }

    private boolean shouldClose(double left, double right) {
        return closingCondition.matches(left, right);
    }

    private boolean keepOpen(double left, double right) {
        return !shouldClose(left, right);
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
