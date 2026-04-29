package org.streamreasoning.polyflow.api.secret.report.strategies;


import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.Window;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;

/**
 * Content change (Rcc): reporting is done for t only
 * if the content has changed since t - 1.
 **/
public class OnContentChange implements ReportingStrategy {

    private Segment<?, ?> last_content;

    @Override
    public boolean match(Window w, Segment<?, ?> c, long tapp, long tsys) {
        if (last_content == null) {
            last_content = c;
            return true;
        }

        return !last_content.equals(c);
    }
}
