package custom.examples;

import custom.customdatatypes.Fruit;
import custom.customdatatypes.FruitBasket;
import custom.customdatatypes.FruitDataStream;
import custom.customoperators.*;
import org.streamreasoning.rsp4j.api.coordinators.ContinuousProgram;
import shared.coordinators.ContinuousProgramImpl;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.rsp4j.api.querying.Task;
import shared.querying.TaskImpl;
import org.streamreasoning.rsp4j.api.secret.content.ContentFactory;
import org.streamreasoning.rsp4j.api.secret.report.Report;
import org.streamreasoning.rsp4j.api.secret.report.ReportImpl;
import org.streamreasoning.rsp4j.api.secret.report.strategies.OnWindowClose;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.secret.time.TimeImpl;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;
import shared.contentimpl.factories.AccumulatorContentFactory;
import shared.contentimpl.factories.FilterContentFactory;
import shared.operatorsimpl.r2r.DAG.DAGImpl;
import shared.sds.SDSDefault;
import custom.stream.FruitStreamGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FruitAdvancedGuide {

    public static void main(String[] args) throws InterruptedException {

        /*------------Input and Output Stream definitions------------*/

        // Define a generator to create input elements
        FruitStreamGenerator generator = new FruitStreamGenerator();

        // Define the two input streams
        DataStream<Fruit> inputStreamFruit_one = generator.getStream("fruit_market_one");
        DataStream<Fruit> inputStreamFruit_two = generator.getStream("fruit_market_two");

        // define an output stream
        DataStream<Fruit> outStream = new FruitDataStream("fruit_consumer");

        /*------------Window Content------------*/

        //Entity that represents a neutral element for our operations on the 'R' data type
        FruitBasket emptyBasket = new FruitBasket();

        // Factory object to manage the window content, more informations on our GitHub guide!
        ContentFactory<Fruit, Fruit, FruitBasket> filterContentFactory = new FilterContentFactory<>(
                (fruit) -> fruit,
                (fruit) -> {
                    FruitBasket fb = new FruitBasket();
                    fb.addFruit(fruit);
                    return fb;
                },
                (basket_1, basket_2) -> {
                    if(basket_1.getSize()>basket_2.getSize()){
                        basket_1.addAll(basket_2);
                        return basket_1;
                    }
                    else{
                        basket_2.addAll(basket_1);
                        return basket_2;
                    }
                },
                emptyBasket,
                (fruit)->fruit.getWeight()>2
        );

        ContentFactory<Fruit, Fruit, FruitBasket> accumulatorContentFactory = new AccumulatorContentFactory<>(
                (fruit) -> fruit,
                (fruit) -> {
                    FruitBasket fb = new FruitBasket();
                    fb.addFruit(fruit);
                    return fb;
                },
                (basket_1, basket_2) -> {
                    if(basket_1.getSize()>basket_2.getSize()){
                        basket_1.addAll(basket_2);
                        return basket_1;
                    }
                    else{
                        basket_2.addAll(basket_1);
                        return basket_2;
                    }
                },
                emptyBasket
        );


        /*------------Window Properties------------*/

        // Window properties (report)
        Report report = new ReportImpl();
        report.add(new OnWindowClose());

        //Time object used to represent the time in our application
        Time instance = new TimeImpl(0);


        /*------------S2R, R2R and R2S Operators------------*/

        //Define the Stream to Relation operators (blueprint of the windows)
        StreamToRelationOperator<Fruit, Fruit, FruitBasket> fruit_s2r_one =
                new CustomTumblingWindow<>(
                        instance,
                        "TumblingWindow_one",
                        accumulatorContentFactory,
                        report,
                        1000);

        StreamToRelationOperator<Fruit, Fruit, FruitBasket> fruit_s2r_two =
                new CustomTumblingWindow<>(
                        instance,
                        "TumblingWindow_two",
                        filterContentFactory,
                        report,
                        1000);

        //Define Relation to Relation operators and chain them together
        RelationToRelationOperator<FruitBasket> r2r_filter_underripe = new FilterFruitByRipeOp("underripe", Collections.singletonList(fruit_s2r_one.getName()), "filtered_fruit");
        RelationToRelationOperator<FruitBasket> r2r_join = new JoinFruitBasketOp(List.of("filtered_fruit", fruit_s2r_two.getName()), "joined_fruit");

        //Relation to Stream operator, take the final fruit basket and send out each fruit
        RelationToStreamOperator<FruitBasket, Fruit> r2sOp = new RelationToStreamFruitOp();


        /*------------Task definition------------*/

        //Define the Tasks, each of which represent a query
        Task<Fruit, Fruit, FruitBasket, Fruit> task = new TaskImpl<>();
        task = task.addS2ROperator(fruit_s2r_one, inputStreamFruit_one)
                .addS2ROperator(fruit_s2r_two, inputStreamFruit_two)
                .addR2ROperator(r2r_filter_underripe)
                .addR2ROperator(r2r_join)
                .addR2SOperator(r2sOp)
                .addDAG(new DAGImpl<>())
                .addSDS(new SDSDefault<>())
                .addTime(instance);
        task.initialize();




        /*------------Continuous Program definition------------*/

        //Define the Continuous Program, which acts as the coordinator of the whole system
        ContinuousProgram<Fruit, Fruit, FruitBasket, Fruit> cp = new ContinuousProgramImpl<>();

        List<DataStream<Fruit>> inputStreams = new ArrayList<>();
        inputStreams.add(inputStreamFruit_one);
        inputStreams.add(inputStreamFruit_two);

        List<DataStream<Fruit>> outputStreams = new ArrayList<>();
        outputStreams.add(outStream);


        cp.buildTask(task, inputStreams, outputStreams);


        /*------------Output Stream consumer------------*/

        outStream.addConsumer((out, el, ts) -> System.out.println("Output Element: ["+el+ "]" + " @ " + ts));

        generator.startStreaming();
        Thread.sleep(20_000);
        generator.stopStreaming();
    }

}
