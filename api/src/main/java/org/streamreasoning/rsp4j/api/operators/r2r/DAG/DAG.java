package org.streamreasoning.rsp4j.api.operators.r2r.DAG;

import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;

import java.util.List;

/**
 * This interface represents a DAG object
 */

public interface DAG<R extends Iterable<?>> {

     /**
     * Appends a new R2R operation to the sequence of operations of the specified tvg
     */
   void addToDAG(List<String> tvgNames, RelationToRelationOperator<R> op);

    /**
     * Begins the computation of the DAG, returns the result
     */
   R eval();

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
