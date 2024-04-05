package org.streamreasoning.rsp4j.api.operators.r2r;


import org.streamreasoning.rsp4j.api.querying.result.SolutionMapping;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

public interface RelationToRelationOperator<R extends Iterable<?>> {

    R evalUnary(R dataset);
    R evalBinary(R dataset1, R dataset2);

    TimeVarying<Collection<R>> apply(SDS<R> sds);

    SolutionMapping<R> createSolutionMapping(R result);

    default Map<String, RelationToRelationOperator<R>> getR2RComponents(){
        return Collections.emptyMap();
    }

}
    