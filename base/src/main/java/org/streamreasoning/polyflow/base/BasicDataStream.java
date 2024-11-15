package org.streamreasoning.polyflow.base;

import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.Consumer;
import org.streamreasoning.polyflow.api.stream.data.DataStream;

import java.util.ArrayList;
import java.util.List;

public class BasicDataStream<T> implements DataStream<T> {

    List<Consumer<T>> consumerList = new ArrayList<>();
    String name;

    public BasicDataStream(String name) {
        this.name = name;
    }

    @Override
    public void addConsumer(Consumer<T> windowAssigner) {
        this.consumerList.add(windowAssigner);
    }

    @Override
    public void put(T fruit, long ts) {
        consumerList.forEach(c -> c.notify(this, fruit, ts));
    }

    @Override
    public String getName() {
        return name;
    }
}
