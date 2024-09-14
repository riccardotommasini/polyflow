package org.streamreasoning.polyflow.api.operators.dag;

import org.streamreasoning.polyflow.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.polyflow.api.sds.timevarying.TimeVarying;

import java.util.List;

public interface DAGNode<R extends Iterable<?>> {

    /**
     * Returns the names of the operands of this DAG node
     */
    List<String> getOperandsNames();


    /**
     * Returns the R2R operator associated with this DAG node
     */
    RelationToRelationOperator<R> getR2rOperator();

    /**
     * Sets the DAG node that should be executed after the current one
     */
    void setNext(DAGNode<R> next);

    /**
     * Sets the DAG nodes that should be executed before the current one
     */
    void addPrev(DAGNode<R> prev);

    /**
     * Returns the next node in the DAG
     */
    DAGNode<R> getNext();

    /**
     * Returns the previous node in the DAG
     */
    List<DAGNode<R>> getPrev();

    /**
     * True if it's not the last DAG node of the DAG, false otherwise
     */
    boolean hasNext();

    /**
     * True if it's not the first DAG node of the DAG, false otherwise
     */
    boolean hasPrev();

    /**
     * Computes the result for the current DAG Node
     **/
    R eval(long ts);

    /**
     * Returns a lazy time varying that can be materialized later to compute the result
     */
    TimeVarying<R> apply();


}
