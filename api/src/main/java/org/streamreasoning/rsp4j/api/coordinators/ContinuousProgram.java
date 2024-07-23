package org.streamreasoning.rsp4j.api.coordinators;

import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.Consumer;
import org.streamreasoning.rsp4j.api.querying.Task;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.util.List;

/**
 * Interface that should be extended by a Continuous Program object, it manages a list of tasks, each of which represents a query
 */
public interface ContinuousProgram<I, W, R extends Iterable<?>, O> extends Consumer<I> {

    /**
     * Passes the query that will be used to build the various components needed to answer it(tasks, operators etc..)
     */
    void buildTask(String query);

    /**
     * Stores the Task received as input and binds it to the input and output streams passed to the method
     */
    void buildTask(Task<I, W, R, O> task, List<DataStream<I>> inputStreams, List<DataStream<O>> outputStreams);

    /**
     * Stores the Task (which represents a view) received as input and binds it to the input streams passed to the method
     */
    void buildView(Task<I, W, R, O> view, List<DataStream<I>> inputStreams);

}
