package graph.jena.examples;

import graph.jena.datatypes.JenaGraphOrBindings;
import graph.jena.operatorsimpl.r2r.jena.FullQueryUnaryJena;
import graph.jena.operatorsimpl.r2r.jena.Join;
import graph.jena.operatorsimpl.r2r.jena.JoinRSPQLstarQueryJena;
import graph.jena.operatorsimpl.r2s.RelationToStreamOpImpl;
import graph.jena.sds.SDSJena;
import graph.jena.sds.TimeVaryingFactoryJena;
import graph.jena.stream.JenaBindingStream;
import graph.jena.stream.JenaStreamRDFStarGenerator;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.graph.GraphFactory;
import org.streamreasoning.rsp4j.api.coordinators.ContinuousProgram;
import shared.coordinators.ContinuousProgramImpl;
import org.streamreasoning.rsp4j.api.enums.ReportGrain;
import org.streamreasoning.rsp4j.api.enums.Tick;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.rsp4j.api.querying.Task;
import shared.querying.TaskImpl;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVaryingFactory;
import org.streamreasoning.rsp4j.api.secret.report.Report;
import org.streamreasoning.rsp4j.api.secret.report.ReportImpl;
import org.streamreasoning.rsp4j.api.secret.report.strategies.OnWindowClose;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.secret.time.TimeImpl;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;
import shared.contentimpl.factories.AccumulatorContentFactory;
import shared.operatorsimpl.r2r.DAG.DAGImpl;
import shared.operatorsimpl.s2r.CSPARQLStreamToRelationOpImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RDFstar_multiwindow {
    public static void main(String[] args) throws InterruptedException, IOException {

        JenaStreamRDFStarGenerator generator = new JenaStreamRDFStarGenerator();

        DataStream<Graph> activity = generator.getStream("http://test/activity");
        DataStream<Graph> location = generator.getStream("http://test/location");
        DataStream<Graph> breathing = generator.getStream("http://test/breathing");
        DataStream<Graph> heart = generator.getStream("http://test/heart");
        DataStream<Graph> oxygen = generator.getStream("http://test/oxygen");

        // define output stream
        JenaBindingStream outStream = new JenaBindingStream("out");

        // Engine properties
        Report report = new ReportImpl();
        report.add(new OnWindowClose());

        Tick tick = Tick.TIME_DRIVEN;
        ReportGrain report_grain = ReportGrain.SINGLE;
        Time instance = new TimeImpl(0);

        JenaGraphOrBindings emptyContent = new JenaGraphOrBindings(GraphFactory.createGraphMem());

        AccumulatorContentFactory<Graph, Graph, JenaGraphOrBindings> accumulatorContentFactory = new AccumulatorContentFactory<>(
                (g) -> g,
                (g) -> new JenaGraphOrBindings(g),
                (r1, r2) -> new JenaGraphOrBindings(new Union(r1.getContent(), r2.getContent())),
                emptyContent
        );

        TimeVaryingFactory<JenaGraphOrBindings> tvFactory = new TimeVaryingFactoryJena();

        ContinuousProgram<Graph, Graph, JenaGraphOrBindings, Binding> cp = new ContinuousProgramImpl<>();

        StreamToRelationOperator<Graph, Graph, JenaGraphOrBindings> s2rOp_1 =
                new CSPARQLStreamToRelationOpImpl<>(
                        tick,
                        instance,
                        "w1",
                        accumulatorContentFactory,
                        tvFactory,
                        report_grain,
                        report,
                        1000,
                        1000);
        StreamToRelationOperator<Graph, Graph, JenaGraphOrBindings> s2rOp_2 =
                new CSPARQLStreamToRelationOpImpl<>(
                        tick,
                        instance,
                        "w2",
                        accumulatorContentFactory,
                        tvFactory,
                        report_grain,
                        report,
                        1000,
                        1000);
        StreamToRelationOperator<Graph, Graph, JenaGraphOrBindings> s2rOp_3 =
                new CSPARQLStreamToRelationOpImpl<>(
                        tick,
                        instance,
                        "w3",
                        accumulatorContentFactory,
                        tvFactory,
                        report_grain,
                        report,
                        1000,
                        1000);
        StreamToRelationOperator<Graph, Graph, JenaGraphOrBindings> s2rOp_4 =
                new CSPARQLStreamToRelationOpImpl<>(
                        tick,
                        instance,
                        "w4",
                        accumulatorContentFactory,
                        tvFactory,
                        report_grain,
                        report,
                        1000,
                        1000);
        StreamToRelationOperator<Graph, Graph, JenaGraphOrBindings> s2rOp_5 =
                new CSPARQLStreamToRelationOpImpl<>(
                        tick,
                        instance,
                        "w5",
                        accumulatorContentFactory,
                        tvFactory,
                        report_grain,
                        report,
                        1000,
                        1000);


        String prefix = "BASE <http://base/>\n" +
                "PREFIX ex: <http://www.example.org/ontology#>\n" +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX sosa: <http://www.w3.org/ns/sosa/> ";

        String q1 = prefix+"SELECT ?person ?activity WHERE {GRAPH ?g1 {\n" +
                "            ?o1 a sosa:Observation ;\n" +
                "              sosa:featureOfInterest ?person ;\n" +
                "              sosa:madeObservation <sensor/system>;\n" +
                "              sosa:hasSimpleResult ?activity  .\n" +
                "        }}GROUP BY ?person ?activity";
        String q2 = prefix+"SELECT ?partner ?loc WHERE {GRAPH ?g2 {\n" +
                "            ?o2 a sosa:Observation ;\n" +
                "              sosa:featureOfInterest ?partner ;\n" +
                "              sosa:madeObservation <sensor/location/2>;\n" +
                "              sosa:hasSimpleResult ?loc  .\n" +
                "        }}GROUP BY ?partner ?loc";
        String q3 = prefix+"SELECT ?person (AVG(?hr) AS ?avgHr) WHERE {GRAPH ?g3 {\n" +
                "            ?o3 a sosa:Observation ;\n" +
                "                sosa:madeObservation <sensor/heart_rate/1>;\n" +
                "                sosa:featureOfInterest ?person .\n" +
                "            <<?o3 sosa:hasSimpleResult ?hr>> ex:confidence ?c3 .\n" +
                "            FILTER(?c3 > 0.95)\n" +
                "        }} GROUP BY ?person";
        String q4 = prefix+"SELECT ?person (AVG(?br) AS ?avgBr) WHERE {GRAPH ?g4 {\n" +
                "            ?o4 a sosa:Observation ;\n" +
                "            sosa:madeObservation <sensor/breathing_rate/1>;\n" +
                "                sosa:featureOfInterest ?person .\n" +
                "            <<?o4 sosa:hasSimpleResult ?br>> ex:confidence ?c4 .\n" +
                "            FILTER(?c4 > 0.95)\n" +
                "\n" +
                "        }}GROUP BY ?person";
        String q5 = prefix+"SELECT ?person (AVG(?ox) AS ?avgOx) WHERE {GRAPH ?g5 {\n" +
                "            ?o5 a sosa:Observation ;\n" +
                "                sosa:madeObservation <sensor/oxygen/1>;\n" +
                "                sosa:featureOfInterest ?person .\n" +
                "            <<?o5 sosa:hasSimpleResult ?ox>> ex:confidence ?c5 .\n" +
                "            FILTER(?c5 > 0.95)\n" +
                "\n" +
                "        }}GROUP BY ?person";

        RelationToRelationOperator<JenaGraphOrBindings> r2rOp_1 = new FullQueryUnaryJena(q1, Collections.singletonList(s2rOp_1.getName()), "partial_1");
        RelationToRelationOperator<JenaGraphOrBindings> r2rOp_2 = new FullQueryUnaryJena(q2, Collections.singletonList(s2rOp_2.getName()), "partial_2");
        RelationToRelationOperator<JenaGraphOrBindings> r2rOp_3 = new FullQueryUnaryJena(q3, Collections.singletonList(s2rOp_3.getName()), "partial_3");
        RelationToRelationOperator<JenaGraphOrBindings> r2rOp_4 = new FullQueryUnaryJena(q4, Collections.singletonList(s2rOp_4.getName()), "partial_4");
        RelationToRelationOperator<JenaGraphOrBindings> r2rOp_5 = new FullQueryUnaryJena(q5, Collections.singletonList(s2rOp_5.getName()), "partial_5");
        RelationToRelationOperator<JenaGraphOrBindings> r2rJoin_1 = new Join(List.of("partial_1", "partial_2"), "join_1");
        RelationToRelationOperator<JenaGraphOrBindings> r2rJoin_2 = new Join(List.of("join_1", "partial_3"), "join_2");
        RelationToRelationOperator<JenaGraphOrBindings> r2rJoin_3 = new Join(List.of("join_2", "partial_4"), "join_3");
        RelationToRelationOperator<JenaGraphOrBindings> r2rJoin_4 = new Join(List.of("join_3", "partial_5"), "join_4");
        RelationToRelationOperator<JenaGraphOrBindings> r2rFinal = new JoinRSPQLstarQueryJena(Collections.singletonList("join_4"), "final");



        RelationToStreamOperator<JenaGraphOrBindings, Binding> r2sOp = new RelationToStreamOpImpl();

        Task<Graph, Graph, JenaGraphOrBindings, Binding> task = new TaskImpl<>();
        task = task.addS2ROperator(s2rOp_1, activity)
                .addS2ROperator(s2rOp_2, location)
                .addS2ROperator(s2rOp_3, heart)
                .addS2ROperator(s2rOp_4, breathing)
                .addS2ROperator(s2rOp_5, oxygen)
                .addR2ROperator(r2rOp_1)
                .addR2ROperator(r2rOp_2)
                .addR2ROperator(r2rOp_3)
                .addR2ROperator(r2rOp_4)
                .addR2ROperator(r2rOp_5)
                .addR2ROperator(r2rJoin_1)
                .addR2ROperator(r2rJoin_2)
                .addR2ROperator(r2rJoin_3)
                .addR2ROperator(r2rJoin_4)
                .addR2ROperator(r2rFinal)
                .addR2SOperator(r2sOp)
                .addDAG(new DAGImpl<>())
                .addSDS(new SDSJena())
                .addTime(instance);
        task.initialize();

        List<DataStream<Graph>> inputStreams = new ArrayList<>();
        inputStreams.add(activity);
        inputStreams.add(location);
        inputStreams.add(heart);
        inputStreams.add(breathing);
        inputStreams.add(oxygen);


        List<DataStream<Binding>> outputStreams = new ArrayList<>();
        outputStreams.add(outStream);

        cp.buildTask(task, inputStreams, outputStreams);

        outStream.addConsumer((out, el, ts) -> System.out.println(el + " @ " + ts));

        generator.startStreaming();
        Thread.sleep(20_000);
        //generator.stopStreaming();
    }
}
