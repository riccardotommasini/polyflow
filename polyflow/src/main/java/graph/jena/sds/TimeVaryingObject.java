package graph.jena.sds;

import org.apache.commons.rdf.api.IRI;
import org.streamreasoning.rsp4j.api.operators.s2r.Convertible;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;

public class TimeVaryingObject<W extends Convertible<?>> implements TimeVarying<W> {

    private final StreamToRelationOperator<?, W> op;
    private final IRI name;
    private W content;

    public TimeVaryingObject(StreamToRelationOperator<?, W> op, IRI name) {
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
        content = op.content(ts).coalesce();
    }

    @Override
    public W get() {
        return content;
    }

    @Override
    public String iri() {
        return name.getIRIString();
    }


}
