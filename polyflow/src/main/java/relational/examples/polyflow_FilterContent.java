package relational.examples;

import operatorsimpl.s2r.StreamToRelationOpImpl;
import org.javatuples.Tuple;
import org.streamreasoning.rsp4j.api.coordinators.ContinuousProgram;
import org.streamreasoning.rsp4j.api.enums.ReportGrain;
import org.streamreasoning.rsp4j.api.enums.Tick;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.r2s.RelationToStreamOperator;
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
import relational.content.FilterContentFactory;
import relational.operatorsimpl.r2r.CustomRelationalQuery;
import relational.operatorsimpl.r2r.DAGImpl;
import relational.operatorsimpl.r2r.R2RjtablesawImpl;
import relational.operatorsimpl.r2s.RelationToStreamjtablesawImpl;
import relational.sds.SDSjtablesaw;
import relational.sds.TimeVaryingFactoryjtablesaw;
import relational.stream.RowStream;
import relational.stream.RowStreamGenerator;
import tech.tablesaw.api.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class polyflow_FilterContent {

        public static void main(String [] args) throws InterruptedException {

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
            Time instance = new TimeImpl(0);


            FilterContentFactory<Tuple, Tuple, Table> filterContentFactory = new FilterContentFactory<>(
                    t->t,
                    (t, r)->{
                        if(r == null){
                            r = Table.create();
                        }
                        if(t == null)
                            return r;

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
                    (t)-> {
                        if(t.getSize() > 2){
                            return (Integer)t.getValue(2)>4;
                        }
                        return true;
                    }
            );

            TimeVaryingFactory<Table> tvFactory = new TimeVaryingFactoryjtablesaw<>();

            //TableWrapper because we need the interface convertible on the W generic type
            ContinuousProgram<Tuple, Tuple, Table, Tuple> cp = new ContinuousProgram<>();

            StreamToRelationOpImpl<Tuple, Tuple, Table> s2rOp_1 =
                    new StreamToRelationOpImpl<>(
                            tick,
                            instance,
                            "w1",
                            filterContentFactory,
                            tvFactory,
                            report_grain,
                            report,
                            1000,
                            1000);
            StreamToRelationOpImpl<Tuple, Tuple, Table> s2rOp_2 =
                    new StreamToRelationOpImpl<>(
                            tick,
                            instance,
                            "w2",
                            filterContentFactory,
                            tvFactory,
                            report_grain,
                            report,
                            1000,
                            1000);

            List<String> s2r_names = new ArrayList<>();
            s2r_names.add(s2rOp_1.getName());
            s2r_names.add(s2rOp_2.getName());

            CustomRelationalQuery join = new CustomRelationalQuery("c1");

            RelationToRelationOperator<Table> r2rBinaryOp = new R2RjtablesawImpl(join, s2r_names, true, "empty", "join");

            RelationToStreamOperator<Table, Tuple> r2sOp = new RelationToStreamjtablesawImpl();

            Task<Tuple, Tuple, Table, Tuple> task = new TaskImpl<>();
            task = task.addS2ROperator(s2rOp_1, inputStream_1)
                    .addS2ROperator(s2rOp_2, inputStream_2)
                    .addR2ROperator(r2rBinaryOp)
                    .addR2SOperator(r2sOp)
                    .addSDS(new SDSjtablesaw())
                    .addDAG(new DAGImpl<>())
                    .addTime(instance);
            task.initialize();

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





