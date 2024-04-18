package relational.sds;

import graph.jena.content.ValidatedGraph;
import org.apache.commons.rdf.api.IRI;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;
import relational.datatypes.TableWrapper;

import java.util.*;
import java.util.stream.Stream;

public class SDSjtablesaw implements SDS<TableWrapper> {

    private final List<TimeVarying<TableWrapper>> tvgs = new ArrayList<>();
    private boolean materialized = false;

    @Override
    public Collection<TimeVarying<TableWrapper>> asTimeVaryingEs() {
        return tvgs;
    }

    @Override
    public void add(String iri, TimeVarying<TableWrapper> tvg) {

    }

    @Override
    public void add(TimeVarying<TableWrapper> tvg) {
        tvgs.add(tvg);
    }

    @Override
    public SDS<TableWrapper> materialize(long ts) {

        for(TimeVarying<TableWrapper> tvg : tvgs){
            tvg.materialize(ts);
        }
        materialized();
        return this;
    }

    @Override
    public void materialized() {
        this.materialized = true;
    }

    @Override
    public Stream<TableWrapper> toStream() {
        return null;
    }
}
