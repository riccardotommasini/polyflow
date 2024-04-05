package org.streamreasoning.rsp4j.api.querying;

import org.streamreasoning.rsp4j.api.containers.AggregationContainer;
import org.streamreasoning.rsp4j.api.containers.R2RContainer;
import org.streamreasoning.rsp4j.api.containers.R2SContainer;
import org.streamreasoning.rsp4j.api.containers.S2RContainer;
import org.streamreasoning.rsp4j.api.operators.s2r.Convertible;
import org.streamreasoning.rsp4j.api.sds.DataSet;
import org.streamreasoning.rsp4j.api.operators.r2r.Var;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface Task<I, W extends Convertible<R>, R extends Iterable<?>, O> {
    Set<S2RContainer<I, W>> getS2Rs();

    List<R2RContainer<R>> getR2Rs();

    R2SContainer<R, O> getR2Ss();
    /**
     * Adds an S2R container to the task and registers it as consumer of the input stream it's interest in
     * @param s2rContainer Container that needs to be added to the Task
     * @param inputStream Stream which the operator inside the container is interested in
     * @return The task itself
     */
    public Task<I, W, R, O> addS2RContainer(S2RContainer<I, W> s2rContainer, DataStream<I> inputStream);

    /**
     * Adds an R2R container to the task
     * @return The task itself
     */
    public Task<I, W, R, O> addR2RContainer(R2RContainer<R> r2rContainer);

    /**
     * Adds a R2S container to the task
     * @return The task itself
     */
    public Task<I, W, R, O> addR2SContainer(R2SContainer<R, O> r2sContainer);

    public Task<I, W, R, O> addTime(Time time);

    public Task<I, W, R, O> addSDS(SDS<W> sds);

    /**
     * Initializes the Task by creating the Time Varying Objects and adds them to the SDS
     */
    public void initialize();



    /**
     * This method passes an input element that just arrived from an input stream to the task in order to elaborate it
     * @param inputStream InputStream that generated the element
     * @param element Element that arrived from the stream
     * @param timestamp Event time
     * @return Optional Collection of O, which is the output of the task after the computation. Empty if the element did not trigger a computation
     */
    Collection<Collection<O>> elaborateElement(DataStream<I> inputStream, I element, long timestamp);
}
