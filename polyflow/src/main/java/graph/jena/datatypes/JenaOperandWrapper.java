package graph.jena.datatypes;

import graph.jena.content.ValidatedGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.sparql.engine.binding.Binding;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class JenaOperandWrapper implements Iterable<Binding> {

    public JenaOperandWrapper() {

    }

    private ValidatedGraph content;
    private List<Binding> result;

    public JenaOperandWrapper(Graph content) {
        this.content = new ValidatedGraph(content, content);
    }


    @Override
    public Iterator<Binding> iterator() {
        return result.iterator();
    }

    @Override
    public void forEach(Consumer<? super Binding> action) {
        result.forEach(action);
    }


    public ValidatedGraph getContent() {
        return this.content;
    }

    public List<Binding> getResult() {
        return this.result;
    }

    public void setContent(ValidatedGraph content) {
        this.content = content;
    }

    public void setResult(List<Binding> res) {
        this.result = res;
    }
}
