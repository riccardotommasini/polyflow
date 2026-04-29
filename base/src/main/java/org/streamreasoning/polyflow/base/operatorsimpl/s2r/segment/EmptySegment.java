package org.streamreasoning.polyflow.base.operatorsimpl.s2r.segment;

import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;

import java.util.Iterator;

public class EmptySegment<I, R> implements Segment<I, R> {

    private final R empty;

    public EmptySegment(R empty) {
        this.empty = empty;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void add(I e) {
        throw new UnsupportedOperationException("Cannot add to an empty segment");
    }

    @Override
    public R coalesce() {
        return empty;
    }

    @Override
    public void remove(I e) {
        throw new UnsupportedOperationException("Cannot remove from an empty segment");
    }

    @Override
    public Iterator<I> iterator() {
        throw new UnsupportedOperationException("Cannot iterate over an empty segment");
    }
}
