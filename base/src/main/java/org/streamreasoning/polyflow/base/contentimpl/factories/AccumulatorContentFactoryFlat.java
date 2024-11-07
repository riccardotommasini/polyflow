package org.streamreasoning.polyflow.base.contentimpl.factories;

import org.streamreasoning.polyflow.api.secret.content.Content;
import org.streamreasoning.polyflow.api.secret.content.ContentFactory;
import org.streamreasoning.polyflow.base.contentimpl.EmptyContent;
import org.streamreasoning.polyflow.base.contentimpl.content.AccumulatorContentFlat;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class AccumulatorContentFactoryFlat<I, W, R> implements ContentFactory<I, W, R> {

    Function<I, Stream<W>> f1;
    Function<W, R> f2;
    BiFunction<R, R, R> sumR;
    R emptyContent;

    public AccumulatorContentFactoryFlat(Function<I, Stream<W>> f1, Function<W, R> f2, BiFunction<R, R, R> sumR, R emptyContent) {
        this.f1 = f1;
        this.f2 = f2;
        this.sumR = sumR;
        this.emptyContent = emptyContent;
    }

    @Override
    public Content<I, W, R> createEmpty() {
        return new EmptyContent<>(emptyContent);
    }

    @Override
    public Content<I, W, R> create() {
        return new AccumulatorContentFlat<>(f1, f2, sumR, emptyContent);
    }
}
