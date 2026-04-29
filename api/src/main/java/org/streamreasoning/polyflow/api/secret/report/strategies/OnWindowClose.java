package org.streamreasoning.polyflow.api.secret.report.strategies;

import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.Window;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;

/**
 * Window close (Rwc): reporting is done for t
 * only when the active window closes (i.e., |Scope(t)| = w ).
 **/
public class OnWindowClose implements ReportingStrategy {

    @Override
    public boolean match(Window w, Segment<?, ?> c, long tapp, long tsys) {
        return w.getC() < tapp;
    }

}
