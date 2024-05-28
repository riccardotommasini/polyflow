package org.streamreasoning.rsp4j.api.querying;

import org.streamreasoning.rsp4j.api.operators.multimodal.m2m.ModelToModelOperator;
import org.streamreasoning.rsp4j.api.operators.multimodal.s2s.StreamToStreamOperator;
import org.streamreasoning.rsp4j.api.operators.r2r.DAG.DAG;
import org.streamreasoning.rsp4j.api.operators.r2r.DAG.DAG2;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.rsp4j.api.sds.SDS2;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.util.Collection;
import java.util.List;

public interface Task2 <I1, W1, R1 extends Iterable<?>, O1, I2, W2, R2 extends Iterable<?>, O2>{

    List<Task<I1,W1,R1,O1>> getTasksOne();
    List<Task<I2,W2,R2,O2>> getTasksTwo();
    ModelToModelOperator<R1, R2> getM2M();
    StreamToStreamOperator<I1, I2> getS2S();

    List<RelationToRelationOperator<?>> getR2Rs();
    RelationToStreamOperator<?, ?> getR2S();

    Task2<I1, W1, R1, O1, I2, W2, R2, O2> addTaskOne(Task<I1, W1, R1, O1> task);
    Task2<I1, W1, R1, O1, I2, W2, R2, O2> addTaskTwo(Task<I2, W2, R2, O2> task);

    Task2<I1, W1, R1, O1, I2, W2, R2, O2> addM2MOperator(ModelToModelOperator<R1, R2> m2m);

    Task2<I1, W1, R1, O1, I2, W2, R2, O2> addS2SOperator(StreamToStreamOperator<I1, I2> s2s);

    Task2<I1, W1, R1, O1, I2, W2, R2, O2> addR2ROperator(RelationToRelationOperator<R2> r2r);

    Task2<I1, W1, R1, O1, I2, W2, R2, O2> addR2SOperatorOne(RelationToStreamOperator<R2, O1> r2s);
    Task2<I1, W1, R1, O1, I2, W2, R2, O2> addR2SOperatorTwo(RelationToStreamOperator<R2, O2> r2s);

    Task2<I1, W1, R1, O1, I2, W2, R2, O2> addTime(Time time);

    Task2<I1, W1, R1, O1, I2, W2, R2, O2> addDAG(DAG2<R1, R2> dag);

    Task2<I1, W1, R1, O1, I2, W2, R2, O2> addSDS(SDS2<R1, R2> sds);

    Collection<Collection<O1>> getOutputOne();
    Collection<Collection<O2>> getOutputTwo();
    void registerInputOne(DataStream<I1> dataStream, Task<I1, W1, R1, O1> task);
    void registerInputTwo(DataStream<I2> dataStream, Task<I2, W2, R2, O2> task);
    /**
     * Clears the Task2 from results of previous computations
     */
    void clear();

    /**
     * Initializes the Task by creating the Time Varying Objects and the DAG of the Task.
     * Adds the Time Varying Objects to the SDS
     */
    void initialize();

    /**
     * The method returns void because in a Task2 we can have two possible outputs, O1 and O2, so the Coordinator (ContinoutProgram2) will
     * poll for the results it needs based on the output streams interested. The results of the computation are stored in the Task2 object
     */
    void elaborateElement(DataStream<?> inputStream, Object element, long timestamp);



}
