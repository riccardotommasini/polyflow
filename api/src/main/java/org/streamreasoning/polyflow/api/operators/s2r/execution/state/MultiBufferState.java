package org.streamreasoning.polyflow.api.operators.s2r.execution.state;

import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.Window;

public interface MultiBufferState<I, R> {

    Segment<I, R> create(Window w);

    Segment<I, R> get(Window w);

    Iterable<Window> windows();

    void evict(Window w);

    Segment<I, R> emptySegment();

    int size();

    boolean isEmpty();

    default boolean toReport() {
        return false;
    }
}
