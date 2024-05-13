package operatorsimpl.r2r.DAG;

import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.r2r.DAG.DAGNode;

import java.util.ArrayList;
import java.util.List;

public class DAGNodeImpl<R extends Iterable<?>> implements DAGNode<R> {

    private RelationToRelationOperator<R> r2rOperator;
    private List<String> operandsNames;
    private List<R> operands = new ArrayList<>();
    private boolean isBinary;
    private DAGNode<R> next;
    private List<DAGNode<R>> prev;

    private String opName;

    public DAGNodeImpl(RelationToRelationOperator<R> r2rOperator, List<String> operandsNames, boolean isBinary){
        this.r2rOperator = r2rOperator;
        this.operandsNames = operandsNames;
        this.isBinary = isBinary;
        this.prev = new ArrayList<>();
    }

    @Override
    public List<String> getOperandsNames(){
        return operandsNames;
   }

    @Override
    public List<R> getOperands() {
       return operands;
    }

    @Override
    public boolean isBinary() {
        return isBinary;
    }
    @Override
    public RelationToRelationOperator<R> getR2rOperator() {
        return r2rOperator;
    }

    @Override
    public String getOpName() {
        return isBinary? r2rOperator.getBinaryOpName():r2rOperator.getUnaryOpName();
    }

    @Override
    public void setNext(DAGNode<R> next){
        this.next = next;
    }
    @Override
    public void addOperand(R operand){this.operands.add(operand);}

    @Override
    public void addPrev(DAGNode<R> prev){
        this.prev.add(prev);
    }
    @Override
    public DAGNode<R> getNext(){
        return this.next;
    }
    @Override
    public List<DAGNode<R>> getPrev(){
        return this.prev;
    }
    @Override
    public boolean hasNext(){
        return this.next != null;
    }
    @Override
    public boolean hasPrev(){
        return !this.prev.isEmpty();
    }
    @Override
    public void clear(){
       this.operands = new ArrayList<>();
    }

    @Override
    public R eval(){

        //Operator unary, keep applying operations on the DAG and store the result of the current node for provenance reasons
        if(!isBinary){
            if(hasPrev()){
                return this.r2rOperator.evalUnary(prev.get(0).eval());
            }

            else {
                return this.r2rOperator.evalUnary(operands.get(0));
            }

        }

        //Operator is binary, we have different possible configurations
        else{
            if(hasPrev()) {
                if(this.prev.size() > 1)
                    return this.r2rOperator.evalBinary(prev.get(0).eval(), prev.get(1).eval());
                else{
                    return this.r2rOperator.evalBinary( operands.get(0), prev.get(0).eval());
                }
            }
            else{
                return this.r2rOperator.evalBinary(operands.get(0), operands.get(1));
            }
        }

    }

}
