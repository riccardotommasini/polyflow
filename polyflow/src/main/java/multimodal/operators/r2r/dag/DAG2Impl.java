package multimodal.operators.r2r.dag;

import org.streamreasoning.rsp4j.api.operators.multimodal.m2m.ModelToModelOperator;
import org.streamreasoning.rsp4j.api.operators.r2r.DAG.DAG2;
import org.streamreasoning.rsp4j.api.operators.r2r.DAG.DAGNode;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;
import shared.operatorsimpl.r2r.DAG.BinaryDAGNodeImpl;
import shared.operatorsimpl.r2r.DAG.DAGRootNodeImpl;
import shared.operatorsimpl.r2r.DAG.UnaryDAGNodeImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DAG2Impl<R1 extends Iterable<?>, R2 extends Iterable<?>> implements DAG2<R1, R2> {


    Map<String, DAGNode<R2>> root = new HashMap<>();
    ModelToModelOperator<R1, R2> m2m;
    DAGNode<R2> tail;
    @Override
    public void addToDAG(RelationToRelationOperator<R2> op) {
        DAGNode<R2> dagNode;
        if(op.getTvgNames().size()>1) //Binary R2R
            dagNode = new BinaryDAGNodeImpl<>(op);

        else //Unary R2R
            dagNode = new UnaryDAGNodeImpl<>(op);

        root.put(op.getResName(), dagNode);
        for(String prev : op.getTvgNames()){
            DAGNode<R2> node = root.get(prev);
            node.setNext(dagNode);
            dagNode.addPrev(node);
        }
    }

    @Override
    public void addM2MToDAG(ModelToModelOperator<R1, R2> op) {
        this.m2m = op;
    }

    @Override
    public void addTVGs(Collection<TimeVarying<R1>> tvgsOne, Collection<TimeVarying<R2>> tvgsTwo) {
        for(TimeVarying<R1> tvg : tvgsOne){
            root.put(tvg.iri(), new DAG2RootConvertibleNode<>(tvg, m2m));
        }
        for(TimeVarying<R2> tvg : tvgsTwo){
            root.put(tvg.iri(), new DAGRootNodeImpl<>(tvg));
        }
    }

    @Override
    public R2 eval(long ts) {
        return this.tail.eval(ts);
    }

    @Override
    public TimeVarying<R2> apply() {
        return this.tail.apply();
    }

    @Override
    public DAGNode<R2> getTail() {
        return this.tail;
    }

    @Override
    public void initialize() {
        DAGNode<R2> tmp  = root.values().stream().findFirst().get();
        while(tmp.hasNext()){
            tmp = tmp.getNext();
        }
        this.tail = tmp;
    }

    @Override
    public void printDAG() {

    }
}
