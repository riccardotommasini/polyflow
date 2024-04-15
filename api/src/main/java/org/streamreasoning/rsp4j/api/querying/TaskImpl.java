package org.streamreasoning.rsp4j.api.querying;

import org.apache.log4j.Logger;
import org.streamreasoning.rsp4j.api.RDFUtils;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.rsp4j.api.operators.s2r.Convertible;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOp;
import org.streamreasoning.rsp4j.api.querying.DAG.DAG;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.util.*;
import java.util.stream.Collectors;

public class TaskImpl<I, W extends Convertible<R>, R extends Iterable<?>, O> implements Task<I, W, R, O>{


    private static final Logger log = Logger.getLogger(TaskImpl.class);
    Set<StreamToRelationOp<I, W>> s2rOperators;
    List<RelationToRelationOperator<R>> r2rOperators;
    RelationToStreamOperator<R, O> r2sOperator;
    Map<DataStream<I>, List<StreamToRelationOp<I, W>>> registeredS2R;
    Time time;

    DAG<R> dag;

    SDS<W> sds;

    public TaskImpl(){

        this.s2rOperators = new HashSet<>();
        this.r2rOperators = new ArrayList<>();
        this.registeredS2R = new HashMap<>();
    }

    @Override
    public Set<StreamToRelationOp<I, W>> getS2Rs() {
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
    public Task<I, W, R, O> addS2ROperator(StreamToRelationOp<I, W> s2rOperator, DataStream<I> inputStream) {
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
    public Task<I, W, R, O> addSDS(SDS<W> sds){
        this.sds = sds;
        return this;
    }

    @Override
    public Task<I, W, R, O> addDAG(DAG<R> dag){
        this.dag = dag;
        return this;
    }

    public void initialize(){
        for(StreamToRelationOp<I, W> operator: s2rOperators){
            TimeVarying<W> tvg = operator.apply();
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

        for(StreamToRelationOp<I, W> s2r : registeredS2R.get(inputStream)){
            s2r.windowing(element, timestamp);
        }

        while(time.hasEvaluationInstant()){
            long t = time.getEvaluationTime().t;
            log.debug("Evaluation time instant found with t= "+t+", R2R computation will begin");
            R partialRes = computeR2R(t);
            res.add(r2sOperator.eval(partialRes, timestamp).collect(Collectors.toList()));
        }

        for(StreamToRelationOp<I, W> operator : s2rOperators){
            operator.evict();
        }

        return res;

    }

    private R computeR2R(long ts){

        this.sds.materialize(ts);
        for(TimeVarying<W> tvg : sds.asTimeVaryingEs()){
            //Operation that is more efficient to do on W than on R
            tvg.get().compute();
        }

        R result = null;
        for(TimeVarying<W> tvg : sds.asTimeVaryingEs()){
            result = dag.eval(tvg.iri(), tvg.get().convertToR());
        }
        return result;

        /*List<R> binary = new ArrayList<>();
        for(TimeVarying<W> tvg : sds.asTimeVaryingEs()){
            R result = tvg.get().convertToR();
            for(RelationToRelationOperator<R> operator : r2rOperators){
                //If the R2R has to be applied to the TVG (based on the iri name) and is not a binary operation
                //TODO :Assume that the list of R2R containers has all the unary operations first and the binary operations as last, need to improve later
                if(operator.getTvgNames().contains(tvg.iri()) && !operator.isBinary()){
                    result = operator.evalUnary(result);
                }
            }
            binary.add(result);
        }

        if(binary.isEmpty()){
            throw new RuntimeException("R2R returned empty object");
        }

        R result = binary.get(0);


        //TODO: Assume only 1 binary R2R, which makes sense if we only have 2 tvgs
        if(binary.size() > 1) {
            for (RelationToRelationOperator<R> operator : r2rOperators) {
                if (operator.isBinary()) {
                    result = operator.evalBinary(binary.get(0), binary.get(1));
                }
            }
        }
        return result;*/


    }




}
