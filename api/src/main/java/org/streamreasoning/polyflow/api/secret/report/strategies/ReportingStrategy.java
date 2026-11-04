package org.streamreasoning.polyflow.api.secret.report.strategies;


import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.Window;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;

/**
 * According to Botan et al.
 * SPEs use different reporting
 * strategies to define their reporting policy.
 **/
public interface ReportingStrategy {
    /**
     * Whether matching reads the supplied segment. Unknown/custom strategies are
     * content-dependent by default; time-only strategies may receive null content.
     */
    default boolean requiresContent() {
        return true;
    }

    boolean match(Window w, Segment<?, ?> c, long tapp, long tsys);

}
