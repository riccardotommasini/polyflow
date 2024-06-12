/*
package graph.jena.examples;

import graph.jena.datatypes.JenaGraphOrBindings;
import graph.jena.operatorsimpl.r2r.jena.RSPQLstarQueryJena;
import graph.jena.operatorsimpl.r2s.RelationToStreamOpImpl;
import graph.jena.sds.SDSJena;
import graph.jena.sds.TimeVaryingFactoryJena;
import graph.jena.stream.JenaBindingStream;
import graph.jena.stream.JenaStreamGenerator;
import graph.jena.stream.JenaStreamRDFStarGenerator;
import org.antlr.v4.runtime.misc.Utils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.graph.GraphFactory;
import org.streamreasoning.rsp4j.api.coordinators.ContinuousProgram;
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

        ContinuousProgram<Graph, Graph, JenaGraphOrBindings, Binding> cp = new ContinuousProgram<>();

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


        final String qString = new String(Utils.readFile(polyflowExample_RDFstar.class.getResource("/query.txt").getPath()));
        RelationToRelationOperator<JenaGraphOrBindings> r2rOp_1 = new RSPQLstarQueryJena(qString, Collections.singletonList(s2rOp_1.getName()), "partial_1");
        RelationToRelationOperator<JenaGraphOrBindings> r2rOp_2 = new RSPQLstarQueryJena(qString, Collections.singletonList(s2rOp_2.getName()), "partial_2");
        RelationToRelationOperator<JenaGraphOrBindings> r2rOp_3 = new RSPQLstarQueryJena(qString, Collections.singletonList(s2rOp_3.getName()), "partial_3");
        RelationToRelationOperator<JenaGraphOrBindings> r2rOp_4 = new RSPQLstarQueryJena(qString, Collections.singletonList(s2rOp_4.getName()), "partial_4");
        RelationToRelationOperator<JenaGraphOrBindings> r2rOp_5 = new RSPQLstarQueryJena(qString, Collections.singletonList(s2rOp_5.getName()), "partial_5");





        RelationToStreamOperator<JenaGraphOrBindings, Binding> r2sOp = new RelationToStreamOpImpl();

        Task<Graph, Graph, JenaGraphOrBindings, Binding> task = new TaskImpl<>();
        task = task.addS2ROperator(s2rOp_1, activity)
                .addS2ROperator(s2rOp_2, location)
                .addS2ROperator(s2rOp_3, heart)
                .addS2ROperator(s2rOp_4, breathing)
                .addS2ROperator(s2rOp_5, oxygen)

                .addR2ROperator(r2rOp1)
                .addR2SOperator(r2sOp)
                .addDAG(new DAGImpl<>())
                .addSDS(new SDSJena())
                .addTime(instance);
        task.initialize();

        List<DataStream<Graph>> inputStreams = new ArrayList<>();
        inputStreams.add(inputStream);


        List<DataStream<Binding>> outputStreams = new ArrayList<>();
        outputStreams.add(outStream);

        cp.buildTask(task, inputStreams, outputStreams);

        outStream.addConsumer((out, el, ts) -> System.out.println(el + " @ " + ts));

        generator.startStreaming();
        Thread.sleep(20_000);
        //generator.stopStreaming();
    }
}
*/
