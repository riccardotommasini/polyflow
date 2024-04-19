package relational.examples;

import operatorsimpl.s2r.StreamToRelationOpImpl;
import org.javatuples.Tuple;
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
import relational.operatorsimpl.r2r.CustomRelationalQuery;
import relational.operatorsimpl.r2r.DAGImpl;
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

public class polyflowExample_relational_twoStreams {

    public static void main(String [] args) throws InterruptedException {

        RowStreamGenerator generator = new RowStreamGenerator();

        DataStream<Tuple> inputStream_1 = generator.getStream("http://test/stream1");
        DataStream<Tuple> inputStream_2 = generator.getStream("http://test/stream2");
        // define output stream
        DataStream<Tuple> outStream = new RowStream<>("out");

        // Engine properties
        Report report = new ReportImpl();
        report.add(new OnWindowClose());

        Tick tick = Tick.TIME_DRIVEN;
        ReportGrain report_grain = ReportGrain.SINGLE;
        Time instance = new TimeImpl(0);


        WindowContentFactory windowContentFactory = new WindowContentFactory();

        TimeVaryingFactory<TableWrapper> tvFactory = new TimeVaryingFactoryjtablesaw();

        //TableWrapper because we need the interface convertible on the W generic type
        ContinuousProgram<Tuple, TableWrapper, Table, Tuple> cp = new ContinuousProgram<>();

        StreamToRelationOp<Tuple, TableWrapper> s2rOp_1 =
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

        StreamToRelationOp<Tuple, TableWrapper> s2rOp_2 =
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


        List<String> s2r_names = new ArrayList<>();
        s2r_names.add(s2rOp_1.getName());
        s2r_names.add(s2rOp_2.getName());

        CustomRelationalQuery selection = new CustomRelationalQuery(4, "c3");
        CustomRelationalQuery join = new CustomRelationalQuery("c1");

        RelationToRelationOperator<Table> r2rOp = new R2RjtablesawImpl(selection, Collections.singletonList(s2rOp_1.getName()), false, "selection", "empty");
        RelationToRelationOperator<Table> r2rBinaryOp = new R2RjtablesawImpl(join, s2r_names, true, "empty", "join");

        RelationToStreamOperator<Table, Tuple> r2sOp = new RelationToStreamjtablesawImpl();

        Task<Tuple, TableWrapper, Table, Tuple> task = new TaskImpl<>();
        task = task.addS2ROperator(s2rOp_1, inputStream_1)
                .addS2ROperator(s2rOp_2, inputStream_2)
                .addR2ROperator(r2rOp)
                .addR2ROperator(r2rBinaryOp)
                .addR2SOperator(r2sOp)
                .addDAG(new DAGImpl<>())
                .addSDS(new SDSjtablesaw())
                .addTime(instance);
        task.initialize();
        task.getDAG().printDAG();
        List<DataStream<Tuple>> inputStreams = new ArrayList<>();
        inputStreams.add(inputStream_1);
        inputStreams.add(inputStream_2);


        List<DataStream<Tuple>> outputStreams = new ArrayList<>();
        outputStreams.add(outStream);

        cp.buildTask(task, inputStreams, outputStreams);

        outStream.addConsumer((out, el, ts)-> System.out.println(el + " @ " + ts));

        generator.startStreaming();
        //Thread.sleep(20_000);
        //generator.stopStreaming();

    }
}
