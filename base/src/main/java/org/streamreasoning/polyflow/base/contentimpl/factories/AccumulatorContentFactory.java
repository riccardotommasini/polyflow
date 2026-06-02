package org.streamreasoning.polyflow.base.contentimpl.factories;

import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.SegmentFactory;
import org.streamreasoning.polyflow.base.operatorsimpl.s2r.segment.EmptySegment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AccumulatorContentFactory<I, W, R> implements SegmentFactory<I, R> {

    private final Function<I, W> inputMapper;
    private final Function<W, R> resultMapper;
    private final BiFunction<R, R, R> merger;
    private final R emptyContent;

    public AccumulatorContentFactory(Function<I, W> inputMapper, Function<W, R> resultMapper,
                                     BiFunction<R, R, R> merger, R emptyContent) {
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
        return new AccumulatorSegment<>(inputMapper, resultMapper, merger, emptyContent);
    }

    static class AccumulatorSegment<I, W, R> implements Segment<I, R> {
        private final Function<I, W> inputMapper;
        private final Function<W, R> resultMapper;
        private final BiFunction<R, R, R> merger;
        private final R emptyContent;
        private final List<W> content = new ArrayList<>();

        AccumulatorSegment(Function<I, W> inputMapper, Function<W, R> resultMapper,
                           BiFunction<R, R, R> merger, R emptyContent) {
            this.inputMapper = inputMapper;
            this.resultMapper = resultMapper;
            this.merger = merger;
            this.emptyContent = emptyContent;
        }

        @Override
        public int size() {
            return content.size();
        }

        @Override
        public void add(I item) {
            content.add(inputMapper.apply(item));
        }

        @Override
        public R coalesce() {
            R result = emptyContent;
            for (W item : content) {
                result = merger.apply(result, resultMapper.apply(item));
            }
            return result;
        }

        @Override
        public Iterator<I> iterator() {
            return Segment.super.iterator();
        }
    }
}
