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
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
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
import java.util.List;

public class TaskTest {


    Task<Graph, Graph, JenaOperandWrapper, Binding> task = new TaskImpl<>();

    public void initializeTask(Task<Graph, Graph, JenaOperandWrapper, Binding> task){
         /*------INITIALIZATION OF COMPONENTS USED BY THE TASK------*/


        JenaStreamGenerator generator = new JenaStreamGenerator();
        DataStream<Graph> inputStreamColors = generator.getStream("http://test/stream1");
        DataStream<Graph> inputStreamNumbers = generator.getStream("http://test/stream2");
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

        StreamToRelationOperator<Graph, Graph, JenaOperandWrapper> s2rOp_two =
                new StreamToRelationOpImpl<>(
                        tick,
                        instance,
                        "w2",
                        accumulatorContentFactory,
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


        /*-----END OF INITIALIZATION-----*/
        task.addS2ROperator(s2rOp_one, inputStreamColors)
                .addS2ROperator(s2rOp_two, inputStreamNumbers)
                .addR2ROperator(r2rOp)
                .addR2ROperator(r2rBinaryOp)
                .addR2SOperator(r2sOp)
                .addDAG(new DAGImpl<>())
                .addSDS(new SDSJena())
                .addTime(instance);




    }

    @Test
    public void noDuplicateTests(){

        Report report = new ReportImpl();
        report.add(new OnWindowClose());
        Tick tick = Tick.TIME_DRIVEN;
        ReportGrain report_grain = ReportGrain.SINGLE;
        Time instance = new TimeImpl(0);
        initializeTask(this.task);
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

        //Create a dummy S2R to check if the Task correctly throws an exception when an S2R with the same name is already present
        StreamToRelationOperator<Graph, Graph, JenaOperandWrapper> s2rOp_dummy =
                new StreamToRelationOpImpl<>(
                        tick,
                        instance,
                        "w2",
                        accumulatorContentFactory,
                        tvFactory,
                        report_grain,
                        report,
                        500,
                        500);



        assertThrows(RuntimeException.class, ()->task.addS2ROperator(s2rOp_dummy, new RowStream<>("foo")));
    }
}
