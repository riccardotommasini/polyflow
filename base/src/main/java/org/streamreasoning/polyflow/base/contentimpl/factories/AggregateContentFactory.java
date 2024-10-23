package org.streamreasoning.polyflow.base.contentimpl.factories;

import org.streamreasoning.polyflow.api.secret.content.Content;
import org.streamreasoning.polyflow.api.secret.content.ContentFactory;
import org.streamreasoning.polyflow.base.contentimpl.EmptyContent;
import org.streamreasoning.polyflow.base.contentimpl.content.AggregateContent;

import java.util.function.BiFunction;
import java.util.function.Function;

public class AggregateContentFactory<I, W, R> implements ContentFactory<I, W, R> {

    private Function<I, W> f1;
    private Function<W, R> f2;
    private BiFunction<R, R, R> sumR;
    private Function<R, R> aggregate;
    private R emptyContent;

    public AggregateContentFactory(Function<I, W> f1, Function<W, R> f2, BiFunction<R, R, R> sumR,
                                   Function<R, R> aggregate, R emptyContent) {
        this.f1 = f1;
        this.f2 = f2;
        this.sumR = sumR;
        this.aggregate = aggregate;
        this.emptyContent = emptyContent;
    }


    @Override
    public Content<I, W, R> createEmpty() {
        return new EmptyContent<>(emptyContent);
    }

    @Override
    public Content<I, W, R> create() {
        return new AggregateContent<>(f1, f2, sumR, aggregate, emptyContent);
    }
}
