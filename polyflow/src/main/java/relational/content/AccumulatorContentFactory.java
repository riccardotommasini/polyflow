package relational.content;

import org.streamreasoning.rsp4j.api.secret.content.Content;
import org.streamreasoning.rsp4j.api.secret.content.ContentFactory;

import java.util.function.BiFunction;
import java.util.function.Function;

public class AccumulatorContentFactory<I, W, R> implements ContentFactory<I, W, R> {

    Function<I, W> f1;
    BiFunction<W, R, R> f2;
    public AccumulatorContentFactory(Function<I, W> f1, BiFunction<W, R, R> f2){
        this.f1 = f1;
        this.f2 = f2;
    }
    @Override
    public Content<I, W, R> createEmpty() {
        return null;
    }

    @Override
    public Content<I, W, R> create() {
        return new AccumulatorContent<>(f1, f2);
    }
}
