package org.streamreasoning.polyflow.base.operatorsimpl.s2r.state;

import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.Window;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.MultiBufferState;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.SegmentFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapMultiBufferState<I, R> implements MultiBufferState<I, R> {

    private final Map<Window, Segment<I, R>> windowSegments = new LinkedHashMap<>();
    private final SegmentFactory<I, R> segmentFactory;
    private final Segment<I, R> emptySegment;
    public String segmentName;

    public MapMultiBufferState(SegmentFactory<I, R> segmentFactory) {
        this.segmentFactory = segmentFactory;
        this.emptySegment = segmentFactory.createEmpty();
        segmentName = segmentFactory.getClass().getSimpleName();
    }

    @Override
    public Segment<I, R> create(Window w) {
        return windowSegments.computeIfAbsent(w, ignored -> segmentFactory.create());
    }

    @Override
    public Segment<I, R> get(Window w) {
        return windowSegments.get(w);
    }

    @Override
    public Iterable<Window> windows() {
        return windowSegments.keySet();
    }

    @Override
    public void evict(Window w) {
        windowSegments.remove(w);
    }

    @Override
    public Segment<I, R> emptySegment() {
        return emptySegment;
    }

    @Override
    public int size() {
        return windowSegments.size();
    }

    @Override
    public boolean isEmpty() {
        return windowSegments.isEmpty();
    }

    @Override
    public String toString() {
        return "MapMultiBuffer";
    }

    public String getSegmentName() {
        return segmentName;
    }
}
