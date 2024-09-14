package org.streamreasoning.polyflow.api.stream.data;

import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.Consumer;

/**
 * Created by riccardo on 10/07/2017.
 */

//TODO wrap schema for RDFUtils stream?
public interface DataStream<E> {

    void addConsumer(Consumer<E> windowAssigner);

    void put(E e, long ts);

    String getName();

}
