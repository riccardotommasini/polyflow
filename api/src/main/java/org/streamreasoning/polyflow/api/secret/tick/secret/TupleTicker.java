package org.streamreasoning.polyflow.api.secret.tick.secret;

import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.polyflow.api.secret.tick.Ticker;
import org.streamreasoning.polyflow.api.secret.time.Time;

/**
 * The Tick dimension in our model defines the condition which drives an SPE
 * to take action on its input (also referred to as “window state change” or “window re-evaluation” [13]).
 * Like Report, Tick is also part of a system’s inter- nal execution model.
 * While some systems react to individual tuples as they arrive, others collectively
 * react to all or subsets of tuples with the same tapp value.
 * During our analysis, we have identified three main ways that di↵erent systems “tick”:
 * (a) tuple-driven, where each tuple arrival causes a system to react;
 * (b) time-driven, where the progress of tapp causes a system to react;
 * (c) batch-driven, where either a new batch arrival or the progress of tapp causes a system to react.
 **/
public class TupleTicker implements Ticker {

    private final StreamToRelationOperator<?, ?, ?> wa;
    private Time time;

    public TupleTicker(StreamToRelationOperator<?, ?, ?> wa, Time time) {
        this.wa = wa;
        this.time = time;
    }

    @Override
    public boolean tick(long t_e) {
        //each tuple makes the system react
        return true;
    }
}


