/*
package graph.jena.content;

import org.streamreasoning.rsp4j.api.secret.content.Content;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class JenaStandardContent<I, W, R> implements Content<I, W, R> {



    List<W> content = new ArrayList<>();
    Function<I, W> f1;
    BiFunction<W, R, R> f2;


    @Override
    public int size() {
        return 0;
    }

    @Override
    public void add(I e) {
        content.add(f1.apply(e));
    }

    @Override
    public R coalesce() {
        R result = null;

    }
}
*/
