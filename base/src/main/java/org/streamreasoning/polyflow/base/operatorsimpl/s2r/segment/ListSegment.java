package org.streamreasoning.polyflow.base.operatorsimpl.s2r.segment;

import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ListSegment<I, R> implements Segment<I, R> {

    private final List<I> content = new ArrayList<>();
    private final Function<I, R> lift;
    private final BiFunction<R, R, R> sumR;
    private final R emptyContent;

    public ListSegment(Function<I, R> lift, BiFunction<R, R, R> sumR, R emptyContent) {
        this.lift = lift;
        this.sumR = sumR;
        this.emptyContent = emptyContent;
    }

    @Override
    public int size() {
        return content.size();
    }

    @Override
    public void add(I e) {
        content.add(e);
    }

    @Override
    public void remove(I e) {
        content.remove(e);
    }

    @Override
    public Iterator<I> iterator() {
        return content.iterator();
    }

    @Override
    public R coalesce() {
        return content.stream().map(lift).reduce(emptyContent, sumR::apply);
    }

    @Override
    public String toString() {
        return "List";
    }
}
