package org.streamreasoning.polyflow.base.contentimpl.factories;

import org.streamreasoning.polyflow.api.secret.content.Content;
import org.streamreasoning.polyflow.api.secret.content.ContentFactory;
import org.streamreasoning.polyflow.base.contentimpl.EmptyContent;
import org.streamreasoning.polyflow.base.contentimpl.content.BasicContent;

import java.util.function.BiFunction;
import java.util.function.Function;

public class BasicContentFactory<I, W, R> implements ContentFactory<I, W, R> {

    private final W empty;
    BiFunction<I, W, W> f1;
    Function<W, R> f2;
    Function<W, Integer> f3;

    public BasicContentFactory(BiFunction<I, W, W> f1, Function<W, R> f2, Function<W, Integer> f3, W empty) {
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.empty = empty;
    }

    @Override
    public Content<I, W, R> createEmpty() {
        return new EmptyContent<>(empty, System.currentTimeMillis());
    }

    @Override
    public Content<I, W, R> create() {
        return new BasicContent<>(f1, f2, f3);
    }
}
