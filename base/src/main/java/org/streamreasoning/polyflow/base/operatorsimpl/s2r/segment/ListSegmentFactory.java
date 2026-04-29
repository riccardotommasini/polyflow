package org.streamreasoning.polyflow.base.operatorsimpl.s2r.segment;

import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.SegmentFactory;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ListSegmentFactory<I, R> implements SegmentFactory<I, R> {

    private final Function<I, R> lift;
    private final BiFunction<R, R, R> sumR;
    private final R emptyContent;

    public ListSegmentFactory(Function<I, R> lift, BiFunction<R, R, R> sumR, R emptyContent) {
        this.lift = lift;
        this.sumR = sumR;
        this.emptyContent = emptyContent;
    }

    @Override
    public Segment<I, R> createEmpty() {
        return new EmptySegment<>(emptyContent);
    }

    @Override
    public Segment<I, R> create() {
        return new ListSegment<>(lift, sumR, emptyContent);
    }
}
