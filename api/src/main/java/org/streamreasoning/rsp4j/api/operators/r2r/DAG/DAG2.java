package org.streamreasoning.rsp4j.api.operators.r2r.DAG;

import org.streamreasoning.rsp4j.api.operators.multimodal.m2m.ModelToModelOperator;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;

import java.util.Collection;

public interface DAG2<R1, R2 extends Iterable<?>> {

    /**
     * Appends a new R2R operation to the sequence of operations of the specified tvg
     */
    void addToDAG(RelationToRelationOperator<R2> op);

    /**
     * Appends a new M2M operator to the nodes of type R1 of the DAG to convert them to R2 before the computation
     */
    void addM2MToDAG(ModelToModelOperator<R1, R2> op);

    /**
     * Adds all the TVGs of the tasks, they act as a 'root' node for the DAG
     */
    void addTVGs(Collection<TimeVarying<R1>> tvgsOne, Collection<TimeVarying<R2>> tvgsTwo);
    /**
     * Begins the computation of the DAG, returns the result
     */
    R2 eval(long ts);

    /**
     * Returns a lazy time varying that can later be materialized to compute the result
     */
    TimeVarying<R2> apply();
    /**
     * Returns the tail of the DAG
     */
    DAGNode<R2> getTail();
    /**
     * Initializes the DAG by traversing it and storing the last element (tail) to begin the computation backwards efficiently
     */

    void initialize();

    /**
     * Prints the DAG
     */
    void printDAG();
}
