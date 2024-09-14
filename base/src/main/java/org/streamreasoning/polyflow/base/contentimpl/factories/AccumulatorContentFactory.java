package org.streamreasoning.polyflow.base.contentimpl.factories;

import org.streamreasoning.polyflow.base.contentimpl.EmptyContent;
import org.streamreasoning.polyflow.api.secret.content.Content;
import org.streamreasoning.polyflow.api.secret.content.ContentFactory;
import org.streamreasoning.polyflow.base.contentimpl.content.AccumulatorContent;

import java.util.function.BiFunction;
import java.util.function.Function;

public class AccumulatorContentFactory<I, W, R> implements ContentFactory<I, W, R> {

    Function<I, W> f1;
    Function<W, R> f2;
    BiFunction<R, R, R> sumR;
    R emptyContent;

    public AccumulatorContentFactory(Function<I, W> f1, Function<W, R> f2, BiFunction<R, R, R> sumR,  R emptyContent){
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
        return new AccumulatorContent<>(f1, f2, sumR, emptyContent);
    }
}
