package org.streamreasoning.polyflow.base.contentimpl.factories;

import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.SegmentFactory;
import org.streamreasoning.polyflow.base.operatorsimpl.s2r.segment.EmptySegment;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class StatefulContentFactory<I, W, R> implements SegmentFactory<I, R> {

    private final Supplier<Object> initialState;
    private final BiFunction<W, Object, Object> stateUpdater;
    private final Function<Object, Boolean> reportPredicate;
    private final Function<I, W> inputMapper;
    private final Function<W, R> resultMapper;
    private final BiFunction<R, R, R> merger;
    private final R emptyContent;

    public StatefulContentFactory(Supplier<Object> initialState, BiFunction<W, Object, Object> stateUpdater,
                                  Function<Object, Boolean> reportPredicate, Function<I, W> inputMapper,
                                  Function<W, R> resultMapper, BiFunction<R, R, R> merger, R emptyContent) {
        this.initialState = initialState;
        this.stateUpdater = stateUpdater;
        this.reportPredicate = reportPredicate;
        this.inputMapper = inputMapper;
        this.resultMapper = resultMapper;
        this.merger = merger;
        this.emptyContent = emptyContent;
    }

    @Override
    public Segment<I, R> createEmpty() {
        return new EmptySegment<>(emptyContent);
    }

    @Override
    public Segment<I, R> create() {
        return new Segment<>() {
            private final Segment<I, R> delegate = new AccumulatorContentFactory.AccumulatorSegment<>(inputMapper, resultMapper, merger, emptyContent);
            private Object state = initialState.get();

            @Override
            public int size() {
                return delegate.size();
            }

            @Override
            public void add(I item) {
                W mapped = inputMapper.apply(item);
                state = stateUpdater.apply(mapped, state);
                delegate.add(item);
            }

            @Override
            public R coalesce() {
                return delegate.coalesce();
            }

            @Override
            public boolean toReport() {
                return reportPredicate.apply(state);
            }
        };
    }
}
