package org.streamreasoning.polyflow.base.contentimpl.factories;

import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.SegmentFactory;
import org.streamreasoning.polyflow.base.operatorsimpl.s2r.segment.EmptySegment;

import java.util.function.BiFunction;
import java.util.function.Function;



public class AggregateContentFactory<I, W, R> implements SegmentFactory<I, R> {

    private final Function<I, W> inputMapper;
    private final Function<W, R> resultMapper;
    private final BiFunction<R, R, R> merger;
    private final Function<R, R> aggregator;
    private final R emptyContent;

    public AggregateContentFactory(Function<I, W> inputMapper, Function<W, R> resultMapper,
                                   BiFunction<R, R, R> merger, Function<R, R> aggregator, R emptyContent) {
        this.inputMapper = inputMapper;
        this.resultMapper = resultMapper;
        this.merger = merger;
        this.aggregator = aggregator;
        this.emptyContent = emptyContent;
    }

    @Override
    public Segment<I, R> createEmpty() {
        return new EmptySegment<>(emptyContent);
    }

    @Override
    public Segment<I, R> create() {
        Segment<I, R> delegate = new AccumulatorContentFactory.AccumulatorSegment<>(inputMapper, resultMapper, merger, emptyContent);
        return new Segment<>() {
            @Override
            public int size() {
                return delegate.size();
            }

            @Override
            public void add(I item) {
                delegate.add(item);
            }

            @Override
            public R coalesce() {
                return aggregator.apply(delegate.coalesce());
            }
        };
    }
}
