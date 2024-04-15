package relational.operatorsimpl.r2r;

import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.querying.DAG.DAG;
import org.streamreasoning.rsp4j.api.querying.DAG.DAGNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DAGImpl<R extends Iterable<?>> implements DAG<R> {


    Map<String, DAGNode<R>> root = new HashMap<>();

    R result;


    @Override
    public void addToDAG(List<String> tvgNames, RelationToRelationOperator<R> op) {

        DAGNode<R> dagNode = new DAGNodeImpl<>(op, tvgNames, op.isBinary());

        for(String tvg: tvgNames){
            if(!root.containsKey(tvg)){
                root.put(tvg, dagNode);
            }
            else{
                DAGNode<R> node = root.get(tvg);
                while(node.hasNext()){
                    node = node.getNext();
                }
                node.setNext(dagNode);
            }
        }

    }

    @Override
    public R eval(String tvgName, R operand) {
        if(!root.containsKey(tvgName)){
            throw new RuntimeException("No DAG is available for the specified tvg");
        }
        this.result = root.get(tvgName).eval(operand);
        return result;

    }
}
