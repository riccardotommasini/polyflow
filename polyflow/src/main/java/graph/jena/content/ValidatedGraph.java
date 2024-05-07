package graph.jena.content;

import graph.jena.datatypes.JenaOperandWrapper;
import org.apache.jena.graph.Graph;
import org.streamreasoning.rsp4j.api.operators.s2r.Convertible;

public class ValidatedGraph {

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

}
