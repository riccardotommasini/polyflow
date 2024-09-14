package org.streamreasoning.polyflow.api.sds;

import org.streamreasoning.polyflow.api.sds.timevarying.TimeVarying;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Created by riccardo on 01/07/2017.
 */
public interface SDS<E> {

    Collection<TimeVarying<E>> asTimeVaryingEs();

    void add(String iri, TimeVarying<E> tvg);

    void add(TimeVarying<E> tvg);

    default SDS<E> materialize(long ts) {
        asTimeVaryingEs().forEach(eTimeVarying -> eTimeVarying.materialize(ts));
        return this;
    }


    Stream<E> toStream();
}
