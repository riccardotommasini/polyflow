package relational.examples;

import operatorsimpl.s2r.StreamToRelationOpImpl;
import org.javatuples.Quartet;
import org.streamreasoning.rsp4j.api.coordinators.ContinuousProgram;
import org.streamreasoning.rsp4j.api.enums.ReportGrain;
import org.streamreasoning.rsp4j.api.enums.Tick;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOp;
import org.streamreasoning.rsp4j.api.querying.Task;
import org.streamreasoning.rsp4j.api.querying.TaskImpl;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVaryingFactory;
import org.streamreasoning.rsp4j.api.secret.report.Report;
import org.streamreasoning.rsp4j.api.secret.report.ReportImpl;
import org.streamreasoning.rsp4j.api.secret.report.strategies.OnWindowClose;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.secret.time.TimeImpl;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;
import relational.content.WindowContentFactory;
import relational.datatypes.TableWrapper;
import relational.operatorsimpl.r2r.R2RjtablesawImpl;
import relational.operatorsimpl.r2s.RelationToStreamjtablesawImpl;
import relational.sds.SDSjtablesaw;
import relational.sds.TimeVaryingFactoryjtablesaw;
import relational.stream.RowStream;
import relational.stream.RowStreamGenerator;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.Collections;
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


        WindowContentFactory windowContentFactory = new WindowContentFactory();

        TimeVaryingFactory<TableWrapper> tvFactory = new TimeVaryingFactoryjtablesaw();

        //TableWrapper because we need the interface convertible on the W generic type
        ContinuousProgram<Quartet<Long, String, Integer, Boolean>, TableWrapper, Table, Quartet<Long, String, Integer, Boolean>> cp = new ContinuousProgram<>();

        StreamToRelationOp<Quartet<Long, String, Integer, Boolean>, TableWrapper> s2rOp =
                new StreamToRelationOpImpl<>(
                        tick,
                        instance,
                        "w1",
                        windowContentFactory,
                        tvFactory,
                        report_grain,
                        report,
                        1000,
                        1000);


        RelationToRelationOperator<Table> r2rOp = new R2RjtablesawImpl(5, Collections.singletonList(s2rOp.getName()), false);
        RelationToStreamOperator<Table, Quartet<Long, String, Integer, Boolean>> r2sOp = new RelationToStreamjtablesawImpl();

        Task<Quartet<Long, String, Integer, Boolean>, TableWrapper, Table, Quartet<Long, String, Integer, Boolean>> task = new TaskImpl<>();
        task = task.addS2ROperator(s2rOp, inputStream)
                .addR2ROperator(r2rOp)
                .addR2SOperator(r2sOp)
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
