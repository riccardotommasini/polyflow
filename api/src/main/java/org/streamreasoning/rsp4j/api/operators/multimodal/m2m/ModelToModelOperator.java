package org.streamreasoning.rsp4j.api.operators.multimodal.m2m;

import java.util.List;

public interface ModelToModelOperator<R1, R2> {


    R2 eval(R1 dataset);


    List<String> getTvgNames();



}
