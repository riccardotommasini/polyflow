package org.streamreasoning.polyflow.api.secret.report;


import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.Window;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;
import org.streamreasoning.polyflow.api.secret.report.strategies.ReportingStrategy;

import java.util.function.Supplier;

/**
 * The Report dimension in our model defines the conditions under
 * which the window contents become visible for further query evaluation
 * and result reporting.
 * SPEs use different reporting strategies.
 * Botan et al. identified four basic reporting strategies.
 * 1. Content change (Rcc): reporting is done for t only if the content has changed since t   1.
 * 2. Window close (Rwc): reporting is done for t only when the active window closes (i.e., |Scope(t)| = !).
 * 3. Non-empty content (Rne): reporting is done for t only if the content at t is not empty.
 * 4. Periodic (Rpr): reporting is done for t only if it is a multiple of x , where x denotes the reporting frequency.
 **/
public interface Report {

    boolean report(Window w, Segment<?, ?> c, long tapp, long tsys);

    /**
     * Defer segment construction when the report implementation supports it.
     * Existing custom reports retain their eager behavior through this fallback.
     */
    default boolean reportLazy(Window w, Supplier<? extends Segment<?, ?>> content, long tapp, long tsys) {
        return report(w, content.get(), tapp, tsys);
    }

    void add(ReportingStrategy r);

    ReportingStrategy[] strategies();
}
