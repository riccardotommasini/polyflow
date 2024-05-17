package org.streamreasoning.rsp4j.api.operators.r2r;


import org.streamreasoning.rsp4j.api.querying.result.SolutionMapping;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface RelationToRelationOperator<R extends Iterable<?>> {

    /**
     * Takes as input one (or two) R and applies an operation to return another R
     */
    R eval(List<R> datasets);


    TimeVarying<Collection<R>> apply(SDS<R> sds);

    /**
     * Get the names of all the TVG on which this operator should be applied
     */
    List<String> getTvgNames();

    /**
     * Returns the name of the partial result of this operator
     */
    String getResName();


}
    