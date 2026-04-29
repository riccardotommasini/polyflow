package org.streamreasoning.polyflow.api.secret.report.strategies;

import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.Window;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;

public class OnStateReady implements ReportingStrategy{
    @Override
    public boolean match(Window w, Segment<?, ?> c, long tapp, long tsys) {
        return c.toReport();
    }
}
