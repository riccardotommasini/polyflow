package relational.stream;

import org.apache.jena.graph.Graph;
import org.javatuples.Quartet;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.Consumer;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;
import tech.tablesaw.api.Row;

import java.util.ArrayList;
import java.util.List;

public class RowStream implements DataStream<Quartet<Long, String, Integer, Boolean>> {

    String URI;
    protected List<Consumer<Quartet<Long, String, Integer, Boolean>>> consumers = new ArrayList<>();

    public RowStream(String streamURI){
        this.URI = streamURI;
    }
    @Override
    public void addConsumer(Consumer<Quartet<Long, String, Integer, Boolean>> windowAssigner) {
        this.consumers.add(windowAssigner);
    }

    @Override
    public void put(Quartet<Long, String, Integer, Boolean> row, long ts) {
        consumers.forEach(graphConsumer -> graphConsumer.notify(this, row, ts));
    }

    @Override
    public String getName() {
        return URI;
    }
}
