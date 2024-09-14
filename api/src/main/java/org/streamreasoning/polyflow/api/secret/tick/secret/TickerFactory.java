package org.streamreasoning.polyflow.api.secret.tick.secret;

import org.streamreasoning.polyflow.api.enums.Tick;
import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.polyflow.api.secret.tick.Ticker;

public class TickerFactory {

    public static Ticker tick(Tick t, StreamToRelationOperator<?, ?, ?> wa) {
        switch (t) {
            case TUPLE_DRIVEN:
                return new TupleTicker(wa, wa.time());
            case BATCH_DRIVEN:
                return new BatchTicker(wa, wa.time());
            case TIME_DRIVEN:
            default:
                return new TimeTicker(wa, wa.time());
        }
    }

}
