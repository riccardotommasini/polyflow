package relational.content;

import org.streamreasoning.rsp4j.api.secret.content.Content;
import org.streamreasoning.rsp4j.api.secret.content.ContentFactory;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class FilterContentFactory<I, W, R>  implements ContentFactory<I, W, R> {

    Function<I, W> f1;
    BiFunction<W, R, R> f2;

    Predicate<I> predicate;

    public FilterContentFactory(Function<I, W> f1, BiFunction<W, R, R> f2, Predicate<I> predicate){
        this.f1 = f1;
        this.f2 = f2;
        this.predicate = predicate;

    }

    @Override
    public Content<I, W, R> createEmpty() {
        return null;
    }

    @Override
    public Content<I, W, R> create() {
        return new FilterContent<>(f1, f2, predicate);
    }
}
