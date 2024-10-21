package org.streamreasoning.polyflow.base.stream;

import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.Consumer;
import org.streamreasoning.polyflow.api.stream.data.DataStream;

import java.util.ArrayList;
import java.util.List;

public class defaultDataStream<I> implements DataStream<I> {

    List<Consumer<I>> consumerList = new ArrayList<>();
    String name;

    public defaultDataStream(String name){
        this.name = name;
    }
    @Override
    public void addConsumer(Consumer<I> windowAssigner) {
        this.consumerList.add(windowAssigner);
    }

    @Override
    public void put(I i, long ts) {
        consumerList.forEach(c->c.notify(this, i, ts));
    }

    @Override
    public String getName() {
        return name;
    }
}
