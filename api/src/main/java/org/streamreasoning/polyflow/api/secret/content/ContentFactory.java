package org.streamreasoning.polyflow.api.secret.content;

import org.streamreasoning.polyflow.api.operators.s2r.execution.state.SegmentFactory;

/**
 * @deprecated Use {@link SegmentFactory}. ContentFactory is kept as a legacy
 * alias for existing implementations and downstream code.
 */
@Deprecated
public interface ContentFactory<T1, T2, T3> extends SegmentFactory<T1, T3> {
}
