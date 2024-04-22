package relational.sds;

import org.streamreasoning.rsp4j.api.operators.s2r.Convertible;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVaryingFactory;

public class TimeVaryingFactoryjtablesaw<W extends Convertible<?>> implements TimeVaryingFactory<W> {
    @Override
    public TimeVarying<W> create(StreamToRelationOperator<?, W> s2r, String name) {
        return new TimeVaryingObjectjtablesaw<>(s2r, name);
    }
}
