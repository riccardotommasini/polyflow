package org.streamreasoning.polyflow.api.operators.s2r.execution.state;

public interface SegmentFactory<I, R> {

    Segment<I, R> createEmpty();

    Segment<I, R> create();
}
