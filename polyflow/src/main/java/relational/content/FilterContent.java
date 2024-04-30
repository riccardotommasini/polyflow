package relational.content;

import org.streamreasoning.rsp4j.api.secret.content.Content;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class FilterContent <I, W, R> implements Content<I, W, R> {

    List<W> content = new ArrayList<>();

    Function<I, W> f1;
    BiFunction<W, R, R> f2;

    Predicate<I> predicate;

    public FilterContent(Function<I, W> f1, BiFunction<W, R, R> f2, Predicate<I> predicate){
        this.f1 = f1;
        this.f2 = f2;
        this.predicate = predicate;
    }


    @Override
    public int size() {
        return content.size();
    }

    @Override
    public void add(I e) {
        if(predicate.test(e))
            content.add(f1.apply(e));
        else System.out.print("Not adding element "+e.toString()+" to window content\n");

    }

    @Override
    public R coalesce() {
        R result = null;
        if(content.isEmpty())
            result = f2.apply(null, result);

        for(W cont : content){
            result = f2.apply(cont, result);
        }
        return result;
    }
}
