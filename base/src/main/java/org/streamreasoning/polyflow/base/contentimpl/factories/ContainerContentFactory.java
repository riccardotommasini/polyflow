package org.streamreasoning.polyflow.base.contentimpl.factories;

import org.streamreasoning.polyflow.api.secret.content.Content;
import org.streamreasoning.polyflow.api.secret.content.ContentFactory;
import org.streamreasoning.polyflow.base.contentimpl.EmptyContent;
import org.streamreasoning.polyflow.base.contentimpl.content.ContainerContent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ContainerContentFactory<I, W, R, K> implements ContentFactory<I, W, R> {

    private Function<I, K> keyFromI;
    private Function<W, K> keyFromW;
    private Function<R, K> keyFromR;
    private BiFunction<R, R, R> sumR;
    private Map<K, Content<I, W, R>> keyedContent = new HashMap<>();
    private R emptyContent;

    private ContentFactory<I, W, R> internalContentFactory;

    public ContainerContentFactory(Function<I, K> keyFromI ,Function<W, K> keyFromW, Function<R, K> keyFromR,
                                   BiFunction<R, R, R> sumR, R emptyContent, ContentFactory<I, W, R> internalContentFactory ){
        this.keyFromI = keyFromI;
        this.keyFromW = keyFromW;
        this.keyFromR = keyFromR;
        this.sumR = sumR;
        this.emptyContent = emptyContent;
        this.internalContentFactory = internalContentFactory;
    }

    @Override
    public Content<I, W, R> createEmpty() {
        return new EmptyContent<>(emptyContent);
    }

    @Override
    public Content<I, W, R> create() {
        return new ContainerContent<>(keyFromI, keyFromW, keyFromR, sumR, emptyContent, internalContentFactory);
    }
}
