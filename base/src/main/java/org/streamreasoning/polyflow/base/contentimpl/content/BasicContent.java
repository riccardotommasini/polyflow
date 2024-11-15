package org.streamreasoning.polyflow.base.contentimpl.content;

import org.streamreasoning.polyflow.api.secret.content.Content;

import java.util.function.BiFunction;
import java.util.function.Function;

public class BasicContent<I, W, R> implements Content<I, W, R> {

    W content;
    BiFunction<I, W, W> f1;
    Function<W, R> f2;
    Function<W, Integer> size;

    public BasicContent(BiFunction<I, W, W> f1,
                        Function<W, R> f2,
                        Function<W, Integer> size
    ) {
        this.f1 = f1;
        this.f2 = f2;
        this.size = size;
    }


    @Override
    public int size() {
        return size.apply(content);
    }

    @Override
    public void add(I e) {
        content = f1.apply(e, content);
    }

    @Override
    public R coalesce() {
        return f2.apply(content);
    }

    @Override
    public W get() {
        return content;
    }
}
