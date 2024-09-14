package org.streamreasoning.polyflow.base.contentimpl.factories;

import org.streamreasoning.polyflow.api.secret.content.Content;
import org.streamreasoning.polyflow.api.secret.content.ContentFactory;
import org.streamreasoning.polyflow.base.contentimpl.content.LastContent;

import java.util.function.Function;

public class LastContentFactory<I, W, R> implements ContentFactory<I, W, R> {

    Function<I, W> f1;
    Function<W, R> f2;
    R emptyContent;

    public LastContentFactory(Function<I, W> f1, Function<W, R> f2, R emptyContent){
        this.f1 = f1;
        this.f2 = f2;
        this.emptyContent = emptyContent;
    }
    @Override
    public Content<I, W, R> createEmpty() {
        throw new RuntimeException("why does this still exist?");
    }

    @Override
    public Content<I, W, R> create() {
        return new LastContent<>(f1, f2, emptyContent);
    }
}
