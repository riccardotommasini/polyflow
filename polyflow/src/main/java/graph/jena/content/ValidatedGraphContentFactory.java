/*
package graph.jena.content;

import graph.jena.datatypes.JenaOperandWrapper;
import org.apache.jena.Jena;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.shacl.Shapes;
import org.streamreasoning.rsp4j.api.secret.content.Content;
import org.streamreasoning.rsp4j.api.secret.content.ContentFactory;
import org.streamreasoning.rsp4j.api.secret.time.Time;

public class ValidatedGraphContentFactory implements ContentFactory<Graph, ValidatedGraph, JenaOperandWrapper> {

    private Time time;
    private Shapes shapes;

    public ValidatedGraphContentFactory(Time time, Shapes shapes) {
        this.time = time;
        this.shapes = shapes;
    }


    @Override
    //To validate for Empty graph
    public Content<Graph, ValidatedGraph, JenaOperandWrapper> createEmpty() {
        return new EmptyContent<>(new ValidatedGraph(Factory.createDefaultGraph(), Factory.createDefaultGraph()));
    }

    @Override
    public ValidatedContent<Graph, ValidatedGraph, JenaOperandWrapper> create() {
        return new ValidatedContentJenaGraph(time, shapes);
    }
}
*/
