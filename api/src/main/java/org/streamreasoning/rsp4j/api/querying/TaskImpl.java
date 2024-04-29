package org.streamreasoning.rsp4j.api.querying;

import org.apache.log4j.Logger;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.rsp4j.api.operators.s2r.Convertible;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.r2r.DAG.DAG;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.util.*;
import java.util.stream.Collectors;

public class TaskImpl<I, W, R extends Iterable<?>, O> implements Task<I, W, R, O>{


    private static final Logger log = Logger.getLogger(TaskImpl.class);
    private List<StreamToRelationOperator<I, W, R>> s2rOperators;
    private List<RelationToRelationOperator<R>> r2rOperators;
    private RelationToStreamOperator<R, O> r2sOperator;
    private Map<DataStream<I>, List<StreamToRelationOperator<I, W, R>>> registeredS2R;
    private Time time;

    private DAG<R> dag;

    private SDS<R> sds;

    public TaskImpl(){

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
        return r2sOperator;
    }

    @Override
    public Task<I, W, R, O> addS2ROperator(StreamToRelationOperator<I, W, R> s2rOperator, DataStream<I> inputStream) {
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
        this.r2sOperator= r2sOperator;
        return this;
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

    public void initialize(){
        for(StreamToRelationOperator<I, W, R> operator: s2rOperators){
            TimeVarying<R> tvg = operator.apply();
            this.sds.add(tvg);
            if(tvg.named()){
                this.sds.add(tvg.iri(), tvg);
            }
        }

       /*
         here we assume that when we encounter a binary R2R operator, all of the previous operators in the dag of both operands have been already added
         Moreover, after a binary operator, if more R2R needs to be computed, we add them as unary operators with the tvg name of the first operand of the
         binary R2R, in order to be consistent with the DAG shape.

         tableA: o -> o -> o -> \
                                 O -> o this last 'o' will be an R2R operator with the tvg name tableA, so it will only be added once to the DAG
         tableB: o -> o -> o -> /

       */

       for(RelationToRelationOperator<R> op : r2rOperators){
           if(!op.isBinary()) {
               for (String tvgName : op.getTvgNames()) {
                   dag.addToDAG(Collections.singletonList(tvgName), op);
               }
           }
           else {
               //We assume that each binary operator contains at most 2 tvg names, which are the names of its operands
               dag.addToDAG(op.getTvgNames(), op);
           }
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

        while(time.hasEvaluationInstant()){
            long t = time.getEvaluationTime().t;
            log.debug("Evaluation time instant found with t= "+t+", R2R computation will begin");
            R partialRes = eval(t);
            res.add(r2sOperator.eval(partialRes, timestamp).collect(Collectors.toList()));
        }

        for(StreamToRelationOperator<I, W, R> operator : s2rOperators){
            operator.evict();
        }

        return res;

    }

    private R eval(long ts){

        this.sds.materialize(ts);
        R result = null;
        for(TimeVarying<R> tvg : sds.asTimeVaryingEs()){
            result = dag.eval(tvg.iri(), tvg.get());
        }
        dag.clear();
        return result;


    }




}
