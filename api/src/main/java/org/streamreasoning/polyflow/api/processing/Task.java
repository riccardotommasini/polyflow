package org.streamreasoning.polyflow.api.processing;
import org.streamreasoning.polyflow.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.polyflow.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.polyflow.api.operators.dag.DAG;
import org.streamreasoning.polyflow.api.sds.SDS;
import org.streamreasoning.polyflow.api.sds.timevarying.TimeVarying;
import org.streamreasoning.polyflow.api.secret.time.Time;
import org.streamreasoning.polyflow.api.stream.data.DataStream;

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

    void evictWindows();

    void evictWindows(long ts);


    /**
     * This method passes an input element that just entered an input stream to the Task in order to update its windows
     * @param inputStream InputStream that generated the element
     * @param element Element that arrived from the stream
     * @param timestamp Event time
     */
    void elaborateElement(DataStream<I> inputStream, I element, long timestamp);

    /**
     * Starts the computation associated with the Task and returns the result.
     * The Task will check if some computation time instants are present, and if so, it will proceed to compute the result.
     * @return Collection of Collection of O. Each inner Collection<O> is the result of a computation at a given time.
     */
    Collection<Collection<O>> compute();
    /**
     * Starts the computation associated with the Task at the given timestamp and returns the result.
     * The Task's windows will not be evicted since this is supposed to be a "computation on demand", not a computation triggered by
     * an event entering the input stream.
     * @return Optional Collection of O, which is the output of the task after the computation.
     */
    Collection<O> computeLazy(long ts);

    /**
     * Returns a time varying that represents the lazy evaluation of the task, can be materialized when the result is needed
     */
    TimeVarying<R> apply();
}
