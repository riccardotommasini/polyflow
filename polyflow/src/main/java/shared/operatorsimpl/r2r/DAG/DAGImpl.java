package shared.operatorsimpl.r2r.DAG;

import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.r2r.DAG.DAG;
import org.streamreasoning.rsp4j.api.operators.r2r.DAG.DAGNode;

import java.util.*;

public class DAGImpl<R extends Iterable<?>> implements DAG<R> {


    Map<String, DAGNode<R>> root = new HashMap<>();

    DAGNode<R> tail;


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
                dagNode.addPrev(node);
            }
        }

    }


    @Override
    public void prepare(String tvgName, R operand) {
        if(!root.containsKey(tvgName)){
            throw new RuntimeException("No DAG is available for the specified tvg");
        }
        root.get(tvgName).addOperand(operand);
    }

    @Override
    public void initialize(){
        DAGNode<R> tmp  = root.values().stream().findFirst().get();
        while(tmp.hasNext()){
            tmp = tmp.getNext();
        }
        this.tail = tmp;
    }

    @Override
    public R eval(){
        return this.tail.eval();
    }

    @Override
    public void clear(){
        for(DAGNode<R> node : root.values()){
            node.clear();
            while(node.hasNext()) {
                node = node.getNext();
                node.clear();
            }
        }
    }

    @Override
    public void printDAG(){
        Set<DAGNode<R>> printed = new HashSet<>();
        for(DAGNode<R> node : root.values()){
            while(node != null){
                if(!printed.contains(node)) {
                    printed.add(node);
                    System.out.print("[T:" + node.getOperandsNames() + " O:" + node.getOpName() + "]->");
                    node = node.getNext();
                }
                else if(printed.contains(node) && node.isBinary()){
                    System.out.print("[T:" + node.getOperandsNames() + " O:" + node.getOpName() + "]->");
                    break;
                }
            }
            System.out.print("\n");

        }
    }
}
