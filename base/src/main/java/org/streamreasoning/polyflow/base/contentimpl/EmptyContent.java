package org.streamreasoning.polyflow.base.contentimpl;

import org.streamreasoning.polyflow.api.secret.content.Content;

public class EmptyContent<I, W, R> implements Content<I, W, R> {

    long ts;
    private R r;
    private W w;


    public EmptyContent(R o) {
        this.r = o;
        ts = System.currentTimeMillis();
    }

    public EmptyContent(W o, long ts) {
        this.w = o;
        this.ts = ts;
    }


    @Override
    public int size() {
        return 0;
    }

    @Override
    public void add(I e) {
        throw new UnsupportedOperationException();
    }


    @Override
    public R coalesce() {
        return r;
    }
}