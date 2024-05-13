package org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner;


import org.streamreasoning.rsp4j.api.enums.ReportGrain;
import org.streamreasoning.rsp4j.api.enums.Tick;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;
import org.streamreasoning.rsp4j.api.secret.content.Content;
import org.streamreasoning.rsp4j.api.secret.report.Report;
import org.streamreasoning.rsp4j.api.secret.time.Time;

import java.util.List;

/*
 * I represents the variable type of the input, e.g., RDF TRIPLE, RDF GRAPHS OR TUPLE
 * W represents the variable type of the maintained status, e.g., BAG of RDF Triple, RDF Graph (set) or RELATION
 * */

public interface StreamToRelationOperator<I, W, R> {

    Report report();

    Tick tick();

    Time time();

    ReportGrain grain();

    Content<I, W, R> content(long t_e);

    List<Content<I, W, R>> getContents(long t_e);

    TimeVarying<R> get();

    String getName();

    default boolean named() {
        return getName() != null;
    }

    TimeVarying<R> apply();

    /**
     * Inserts the element in the windows it belongs. If needed, signals that the query result needs to be computed
     * @param arg Element that arrives from the stream
     * @param ts Event time
     */
    void windowing(I arg, long ts);

    /**
     * Clears all the windows that expired
     */
    void evict();


}
