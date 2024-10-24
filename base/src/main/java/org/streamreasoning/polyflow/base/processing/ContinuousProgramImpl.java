package org.streamreasoning.polyflow.base.processing;

import org.streamreasoning.polyflow.api.processing.ContinuousProgram;
import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.Consumer;
import org.streamreasoning.polyflow.api.processing.Task;
import org.streamreasoning.polyflow.api.stream.data.DataStream;

import java.util.*;


public class ContinuousProgramImpl<I,W, R extends Iterable<?>, O> implements ContinuousProgram<I, W, R, O>, Consumer<I> {

    Map<String, Task<I, W, R, O>> taskMap;
    Map<DataStream<I>, List<Task<I, W, R, O>>> registeredTasks;
    Map<Task<I, W, R, O>, List<DataStream<O>>> taskToOutMap;


    public ContinuousProgramImpl(){
        this.taskMap = new HashMap<>();
        this.registeredTasks = new HashMap<>();
        this.taskToOutMap = new HashMap<>();
    }

    @Override
    public void buildTask(String query) {
        //Parse query and extract operators and streams; add them to a new Task


    }
    public void buildTask(Task<I, W, R, O> task, List<DataStream<I>> inputStreams, List<DataStream<O>> outputStreams){
        if(this.taskMap.containsKey(task.getId())){
            throw new RuntimeException("Task with ID "+task.getId()+" already exists!");
        }
        this.taskMap.put(task.getId(), task);
        inputStreams.forEach(input -> addInputStream(input, task));
        outputStreams.forEach(output -> addOutputStream(output, task));
    }

    public void buildView(Task<I, W, R, O> view, List<DataStream<I>> inputStreams){
        if(this.taskMap.containsKey(view.getId())){
            throw new RuntimeException("Task with ID "+view.getId()+" already exists!");
        }
        this.taskMap.put(view.getId(), view);
        inputStreams.forEach(input->addInputStream(input, view));
    }

    @Override
    public void unregisterTask(String taskId) {
        Task<I, W, R, O> toRemove = taskMap.get(taskId);
        if(toRemove == null){
            return;
        }
        taskMap.remove(taskId);
        registeredTasks.values().forEach(l->l.removeIf(t->t.getId().equals(taskId)));
        taskToOutMap.remove(toRemove);
    }


    /**
     * Maps a task to the inputStream it's interested in and adds the Continuous program as a consumer of the input stream
     * @param inputStream Input stream that the task is interested in
     * @param task Task that consumes from InputStream
     */
    private void addInputStream(DataStream<I> inputStream, Task<I, W, R, O> task) {

        inputStream.addConsumer(this);

        if(!registeredTasks.containsKey(inputStream)){
            registeredTasks.put(inputStream, new ArrayList<>());
        }
        if(!registeredTasks.get(inputStream).contains(task)){
            registeredTasks.get(inputStream).add(task);
        }

    }

    /**
     * Maps a task to the associated output stream
     * @param outputStream The stream that the task will write on
     * @param task The task that will write on the output stream
     */
    private void addOutputStream(DataStream<O> outputStream, Task<I, W, R, O> task){

        if(!taskToOutMap.containsKey(task)){
            taskToOutMap.put(task, new ArrayList<>());
        }
        if(!taskToOutMap.get(task).contains(outputStream)){
            taskToOutMap.get(task).add(outputStream);
        }

    }

    /**
     * Notifies all the tasks subscribed to the input stream that generated the event
     * @param inputStream The stream that generated the element
     * @param element The value that entered the stream
     * @param timestamp The event time of the element
     */
    @Override
    public void notify(DataStream<I> inputStream, I element, long timestamp) {

        if(registeredTasks.containsKey(inputStream)) {
            for (Task<I, W, R, O> t : registeredTasks.get(inputStream)) {
                //Update all the windows for all the interested tasks
                t.elaborateElement(inputStream, element, timestamp);
            }
            for(Task<I, W, R, O> t : registeredTasks.get(inputStream)){
                //If a Task has an associated output stream --> it's a push query, not a view, so we proceed with the computation
                if (taskToOutMap.containsKey(t)) {
                    Collection<Collection<O>> result;
                    result = t.compute();
                    result.forEach(coll -> coll.forEach(o -> taskToOutMap.get(t).forEach(out -> out.put(o, timestamp))));
                }
            }
        }

        //Evict the windows of the views for memory efficiency
        for(Task<I, W, R, O> v : taskMap.values()){
            v.evictWindows();
        }


    }

}
