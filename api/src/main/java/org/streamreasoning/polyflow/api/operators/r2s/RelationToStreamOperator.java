package org.streamreasoning.polyflow.api.operators.r2s;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Created by riccardo on 07/07/2017.
 */
public interface RelationToStreamOperator<R extends Iterable<?>, O> {

    default O transform(Object sm, long ts) {
        return (O) sm;
    }

    default Stream<O> eval(R sml, long ts) {
        Collection<O> result = new ArrayList<>();
        sml.forEach(e -> result.add(transform(e, ts)));
        return result.stream();
    }

}