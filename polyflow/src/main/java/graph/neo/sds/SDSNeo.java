package graph.neo.sds;

import graph.neo.stream.data.PGraph;
import graph.neo.stream.data.PGraphOrTable;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;

import java.util.*;
import java.util.stream.Stream;

public class SDSNeo implements SDS<PGraphOrTable> {

    private final Set<TimeVarying<PGraphOrTable>> defs = new HashSet<>();
    private final Map<Node, TimeVarying<PGraphOrTable>> tvgs = new HashMap<>();

    @Override
    public Collection<TimeVarying<PGraphOrTable>> asTimeVaryingEs() {
        return tvgs.values();
    }


    @Override
    public void add(String iri, TimeVarying<PGraphOrTable> tvg) {
        tvgs.put(NodeFactory.createURI(iri), tvg);
    }

    @Override
    public void add(TimeVarying<PGraphOrTable> tvg) {
        defs.add(tvg);
    }


    @Override
    public SDS<PGraphOrTable> materialize(final long ts) {
        defs.forEach(g -> g.materialize(ts));

        tvgs.entrySet().forEach(e -> e.getValue().materialize(ts));

        return this;
    }

    @Override
    public Stream<PGraphOrTable> toStream() {
        return null;
    }


    class NamedGraph {
        public Node name;
        public PGraph g;

        public NamedGraph(Node name, PGraph g) {
            this.name = name;
            this.g = g;
        }
    }
}

