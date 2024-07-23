package custom.customdatatypes;

import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.Consumer;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.util.ArrayList;
import java.util.List;

public class FruitDataStream implements DataStream<Fruit> {

    List<Consumer<Fruit>> consumerList = new ArrayList<>();
    String name;

    public FruitDataStream(String name){
        this.name = name;
    }

    @Override
    public void addConsumer(Consumer<Fruit> windowAssigner) {
        this.consumerList.add(windowAssigner);
    }

    @Override
    public void put(Fruit fruit, long ts) {
        consumerList.forEach(c->c.notify(this, fruit, ts));
    }

    @Override
    public String getName() {
        return name;
    }
}
