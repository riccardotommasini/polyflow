package org.streamreasoning.rsp4j.api.coordinators;

import org.streamreasoning.rsp4j.api.querying.Task;
import org.streamreasoning.rsp4j.api.querying.Task2;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.util.List;

public interface ContinuousProgram2<I1, W1, R1 extends Iterable<?>, O1, I2, W2, R2 extends Iterable<?>, O2> {

    /**
     * Passes the query that will be used to build the various components needed to answer it(tasks, operators etc..)
     */
    void buildTask(String query);

    /**
     * Stores the Task received as input and binds it to the input and output streams passed to the method
     */
    void buildInputs(Task2<I1, W1, R1, O1, I2, W2, R2, O2> task, List<DataStream<?>> inputStreams);

    void buildOutputOne(Task2<I1, W1, R1, O1, I2, W2, R2, O2> task, List<DataStream<O1>> outputStreams);

    void buildOutputTwo(Task2<I1, W1, R1, O1, I2, W2, R2, O2> task, List<DataStream<O2>> outputStreams);
    /**
     * Stores the Task (which represents a view) received as input and binds it to the input streams passed to the method
     */
    void buildInputsView(Task2<I1, W1, R1, O1, I2, W2, R2, O2> view, List<DataStream<?>> inputStreams);
}
