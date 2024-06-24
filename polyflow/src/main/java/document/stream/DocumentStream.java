package document.stream;

import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.Consumer;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.util.ArrayList;
import java.util.List;

public class DocumentStream<X> implements DataStream<X> {

    private List<Consumer<X>> consumerList = new ArrayList<>();
    String URI;

    public DocumentStream(String URI){
        this.URI = URI;
    }
    @Override
    public void addConsumer(Consumer<X> windowAssigner) {
        this.consumerList.add(windowAssigner);
    }

    @Override
    public void put(X x, long ts) {
        consumerList.forEach(graphConsumer -> graphConsumer.notify(this, x, ts));
    }

    @Override
    public String getName() {
        return this.URI;
    }
}
