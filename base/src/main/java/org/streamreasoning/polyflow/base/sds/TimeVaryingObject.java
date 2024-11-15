package org.streamreasoning.polyflow.base.sds;

import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.polyflow.api.sds.timevarying.TimeVarying;
import org.streamreasoning.polyflow.api.secret.content.Content;

public class TimeVaryingObject<I, W, R> implements TimeVarying<R> {

    private final StreamToRelationOperator<I, W> op;
    private final String name;
    private Content<I, W, R> content;

    public TimeVaryingObject(StreamToRelationOperator<I, W> op, String name) {
        this.op = op;
        this.name = name;
    }

    /**
     * The setTimestamp function merges the element
     * in the content into a single graph
     * and adds it to the current dataset.
     **/
    @Override
    public void materialize(long ts) {
        content = op.content(ts);
    }

    @Override
    public R get() {
        return content.coalesce();
    }

    @Override
    public String iri() {
        return name;
    }


}
