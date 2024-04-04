package org.streamreasoning.rsp4j.api.querying;

import org.streamreasoning.rsp4j.api.containers.R2RContainer;
import org.streamreasoning.rsp4j.api.containers.R2SContainer;
import org.streamreasoning.rsp4j.api.containers.S2RContainer;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOp;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.util.*;

public class TaskImpl<I, W, R, O> implements Task<I, W, R, O>{

    Set<S2RContainer<I, W>> s2rContainers;
    List<R2RContainer<W, R>> r2rContainers;
    Set<R2SContainer<R, O>> r2sContainers;
    Map<DataStream<I>, List<StreamToRelationOp<I, W>>> registeredS2R;

    Time time;

    public TaskImpl(){

        this.s2rContainers = new HashSet<>();
        this.r2rContainers = new ArrayList<>();
        this.r2sContainers = new HashSet<>();
        this.registeredS2R = new HashMap<>();
    }

    @Override
    public Set<S2RContainer<I, W>> getS2Rs() {
        return s2rContainers;
    }

    @Override
    public List<R2RContainer<W, R>> getR2Rs() {
        return r2rContainers;
    }

    @Override
    public Set<R2SContainer<R, O>> getR2Ss() {
        return r2sContainers;
    }

    @Override
    public Task<I, W, R, O> addS2RContainer(S2RContainer<I, W> s2rContainer, DataStream<I> inputStream) {
        this.s2rContainers.add(s2rContainer);
        if(!registeredS2R.containsKey(inputStream)){
            registeredS2R.put(inputStream, new ArrayList<>());
        }
        if(registeredS2R.get(inputStream).contains(s2rContainer.getS2rOperator())){
            throw new RuntimeException("S2R operator already registered on this input stream");
        }
        registeredS2R.get(inputStream).add(s2rContainer.getS2rOperator());
        return this;
    }

    @Override
    public Task<I, W, R, O> addR2RContainer(R2RContainer<W, R> r2rContainer) {
        this.r2rContainers.add(r2rContainer);
        return this;
    }

    @Override
    public Task<I, W, R, O> addR2SContainer(R2SContainer<R, O> r2sContainer) {
        this.r2sContainers.add(r2sContainer);
        return this;
    }

    @Override
    public Task<I, W, R, O> addTime(Time time){
        this.time = time;
        return this;
    }


    @Override
    public Collection<O> elaborateElement(DataStream<I> inputStream, I element, long timestamp) {

        if(registeredS2R.get(inputStream).isEmpty()){
            throw new RuntimeException("No S2R operator interested in the Stream");
        }

        for(StreamToRelationOp<I, W> s2r : registeredS2R.get(inputStream)){
            s2r.windowing(element, timestamp);
        }

        //TODO: for each instantTime, compute

        return Collections.emptyList();
    }
}
