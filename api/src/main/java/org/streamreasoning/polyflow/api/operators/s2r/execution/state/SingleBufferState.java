package org.streamreasoning.polyflow.api.operators.s2r.execution.state;

import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.Window;

public interface SingleBufferState<I, R> {

    void setWindow(Window w);

    Window getWindow();

    Segment<I, R> segment();

    void append(I e);

    void evict(long ts);

    void clear();

    Segment<I, R> emptySegment();

    int size();

    boolean isEmpty();

    default boolean toReport() {
        return false;
    }
}
