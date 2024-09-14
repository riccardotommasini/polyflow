package org.streamreasoning.polyflow.api.secret.report.strategies;

import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.Window;
import org.streamreasoning.polyflow.api.secret.content.Content;

public class OnStateReady implements ReportingStrategy{
    @Override
    public boolean match(Window w, Content c, long tapp, long tsys) {
        return c.toReport();
    }
}
