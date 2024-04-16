package org.streamreasoning.rsp4j.api.querying.DAG;

import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;

import java.util.List;

public interface DAGNode<R extends Iterable<?>> {

    /**
     * Returns the names of the operands of this DAG node
     */
    List<String> getOperandsNames();

    /**
     * Returns the operands of this DAG node
     */
    List<R> getOperands();

    /**
     * Returns the result of the DAG computation up to (and including) this DAG node
     */
    R getPartialRes();

    /**
     * True if the DAG node contains a binary operations, false otherwise
     */
    boolean isBinary();

    /**
     * Returns the R2R operator associated with this DAG node
     */
    RelationToRelationOperator<R> getR2rOperator();

    /**
     * Sets the DAG node that should be executed after the current one
     */
    void setNext(DAGNode<R> next);

    /**
     * Returns the next node in the DAG
     */
    DAGNode<R> getNext();

    /**
     * True if it's not the last DAG node of the DAG, false otherwise
     */
    boolean hasNext();

    /**
     * If the DAG node does not contain a binary operator, applies the R2R operator and returns the result.
     * If the DAG node contains a binary operator, the first time the method is called stores the first operand;
     * The second time applies the binary R2R operation and returns the result
     */
    R eval(R operand);

    /**
     * Clears the DAG node from previous computation's results
     */
    void clear();

    /**
     * Returns the name of the operation (selection, projection, join)
     */
    String getOpName();


}
