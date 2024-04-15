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
     * Unary function of the operator, takes as input an R and applies an operation to return another R
     */
    R evalUnary(R dataset);

    /**
     * Binary function of the operator, takes as input two R and applies an operation to return another R
     */
    R evalBinary(R dataset1, R dataset2);

    TimeVarying<Collection<R>> apply(SDS<R> sds);

    /**
     * Get the names of all the TVG on which this operator should be applied
     */
    List<String> getTvgNames();

    /**
     * True if the operator is binary, false otherwise
     */
    boolean isBinary();

    SolutionMapping<R> createSolutionMapping(R result);

    default Map<String, RelationToRelationOperator<R>> getR2RComponents(){
        return Collections.emptyMap();
    }

}
    