package org.streamreasoning.polyflow.base.contentimpl;

import org.streamreasoning.polyflow.api.secret.content.Content;
import org.streamreasoning.polyflow.base.operatorsimpl.s2r.segment.EmptySegment;

public class EmptyContent<I,W,R> extends EmptySegment<I, R> implements Content<I, W, R> {

    public EmptyContent(R o) {
        super(o);
    }
}
