/*
package operators.s2r;

import graph.jena.content.ValidatedContent;
import graph.jena.content.ValidatedGraph;
import graph.jena.content.ValidatedGraphContentFactory;
import graph.jena.sds.TimeVaryingFactoryJena;
import operatorsimpl.s2r.StreamToRelationOpImpl;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.Shapes;
import org.junit.Test;
import org.streamreasoning.rsp4j.api.enums.ReportGrain;
import org.streamreasoning.rsp4j.api.enums.Tick;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVaryingFactory;
import org.streamreasoning.rsp4j.api.secret.report.Report;
import org.streamreasoning.rsp4j.api.secret.report.ReportImpl;
import org.streamreasoning.rsp4j.api.secret.report.strategies.OnWindowClose;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.secret.time.TimeImpl;

import static junit.framework.TestCase.assertTrue;

public class StreamToRelationOpTest {

    @Test
    public void test(){

        //CREATE S2R COMPONENT

        Report report = new ReportImpl();
        report.add(new OnWindowClose());
        Tick tick = Tick.TIME_DRIVEN;
        ReportGrain report_grain = ReportGrain.SINGLE;
        Time instance = new TimeImpl(0);
        Graph shapesGraph = RDFDataMgr.loadGraph(StreamToRelationOpTest.class.getResource("/shapes.ttl").getPath());
        Shapes shapes = Shapes.parse(shapesGraph);

        ValidatedGraphContentFactory validatedGraphContentFactory = new ValidatedGraphContentFactory(instance, shapes);

        TimeVaryingFactory<ValidatedGraph> tvFactory = new TimeVaryingFactoryJena();

        StreamToRelationOperator<Graph, ValidatedGraph> s2rOp =
                new StreamToRelationOpImpl<>(
                        tick,
                        instance,
                        "w1",
                        validatedGraphContentFactory,
                        tvFactory,
                        report_grain,
                        report,
                        1000,
                        1000);

        TimeVarying<ValidatedGraph> tvg = s2rOp.get();

        //CRAFT A GRAPH TO SIMULATE THE INPUT
        Graph graph1 = GraphMemFactory.createGraphMem();
        Node p = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        graph1.add(NodeFactory.createURI("http://test/S0"), p, NodeFactory.createURI("http://test/Red"));
        graph1.add(NodeFactory.createURI("http://test/S1"), p, NodeFactory.createURI("http://test/Black"));

        //ADD THE GRAPH TO THE S2R OPERATOR TO SIMULATE AN EVENT ARRIVING
        s2rOp.windowing(graph1, 2000);

        Graph graph2 = GraphMemFactory.createGraphMem();
        graph2.add(NodeFactory.createURI("http://test/S2"), p, NodeFactory.createURI("http://test/Blue"));
        graph2.add(NodeFactory.createURI("http://test/S3"), p, NodeFactory.createURI("http://test/Black"));

        s2rOp.windowing(graph2, 4000);

        //A WINDOW IS READY TO REPORT, CHECK IF THE TIME OBJECT STORED THE TIME AT WHICH THE COMPUTATION MUST OCCUR
        assertTrue(s2rOp.time().getEvaluationTimeInstants().size() == 1);
        assertTrue(s2rOp.time().getEvaluationTime().t == 4000);

        //CHECK THAT AFTER POLLING AN EVALUATION TIME INSTANT (getEvaluationTime()), THE ELEMENT IS REMOVED FROM THE LIST
        assertTrue(s2rOp.time().getEvaluationTimeInstants().isEmpty());


        //WHEN THE EVENT WITH TS=4000 ARRIVES, THE WINDOW [3000, 4000) DOES NOT CLOSE, BUT THE WINDOW [2000, 3000) CLOSES AND IT'S REPORTED WITH ITS CONTENT (GRAPH 1)

        tvg.materialize(4000);
        ValidatedContent<Graph, ValidatedGraph> content = validatedGraphContentFactory.create();
        content.add(graph1);
        ValidatedGraph expected = content.coalesce();

        expected.getContent().stream().map(Triple.class::cast).forEach(triple ->
                assertTrue(tvg.get().content.contains(triple)));

        Graph graph3 = GraphMemFactory.createGraphMem();
        graph3.add(NodeFactory.createURI("http://test/S2"), p, NodeFactory.createURI("http://test/Green"));
        graph3.add(NodeFactory.createURI("http://test/S3"), p, NodeFactory.createURI("http://test/Black"));

        s2rOp.windowing(graph3, 5000);


        //BY ADDING AN EVENT AT TIME 5000, THE WINDOW [4000, 5000) WHICH CONTAINS GRAPH 2 DOES NOT CLOSE, BUT THE WINDOW [3000, 4000) CLOSES, SHOULD REPORT EMPTY CONTENT

        tvg.materialize(5000);

        assertTrue(tvg.get().content.isEmpty());


        //ADD ANOTHER GRAPH IN THE WINDOW [5000, 6000) TO SEE IF THE APP REPORTS ALL THE CONTENT CORRECTLY
        Graph graph4 = GraphMemFactory.createGraphMem();
        graph4.add(NodeFactory.createURI("http://test/S4"), p, NodeFactory.createURI("http://test/Purple"));
        graph4.add(NodeFactory.createURI("http://test/S5"), p, NodeFactory.createURI("http://test/Black"));

        s2rOp.windowing(graph4, 5500);

        //ADD EVENT AT TIME 7000 SO THAT WINDOW [5000, 6000) IS REPORTED
        s2rOp.windowing(Graph.emptyGraph, 7000);

        content = validatedGraphContentFactory.create();
        content.add(graph3);
        content.add(graph4);
        expected = content.coalesce();

        expected.getContent().stream().map(Triple.class::cast).forEach(triple ->
                assertTrue(tvg.get().content.contains(triple)));

        //CHECK THAT ALL THE TIME INSTANTS ADDED ARE IN THE TIME INSTANTS LIST
        assertTrue(s2rOp.time().getEvaluationTimeInstants().size() == 3);

        //ADD ANOTHER EVENT AT TIME 7000 AND CHECK THAT THE TIME OBJECT ONLY CONTAINS THE INSTANT 7000 ONCE
        s2rOp.windowing(Graph.emptyGraph, 7000);
        assertTrue(s2rOp.time().getEvaluationTimeInstants().size() == 3);
        s2rOp.time().getEvaluationTimeInstants().stream().forEach(t->System.out.println(t.t));

    }
}
*/
