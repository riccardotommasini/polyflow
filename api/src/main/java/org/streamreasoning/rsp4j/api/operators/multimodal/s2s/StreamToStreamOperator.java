package org.streamreasoning.rsp4j.api.operators.multimodal.s2s;

public interface StreamToStreamOperator<I1, I2> {

    I2 eval(I1 stream);
}
