package org.streamreasoning.polyflow.base.processing;

import org.streamreasoning.polyflow.api.processing.ContinuousProgram;
import org.streamreasoning.polyflow.api.processing.Task;
import org.streamreasoning.polyflow.api.stream.data.DataStream;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * This class does not support Tasks that perform different queries. The different Tasks that are
 * registered and used in this class should be copies of the same Task, and data are partitioned among them by
 * a key.
 */
public class ParallelContinuousProgram<I, W, R extends Iterable<?>, O, K> implements ContinuousProgram<I, W, R, O> {

    //taskList contains a list of Tasks which represent the partitions of a single logical Task.
    List<Task<I, W, R, O>> taskList;
    //taskMap is used to manage the IDs of the tasks. We need the taskList as an extra for the hashing logic (order of the tasks must be preserved)
    Map<String, Task<I, W, R, O>> taskMap;
    Map<DataStream<I>, List<Task<I, W, R, O>>> registeredTasks;
    Map<Task<I, W, R, O>, List<DataStream<O>>> taskToOutMap;
    Function<I, K> keyFromI;
    ExecutorService executor;


    public ParallelContinuousProgram(Function<I, K> keyFromI, int threadNumber){
        this.taskMap = new HashMap<>();
        this.taskList = new ArrayList<>();
        this.registeredTasks = new HashMap<>();
        this.taskToOutMap = new HashMap<>();
        this.keyFromI = keyFromI;
        this.executor = Executors.newFixedThreadPool(threadNumber);
    }

    @Override
    public void buildTask(String query) {
        //Parse query and extract operators and streams; add them to a new Task


    }
    public void buildTask(Task<I, W, R, O> task, List<DataStream<I>> inputStreams, List<DataStream<O>> outputStreams){
        if(this.taskMap.containsKey(task.getId())){
            throw new RuntimeException("Task with ID "+task.getId()+" already exists!");
        }
        this.taskList.add(task);
        this.taskMap.put(task.getId(), task);
        inputStreams.forEach(input -> addInputStream(input, task));
        outputStreams.forEach(output -> addOutputStream(output, task));
    }

    public void buildView(Task<I, W, R, O> view, List<DataStream<I>> inputStreams){
        if(this.taskMap.containsKey(view.getId())){
            throw new RuntimeException("Task with ID "+view.getId()+" already exists!");
        }
        this.taskList.add(view);
        this.taskMap.put(view.getId(), view);
        inputStreams.forEach(input->addInputStream(input, view));
    }

    @Override
    public void unregisterTask(String taskId) {
        Task<I, W, R, O> toRemove = taskMap.get(taskId);
        if(toRemove == null){
            return;
        }
        int idx=0;
        for(int i =0; i<taskList.size(); i++)
            if(taskList.get(i) == toRemove) {
                idx = i;
                break;
            }
        taskList.remove(idx);
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
     * Notifies the partition associated to the key of the element received.
     * There is synchronization on the single Tasks to avoid race conditions on the windows.
     */
    @Override
    public void notify(DataStream<I> inputStream, I element, long timestamp) {

        if(registeredTasks.containsKey(inputStream)) {
            K key = keyFromI.apply(element);
            int partition = key.hashCode() % taskList.size();
            Task<I, W, R, O> task = taskList.get(partition);
            executor.execute(()-> {
                synchronized (task) {
                    System.out.println("Sending element in partition " + partition);
                    task.elaborateElement(inputStream, element, timestamp);

                    //If a Task has an associated output stream --> it's a push query, not a view, so we proceed with the computation
                    if (taskToOutMap.containsKey(task)) {
                        Collection<Collection<O>> result;
                        result = task.compute();
                        result.forEach(coll -> coll.forEach(o -> taskToOutMap.get(task).forEach(out -> out.put(o, timestamp))));
                    }
                }
            });
        }

    }
}
