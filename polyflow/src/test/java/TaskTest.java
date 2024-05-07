import graph.jena.JenaBindingStream;
import graph.jena.JenaStreamGenerator;
import graph.jena.content.ValidatedGraph;
import graph.jena.datatypes.JenaOperandWrapper;
import graph.jena.examples.polyflowExample;
import graph.jena.operatorsimpl.r2r.R2RJenaImpl;
import graph.jena.operatorsimpl.r2s.RelationToStreamOpImpl;
import graph.jena.sds.SDSJena;
import graph.jena.sds.TimeVaryingFactoryJena;
import operatorsimpl.s2r.StreamToRelationOpImpl;
import org.apache.jena.Jena;
import org.apache.jena.graph.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.streamreasoning.rsp4j.api.enums.ReportGrain;
import org.streamreasoning.rsp4j.api.enums.Tick;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.rsp4j.api.querying.Task;
import org.streamreasoning.rsp4j.api.querying.TaskImpl;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVaryingFactory;
import org.streamreasoning.rsp4j.api.secret.report.Report;
import org.streamreasoning.rsp4j.api.secret.report.ReportImpl;
import org.streamreasoning.rsp4j.api.secret.report.strategies.OnWindowClose;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.secret.time.TimeImpl;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;
import relational.content.AccumulatorContentFactory;
import relational.operatorsimpl.r2r.DAGImpl;
import relational.stream.RowStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TaskTest {



    @Test
    public void initializeTask(){

         /*------INITIALIZATION OF COMPONENTS USED BY THE TASK------*/

        JenaStreamGenerator generator = new JenaStreamGenerator();
        DataStream<Graph> inputStreamColors = generator.getStream("http://test/stream1");
        JenaBindingStream outStream = new JenaBindingStream("out");
        Report report = new ReportImpl();
        report.add(new OnWindowClose());
        Tick tick = Tick.TIME_DRIVEN;
        ReportGrain report_grain = ReportGrain.SINGLE;
        Time instance = new TimeImpl(0);
        Graph shapesGraph = RDFDataMgr.loadGraph(polyflowExample.class.getResource("/shapes.ttl").getPath());
        Shapes shapes = Shapes.parse(shapesGraph);
        JenaOperandWrapper emptyContent = new JenaOperandWrapper();
        emptyContent.setContent(new ValidatedGraph(Factory.createDefaultGraph(), Factory.createDefaultGraph()));
        TimeVaryingFactory<JenaOperandWrapper> tvFactory = new TimeVaryingFactoryJena();

        AccumulatorContentFactory<Graph, Graph, JenaOperandWrapper> accumulatorContentFactory = new AccumulatorContentFactory<>(
                (g)->g,
                (g)->{
                    JenaOperandWrapper r = new JenaOperandWrapper();
                    r.setContent(new ValidatedGraph(g, g));
                    return r;
                },
                (r1, r2)->{
                    JenaOperandWrapper result = new JenaOperandWrapper();
                    Model m1 = ModelFactory.createModelForGraph(r1.getContent().content);
                    Model m2 = ModelFactory.createModelForGraph(r2.getContent().content);
                    Graph res_content = m1.union(m2).getGraph();

                    m1 = ModelFactory.createModelForGraph(r1.getContent().report);
                    m2 = ModelFactory.createModelForGraph(r2.getContent().report);
                    Graph res_report = m1.union(m2).getGraph();
                    result.setContent(new ValidatedGraph(res_content, res_report ));
                    return result;
                },
                emptyContent
        );
        StreamToRelationOperator<Graph, Graph, JenaOperandWrapper> s2rOp_one =
                new StreamToRelationOpImpl<>(
                        tick,
                        instance,
                        "w1",
                        accumulatorContentFactory,
                        tvFactory,
                        report_grain,
                        report,
                        1000,
                        1000);




        List<String> s2r_names = new ArrayList<>();
        s2r_names.add(s2rOp_one.getName());
        RelationToRelationOperator<JenaOperandWrapper> r2rOp = new R2RJenaImpl("SELECT * WHERE {GRAPH ?g{?s ?p ?o }}", s2r_names, false, "selection", "empty");
        RelationToStreamOperator<JenaOperandWrapper, Binding> r2sOp = new RelationToStreamOpImpl();
        Task<Graph, Graph, JenaOperandWrapper, Binding> task = new TaskImpl<>();

        task.addS2ROperator(s2rOp_one, inputStreamColors)
                .addR2ROperator(r2rOp)
                .addR2SOperator(r2sOp)
                .addDAG(new DAGImpl<>())
                .addSDS(new SDSJena())
                .addTime(instance);
        task.initialize();
        /*-------------END OF INITIALIZATION----------------*/


        /*---------------Test the addS2ROperator method-------------*/

        //Create a dummy S2R to check if the Task correctly throws an exception when an S2R with the same name is already present
        StreamToRelationOperator<Graph, Graph, JenaOperandWrapper> s2rOp_dummy =
                new StreamToRelationOpImpl<>(
                        tick,
                        instance,
                        "w1",
                        accumulatorContentFactory,
                        tvFactory,
                        report_grain,
                        report,
                        500,
                        500);


        noDuplicateTests(s2rOp_dummy, task);

        /*---------------End of test addS2ROperator method-------------*/

        /*---------------Beginning test elaborateElement method-------------*/
        Graph graph1 = GraphMemFactory.createGraphMem();
        Node p = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        graph1.add(NodeFactory.createURI("http://test/S0"), p, NodeFactory.createURI("http://test/Red"));
        graph1.add(NodeFactory.createURI("http://test/S1"), p, NodeFactory.createURI("http://test/Black"));
        elaborateElementTest(inputStreamColors, graph1, r2rOp, task);

    }

    public void noDuplicateTests(StreamToRelationOperator<Graph, Graph, JenaOperandWrapper> s2rOp_dummy, Task<Graph, Graph, JenaOperandWrapper, Binding> task){

        assertThrows(RuntimeException.class, ()->task.addS2ROperator(s2rOp_dummy, new RowStream<>("foo")));
    }


    public void elaborateElementTest(DataStream<Graph> inputStream, Graph g, RelationToRelationOperator<JenaOperandWrapper> r2r, Task<Graph, Graph, JenaOperandWrapper, Binding> task) {

        JenaOperandWrapper dataset = new JenaOperandWrapper();
        ValidatedGraph vg = new ValidatedGraph(g, g);
        dataset.setContent(vg);
        //dataset will now hold a list of bindings, result of the computation of the R2R
        r2r.evalUnary(dataset);

        //Insert element in window 0-1000
        task.elaborateElement(inputStream, g, 500);
        //Make window 0-1000 close and report result by adding an element at time 1200
        Collection<Collection<Binding>> result = task.elaborateElement(inputStream, Factory.createDefaultGraph(), 1200);
        result.forEach(coll -> assertTrue(dataset.getResult().containsAll(coll)));

    }
}
