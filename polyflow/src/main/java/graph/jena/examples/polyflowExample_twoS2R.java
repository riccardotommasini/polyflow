package graph.jena.examples;

import graph.jena.datatypes.JenaOperandWrapper;
import graph.jena.JenaBindingStream;
import graph.jena.JenaStreamGenerator;
import graph.jena.SDSJena;
import graph.jena.content.ValidatedGraph;
import graph.jena.content.ValidatedGraphContentFactory;
import graph.jena.operatorsimpl.r2r.R2RJenaImpl;
import operatorsimpl.r2s.RelationToStreamOpImpl;
import operatorsimpl.s2r.StreamToRelationOpImpl;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.sparql.engine.binding.Binding;
import org.streamreasoning.rsp4j.api.RDFUtils;
import org.streamreasoning.rsp4j.api.containers.R2RContainer;
import org.streamreasoning.rsp4j.api.containers.R2SContainer;
import org.streamreasoning.rsp4j.api.containers.S2RContainer;
import org.streamreasoning.rsp4j.api.coordinators.ContinuousProgram;
import org.streamreasoning.rsp4j.api.enums.ReportGrain;
import org.streamreasoning.rsp4j.api.enums.Tick;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOp;
import org.streamreasoning.rsp4j.api.querying.Task;
import org.streamreasoning.rsp4j.api.querying.TaskImpl;
import org.streamreasoning.rsp4j.api.secret.report.Report;
import org.streamreasoning.rsp4j.api.secret.report.ReportImpl;
import org.streamreasoning.rsp4j.api.secret.report.strategies.OnWindowClose;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.secret.time.TimeImpl;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

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

        ContinuousProgram<Graph, ValidatedGraph, JenaOperandWrapper, Binding> cp = new ContinuousProgram<>();

        StreamToRelationOp<Graph, ValidatedGraph> s2rOp_one =
                new StreamToRelationOpImpl<>(
                        tick,
                        instance,
                        "w1",
                        validatedGraphContentFactory,
                        report_grain,
                        report,
                        1000,
                        1000);

        StreamToRelationOp<Graph, ValidatedGraph> s2rOp_two =
                new StreamToRelationOpImpl<>(
                        tick,
                        instance,
                        "w2",
                        validatedGraphContentFactory,
                        report_grain,
                        report,
                        500,
                        500);

        S2RContainer<Graph, ValidatedGraph> s2rContainer_one = new S2RContainer<>(inputStream.getName(), s2rOp_one, s2rOp_one.getName());
        S2RContainer<Graph, ValidatedGraph> s2rContainer_two = new S2RContainer<>(inputStream.getName(), s2rOp_two, s2rOp_two.getName());

        List<String> s2r_names = new ArrayList<>();
        s2r_names.add(s2rOp_one.getName());
        s2r_names.add(s2rOp_two.getName());

        R2RContainer<JenaOperandWrapper> r2rContainer = new R2RContainer<>(s2r_names, new R2RJenaImpl("SELECT * WHERE {GRAPH ?g{?s ?p ?o }}"), false);
        R2RContainer<JenaOperandWrapper> r2rBinaryContainer = new R2RContainer<>("", new R2RJenaImpl(""), true);

        R2SContainer<JenaOperandWrapper, Binding> r2sContainer = new R2SContainer<>(outStream.getName(), new RelationToStreamOpImpl());

        Task<Graph, ValidatedGraph, JenaOperandWrapper, Binding> task = new TaskImpl<>();
        task = task.addS2RContainer(s2rContainer_one, inputStream)
                .addS2RContainer(s2rContainer_two, inputStream)
                .addR2RContainer(r2rContainer)
                .addR2RContainer(r2rBinaryContainer)
                .addR2SContainer(r2sContainer)
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
