package org.streamreasoning.rsp4j.api.operators.r2r;


import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;

import java.util.Collections;
import java.util.List;

public interface RelationToRelationOperator<R extends Iterable<?>> {

    /**
     * Takes as input one (or two) R and applies an operation to return another R
     */
    R eval(List<R> datasets);


    /**
     * Takes as input a Time varying and returns a time varying that can be later queried to compute the result of the operation
     */
    default TimeVarying<R> apply(TimeVarying<R> node) {
        return new TimeVarying<R>() {
            @Override
            public void materialize(long ts) {
                node.materialize(ts);
            }

            @Override
            public R get() {
                return eval(Collections.singletonList(node.get()));
            }

            @Override
            public String iri() {
                return node.iri() + this.iri();
            }

            @Override
            public void setIri(String name) {

            }
        };
    }

    /**
     * Get the names of all the TVG on which this operator should be applied
     */
    List<String> getTvgNames();

    /**
     * Returns the name of the partial result of this operator
     */
    String getResName();


}
    