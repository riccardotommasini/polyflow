package graph.neo.stream.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PGraphOrTable implements Iterable<Map<String, Object>> {


    private PGraph content;
    private List<Map<String, Object>> result;

    public PGraphOrTable(PGraph content) {
        this.content = content;
        this.result = new ArrayList<>();
    }

    public PGraphOrTable(List<Map<String, Object>> result) {
        this.content = PGraphImpl.createEmpty();
        this.result = result;
    }

    public PGraphOrTable() {
        this.result = new ArrayList<>();
        this.content = PGraphImpl.createEmpty();
    }

    public PGraphOrTable(PGraph r1, PGraph r2) {

        this.content = r1;

        this.content.union(r2);

    }

    @Override
    public Iterator<Map<String, Object>> iterator() {
        return result.iterator();
    }

    @Override
    public void forEach(Consumer<? super Map<String, Object>> action) {
        result.forEach(action);
    }

    public PGraph getContent() {
        return this.content;
    }

    public List<Map<String, Object>> getResult() {
        return this.result;
    }

    public void setContent(PGraph content) {
        this.content = content;
    }

    public void setResult(List<Map<String, Object>> res) {
        this.result = res;
    }
}
