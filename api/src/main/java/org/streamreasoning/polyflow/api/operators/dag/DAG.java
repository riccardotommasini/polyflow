package org.streamreasoning.polyflow.api.operators.dag;

import org.streamreasoning.polyflow.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.polyflow.api.sds.timevarying.TimeVarying;

import java.util.Collection;

/**
 * This interface represents a DAG object
 */

public interface DAG<R extends Iterable<?>> {

     /**
     * Appends a new R2R operation to the sequence of operations of the specified tvg
     */
   void addToDAG(RelationToRelationOperator<R> op);

    /**
     * Adds all the TVGs of the task, they act as a 'root' node for the DAG
     */
   void addTVGs(Collection<TimeVarying<R>> tvgs);
    /**
     * Begins the computation of the DAG, returns the result
     */
   R eval(long ts);

    /**
     * Returns a lazy time varying that can later be materialized to compute the result
     */
   TimeVarying<R> apply();
    /**
     * Returns the tail of the DAG
     */
   DAGNode<R> getTail();
    /**
     * Initializes the DAG by traversing it and storing the last element (tail) to begin the computation backwards efficiently
     */

   void initialize();

    /**
     * Prints the DAG
     */
   void printDAG();
}
