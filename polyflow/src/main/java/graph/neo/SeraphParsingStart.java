package graph.neo;

import graph.neo.opeators.FullQueryUnaryCypher;
import graph.neo.opeators.RelationToStreamOpImpl2;
import graph.neo.sds.SDSNeo;
import graph.neo.seraph.QueryFactory;
import graph.neo.seraph.SeraphQuery;
import graph.neo.seraph.WindowNode;
import graph.neo.stream.PGStreamGenerator;
import graph.neo.stream.data.PGraph;
import graph.neo.stream.data.PGraphOrTable;
import org.streamreasoning.rsp4j.api.coordinators.ContinuousProgram;
import org.streamreasoning.rsp4j.api.enums.ReportGrain;
import org.streamreasoning.rsp4j.api.enums.Tick;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.rsp4j.api.querying.Task;
import org.streamreasoning.rsp4j.api.secret.report.Report;
import org.streamreasoning.rsp4j.api.secret.report.ReportImpl;
import org.streamreasoning.rsp4j.api.secret.report.strategies.OnWindowClose;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.secret.time.TimeImpl;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;
import relational.stream.RowStream;
import shared.contentimpl.factories.AccumulatorContentFactory;
import shared.coordinators.ContinuousProgramImpl;
import shared.operatorsimpl.r2r.DAG.DAGImpl;
import shared.operatorsimpl.s2r.CSPARQLStreamToRelationOpImpl;
import shared.querying.TaskImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SeraphParsingStart {

    public static void main(String[] args) throws InterruptedException, IOException {

        /*------------Input and Output Stream definitions------------*/

        // Define a generator to create input graphs
        PGStreamGenerator generator = new PGStreamGenerator();
        // Define input stream objects from the generator
//        DataStream<PGraph> inputStreamColors = generator.getStream("http://test/stream1");
        // define an output stream

        DataStream<Map<String, Object>> outStream = new RowStream("out");


        /*------------Window Properties------------*/

        // Window properties (report, tick)
        Report report = new ReportImpl();
        report.add(new OnWindowClose());
        Tick tick = Tick.TIME_DRIVEN;
        ReportGrain report_grain = ReportGrain.SINGLE;

        //Time object used to represent the time in our application
        Time instance = new TimeImpl(0);


        /*------------Window Content------------*/

        //Entity that represents the window content. In particular, we create an instance that represents an empty content
        PGraphOrTable emptyContent = new PGraphOrTable();

        /*
        Factory used to create the window content. We provide 4 parameters:
        - Function to transform a type I to a type W
        - Function to transform a type W to a type R
        - Function to merge two types R together
        - Object representing the empty content
        The parameter 'I' is the type of the input data (Graph in this case).
        The parameter 'W' is the type of data that we store inside the window (might differ from 'I'), in this case is still Graph.
        The parameter 'R' is the type of data on which we perform our query operations (select, filter, join etc..). We used a custom data type JenaGraphOrBindings

        The logic behind the content can be customized by defining your own factory and content classes, this particular instance
        of content just accumulates what enters in the window.
         */

        AccumulatorContentFactory<PGraph, PGraph, PGraphOrTable> accumulatorContentFactory = new AccumulatorContentFactory<>(
                (g) -> g,
                (g) -> new PGraphOrTable(g),
                (g1, g2) -> new PGraphOrTable(g1.getContent(), g2.getContent()),
                emptyContent
        );

        /*------------S2R, R2R and R2S Operators------------*/


        SeraphQuery studentTrick = QueryFactory.parse(
                "" +
                "REGISTER QUERY student_trick STARTING AT NOW {\n" +
                "MATCH (b:Bike)-[r:rentedAt]->(s:Station)" +
                "WITHIN PT10S\n" +
                "EMIT b.bike_id, s.station_id, r.val_time\n" +
                "ON ENTERING EVERY PT5S }" +
                "");

        final Task<PGraph, PGraph, PGraphOrTable, Map<String, Object>> task = new TaskImpl<>();

        studentTrick.getWindowMap().forEach((w, s) -> {
            //Define the Stream to Relation operators (blueprint of the windows), each with its own size and sliding parameters.

            generator.addStream(s);

            StreamToRelationOperator<PGraph, PGraph, PGraphOrTable> s2rOp_one =
                    new CSPARQLStreamToRelationOpImpl<>(
                            tick,
                            instance,
                            w.iri(),
                            accumulatorContentFactory,
                            report_grain,
                            report,
                            w.getRange(),
                            w.getStep());

            task.addS2ROperator(s2rOp_one, s);

        });


        List<String> windows = studentTrick.getWindowMap().keySet().stream().map(WindowNode::iri).collect(Collectors.toList());

        //Define Relation to Relation operators and chain them together. Here we select all the graphs from the input streams and perform a union
        RelationToRelationOperator<PGraphOrTable> r2rOp1 = new FullQueryUnaryCypher(studentTrick.getR2R(), windows, "partial_1");

        //Relation to Stream operator, used to transform the result of a query (type R) to a stream of output objects (type O)
        RelationToStreamOpImpl2 r2sOp = new RelationToStreamOpImpl2();


        /*------------Task definition------------*/

        //Define the Tasks, each of which represent a query
        task
                .addR2ROperator(r2rOp1)
                .addR2SOperator(r2sOp)
                .addDAG(new DAGImpl<>())
                .addSDS(new SDSNeo())
                .addTime(instance);

        task.initialize();

        List<DataStream<PGraph>> inputStreams = new ArrayList<>();
        inputStreams.addAll(studentTrick.getWindowMap().values());

        List<DataStream<Map<String, Object>>> outputStreams = new ArrayList<>();
        outputStreams.add(outStream);

        /*------------Continuous Program definition------------*/

        //Define the Continuous Program, which acts as the coordinator of the whole system
        ContinuousProgram<PGraph, PGraph, PGraphOrTable, Map<String, Object>> cp = new ContinuousProgramImpl<>();
        cp.buildTask(task, inputStreams, outputStreams);


        /*------------Output Stream consumer------------*/

        outStream.addConsumer((out, el, ts) -> System.out.println(el + " @ " + ts));

        generator.startStreaming();
        Thread.sleep(20_000);
        generator.stopStreaming();
    }
}
