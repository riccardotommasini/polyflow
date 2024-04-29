/*
package graph.jena.examples;

import graph.jena.datatypes.JenaOperandWrapper;
import graph.jena.JenaBindingStream;
import graph.jena.JenaStreamGenerator;
import graph.jena.sds.SDSJena;
import graph.jena.content.ValidatedGraph;
import graph.jena.content.ValidatedGraphContentFactory;
import graph.jena.operatorsimpl.r2r.R2RJenaImpl;
import graph.jena.sds.TimeVaryingFactoryJena;
import operatorsimpl.r2s.RelationToStreamOpImpl;
import operatorsimpl.s2r.StreamToRelationOpImpl;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.sparql.engine.binding.Binding;
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
import relational.operatorsimpl.r2r.DAGImpl;

import java.util.ArrayList;
import java.util.List;

public class polyflowExample_twoS2R {


    public static void main(String [] args) throws InterruptedException {

        JenaStreamGenerator generator = new JenaStreamGenerator();

        DataStream<Graph> inputStream = generator.getStream("http://test/stream1");
        // define output stream
        JenaBindingStream outStream = new JenaBindingStream("out");

        // Engine properties
        Report report = new ReportImpl();
        report.add(new OnWindowClose());

        Tick tick = Tick.TIME_DRIVEN;
        ReportGrain report_grain = ReportGrain.SINGLE;
        Time instance = new TimeImpl(0);

        Graph shapesGraph = RDFDataMgr.loadGraph(polyflowExample.class.getResource("/shapes.ttl").getPath());
        Shapes shapes = Shapes.parse(shapesGraph);

        ValidatedGraphContentFactory validatedGraphContentFactory = new ValidatedGraphContentFactory(instance, shapes);

        TimeVaryingFactory<ValidatedGraph> tvFactory = new TimeVaryingFactoryJena();

        ContinuousProgram<Graph, ValidatedGraph, JenaOperandWrapper, Binding> cp = new ContinuousProgram<>();

        StreamToRelationOperator<Graph, ValidatedGraph> s2rOp_one =
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

        StreamToRelationOperator<Graph, ValidatedGraph> s2rOp_two =
                new StreamToRelationOpImpl<>(
                        tick,
                        instance,
                        "w2",
                        validatedGraphContentFactory,
                        tvFactory,
                        report_grain,
                        report,
                        500,
                        500);


        List<String> s2r_names = new ArrayList<>();
        s2r_names.add(s2rOp_one.getName());
        s2r_names.add(s2rOp_two.getName());

        RelationToRelationOperator<JenaOperandWrapper> r2rOp = new R2RJenaImpl("SELECT * WHERE {GRAPH ?g{?s ?p ?o }}", s2r_names, false, "selection", "empty");
        RelationToRelationOperator<JenaOperandWrapper> r2rBinaryOp = new R2RJenaImpl("", s2r_names, true, "empty", "concatenation");


        RelationToStreamOperator<JenaOperandWrapper, Binding> r2sOp = new RelationToStreamOpImpl();

        Task<Graph, ValidatedGraph, JenaOperandWrapper, Binding> task = new TaskImpl<>();
        task = task.addS2ROperator(s2rOp_one, inputStream)
                .addS2ROperator(s2rOp_two, inputStream)
                .addR2ROperator(r2rOp)
                .addR2ROperator(r2rBinaryOp)
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

        outStream.addConsumer((out, el, ts)-> System.out.println(el + " @ " + ts));

        generator.startStreaming();
        Thread.sleep(20_000);
        generator.stopStreaming();
    }

}
*/
