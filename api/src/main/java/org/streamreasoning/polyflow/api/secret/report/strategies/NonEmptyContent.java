package org.streamreasoning.polyflow.api.secret.report.strategies;


import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.Window;
import org.streamreasoning.polyflow.api.secret.content.Content;

/**
 * Non-empty content (Rne): reporting is done
 * for t only if the content at t is not empty.
 **/
public class NonEmptyContent implements ReportingStrategy {

    @Override
    public boolean match(Window w, Content c, long tapp, long tsys) {
        return c.size() > 0;
    }

}
