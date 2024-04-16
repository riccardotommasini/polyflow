package relational.operatorsimpl.r2r;

import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.querying.DAG.DAGNode;

import java.util.ArrayList;
import java.util.List;

public class DAGNodeImpl<R extends Iterable<?>> implements DAGNode<R> {

    private RelationToRelationOperator<R> r2rOperator;
    private List<String> operandsNames;
    private List<R> operands = new ArrayList<>();
    private R partialRes;
    private boolean isBinary;
    private DAGNode<R> next;

    public DAGNodeImpl(RelationToRelationOperator<R> r2rOperator, List<String> operandsNames, boolean isBinary){
        this.r2rOperator = r2rOperator;
        this.operandsNames = operandsNames;
        this.isBinary = isBinary;
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
    public R getPartialRes() {
       return this.partialRes;
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
    public void setNext(DAGNode<R> next){
        this.next = next;
    }
    @Override
    public boolean hasNext(){
        return this.next != null;
    }
    @Override
    public DAGNode<R> getNext(){
        return this.next;
    }
    @Override
    public void clear(){
       this.operands = new ArrayList<>();
       this.partialRes = null;
    }

    @Override
    public R eval(R operand){

        R result;

        //Operator unary, keep applying operations on the DAG and store the result of the current node for provenance reasons
        if(!isBinary){
            partialRes = this.r2rOperator.evalUnary(operand);
            if(hasNext()) {
                result = next.eval(partialRes);
            }
            else result = partialRes;
        }
        //Operator is binary
        else{
            //if we receive the first operand we append it and return it as a result (won't be used)
           if(this.operands.isEmpty()){
               this.operands.add(operand);
               result = operand;
           }

           //If instead we receive the second operand, we can proceed with the binary R2R computation and keep going along the DAG chain
           else if(this.operands.size() == 1){
               this.operands.add(operand);
               partialRes = this.r2rOperator.evalBinary(this.operands.get(0), this.operands.get(1));
               if(hasNext()) {
                   result = next.eval(partialRes);
               }
               else result = partialRes;
           }

           else{
               throw new RuntimeException("Too many operands to R2R operator");
           }

        }
        return result;

    }

}
