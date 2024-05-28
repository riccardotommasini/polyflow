package org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner;

import org.streamreasoning.rsp4j.api.stream.data.DataStream;

public interface Consumer2{
    void notify(DataStream<?> inputStream, Object element, long timestamp);

}
