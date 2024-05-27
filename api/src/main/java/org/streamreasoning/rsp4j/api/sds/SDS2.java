package org.streamreasoning.rsp4j.api.sds;

import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;

import java.util.Collection;
import java.util.stream.Stream;

public interface SDS2<R1, R2> {

    Collection<TimeVarying<R1>> asTimeVaryingEsOne();
    Collection<TimeVarying<R2>> asTimeVaryingEsTwo();

    void addToOne(TimeVarying<R1> tvg);
    void addToTwo(TimeVarying<R2> tvg);

    default SDS2<R1, R2> materialize(long ts) {
        asTimeVaryingEsOne().forEach(eTimeVarying -> eTimeVarying.materialize(ts));
        asTimeVaryingEsTwo().forEach(eTimeVarying -> eTimeVarying.materialize(ts));
        materialized();
        return this;
    }

    void materialized();

}
