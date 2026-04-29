package org.streamreasoning.polyflow.base.operatorsimpl.s2r.state;

import org.apache.log4j.Logger;
import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.Window;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.SingleBufferState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;

public class ListSingleBufferState<I, R> implements SingleBufferState<I, R> {

    private static final Logger log = Logger.getLogger(ListSingleBufferState.class);

    private final Segment<I, R> emptySegment;
    private final Segment<I, R> workingSegment;
    private final BiPredicate<I, Long> expiresBefore;
    private Window window = null;

    public ListSingleBufferState(Segment<I, R> workingSegment, Segment<I, R> emptySegment, BiPredicate<I, Long> expiresBefore) {
        this.workingSegment = workingSegment;
        this.emptySegment = emptySegment;
        this.expiresBefore = expiresBefore;
    }

    @Override
    public void setWindow(Window w) {
        this.window = w;
    }

    @Override
    public Window getWindow() {
        return window;
    }

    @Override
    public Segment<I, R> segment() {
        return window == null ? emptySegment : workingSegment;
    }

    @Override
    public void append(I e) {
        workingSegment.add(e);
    }

    @Override
    public void evict(long ts) {
        List<I> toRemove = new ArrayList<>();
        try {
            Iterator<I> it = workingSegment.iterator();
            while (it.hasNext()) {
                I next = it.next();
                if (expiresBefore.test(next, ts)) {
                    try {
                        it.remove();
                    } catch (UnsupportedOperationException e) {
                        toRemove.add(next);
                    }
                }
            }
        } catch (UnsupportedOperationException e) {
            log.warn("Segment iterator not supported, falling back to explicit remove()");
        }

        for (I e : toRemove) {
            workingSegment.remove(e);
        }
    }

    @Override
    public void clear() {
        evict(Long.MAX_VALUE);
        window = null;
    }

    @Override
    public Segment<I, R> emptySegment() {
        return emptySegment;
    }

    @Override
    public int size() {
        return window == null ? 0 : workingSegment.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
}
