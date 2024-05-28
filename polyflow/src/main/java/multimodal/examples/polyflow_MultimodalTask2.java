package multimodal.examples;

import multimodal.operators.m2m.M2MDummy;
import multimodal.operators.r2r.dag.DAG2Impl;
import multimodal.operators.sds.SDS2Impl;
import org.javatuples.Tuple;
import org.streamreasoning.rsp4j.api.coordinators.ContinuousProgram;
import org.streamreasoning.rsp4j.api.coordinators.ContinuousProgram2;
import org.streamreasoning.rsp4j.api.coordinators.ContinuousProgram2Impl;
import org.streamreasoning.rsp4j.api.enums.ReportGrain;
import org.streamreasoning.rsp4j.api.enums.Tick;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.Consumer;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.Consumer2;
import org.streamreasoning.rsp4j.api.querying.*;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVaryingFactory;
import org.streamreasoning.rsp4j.api.secret.report.Report;
import org.streamreasoning.rsp4j.api.secret.report.ReportImpl;
import org.streamreasoning.rsp4j.api.secret.report.strategies.OnWindowClose;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.secret.time.TimeImpl;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;
import relational.operatorsimpl.r2r.CustomRelationalQuery;
import relational.operatorsimpl.r2r.R2RjtablesawJoin;
import relational.operatorsimpl.r2r.R2RjtablesawProjection;
import relational.operatorsimpl.r2r.R2RjtablesawSelection;
import relational.operatorsimpl.r2s.RelationToStreamjtablesawImpl;
import relational.sds.SDSjtablesaw;
import relational.sds.TimeVaryingFactoryjtablesaw;
import relational.stream.RowStream;
import relational.stream.RowStreamGenerator;
import shared.contentimpl.factories.AccumulatorContentFactory;
import shared.operatorsimpl.r2r.DAG.DAGImpl;
import shared.operatorsimpl.s2r.CSPARQLStreamToRelationOpImpl;
import tech.tablesaw.api.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class polyflow_MultimodalTask2 {

    public static void main(String[] args) {

        RowStreamGenerator generator = new RowStreamGenerator();

        DataStream<Tuple> inputStream_1 = generator.getStream("http://test/stream1");
        DataStream<Tuple> inputStream_2 = generator.getStream("http://test/stream2");
        // define output stream
        DataStream<Tuple> outStream = new RowStream("out");

        // Engine properties
        Report report = new ReportImpl();
        report.add(new OnWindowClose());

        Tick tick = Tick.TIME_DRIVEN;
        ReportGrain report_grain = ReportGrain.SINGLE;
        Time instance1 = new TimeImpl(0);
        Time instance2 = new TimeImpl(0);
        Table emptyContent = Table.create();

        AccumulatorContentFactory<Tuple, Tuple, Table> accumulatorContentFactory = new AccumulatorContentFactory<>(
                t -> t,
                (t) -> {
                    Table r = Table.create();

                    for (int i = 0; i < t.getSize(); i++) {
                        if (t.getValue(i) instanceof Long) {
                            String columnName = "c" + (i + 1);
                            if (!r.containsColumn(columnName)) {
                                LongColumn lc = LongColumn.create(columnName);
                                lc.append((Long) t.getValue(i));
                                r.addColumns(lc);
                            } else {
                                LongColumn lc = (LongColumn) r.column(columnName);
                                lc.append((Long) t.getValue(i));
                            }

                        } else if (t.getValue(i) instanceof Integer) {
                            String columnName = "c" + (i + 1);
                            if (!r.containsColumn(columnName)) {
                                IntColumn lc = IntColumn.create(columnName);
                                lc.append((Integer) t.getValue(i));
                                r.addColumns(lc);
                            } else {
                                IntColumn lc = (IntColumn) r.column(columnName);
                                lc.append((Integer) t.getValue(i));
                            }
                        } else if (t.getValue(i) instanceof Boolean) {
                            String columnName = "c" + (i + 1);
                            if (!r.containsColumn(columnName)) {
                                BooleanColumn lc = BooleanColumn.create(columnName);
                                lc.append((Boolean) t.getValue(i));
                                r.addColumns(lc);
                            } else {
                                BooleanColumn lc = (BooleanColumn) r.column(columnName);
                                lc.append((Boolean) t.getValue(i));
                            }
                        } else if (t.getValue(i) instanceof String) {
                            String columnName = "c" + (i + 1);
                            if (!r.containsColumn(columnName)) {
                                StringColumn lc = StringColumn.create(columnName);
                                lc.append((String) t.getValue(i));
                                r.addColumns(lc);
                            } else {
                                StringColumn lc = (StringColumn) r.column(columnName);
                                lc.append((String) t.getValue(i));
                            }
                        }
                    }
                    return r;
                },
                (r1, r2) -> r1.isEmpty() ? r2 : r1.append(r2),
                emptyContent

        );

        TimeVaryingFactory<Table> tvFactory = new TimeVaryingFactoryjtablesaw<>();

        //ContinuousProgram<Tuple, Tuple, Table, Tuple> cp = new ContinuousProgram<>();

        CSPARQLStreamToRelationOpImpl<Tuple, Tuple, Table> s2rOp_1 =
                new CSPARQLStreamToRelationOpImpl<>(
                        tick,
                        instance1,
                        "w1",
                        accumulatorContentFactory,
                        tvFactory,
                        report_grain,
                        report,
                        1000,
                        1000);
        CSPARQLStreamToRelationOpImpl<Tuple, Tuple, Table> s2rOp_2 =
                new CSPARQLStreamToRelationOpImpl<>(
                        tick,
                        instance2,
                        "w2",
                        accumulatorContentFactory,
                        tvFactory,
                        report_grain,
                        report,
                        1000,
                        1000);


        CustomRelationalQuery selection = new CustomRelationalQuery(4, "c3");
        CustomRelationalQuery join = new CustomRelationalQuery("c1");
        CustomRelationalQuery projection = new CustomRelationalQuery(new int[]{1, 2, 4});

        RelationToRelationOperator<Table> r2rOp = new R2RjtablesawSelection(selection, Collections.singletonList(s2rOp_1.getName()), "partial_1");
        RelationToRelationOperator<Table> r2rBinaryOp = new R2RjtablesawJoin(join, Arrays.asList(s2rOp_2.getName(), "partial_1"), "partial_2");
        RelationToRelationOperator<Table> r2rProj = new R2RjtablesawProjection(projection, Collections.singletonList("partial_2"), "final");

        RelationToStreamOperator<Table, Tuple> r2sOp = new RelationToStreamjtablesawImpl();

        Task<Tuple, Tuple, Table, Tuple> task1 = new LazyTaskImpl<>();
        task1 = task1
                .addS2ROperator(s2rOp_1, inputStream_1)
                .addR2ROperator(r2rOp)
                .addSDS(new SDSjtablesaw())
                .addDAG(new DAGImpl<>())
                .addTime(instance1);
        task1.initialize();

        Task<Tuple, Tuple, Table, Tuple> task2 = new LazyTaskImpl<>();
        task2 = task2
                .addS2ROperator(s2rOp_2, inputStream_2)
                .addSDS(new SDSjtablesaw())
                .addDAG(new DAGImpl<>())
                .addTime(instance2);
        task2.initialize();


        List<DataStream<?>> inputStreams = new ArrayList<>();
        inputStreams.add(inputStream_1);
        inputStreams.add(inputStream_2);
        List<DataStream<Tuple>> outputStreams = new ArrayList<>();
        outputStreams.add(outStream);

        Task2<Tuple, Tuple, Table, Tuple, Tuple, Tuple, Table, Tuple> multimodal = new Task2Impl<>();
        multimodal = multimodal
                .addTaskOne(task1)
                .addTaskTwo(task2)
                .addM2MOperator(new M2MDummy<>())
                .addR2ROperator(r2rBinaryOp)
                .addR2ROperator(r2rProj)
                .addR2SOperatorOne(r2sOp)
                .addSDS(new SDS2Impl<>())
                .addDAG(new DAG2Impl<>());
        multimodal.registerInputOne(inputStream_1, task1);
        multimodal.registerInputTwo(inputStream_2, task2);
        multimodal.initialize();
        ContinuousProgram2<Tuple, Tuple, Table, Tuple, Tuple, Tuple, Table, Tuple> cp = new ContinuousProgram2Impl<>();
        cp.buildInputs(multimodal, inputStreams);
        cp.buildOutputOne(multimodal, outputStreams);
        outStream.addConsumer((Consumer2) (out, el, ts) -> System.out.println(el + " @ " + ts));

        generator.startStreaming();
    }


}
