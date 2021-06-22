package org.streamreasoning.rsp4j.api.operators.r2r;


import org.streamreasoning.rsp4j.api.querying.result.SolutionMapping;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;

import java.util.Collection;
import java.util.stream.Stream;

public interface RelationToRelationOperator<W, R> {

    //TODO this should not be time-aware
    Stream<R> eval(Stream<W> sds);

    TimeVarying<Collection<R>> apply(SDS<W> sds);

    SolutionMapping<R> createSolutionMapping(R result);

}
    