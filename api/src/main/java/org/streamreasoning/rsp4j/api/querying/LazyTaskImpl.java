package org.streamreasoning.rsp4j.api.querying;

import org.apache.log4j.Logger;
import org.streamreasoning.rsp4j.api.operators.r2r.DAG.DAG;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.LazyTimeVarying;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.util.*;
import java.util.stream.Collectors;

public class LazyTaskImpl<I, W, R extends Iterable<?>, O> implements Task<I, W, R, O>{
    private static final Logger log = Logger.getLogger(TaskImpl.class);
    private List<StreamToRelationOperator<I, W, R>> s2rOperators;
    private List<RelationToRelationOperator<R>> r2rOperators;
    private Map<DataStream<I>, List<StreamToRelationOperator<I, W, R>>> registeredS2R;
    private Time time;

    private DAG<R> dag;

    private SDS<R> sds;

    public LazyTaskImpl(){

        this.s2rOperators = new ArrayList<>();
        this.r2rOperators = new ArrayList<>();
        this.registeredS2R = new HashMap<>();
    }

    @Override
    public List<StreamToRelationOperator<I, W, R>> getS2Rs() {
        return s2rOperators;
    }

    @Override
    public List<RelationToRelationOperator<R>> getR2Rs() {
        return r2rOperators;
    }

    @Override
    public RelationToStreamOperator<R, O> getR2Ss() {
        throw new RuntimeException("R2S not available in pull query");
    }

    @Override
    public Task<I, W, R, O> addS2ROperator(StreamToRelationOperator<I, W, R> s2rOperator, DataStream<I> inputStream) {

        for(StreamToRelationOperator<I, W, R> op : s2rOperators){
            if(op.getName().equals(s2rOperator.getName())){
                throw new RuntimeException("S2R Operator with same name already present");
            }
        }
        this.s2rOperators.add(s2rOperator);
        if(!registeredS2R.containsKey(inputStream)){
            registeredS2R.put(inputStream, new ArrayList<>());
        }
        if(registeredS2R.get(inputStream).contains(s2rOperator)){
            throw new RuntimeException("S2R operator already registered on this input stream");
        }
        registeredS2R.get(inputStream).add(s2rOperator);
        return this;
    }

    @Override
    public Task<I, W, R, O> addR2ROperator(RelationToRelationOperator<R> r2rOperator) {
        this.r2rOperators.add(r2rOperator);
        return this;
    }

    @Override
    public Task<I, W, R, O> addR2SOperator(RelationToStreamOperator<R, O> r2sOperator) {
        throw new RuntimeException("R2S operator not available in pull query");
    }

    @Override
    public Task<I, W, R, O> addTime(Time time){
        this.time = time;
        return this;
    }

    @Override
    public Task<I, W, R, O> addSDS(SDS<R> sds){
        this.sds = sds;
        return this;
    }

    @Override
    public Task<I, W, R, O> addDAG(DAG<R> dag){
        this.dag = dag;
        return this;
    }

    @Override
    public DAG<R> getDAG(){
        return this.dag;
    }

    @Override
    public SDS<R> getSDS(){ return this.sds;}

    @Override
    public Time getTime() {
        return time;
    }

    @Override
    public void initialize(){
        for(StreamToRelationOperator<I, W, R> operator: s2rOperators){
            TimeVarying<R> tvg = operator.apply();
            this.sds.add(tvg);
            if(tvg.named()){
                this.sds.add(tvg.iri(), tvg);
            }
        }


        dag.addTVGs(sds.asTimeVaryingEs());
        for (RelationToRelationOperator<R> op : r2rOperators){
            dag.addToDAG(op);
        }
        dag.initialize();

    }

    @Override
    public TimeVarying<R> getLazyEvaluation(){
        return new LazyTimeVarying<>(this.sds, this.dag);
    }

    @Override
    public void evictWindows() {
        for(StreamToRelationOperator<I, W, R> s2r : s2rOperators){
            s2r.evict();
        }
    }


    @Override
    public Collection<Collection<O>> elaborateElement(DataStream<I> inputStream, I element, long timestamp) {

        if(registeredS2R.get(inputStream).isEmpty()){
            throw new RuntimeException("No S2R operator interested in the Stream");
        }

        Collection<Collection<O>> res = new ArrayList<>();

        for(StreamToRelationOperator<I, W, R> s2r : registeredS2R.get(inputStream)){
            s2r.windowing(element, timestamp);
        }

        return res;

    }
}
