package document.examples;

import document.datatypes.DocumentCollection;
import document.operatorsimpl.r2r.RelationToRelationDocumentSelection;
import document.operatorsimpl.r2s.RelationToStreamDocument;
import document.stream.DocumentStream;
import document.stream.DocumentStreamGenerator;

import org.streamreasoning.rsp4j.api.coordinators.ContinuousProgram;
import shared.coordinators.ContinuousProgramImpl;
import org.streamreasoning.rsp4j.api.enums.ReportGrain;
import org.streamreasoning.rsp4j.api.enums.Tick;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.rsp4j.api.querying.Task;
import shared.querying.TaskImpl;
import org.streamreasoning.rsp4j.api.secret.report.Report;
import org.streamreasoning.rsp4j.api.secret.report.ReportImpl;
import org.streamreasoning.rsp4j.api.secret.report.strategies.OnWindowClose;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.secret.time.TimeImpl;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import shared.contentimpl.factories.AccumulatorContentFactory;
import shared.operatorsimpl.r2r.DAG.DAGImpl;
import shared.operatorsimpl.s2r.CSPARQLStreamToRelationOpImpl;
import shared.sds.SDSDefault;

import java.util.ArrayList;

import java.util.List;

public class document_example {
    public static void main(String [] args) throws InterruptedException {

        DocumentStreamGenerator generator = new DocumentStreamGenerator();

        DataStream<String> inputStream_1 = generator.getStream("http://test/stream1");
        // define output stream
        DataStream<String> outStream = new DocumentStream<>("out");

        // Engine properties
        Report report = new ReportImpl();
        report.add(new OnWindowClose());

        Tick tick = Tick.TIME_DRIVEN;
        ReportGrain report_grain = ReportGrain.SINGLE;
        Time instance = new TimeImpl(0);
        DocumentCollection emptyContent = new DocumentCollection();

        AccumulatorContentFactory<String, String, DocumentCollection> accumulatorContentFactory = new AccumulatorContentFactory<>(
                t->t,
                (t)->{
                    DocumentCollection dc = new DocumentCollection();
                    dc.addElement(t);
                    return dc;
                },
                (d1, d2)->d1.append(d2),
                emptyContent
        );


        ContinuousProgram<String, String, DocumentCollection, String> cp = new ContinuousProgramImpl<>();

        StreamToRelationOperator<String, String, DocumentCollection> s2rOp_1 =
                new CSPARQLStreamToRelationOpImpl<>(
                        tick,
                        instance,
                        "w1",
                        accumulatorContentFactory,
                        report_grain,
                        report,
                        1000,
                        1000);



        RelationToRelationOperator<DocumentCollection> r2rOp = new RelationToRelationDocumentSelection(List.of("w1"), "partial_1");
        RelationToStreamOperator<DocumentCollection, String> r2sOp = new RelationToStreamDocument();

        Task<String, String, DocumentCollection, String> task = new TaskImpl<>();
        task = task.addS2ROperator(s2rOp_1, inputStream_1)
                .addR2ROperator(r2rOp)
                .addR2SOperator(r2sOp)
                .addSDS(new SDSDefault<>())
                .addDAG(new DAGImpl<>())
                .addTime(instance);
        task.initialize();

        List<DataStream<String>> inputStreams = new ArrayList<>();
        inputStreams.add(inputStream_1);


        List<DataStream<String>> outputStreams = new ArrayList<>();
        outputStreams.add(outStream);

        cp.buildTask(task, inputStreams, outputStreams);

        outStream.addConsumer((out, el, ts)-> System.out.println(el + " @ " + ts));

        generator.startStreaming();
    }

}
