package org.streamreasoning.rsp4j.api.querying;

import org.streamreasoning.rsp4j.api.operators.multimodal.m2m.ModelToModelOperator;
import org.streamreasoning.rsp4j.api.operators.multimodal.s2s.StreamToStreamOperator;
import org.streamreasoning.rsp4j.api.operators.r2r.DAG.DAG;
import org.streamreasoning.rsp4j.api.operators.r2r.DAG.DAG2;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.rsp4j.api.sds.SDS2;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.secret.time.TimeInstant;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

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
    Collection<Collection<O1>> outputOne;
    Collection<Collection<O2>> outputTwo;
    DAG2<R1, R2> dag;
    SDS2<R1, R2> sds;




    public Task2Impl(){
        this.taskOneList = new ArrayList<>();
        this.taskTwoList = new ArrayList<>();
        this.registeredTasksOne = new HashMap<>();
        this.registeredTasksTwo = new HashMap<>();
        this.times = new ArrayList<>();
        this.r2rOperators = new ArrayList<>();
        this.outputOne = new ArrayList<>();
        this.outputTwo = new ArrayList<>();
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
        this.taskOneList.add(task);
        return this;
    }

    @Override
    public Task2<I1, W1, R1, O1, I2, W2, R2, O2> addTaskTwo(Task<I2, W2, R2, O2> task) {
        this.taskTwoList.add(task);
        return this;
    }

    @Override
    public Task2<I1, W1, R1, O1, I2, W2, R2, O2> addM2MOperator(ModelToModelOperator<R1, R2> m2m) {
        this.m2mOperator = m2m;
        return this;
    }

    @Override
    public Task2<I1, W1, R1, O1, I2, W2, R2, O2> addS2SOperator(StreamToStreamOperator<I1, I2> s2s) {
        this.s2sOperator = s2s;
        return this;
    }

    @Override
    public Task2<I1, W1, R1, O1, I2, W2, R2, O2> addR2ROperator(RelationToRelationOperator<R2> r2r) {

        this.r2rOperators.add(r2r);
        return this;
    }

    @Override
    public Task2<I1, W1, R1, O1, I2, W2, R2, O2> addR2SOperatorOne(RelationToStreamOperator<R2, O1> r2s) {
        this.r2sOperatorOne = r2s;
        return this;
    }

    @Override
    public Task2<I1, W1, R1, O1, I2, W2, R2, O2> addR2SOperatorTwo(RelationToStreamOperator<R2, O2> r2s) {
        this.r2sOperatorTwo = r2s;
        return this;
    }

    @Override
    public Task2<I1, W1, R1, O1, I2, W2, R2, O2> addTime(Time time) {
        this.times.add(time);
        return this;
    }

    @Override
    public Task2<I1, W1, R1, O1, I2, W2, R2, O2> addDAG(DAG2<R1,R2> dag){
        this.dag = dag;
        return this;
    }

    @Override
    public Task2<I1, W1, R1, O1, I2, W2, R2, O2> addSDS(SDS2<R1,R2> sds){
        this.sds = sds;
        return this;
    }
    @Override
    public void clear(){
        this.outputOne = new ArrayList<>();
        this.outputTwo = new ArrayList<>();
    }

    @Override
    public void initialize(){
        taskOneList.stream().map(Task::apply).forEach(tvg->sds.addToOne(tvg));
        taskTwoList.stream().map(Task::apply).forEach(tvg->sds.addToTwo(tvg));

        dag.addTVGs(sds.asTimeVaryingEsOne(), sds.asTimeVaryingEsTwo());
        for (RelationToRelationOperator<R2> op : r2rOperators){
            dag.addToDAG(op);
        }
        dag.initialize();

    }

    @Override
    public void elaborateElement(DataStream<?> inputStream, Object element, long timestamp){
        if(registeredTasksOne.containsKey(inputStream)){
            //The tasks should be LazyTasks, so that elaborateElement doesn't trigger any computation, it just updates the windows
            registeredTasksOne.get(inputStream).forEach(t->t.elaborateElement((DataStream<I1>) inputStream, (I1) element, timestamp));
        }
        if(registeredTasksTwo.containsKey(inputStream)){
            registeredTasksTwo.get(inputStream).forEach(t->t.elaborateElement((DataStream<I2>) inputStream, (I2) element, timestamp));
        }
        SortedSet<TimeInstant> sortedSet = new TreeSet<>((TimeInstant a, TimeInstant b)->{
            if(a.t<b.t)
                return -1;
            if(a.t>b.t)
                return 1;
            return 0;
        });
        for(Time t: times){
            while(t.hasEvaluationInstant())
                sortedSet.add(t.getEvaluationTime());
        }


        for(TimeInstant t : sortedSet){
            /*As in the classic task, we have a dag whose root nodes are the various TVGs. Assuming we wanna do the computations on the type R2
            (so we want to convert R1 to R2), the root node that holds the TVG of type R1 will just apply the m2m operator and forward the result to
            the next dag node, which will have an R2R of type R2*/
            R2 res = dag.eval(t.t);
            outputOne.add(r2sOperatorOne.eval(res, t.t).collect(Collectors.toList()));
            outputTwo.add(r2sOperatorTwo.eval(res, t.t).collect(Collectors.toList()));

        }
        taskOneList.forEach(Task::evictWindows);
        taskTwoList.forEach(Task::evictWindows);


    }


}
