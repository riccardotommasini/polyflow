package org.streamreasoning.polyflow.api.operators.s2r.execution.state;

import java.util.Iterator;

/**
 * Per-window data container managed by an S2R operator.
 */
public interface Segment<I, R> {

    int size();

    void add(I e);

    R coalesce();

    default boolean toReport() {
        return false;
    }

    default void remove(I e) {
        throw new UnsupportedOperationException("Remove not supported by this segment");
    }

    default Iterator<I> iterator() {
        throw new UnsupportedOperationException("Iterator not supported by this segment");
    }
}
