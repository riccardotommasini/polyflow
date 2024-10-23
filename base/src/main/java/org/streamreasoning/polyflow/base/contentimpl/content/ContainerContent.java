package org.streamreasoning.polyflow.base.contentimpl.content;

import org.streamreasoning.polyflow.api.secret.content.Content;
import org.streamreasoning.polyflow.api.secret.content.ContentFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ContainerContent<I, W, R, K> implements Content<I, W, R> {

    private Function<I, K> keyFromI;
    private Function<W, K> keyFromW;
    private Function<R, K> keyFromR;
    private BiFunction<R, R, R> sumR;
    private Map<K, Content<I, W, R>> keyedContent = new HashMap<>();
    private R emptyContent;
    ContentFactory<I, W, R> internalContentFactory;

    public ContainerContent(Function<I, K> keyFromI ,Function<W, K> keyFromW, Function<R, K> keyFromR,
                            BiFunction<R, R, R> sumR, R emptyContent, ContentFactory<I, W, R> internalContentFactory ){
        this.keyFromI = keyFromI;
        this.keyFromW = keyFromW;
        this.keyFromR = keyFromR;
        this.sumR = sumR;
        this.emptyContent = emptyContent;
        this.internalContentFactory = internalContentFactory;
    }

    @Override
    public int size() {
        return keyedContent.size();
    }

    @Override
    public void add(I i) {
        K key = keyFromI.apply(i);
        keyedContent.computeIfAbsent(key, k->internalContentFactory.create());
        keyedContent.get(key).add(i);
    }

    @Override
    public R coalesce() {
        return keyedContent.values().stream().map(c->c.coalesce()).reduce(emptyContent, (r1, r2)->sumR.apply(r1,r2));
    }
}
