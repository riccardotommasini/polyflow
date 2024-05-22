package org.streamreasoning.rsp4j.api.querying;

import org.streamreasoning.rsp4j.api.operators.multimodal.m2m.ModelToModelOperator;
import org.streamreasoning.rsp4j.api.operators.multimodal.s2s.StreamToStreamOperator;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.rsp4j.api.secret.time.Time;

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

    Task2<I1, W1, R1, O1, I2, W2, R2, O2> addR2ROperator(RelationToRelationOperator<?> r2r);

    Task2<I1, W1, R1, O1, I2, W2, R2, O2> addR2SOperator(RelationToStreamOperator<?, ?> r2s);

    Task2<I1, W1, R1, O1, I2, W2, R2, O2> addTime(Time time);





}
