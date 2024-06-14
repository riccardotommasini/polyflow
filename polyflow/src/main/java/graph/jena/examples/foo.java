package graph.jena.examples;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.core.ResultBinding;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;

import java.util.ArrayList;
import java.util.List;

public class foo {

    public static void main(String [] args){
        Query q1 = QueryFactory.create("SELECT * WHERE {{?s ?p ?o }}");
        Query q2 = QueryFactory.create("SELECT * WHERE {{?a ?p ?c }}");
        Op op1 = Algebra.compile(q1);
        Op op2 = Algebra.compile(q2);
        Op op3 = OpJoin.create(op1, op2);
        Graph graph = GraphMemFactory.createGraphMem();
        graph.add(NodeFactory.createURI("s"), NodeFactory.createURI("p"), NodeFactory.createURI("o"));
        Graph graph2 = GraphMemFactory.createGraphMem();
        graph2.add(NodeFactory.createURI("a"), NodeFactory.createURI("p"), NodeFactory.createURI("c"));

        /*graph.add(NodeFactory.createURI("a"), NodeFactory.createURI("b"), NodeFactory.createURI("c"));
        QueryIterator iter = Algebra.exec(op3, graph);
        while(iter.hasNext()){
            System.out.println("a");
            System.out.println(iter.next());
        }*/
        QueryIterator i1 = Algebra.exec(op1, graph);
        //QueryIterator i2 = Algebra.exec(op2, graph2);
        while(i1.hasNext()){

            QuerySolution qs = new ResultBinding(ModelFactory.createDefaultModel(),i1.next());
            QueryExecution.model(ModelFactory.createDefaultModel()).query(q1).initialBinding(i1.next());
            QueryExecution qe = QueryExecutionFactory.create(q1, ModelFactory.createDefaultModel(), qs);

            ResultSet foo = qe.execSelect();
            while(foo.hasNext()){
                ResultBinding rb = (ResultBinding)foo.next();
                System.out.println(rb.getBinding());
            }
        }

    }
}
