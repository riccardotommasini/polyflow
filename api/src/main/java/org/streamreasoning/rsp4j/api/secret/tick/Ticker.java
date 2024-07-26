package org.streamreasoning.rsp4j.api.secret.tick;

import org.streamreasoning.rsp4j.api.operators.s2r.execution.instance.Window;

public interface Ticker {
    boolean tick(long t_e);

}
