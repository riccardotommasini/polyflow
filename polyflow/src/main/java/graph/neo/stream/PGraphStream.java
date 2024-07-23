package graph.neo.stream;

import graph.neo.stream.data.PGraph;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.Consumer;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.util.ArrayList;
import java.util.List;

public class PGraphStream implements DataStream<PGraph> {

    protected String stream_uri;
    protected List<Consumer<PGraph>> consumers = new ArrayList<>();

    public PGraphStream(String stream_uri) {
        this.stream_uri = stream_uri;
    }

    @Override
    public void addConsumer(Consumer<PGraph> c) {
        consumers.add(c);
    }

    @Override
    public void put(PGraph e, long ts) {
        consumers.forEach(graphConsumer -> graphConsumer.notify(this, e, ts));
    }

    @Override
    public String getName() {
        return stream_uri;
    }

    public String uri() {
        return stream_uri;
    }

}
