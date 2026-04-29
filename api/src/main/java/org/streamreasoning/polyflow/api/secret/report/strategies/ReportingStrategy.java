package org.streamreasoning.polyflow.api.secret.report.strategies;


import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.Window;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;

/**
 * According to Botan et al.
 * SPEs use different reporting
 * strategies to define their reporting policy.
 **/
public interface ReportingStrategy {
    boolean match(Window w, Segment<?, ?> c, long tapp, long tsys);

}
