package org.streamreasoning.rsp4j.api.secret.report.strategies;

import org.streamreasoning.rsp4j.api.operators.s2r.execution.instance.Window;
import org.streamreasoning.rsp4j.api.secret.content.Content;

public class OnStateReady implements ReportingStrategy{
    @Override
    public boolean match(Window w, Content c, long tapp, long tsys) {
        return c.toReport();
    }
}
