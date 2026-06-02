package org.streamreasoning.polyflow.base.contentimpl.factories;

import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.SegmentFactory;
import org.streamreasoning.polyflow.base.operatorsimpl.s2r.segment.EmptySegment;

import java.util.function.Function;

public class LastContentFactory<I, W, R> implements SegmentFactory<I, R> {

    private final Function<I, W> inputMapper;
    private final Function<W, R> resultMapper;
    private final R emptyContent;

    public LastContentFactory(Function<I, W> inputMapper, Function<W, R> resultMapper, R emptyContent) {
        this.inputMapper = inputMapper;
        this.resultMapper = resultMapper;
        this.emptyContent = emptyContent;
    }

    @Override
    public Segment<I, R> createEmpty() {
        return new EmptySegment<>(emptyContent);
    }

    @Override
    public Segment<I, R> create() {
        return new Segment<>() {
            private W last;
            private int size;

            @Override
            public int size() {
                return size;
            }

            @Override
            public void add(I item) {
                last = inputMapper.apply(item);
                size++;
            }

            @Override
            public R coalesce() {
                return size == 0 ? emptyContent : resultMapper.apply(last);
            }
        };
    }
}
