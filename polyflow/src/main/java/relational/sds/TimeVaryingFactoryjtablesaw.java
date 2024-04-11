package relational.sds;

import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOp;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVaryingFactory;
import relational.datatypes.TableWrapper;

public class TimeVaryingFactoryjtablesaw implements TimeVaryingFactory<TableWrapper> {
    @Override
    public TimeVarying<TableWrapper> create(StreamToRelationOp<?, TableWrapper> s2r, String name) {
        return new TimeVaryingObjectjtablesaw<>(s2r, name);
    }
}
