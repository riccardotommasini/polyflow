package org.streamreasoning.polyflow.base.contentimpl.factories;

import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.SegmentFactory;
import org.streamreasoning.polyflow.base.operatorsimpl.s2r.segment.EmptySegment;

import java.util.function.Function;

public class FirstContentFactory<I, W, R> implements SegmentFactory<I, R> {

    private final Function<I, W> inputMapper;
    private final Function<W, R> resultMapper;
    private final R emptyContent;

    public FirstContentFactory(Function<I, W> inputMapper, Function<W, R> resultMapper, R emptyContent) {
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
            private W first;
            private int size;

            @Override
            public int size() {
                return size;
            }

            @Override
            public void add(I item) {
                if (size == 0) {
                    first = inputMapper.apply(item);
                }
                size++;
            }

            @Override
            public R coalesce() {
                return size == 0 ? emptyContent : resultMapper.apply(first);
            }
        };
    }
}
