package org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner;

import org.streamreasoning.rsp4j.api.stream.data.DataStream;

public interface Consumer<I> {

    /**
     * Method called by an input/output stream to notify its consumers that a new element entered the stream.
     * @param inputStream The stream that generated the element
     * @param element The value that entered the stream
     * @param timestamp The event time of the element
     */
    void notify(DataStream<I> inputStream, I element, long timestamp);
}
