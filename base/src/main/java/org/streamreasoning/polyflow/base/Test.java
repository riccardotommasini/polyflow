package org.streamreasoning.polyflow.base;

import org.streamreasoning.polyflow.api.enums.Tick;
import org.streamreasoning.polyflow.api.processing.Task;
import org.streamreasoning.polyflow.api.sds.timevarying.TimeVarying;
import org.streamreasoning.polyflow.api.secret.content.ContentFactory;
import org.streamreasoning.polyflow.api.secret.report.Report;
import org.streamreasoning.polyflow.api.secret.report.ReportImpl;
import org.streamreasoning.polyflow.api.secret.report.strategies.OnWindowClose;
import org.streamreasoning.polyflow.api.secret.time.Time;
import org.streamreasoning.polyflow.api.secret.time.TimeImpl;
import org.streamreasoning.polyflow.api.stream.data.DataStream;
import org.streamreasoning.polyflow.base.contentimpl.factories.BasicContentFactory;
import org.streamreasoning.polyflow.base.operatorsimpl.dag.DAGImpl;
import org.streamreasoning.polyflow.base.operatorsimpl.s2r.HoppingWindowOpImpl;
import org.streamreasoning.polyflow.base.processing.ContinuousProgramImpl;
import org.streamreasoning.polyflow.base.processing.TaskImpl;
import org.streamreasoning.polyflow.base.sds.SDSDefault;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Test {

    public static void main(String[] args) {

        ContinuousProgramImpl<Phrase, List<Word>, List<Integer>, Integer> cp = new ContinuousProgramImpl<>();

        DataStream<Phrase> input = new BasicDataStream<>("input");
        DataStream<Integer> output = new BasicDataStream<>("output");

        Report report = new ReportImpl();
        report.add(new OnWindowClose());

        Tick tick = Tick.TIME_DRIVEN;
        Time instance = new TimeImpl(0);

        BiFunction<Phrase, List<Word>, List<Word>> f1 = (p, c) -> c;

        Function<List<Word>, List<Integer>> f2 = lw -> {
            List<Integer> li = new ArrayList<>();
            lw.forEach(w -> li.add(1));
            return li;
        };

        Function<List<Word>, Integer> f3 = l -> l.size();

        List<Word> empty = new ArrayList<>();

        ContentFactory<Phrase, List<Word>, List<Integer>> cf = new BasicContentFactory<>(f1, f2, f3, empty);

        HoppingWindowOpImpl<Phrase, List<Word>> s2r = new HoppingWindowOpImpl<>(tick, instance, "win", cf, report, 1000, 1000);

        Task<Phrase, List<Word>, List<Integer>, Integer> task = new TaskImpl<>();

//        List<String> name = List.of(s2r.getName()); //this should be the name of the tvg not the operator
        List<String> tvgs = new ArrayList<>(); //this should be the name of the tvg not the operator

        DummyR2ROp<List<Integer>> dummyr2r = new DummyR2ROp<>(tvgs, "dummyr2r");

        task
                .addS2ROperator(s2r, input)
                .addR2ROperator(dummyr2r)
                .addR2SOperator(new RelationToStreamOpImpl<>())
                .addSDS(new SDSDefault<>()).addDAG(new DAGImpl<>()).addTime(instance);
        task.initialize();

        cp.buildTask(task, List.of(input), List.of(output));

        //observing that, naming based reference should be kept for the time-varying objects,
        //operators should be linked instead for the sake for the dag construction
        task.getTVs(input).stream().map(TimeVarying::iri).forEach(tvgs::add);


        //this the tv that results from applying the window to the stream, expressed in W
        TimeVarying<List<Word>> tvg1 = task.getTVs(input).get(0);

        //this is the equivalent of the W to R transformation that happens in the content f2
        TimeVarying<List<Integer>> compose = task.getTVs(input).get(0).compose(f2);

        //Time Varying Graphs are not naturally composable. We need to use transformers


    }

    private static Word split(Object phrase) {
        return null;
    }

    public class Phrase {

    }

    public class Word implements Iterable<String> {

        @Override
        public Iterator<String> iterator() {
            return null;
        }

        @Override
        public Spliterator<String> spliterator() {
            return Iterable.super.spliterator();
        }

    }
}
