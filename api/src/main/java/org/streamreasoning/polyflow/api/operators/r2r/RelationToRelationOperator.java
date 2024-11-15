package org.streamreasoning.polyflow.api.operators.r2r;


import org.streamreasoning.polyflow.api.sds.timevarying.TimeVarying;

import java.util.Collections;
import java.util.List;

public interface RelationToRelationOperator<R extends Iterable<?>> {

    /**
     * Takes as input one (or two) R and applies an operation to return another R
     */
    R eval(List<R> datasets);

    /**
     * Get the names of all the TVG on which this operator should be applied
     */
    List<String> getTvgNames();

    /**
     * Returns the name of the partial result of this operator
     */
    String getResName();


}
    