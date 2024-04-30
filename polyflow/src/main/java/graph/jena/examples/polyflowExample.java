package graph.jena.examples;

import graph.jena.datatypes.JenaOperandWrapper;
import graph.jena.JenaBindingStream;
import graph.jena.JenaStreamGenerator;
import graph.jena.sds.SDSJena;
import graph.jena.content.ValidatedGraph;
import graph.jena.operatorsimpl.r2r.R2RJenaImpl;
import graph.jena.sds.TimeVaryingFactoryJena;
import graph.jena.operatorsimpl.r2s.RelationToStreamOpImpl;
import operatorsimpl.s2r.StreamToRelationOpImpl;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
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
import org.apache.jena.shacl.Shapes;
import relational.content.AccumulatorContentFactory;
import relational.operatorsimpl.r2r.DAGImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class polyflowExample {

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

        AccumulatorContentFactory<Graph, Graph, JenaOperandWrapper> accumulatorContentFactory = new AccumulatorContentFactory<>(
                (g)->g,
                (g, r)->{
                    if(g == null){
                        r = new JenaOperandWrapper();
                        r.setContent(new ValidatedGraph(Factory.createDefaultGraph(), Factory.createDefaultGraph()));
                        return r;
                    }
                    if(r ==  null){
                        r = new JenaOperandWrapper();
                        r.setContent(new ValidatedGraph(g, g));
                        return r;
                    }
                    else{
                        Model m1 = ModelFactory.createModelForGraph(r.getContent().content);
                        Model m2 = ModelFactory.createModelForGraph(g);
                        Graph res = m1.union(m2).getGraph();
                        r.getContent().content = res;

                        m1 = ModelFactory.createModelForGraph(r.getContent().report);
                        res = m1.union(m2).getGraph();
                        r.getContent().report = res;
                        return r;
                    }
                }
        );

        TimeVaryingFactory<JenaOperandWrapper> tvFactory = new TimeVaryingFactoryJena();

        ContinuousProgram<Graph, Graph, JenaOperandWrapper, Binding> cp = new ContinuousProgram<>();

        StreamToRelationOperator<Graph, Graph, JenaOperandWrapper> s2rOp =
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

        RelationToRelationOperator<JenaOperandWrapper> r2rOp = new R2RJenaImpl("SELECT * WHERE {GRAPH ?g{?s ?p ?o }}", Collections.singletonList(s2rOp.getName()), false, "selection", "empty");
        RelationToStreamOperator<JenaOperandWrapper, Binding> r2sOp = new RelationToStreamOpImpl();

        Task<Graph, Graph, JenaOperandWrapper, Binding> task = new TaskImpl<>();
        task = task.addS2ROperator(s2rOp, inputStream)
                        .addR2ROperator(r2rOp)
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
