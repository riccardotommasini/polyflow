package org.streamreasoning.rsp4j.api.operators.r2r.DAG;

import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;

import java.util.Collection;
import java.util.List;

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
     * Prepares the DAG by setting the starting value from which the computation starts (operand) for each path of the DAG
     */
   void prepare(String tvgName, R operand);

    /**
     * Clears the DAG from previous computation's partial results
     */
   void clear();
    /**
     * Initializes the DAG by traversing it and storing the last element (tail) to begin the computation backwards efficiently
     */

   void initialize();

    /**
     * Prints the DAG
     */
   void printDAG();
}
