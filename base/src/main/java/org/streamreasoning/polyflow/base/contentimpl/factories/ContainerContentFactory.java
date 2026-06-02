package org.streamreasoning.polyflow.base.contentimpl.factories;

import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.SegmentFactory;
import org.streamreasoning.polyflow.base.operatorsimpl.s2r.segment.EmptySegment;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ContainerContentFactory<I, W, R, K> implements SegmentFactory<I, R> {

    private final Function<I, K> inputKeyExtractor;
    private final BiFunction<R, R, R> merger;
    private final R emptyContent;
    private final SegmentFactory<I, R> internalSegmentFactory;

    public ContainerContentFactory(Function<I, K> inputKeyExtractor, Function<W, K> windowKeyExtractor,
                                   Function<R, K> resultKeyExtractor, BiFunction<R, R, R> merger,
                                   R emptyContent, SegmentFactory<I, R> internalSegmentFactory) {
        this.inputKeyExtractor = inputKeyExtractor;
        this.merger = merger;
        this.emptyContent = emptyContent;
        this.internalSegmentFactory = internalSegmentFactory;
    }

    @Override
    public Segment<I, R> createEmpty() {
        return new EmptySegment<>(emptyContent);
    }

    @Override
    public Segment<I, R> create() {
        return new Segment<>() {
            private final Map<K, Segment<I, R>> segments = new LinkedHashMap<>();
            private int size;

            @Override
            public int size() {
                return size;
            }

            @Override
            public void add(I item) {
                K key = inputKeyExtractor.apply(item);
                segments.computeIfAbsent(key, ignored -> internalSegmentFactory.create()).add(item);
                size++;
            }

            @Override
            public R coalesce() {
                R result = emptyContent;
                for (Segment<I, R> segment : segments.values()) {
                    result = merger.apply(result, segment.coalesce());
                }
                return result;
            }

            @Override
            public boolean toReport() {
                return segments.values().stream().anyMatch(Segment::toReport);
            }
        };
    }
}
