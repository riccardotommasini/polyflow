package org.streamreasoning.polyflow.api.secret.report.strategies;

import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.Window;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;

/**
 * Periodic (Rpr): reporting is done for t only
 * when the reporting frequency has elapsed.
 **/
public class Periodic implements ReportingStrategy {
    private long period;
    private long nextReportTime;

    public Periodic(long period) {
        this.period = period;
        this.nextReportTime = period;
    }

    @Override
    public boolean match(Window w, Segment<?, ?> c, long tapp, long tsys) {
        if (period <= 0) {
            throw new IllegalStateException("Periodic report period must be greater than zero");
        }

        if (tapp < nextReportTime) {
            return false;
        }

        nextReportTime = ((tapp / period) + 1) * period;
        return true;
    }
}
