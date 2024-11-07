package org.streamreasoning.polyflow.base.contentimpl.content;

import org.streamreasoning.polyflow.api.secret.content.Content;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class AccumulatorContentFlat<I, W, R> implements Content<I, W, R> {


    List<W> content = new ArrayList<>();
    Function<I, Stream<W>> f1;
    Function<W, R> f2;
    BiFunction<R, R, R> sumR;
    R emptyContent;

    public AccumulatorContentFlat(Function<I, Stream<W>> f1, Function<W, R> f2, BiFunction<R, R, R> sumR, R emptyContent) {
        this.f1 = f1;
        this.f2 = f2;
        this.sumR = sumR;
        this.emptyContent = emptyContent;
    }


    @Override
    public int size() {
        return content.size();
    }

    @Override
    public void add(I e) {
        this.f1.apply(e).forEach(this.content::add);
    }

    @Override
    public R coalesce() {

        return content.stream().map(f2).reduce(emptyContent, (x, y) -> sumR.apply(x, y));
    }
}
