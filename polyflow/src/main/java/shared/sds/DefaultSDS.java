package shared.sds;

import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class DefaultSDS<R> implements SDS<R> {
    private final List<TimeVarying<R>> tvgs = new ArrayList<>();

    @Override
    public Collection<TimeVarying<R>> asTimeVaryingEs() {
        return tvgs;
    }

    @Override
    public void add(String iri, TimeVarying<R> tvg) {

    }

    @Override
    public void add(TimeVarying<R> tvg) {
        tvgs.add(tvg);
    }

    @Override
    public void materialized() {

    }

    @Override
    public Stream<R> toStream() {
        return null;
    }
}
