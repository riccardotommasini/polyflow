package org.streamreasoning.polyflow.api.secret.content;

import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;

/**
 * @deprecated Use {@link Segment}. Content is kept as a legacy alias for
 * existing implementations and downstream code.
 */
@Deprecated
public interface Content<I, W, R> extends Segment<I, R> {
}
