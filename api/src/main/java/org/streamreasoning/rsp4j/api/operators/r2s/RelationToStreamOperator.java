package org.streamreasoning.rsp4j.api.operators.r2s;

import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
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

    default Collection<O> eval(TimeVarying<Collection<R>> sml, long ts) {
        sml.materialize(ts);
        Collection<R> rs = sml.get();
        return eval(rs.stream(), ts).collect(Collectors.toList());
    }
}