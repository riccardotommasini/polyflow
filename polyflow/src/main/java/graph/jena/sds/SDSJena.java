package graph.jena.sds;

import graph.jena.datatypes.JenaGraphOrBindings;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;

import java.util.*;
import java.util.stream.Stream;

public class SDSJena implements SDS<JenaGraphOrBindings>, Dataset {

    private final Set<TimeVarying<JenaGraphOrBindings>> defs = new HashSet<>();
    private final Map<Node, TimeVarying<JenaGraphOrBindings>> tvgs = new HashMap<>();
    private final Node def = NodeFactory.createURI("def");

    private static Dataset dataset = DatasetFactory.createGeneral();


    @Override
    public Collection<TimeVarying<JenaGraphOrBindings>> asTimeVaryingEs() {
        return tvgs.values();
    }


    @Override
    public void add(String iri, TimeVarying<JenaGraphOrBindings> tvg) {
        tvgs.put(NodeFactory.createURI(iri), tvg);
    }

    @Override
    public void add(TimeVarying<JenaGraphOrBindings> tvg) {
        defs.add(tvg);
    }


    @Override
    public SDS<JenaGraphOrBindings> materialize(final long ts) {
        //TODO here applies the consolidation strategies
        //Default consolidation coaleces all the current
        //content graphs and produces the SDS to who execute the query.

        //I need to re-add all the triples because the dataset works on quads
        //Altenrativelt one can wrap it into a graph interface and update it directly within the tvg
        // this way there's no need to readd after materialization

        dataset.asDatasetGraph().clear();

        DatasetGraph dg = dataset.asDatasetGraph();


        defs.stream().map(g -> {
                    g.materialize(ts);
                    return g.get();
                }).map(r->r.getContent()).flatMap(Graph::stream)
                .forEach(t -> dg.add(def, t.getSubject(), t.getPredicate(), t.getObject()));

        tvgs.entrySet().stream()
                .map(e -> {
                    e.getValue().materialize(ts);
                    return new NamedGraph(e.getKey(), e.getValue().get().getContent());
                }).forEach(n -> n.g.stream()
                        .forEach(o -> dg.add(n.name, o.getSubject(), o.getPredicate(), o.getObject())));

        return this;
    }

    @Override
    public Stream<JenaGraphOrBindings> toStream() {
        return null;
    }

    //From Jena Dataset, redirected to interal dataset
    @Override
    public Model getDefaultModel() {
        return getDefaultModel();
    }

    @Override
    public Model getUnionModel() {
        return dataset.getUnionModel();
    }

    @Override
    public Dataset setDefaultModel(Model model) {
        return dataset.setDefaultModel(model);
    }

    @Override
    public Model getNamedModel(String uri) {
        return dataset.getNamedModel(uri);
    }

    @Override
    public Model getNamedModel(Resource uri) {
        return dataset.getNamedModel(uri);
    }

    @Override
    public boolean containsNamedModel(String uri) {
        return dataset.containsNamedModel(uri);
    }

    @Override
    public boolean containsNamedModel(Resource uri) {
        return dataset.containsNamedModel(uri);
    }

    @Override
    public Dataset addNamedModel(String uri, Model model) {
        return dataset.addNamedModel(uri, model);
    }

    @Override
    public Dataset addNamedModel(Resource resource, Model model) {
        return dataset.addNamedModel(resource, model);
    }

    @Override
    public Dataset removeNamedModel(String uri) {
        return dataset.removeNamedModel(uri);
    }

    @Override
    public Dataset removeNamedModel(Resource resource) {
        return dataset.removeNamedModel(resource);
    }

    @Override
    public Dataset replaceNamedModel(String uri, Model model) {
        return dataset.replaceNamedModel(uri, model);
    }

    @Override
    public Dataset replaceNamedModel(Resource resource, Model model) {
        return dataset.replaceNamedModel(resource, model);
    }

    @Override
    public Iterator<String> listNames() {
        return dataset.listNames();
    }

    @Override
    public Iterator<Resource> listModelNames() {
        return dataset.listModelNames();
    }

    @Override
    public Lock getLock() {
        return dataset.getLock();
    }

    @Override
    public Context getContext() {
        return dataset.getContext();
    }

    @Override
    public boolean supportsTransactions() {
        return dataset.supportsTransactions();
    }

    @Override
    public boolean supportsTransactionAbort() {
        return dataset.supportsTransactionAbort();
    }

    @Override
    public void begin(TxnType type) {
        dataset.begin(type);
    }

    @Override
    public void begin(ReadWrite readWrite) {
        dataset.begin(readWrite);
    }

    @Override
    public boolean promote(Promote mode) {
        return dataset.promote(mode);
    }

    @Override
    public void commit() {
        dataset.commit();
    }

    @Override
    public void abort() {
        dataset.abort();
    }

    @Override
    public boolean isInTransaction() {
        return dataset.isInTransaction();
    }

    @Override
    public void end() {
        dataset.end();
    }

    @Override
    public ReadWrite transactionMode() {
        return dataset.transactionMode();
    }

    @Override
    public TxnType transactionType() {
        return dataset.transactionType();
    }

    @Override
    public DatasetGraph asDatasetGraph() {
        return dataset.asDatasetGraph();
    }

    @Override
    public void close() {
        dataset.close();
    }

    @Override
    public boolean isEmpty() {
        return dataset.isEmpty();
    }

    class NamedGraph {
        public Node name;
        public Graph g;

        public NamedGraph(Node name, Graph g) {
            this.name = name;
            this.g = g;
        }
    }
}

