package org.streamreasoning.rsp4j.api.operators.r2r.DAG;

import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;

import java.util.List;

public interface DAG<R extends Iterable<?>> {

     /**
     * Appends a new R2R operation to the sequence of operations of the specified tvg
     */
   void addToDAG(List<String> tvgNames, RelationToRelationOperator<R> op);

    /**
     * Begins the computation of the specified path of the DAG, returns the (possibly partial) result of that branch of computation
     */
   R eval(String tvgName, R operand);

    /**
     * Clears the DAG from previous computation's partial results
     */
   void clear();

    /**
     * Prints the DAG
     */
   void printDAG();
}
