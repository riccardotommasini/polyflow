package org.streamreasoning.rsp4j.api.querying;

import org.streamreasoning.rsp4j.api.operators.multimodal.m2m.ModelToModelOperator;
import org.streamreasoning.rsp4j.api.operators.multimodal.s2s.StreamToStreamOperator;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Task2Impl<I1, W1, R1 extends Iterable<?>, O1, I2, W2, R2 extends Iterable<?>, O2> implements Task2<I1, W1, R1, O1, I2, W2, R2, O2>{


    List<Task<I1, W1, R1, O1>> taskOneList;
    List<Task<I2, W2, R2, O2>> taskTwoList;
    Map<DataStream<I1>, List<Task<I1, W1, R1, O1>>> registeredTasksOne;
    Map<DataStream<I2>, List<Task<I2, W2, R2, O2>>> registeredTasksTwo;
    List<Time> times;
    ModelToModelOperator<R1, R2> m2mOperator;
    StreamToStreamOperator<I1, I2> s2sOperator;
    RelationToStreamOperator<R2, O1> r2sOperatorOne;
    RelationToStreamOperator<R2, O2> r2sOperatorTwo; 
    List<RelationToRelationOperator<R2>> r2rOperators;



    public Task2Impl(){
        this.taskOneList = new ArrayList<>();
        this.taskTwoList = new ArrayList<>();
        this.registeredTasksOne = new HashMap<>();
        this.registeredTasksTwo = new HashMap<>();
        this.times = new ArrayList<>();
        this.r2rOperators = new ArrayList<>();
    }

    @Override
    public List<Task<I1, W1, R1, O1>> getTasksOne() {
        return taskOneList;
    }

    @Override
    public List<Task<I2, W2, R2, O2>> getTasksTwo() {
        return taskTwoList;
    }

    @Override
    public ModelToModelOperator<R1, R2> getM2M() {
        return null;
    }

    @Override
    public StreamToStreamOperator<I1, I2> getS2S() {
        return null;
    }

    @Override
    public List<RelationToRelationOperator<?>> getR2Rs() {
        return null;
    }

    @Override
    public RelationToStreamOperator<?, ?> getR2S() {
        return null;
    }

    @Override
    public Task2<I1, W1, R1, O1, I2, W2, R2, O2> addTaskOne(Task<I1, W1, R1, O1> task) {
        return null;
    }

    @Override
    public Task2<I1, W1, R1, O1, I2, W2, R2, O2> addTaskTwo(Task<I2, W2, R2, O2> task) {
        return null;
    }

    @Override
    public Task2<I1, W1, R1, O1, I2, W2, R2, O2> addM2MOperator(ModelToModelOperator<R1, R2> m2m) {
        return null;
    }

    @Override
    public Task2<I1, W1, R1, O1, I2, W2, R2, O2> addS2SOperator(StreamToStreamOperator<I1, I2> s2s) {
        return null;
    }

    @Override
    public Task2<I1, W1, R1, O1, I2, W2, R2, O2> addR2ROperator(RelationToRelationOperator<?> r2r) {
        return null;
    }

    @Override
    public Task2<I1, W1, R1, O1, I2, W2, R2, O2> addR2SOperator(RelationToStreamOperator<?, ?> r2s) {
        return null;
    }

    @Override
    public Task2<I1, W1, R1, O1, I2, W2, R2, O2> addTime(Time time) {
        return null;
    }
}
