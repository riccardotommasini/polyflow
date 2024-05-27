package multimodal.operators.sds;

import org.streamreasoning.rsp4j.api.sds.SDS2;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SDS2Impl<R1, R2> implements SDS2<R1, R2> {

    List<TimeVarying<R1>> tvgOne = new ArrayList<>();
    List<TimeVarying<R2>> tvgTwo = new ArrayList<>();
    boolean materialized = false;
    @Override
    public Collection<TimeVarying<R1>> asTimeVaryingEsOne() {
        return tvgOne;
    }

    @Override
    public Collection<TimeVarying<R2>> asTimeVaryingEsTwo() {
        return tvgTwo;
    }

    @Override
    public void addToOne(TimeVarying<R1> tvg) {
        this.tvgOne.add(tvg);
    }

    @Override
    public void addToTwo(TimeVarying<R2> tvg) {
        this.tvgTwo.add(tvg);
    }

    @Override
    public void materialized() {

    }
}
