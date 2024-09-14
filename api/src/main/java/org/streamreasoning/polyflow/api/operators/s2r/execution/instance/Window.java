package org.streamreasoning.polyflow.api.operators.s2r.execution.instance;

public interface Window {

    long getC();

    long getO();

     default void setC(long ts){};

}

