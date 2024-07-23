package relational.examples;

import org.javatuples.Tuple;
import org.streamreasoning.rsp4j.api.coordinators.ContinuousProgram;
import shared.coordinators.ContinuousProgramImpl;
import org.streamreasoning.rsp4j.api.enums.ReportGrain;
import org.streamreasoning.rsp4j.api.enums.Tick;
import org.streamreasoning.rsp4j.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.rsp4j.api.querying.Task;
import shared.querying.TaskImpl;
import org.streamreasoning.rsp4j.api.secret.report.Report;
import org.streamreasoning.rsp4j.api.secret.report.ReportImpl;
import org.streamreasoning.rsp4j.api.secret.report.strategies.OnStateReady;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.secret.time.TimeImpl;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;
import relational.operatorsimpl.r2s.RelationToStreamjtablesawImpl;
import relational.sds.SDSjtablesaw;
import relational.stream.RowStream;
import relational.stream.RowStreamGenerator;
import shared.contentimpl.factories.StatefulContentFactory;
import shared.operatorsimpl.r2r.DAG.DAGImpl;
import shared.operatorsimpl.s2r.FrameOp;
import tech.tablesaw.api.*;

import java.util.ArrayList;
import java.util.List;

public class polyflow_ThresholdFrame {
    public static void main(String [] args) throws InterruptedException {

        RowStreamGenerator generator = new RowStreamGenerator();

        DataStream<Tuple> inputStream_1 = generator.getStream("http://test/stream1");
        // define output stream
        DataStream<Tuple> outStream = new RowStream("out");

        // Engine properties
        Report report = new ReportImpl();
        report.add(new OnStateReady());

        Tick tick = Tick.TIME_DRIVEN;
        ReportGrain report_grain = ReportGrain.SINGLE;
        Time instance = new TimeImpl(0);
        Table emptyContent = Table.create();

        StatefulContentFactory<Tuple, Tuple, Table> statefulContentFactory = new StatefulContentFactory<>(
                ()-> null,
                (t, s)-> t.getValue(2),
                (s)-> (Integer)s<3,
                t->t,
                (t)->{
                    Table r = Table.create();

                    for(int i = 0; i<t.getSize(); i++){
                        if(t.getValue(i) instanceof Long){
                            String columnName = "c"+ (i+1);
                            if(!r.containsColumn(columnName)) {
                                LongColumn lc = LongColumn.create(columnName);
                                lc.append((Long) t.getValue(i));
                                r.addColumns(lc);
                            }
                            else{
                                LongColumn lc = (LongColumn) r.column(columnName);
                                lc.append((Long) t.getValue(i));
                            }

                        }
                        else if(t.getValue(i) instanceof Integer){
                            String columnName = "c"+ (i+1);
                            if(!r.containsColumn(columnName)) {
                                IntColumn lc = IntColumn.create(columnName);
                                lc.append((Integer) t.getValue(i));
                                r.addColumns(lc);
                            }
                            else{
                                IntColumn lc = (IntColumn) r.column(columnName);
                                lc.append((Integer) t.getValue(i));
                            }
                        }
                        else if(t.getValue(i) instanceof Boolean){
                            String columnName = "c"+ (i+1);
                            if(!r.containsColumn(columnName)) {
                                BooleanColumn lc = BooleanColumn.create(columnName);
                                lc.append((Boolean) t.getValue(i));
                                r.addColumns(lc);
                            }
                            else{
                                BooleanColumn lc = (BooleanColumn) r.column(columnName);
                                lc.append((Boolean) t.getValue(i));
                            }
                        }
                        else if(t.getValue(i) instanceof String){
                            String columnName = "c"+ (i+1);
                            if(!r.containsColumn(columnName)) {
                                StringColumn lc = StringColumn.create(columnName);
                                lc.append((String) t.getValue(i));
                                r.addColumns(lc);
                            }
                            else{
                                StringColumn lc = (StringColumn) r.column(columnName);
                                lc.append((String) t.getValue(i));
                            }
                        }
                    }
                    return r;
                },
                (r1, r2)->r1.isEmpty()? r2:r1.append(r2),
                emptyContent

        );


        ContinuousProgram<Tuple, Tuple, Table, Tuple> cp = new ContinuousProgramImpl<>();

        StreamToRelationOperator<Tuple, Tuple, Table> s2rOp_1 =
                new FrameOp<>(
                        tick,
                        instance,
                        "w1",
                        statefulContentFactory,
                        report_grain,
                        report);




        RelationToStreamOperator<Table, Tuple> r2sOp = new RelationToStreamjtablesawImpl();

        Task<Tuple, Tuple, Table, Tuple> task = new TaskImpl<>();
        task = task.addS2ROperator(s2rOp_1, inputStream_1)
                .addR2SOperator(r2sOp)
                .addSDS(new SDSjtablesaw())
                .addDAG(new DAGImpl<>())
                .addTime(instance);
        task.initialize();

        List<DataStream<Tuple>> inputStreams = new ArrayList<>();
        inputStreams.add(inputStream_1);


        List<DataStream<Tuple>> outputStreams = new ArrayList<>();
        outputStreams.add(outStream);

        cp.buildTask(task, inputStreams, outputStreams);

        outStream.addConsumer((out, el, ts)-> System.out.println(el + " @ " + ts));

        generator.startStreaming();

    }
}
