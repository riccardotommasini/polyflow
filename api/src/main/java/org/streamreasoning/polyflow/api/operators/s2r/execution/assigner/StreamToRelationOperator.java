package org.streamreasoning.polyflow.api.operators.s2r.execution.assigner;


import org.streamreasoning.polyflow.api.enums.Tick;
import org.streamreasoning.polyflow.api.sds.timevarying.TimeVarying;
import org.streamreasoning.polyflow.api.secret.content.Content;
import org.streamreasoning.polyflow.api.secret.report.Report;
import org.streamreasoning.polyflow.api.secret.time.Time;

import java.util.List;

/*
 * I represents the variable type of the input, e.g., RDF TRIPLE, RDF GRAPHS OR TUPLE
 * W represents the variable type of the maintained status, e.g., BAG of RDF Triple, RDF Graph (set) or RELATION
 * */

public interface StreamToRelationOperator<I, W, R extends Iterable<?>> {

    Report report();

    Tick tick();

    Time time();

    Content<I, W, R> content(long t_e);

    List<Content<I, W, R>> getContents(long t_e);

    TimeVarying<R> get();

    String getName();

    default boolean named() {
        return getName() != null;
    }


    /**
     * Inserts the element in the windows it belongs. If needed, signals that the query result needs to be computed
     *
     * @param arg Element that arrives from the stream
     * @param ts  Event time
     */
    void compute(I arg, long ts);

    /**
     * Clears all the windows that expired
     */
    void evict();

    /**
     * Clears all the windows with closing time smaller than the parameter ts.
     * Can be used when multiple windows are active at the same time and they need
     * to be synchronized (if one window receives an event at time t, the time
     * of all the other windows advances as well and they get evicted even if they did not receive an event directly)
     */
    void evict(long ts);


}
