package org.streamreasoning.rsp4j.api.querying;

import org.streamreasoning.rsp4j.api.containers.R2RContainer;
import org.streamreasoning.rsp4j.api.containers.R2SContainer;
import org.streamreasoning.rsp4j.api.containers.S2RContainer;
import org.streamreasoning.rsp4j.api.operators.s2r.Convertible;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOp;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.util.*;
import java.util.stream.Collectors;

public class TaskImpl<I, W extends Convertible<R>, R extends Iterable<?>, O> implements Task<I, W, R, O>{

    Set<S2RContainer<I, W>> s2rContainers;
    List<R2RContainer<R>> r2rContainers;
    R2SContainer<R, O> r2sContainer;
    Map<DataStream<I>, List<StreamToRelationOp<I, W>>> registeredS2R;
    Time time;

    SDS<W> sds;

    public TaskImpl(){

        this.s2rContainers = new HashSet<>();
        this.r2rContainers = new ArrayList<>();
        this.registeredS2R = new HashMap<>();
    }

    @Override
    public Set<S2RContainer<I, W>> getS2Rs() {
        return s2rContainers;
    }

    @Override
    public List<R2RContainer<R>> getR2Rs() {
        return r2rContainers;
    }

    @Override
    public R2SContainer<R, O> getR2Ss() {
        return r2sContainer;
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
    public Task<I, W, R, O> addR2RContainer(R2RContainer<R> r2rContainer) {
        this.r2rContainers.add(r2rContainer);
        return this;
    }

    @Override
    public Task<I, W, R, O> addR2SContainer(R2SContainer<R, O> r2sContainer) {
        this.r2sContainer= r2sContainer;
        return this;
    }

    @Override
    public Task<I, W, R, O> addTime(Time time){
        this.time = time;
        return this;
    }

    @Override
    public Task<I, W, R, O> addSDS(SDS<W> sds){
        this.sds = sds;
        return this;
    }

    public void initialize(){
        for(S2RContainer<I, W> container: s2rContainers){
            this.sds.add(container.getS2rOperator().apply());
        }
    }


    @Override
    public Collection<Collection<O>> elaborateElement(DataStream<I> inputStream, I element, long timestamp) {

        if(registeredS2R.get(inputStream).isEmpty()){
            throw new RuntimeException("No S2R operator interested in the Stream");
        }

        Collection<Collection<O>> res = new ArrayList<>();

        for(StreamToRelationOp<I, W> s2r : registeredS2R.get(inputStream)){
            s2r.windowing(element, timestamp);
        }

        while(time.hasEvaluationInstant()){
            R partialRes = computeR2R(time.getEvaluationTime().t);
            res.add(r2sContainer.getR2sOperator().eval(partialRes, timestamp).collect(Collectors.toList()));
        }

        for(S2RContainer<I, W> container : s2rContainers){
            container.getS2rOperator().evict();
        }

        return res;

    }

    private R computeR2R(long ts){

        this.sds.materialize(ts);
        for(TimeVarying<W> tvg : sds.asTimeVaryingEs()){
            tvg.get().compute();
        }

        List<R> binary = new ArrayList<>();
        for(TimeVarying<W> tvg : sds.asTimeVaryingEs()){
            R result = tvg.get().convertToR();
            for(R2RContainer<R> container : r2rContainers){
                //If the R2R has to be applied to the TVG (based on the iri name) and is not a binary operation
                //TODO :Assume that the list of R2R containers has all the unary operations first and the binary operations as last, need to improve later
                if(container.getTvgNames().contains(tvg.iri()) && !container.isBinary()){
                    result = container.getR2rOperator().evalUnary(result);
                }
            }
            binary.add(result);
        }

        if(binary.isEmpty()){
            throw new RuntimeException("R2R returned empty object");
        }

        R result = binary.get(0);


        //TODO: Assume only 1 binary R2R, need to change later
        if(binary.size() > 1) {
            for (R2RContainer<R> container : r2rContainers) {
                if (container.isBinary()) {
                    result = container.getR2rOperator().evalBinary(binary.get(0), binary.get(1));
                }
            }
        }
        return result;


    }




}
