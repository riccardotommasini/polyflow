package relational.examples;

import graph.jena.JenaBindingStream;
import graph.jena.JenaStreamGenerator;
import graph.jena.SDSJena;
import graph.jena.content.ValidatedGraph;
import graph.jena.content.ValidatedGraphContentFactory;
import graph.jena.datatypes.JenaOperandWrapper;
import graph.jena.examples.polyflowExample;
import graph.jena.operatorsimpl.r2r.R2RJenaImpl;
import operatorsimpl.r2s.RelationToStreamOpImpl;
import operatorsimpl.s2r.StreamToRelationOpImpl;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.sparql.engine.binding.Binding;
import org.javatuples.Quartet;
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
import relational.datatypes.TableWrapper;
import relational.stream.RowStream;
import relational.stream.RowStreamGenerator;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.List;

public class polyflowExample_relational {


    public static void main(String [] args) throws InterruptedException {

        RowStreamGenerator generator = new RowStreamGenerator();

        DataStream<Quartet<Long, String, Integer, Boolean>> inputStream = generator.getStream("http://test/stream1");
        // define output stream
        DataStream<Quartet<Long, String, Integer, Boolean>> outStream = new RowStream("out");

        // Engine properties
        Report report = new ReportImpl();
        report.add(new OnWindowClose());

        Tick tick = Tick.TIME_DRIVEN;
        ReportGrain report_grain = ReportGrain.SINGLE;
        Time instance = new TimeImpl(0);


        TableContentFactory tableContentFactory = new TableContentFactory(instance);

        //TableWrapper because we need the interface convertible on the W generic type
        ContinuousProgram<Quartet<Long, String, Integer, Boolean>, TableWrapper, Table, Quartet<Long, String, Integer, Boolean>> cp = new ContinuousProgram<>();

        StreamToRelationOp<Quartet<Long, String, Integer, Boolean>, TableWrapper> s2rOp =
                new StreamToRelationOpImpl<>(
                        tick,
                        instance,
                        "w1",
                        tableContentFactory,
                        report_grain,
                        report,
                        1000,
                        1000);

        S2RContainer<Quartet<Long, String, Integer, Boolean>, TableWrapper> s2rContainer = new S2RContainer<>(inputStream.getName(), s2rOp, s2rOp.getName());
        R2RContainer<Table> r2rContainer = new R2RContainer<>(s2rOp.getName(), new R2RjtablesawImpl("write_query_here?"), false);
        R2SContainer<Table, Quartet<Long, String, Integer, Boolean>> r2sContainer = new R2SContainer<>(outStream.getName(), new RelationToStreamjtablesawImpl());

        Task<Quartet<Long, String, Integer, Boolean>, TableWrapper, Table, Quartet<Long, String, Integer, Boolean>> task = new TaskImpl<>();
        task = task.addS2RContainer(s2rContainer, inputStream)
                .addR2RContainer(r2rContainer)
                .addR2SContainer(r2sContainer)
                .addSDS(new SDSjtablesaw())
                .addTime(instance);
        task.initialize();

        List<DataStream<Quartet<Long, String, Integer, Boolean>>> inputStreams = new ArrayList<>();
        inputStreams.add(inputStream);


        List<DataStream<Quartet<Long, String, Integer, Boolean>>> outputStreams = new ArrayList<>();
        outputStreams.add(outStream);

        cp.buildTask(task, inputStreams, outputStreams);

        outStream.addConsumer((out, el, ts)-> System.out.println(el + " @ " + ts));

        generator.startStreaming();
        Thread.sleep(20_000);
        generator.stopStreaming();
    }


}
