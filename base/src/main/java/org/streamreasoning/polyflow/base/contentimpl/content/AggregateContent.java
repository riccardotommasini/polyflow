package org.streamreasoning.polyflow.base.contentimpl.content;

import org.streamreasoning.polyflow.api.secret.content.Content;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AggregateContent<I, W, R> implements Content<I, W, R> {

    private Function<I, W> f1;
    private Function<W, R> f2;
    private BiFunction<R, R, R> sumR;
    private Function<R, R> aggregate;
    private R emptyContent;

    private List<W> content;

    public AggregateContent(Function<I, W> f1, Function<W, R> f2, BiFunction<R, R, R> sumR,
                                   Function<R, R> aggregate, R emptyContent) {
        this.f1 = f1;
        this.f2 = f2;
        this.sumR = sumR;
        this.aggregate = aggregate;
        this.emptyContent = emptyContent;
        this.content = new ArrayList<>();
    }


    @Override
    public int size() {
        return content.size();
    }

    @Override
    public void add(I i) {
        content.add(f1.apply(i));
    }

    @Override
    public R coalesce() {
        return aggregate.apply(content.stream().map(w->f2.apply(w)).reduce(emptyContent, (r1, r2)->sumR.apply(r1, r2)));
    }
}
