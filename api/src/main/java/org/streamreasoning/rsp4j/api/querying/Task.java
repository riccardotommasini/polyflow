package org.streamreasoning.rsp4j.api.querying;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.r2r.DAG.DAG;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.util.Collection;
import java.util.List;

public interface Task<I, W, R extends Iterable<?>, O> {

    List<StreamToRelationOperator<I, W, R>> getS2Rs();

    List<RelationToRelationOperator<R>> getR2Rs();

    RelationToStreamOperator<R, O> getR2Ss();
    /**
     * Adds an S2R operator to the task and registers it as consumer of the input stream it's interest in
     * @param s2rOperator The stream to relation operator
     * @param inputStream Stream which the operator inside the container is interested in
     * @return The task itself
     */
    Task<I, W, R, O> addS2ROperator(StreamToRelationOperator<I, W, R> s2rOperator, DataStream<I> inputStream);

    /**
     * Adds an R2R operator to the task
     * @return The task itself
     */
    Task<I, W, R, O> addR2ROperator(RelationToRelationOperator<R> r2rOperator);

    /**
     * Adds a R2S operator to the task
     * @return The task itself
     */
    Task<I, W, R, O> addR2SOperator(RelationToStreamOperator<R, O> r2sOperator);

    Task<I, W, R, O> addTime(Time time);

    Task<I, W, R, O> addSDS(SDS<R> sds);

    Task<I, W, R, O> addDAG(DAG<R> dag);

    DAG<R> getDAG();

    SDS<R> getSDS();

    Time getTime();

    /**
     * Initializes the Task by creating the Time Varying Objects and the DAG of the Task.
     * Adds the Time Varying Objects to the SDS
     */
    void initialize();

    /**
     * Returns a Time Varying object representing the query result, which can be evaluated when needed
     * Should be used with a Lazy Task, which represents a pull-query
     */
    TimeVarying<R> getLazyEvaluation();

    void evictWindows();


    /**
     * This method passes an input element that just arrived from an input stream to the task in order to elaborate it
     * @param inputStream InputStream that generated the element
     * @param element Element that arrived from the stream
     * @param timestamp Event time
     * @return Optional Collection of O, which is the output of the task after the computation. Empty if the element did not trigger a computation
     */
    Collection<Collection<O>> elaborateElement(DataStream<I> inputStream, I element, long timestamp);
}
