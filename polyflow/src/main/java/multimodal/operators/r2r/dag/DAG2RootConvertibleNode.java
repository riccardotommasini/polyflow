package multimodal.operators.r2r.dag;

import org.streamreasoning.rsp4j.api.operators.multimodal.m2m.ModelToModelOperator;
import org.streamreasoning.rsp4j.api.operators.r2r.DAG.DAGNode;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;
import shared.operatorsimpl.r2r.DAG.DAGRootNodeImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DAG2RootConvertibleNode<R1, R2 extends Iterable<?>> implements DAGNode<R2> {

    ModelToModelOperator<R1, R2> m2m;
    TimeVarying<R1> tvg;
    private List<DAGNode<R2>> next = new ArrayList<>();

    public DAG2RootConvertibleNode(TimeVarying<R1> tvg, ModelToModelOperator<R1, R2> m2m){
        this.tvg = tvg;
        this.m2m = m2m;
    }
    @Override
    public List<String> getOperandsNames() {
        return Collections.emptyList();
    }

    @Override
    public RelationToRelationOperator<R2> getR2rOperator() {
        return null;
    }

    @Override
    public void setNext(DAGNode<R2> next) {
        this.next.add(next);
    }

    @Override
    public void addPrev(DAGNode<R2> prev) {
        throw new RuntimeException("Impossible to add a previous node to a dag root");
    }

    @Override
    public DAGNode<R2> getNext() {
        return next.get(0);
    }

    @Override
    public List<DAGNode<R2>> getPrev() {
        return null;
    }

    @Override
    public boolean hasNext() {
        return this.next.isEmpty();
    }

    @Override
    public boolean hasPrev() {
        return false;
    }

    @Override
    public R2 eval(long ts) {
        tvg.materialize(ts);
        //Convert the R1 to R2 and return it, this is the 'multimodal' part that converts a type to another
        return m2m.eval(tvg.get());
    }

    @Override
    public TimeVarying<R2> apply() {
        return null;
    }
}
