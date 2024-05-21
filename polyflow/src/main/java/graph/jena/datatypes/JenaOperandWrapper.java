package graph.jena.datatypes;

import graph.jena.content.ValidatedGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.other.G;
import org.apache.jena.sparql.engine.binding.Binding;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class JenaOperandWrapper implements Iterable<Binding> {


    private Graph content;
    private List<Binding> result;

    public JenaOperandWrapper(Graph content) {
        this.content = content;
    }

    public JenaOperandWrapper() {

    }

    @Override
    public Iterator<Binding> iterator() {
        return result.iterator();
    }

    @Override
    public void forEach(Consumer<? super Binding> action) {
        result.forEach(action);
    }


    public Graph getContent() {
        return this.content;
    }

    public List<Binding> getResult() {
        return this.result;
    }

    public void setContent(Graph content) {
        this.content = content;
    }

    public void setResult(List<Binding> res) {
        this.result = res;
    }
}
