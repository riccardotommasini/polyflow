package relational.examples;

import operatorsimpl.s2r.StreamToRelationOpImpl;
import org.javatuples.Quartet;
import org.javatuples.Septet;
import org.streamreasoning.rsp4j.api.containers.R2RContainer;
import org.streamreasoning.rsp4j.api.containers.R2SContainer;
import org.streamreasoning.rsp4j.api.containers.S2RContainer;
import org.streamreasoning.rsp4j.api.coordinators.ContinuousProgram;
import org.streamreasoning.rsp4j.api.enums.ReportGrain;
import org.streamreasoning.rsp4j.api.enums.Tick;
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
import relational.operatorsimpl.r2s.RelationToStreamjtableJoin;
import relational.sds.SDSjtablesaw;
import relational.sds.TimeVaryingFactoryjtablesaw;
import relational.stream.RowStream;
import relational.stream.RowStreamGenerator;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.List;

public class polyflowExample_relational_twoStreams {

    public static void main(String [] args) throws InterruptedException {

        RowStreamGenerator generator = new RowStreamGenerator();

        DataStream<Quartet<Long, String, Integer, Boolean>> inputStream_1 = generator.getStream("http://test/stream1");
        DataStream<Quartet<Long, String, Integer, Boolean>> inputStream_2 = generator.getStream("http://test/stream2");
        // define output stream
        DataStream<Septet<Long, String, Integer, Boolean, Long, String, Boolean>> outStream = new RowStream<>("out");

        // Engine properties
        Report report = new ReportImpl();
        report.add(new OnWindowClose());

        Tick tick = Tick.TIME_DRIVEN;
        ReportGrain report_grain = ReportGrain.SINGLE;
        Time instance = new TimeImpl(0);


        WindowContentFactory windowContentFactory = new WindowContentFactory();

        TimeVaryingFactory<TableWrapper> tvFactory = new TimeVaryingFactoryjtablesaw();

        //TableWrapper because we need the interface convertible on the W generic type
        ContinuousProgram<Quartet<Long, String, Integer, Boolean>, TableWrapper, Table, Septet<Long, String, Integer, Boolean, Long, String, Boolean>> cp = new ContinuousProgram<>();

        StreamToRelationOp<Quartet<Long, String, Integer, Boolean>, TableWrapper> s2rOp_1 =
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

        StreamToRelationOp<Quartet<Long, String, Integer, Boolean>, TableWrapper> s2rOp_2 =
                new StreamToRelationOpImpl<>(
                        tick,
                        instance,
                        "w2",
                        windowContentFactory,
                        tvFactory,
                        report_grain,
                        report,
                        1000,
                        1000);

        S2RContainer<Quartet<Long, String, Integer, Boolean>, TableWrapper> s2rContainer_1 = new S2RContainer<>(inputStream_1.getName(), s2rOp_1, s2rOp_1.getName());
        S2RContainer<Quartet<Long, String, Integer, Boolean>, TableWrapper> s2rContainer_2 = new S2RContainer<>(inputStream_2.getName(), s2rOp_2, s2rOp_2.getName());

        List<String> s2r_names = new ArrayList<>();
        s2r_names.add(s2rOp_1.getName());
        s2r_names.add(s2rOp_2.getName());

        R2RContainer<Table> r2rContainer = new R2RContainer<>(s2r_names, new R2RjtablesawImpl(0), false);
        R2RContainer<Table> r2rBinaryContainer = new R2RContainer<>(s2r_names, new R2RjtablesawImpl(-1), true);

        R2SContainer<Table, Septet<Long, String, Integer, Boolean, Long, String, Boolean>> r2sContainer = new R2SContainer<>(outStream.getName(), new RelationToStreamjtableJoin());

        Task<Quartet<Long, String, Integer, Boolean>, TableWrapper, Table, Septet<Long, String, Integer, Boolean, Long, String, Boolean>> task = new TaskImpl<>();
        task = task.addS2RContainer(s2rContainer_1, inputStream_1)
                .addS2RContainer(s2rContainer_2, inputStream_2)
                .addR2RContainer(r2rContainer)
                .addR2RContainer(r2rBinaryContainer)
                .addR2SContainer(r2sContainer)
                .addSDS(new SDSjtablesaw())
                .addTime(instance);
        task.initialize();

        List<DataStream<Quartet<Long, String, Integer, Boolean>>> inputStreams = new ArrayList<>();
        inputStreams.add(inputStream_1);
        inputStreams.add(inputStream_2);


        List<DataStream<Septet<Long, String, Integer, Boolean, Long, String, Boolean>>> outputStreams = new ArrayList<>();
        outputStreams.add(outStream);

        cp.buildTask(task, inputStreams, outputStreams);

        outStream.addConsumer((out, el, ts)-> System.out.println(el + " @ " + ts));

        generator.startStreaming();
        Thread.sleep(20_000);
        generator.stopStreaming();
    }
}
