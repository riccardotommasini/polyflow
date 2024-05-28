package org.streamreasoning.rsp4j.api.coordinators;

import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.Consumer;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.Consumer2;
import org.streamreasoning.rsp4j.api.querying.Task;
import org.streamreasoning.rsp4j.api.querying.Task2;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.util.*;

public class ContinuousProgram2Impl<I1, W1, R1 extends Iterable<?>, O1, I2, W2, R2 extends Iterable<?>, O2> implements Consumer2, ContinuousProgram2<I1, W1, R1,O1, I2, W2, R2, O2> {

    List<Task2<I1, W1, R1, O1, I2, W2, R2, O2>> taskList;
    List<Task2<I1, W1, R1, O1, I2, W2, R2, O2>> viewList;
    Map<DataStream<?>, List<Task2<I1, W1, R1, O1, I2, W2, R2, O2>>> registeredViews;
    Map<DataStream<?>, List<Task2<I1, W1, R1, O1, I2, W2, R2, O2>>> registeredTasks;
    Map<Task2<I1, W1, R1, O1, I2, W2, R2, O2>, List<DataStream<O1>>> taskToOutMapOne;
    Map<Task2<I1, W1, R1, O1, I2, W2, R2, O2>, List<DataStream<O2>>> taskToOutMapTwo;

    public ContinuousProgram2Impl(){
        this.taskList = new ArrayList<>();
        this.viewList = new ArrayList<>();
        this.registeredTasks = new HashMap<>();
        this.registeredViews = new HashMap<>();
        this.taskToOutMapOne = new HashMap<>();
        this.taskToOutMapTwo = new HashMap<>();
    }
    @Override
    public void buildTask(String query) {

    }

    @Override
    public void buildInputs(Task2<I1, W1, R1, O1, I2, W2, R2, O2> task, List<DataStream<?>> inputStreams) {
        this.taskList.add(task);
        inputStreams.forEach(input -> addInputStream(input, task));

    }

    @Override
    public void buildInputsView(Task2<I1, W1, R1, O1, I2, W2, R2, O2> view, List<DataStream<?>> inputStreams) {
        this.viewList.add(view);
        inputStreams.forEach(input->addInputStreamViews(input, view));
    }

    public void buildOutputOne(Task2<I1, W1, R1, O1, I2, W2, R2, O2> task, List<DataStream<O1>> outputStreams){
        outputStreams.forEach(output->addOutputStreamOne(output, task));
    }

    public void buildOutputTwo(Task2<I1, W1, R1, O1, I2, W2, R2, O2> task, List<DataStream<O2>> outputStreams){
        outputStreams.forEach(output->addOutputStreamTwo(output, task));
    }

    public void addInputStreamViews(DataStream<?> inputStream,Task2<I1, W1, R1, O1, I2, W2, R2, O2> view){
        inputStream.addConsumer(this);

        if(!registeredViews.containsKey(inputStream)){
            registeredViews.put(inputStream, new ArrayList<>());
        }
        if(!registeredViews.get(inputStream).contains(view)){
            registeredViews.get(inputStream).add(view);
        }
    }

    private void addInputStream(DataStream<?> inputStream, Task2<I1, W1, R1, O1, I2, W2, R2, O2> task) {

        inputStream.addConsumer(this);

        if(!registeredTasks.containsKey(inputStream)){
            registeredTasks.put(inputStream, new ArrayList<>());
        }
        if(!registeredTasks.get(inputStream).contains(task)){
            registeredTasks.get(inputStream).add(task);
        }

    }

    private void addOutputStreamOne(DataStream<O1> outputStream, Task2<I1, W1, R1, O1, I2, W2, R2, O2> task){

        if(!taskToOutMapOne.containsKey(task)){
            taskToOutMapOne.put(task, new ArrayList<>());
        }
        if(!taskToOutMapOne.get(task).contains(outputStream)){
            taskToOutMapOne.get(task).add(outputStream);
        }

    }

    private void addOutputStreamTwo(DataStream<O2> outputStream, Task2<I1, W1, R1, O1, I2, W2, R2, O2> task){

        if(!taskToOutMapTwo.containsKey(task)){
            taskToOutMapTwo.put(task, new ArrayList<>());
        }
        if(!taskToOutMapTwo.get(task).contains(outputStream)){
            taskToOutMapTwo.get(task).add(outputStream);
        }

    }


    @Override
    public void notify(DataStream<?> inputStream, Object element, long timestamp) {

        if(registeredViews.containsKey(inputStream)) {
            for (Task2<I1, W1, R1, O1, I2, W2, R2, O2> v : registeredViews.get(inputStream)) {
                v.elaborateElement(inputStream, element, timestamp);
            }
        }



        if(registeredTasks.containsKey(inputStream)) {
            for (Task2<I1, W1, R1, O1, I2, W2, R2, O2> t : registeredTasks.get(inputStream)) {
                //elaborateElement will transform R to Collection<O> using the task's r2s operators
                t.elaborateElement(inputStream, element, timestamp);
                if (taskToOutMapOne.containsKey(t)) {
                    t.getOutputOne().forEach(coll -> coll.forEach(o -> taskToOutMapOne.get(t).forEach(out -> out.put(o, timestamp))));
                }
                if (taskToOutMapTwo.containsKey(t)) {
                    t.getOutputTwo().forEach(coll -> coll.forEach(o -> taskToOutMapTwo.get(t).forEach(out -> out.put(o, timestamp))));
                }
                if(!taskToOutMapOne.containsKey(t) && !taskToOutMapTwo.containsKey(t)){
                    throw new RuntimeException("Task2 has no associated output streams");
                }
            }
        }
        registeredViews.values().forEach(l->l.forEach(Task2::clear));
        registeredTasks.values().forEach(l->l.forEach(Task2::clear));

    }


}
