package jena.content;

import datatypes.JenaOperandWrapper;
import org.apache.jena.graph.Graph;
import org.streamreasoning.rsp4j.api.operators.s2r.Convertible;

public class ValidatedGraph implements Convertible<JenaOperandWrapper> {

    public Graph report;
    public Graph content;
    public ValidatedGraph(Graph report, Graph content){
        this.report = report;
        this.content = content;
    }

    public Graph getReport(){
        return this.report;
    }

    public Graph getContent(){
        return this.content;
    }

    @Override
    public void compute() {

    }

    @Override
    public JenaOperandWrapper convertToR() {
        JenaOperandWrapper wrapper = new JenaOperandWrapper();
        wrapper.setContent(this);
        return wrapper;
    }
}
