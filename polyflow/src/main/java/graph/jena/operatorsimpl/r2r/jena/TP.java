package graph.jena.operatorsimpl.r2r.jena;

import graph.jena.datatypes.JenaOperandWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class TP implements UnaryOperator<JenaOperandWrapper>, RelationToRelationOperator<JenaOperandWrapper> {


    private OpTriple bgp;

    private List<String> tvgNames;

    private String unaryOpName;

    public TP(OpTriple bgp, List<String> tvgNames, String unaryOpName) {
        this.bgp = bgp;
        this.tvgNames = tvgNames;
        this.unaryOpName = unaryOpName;

    }

    public JenaOperandWrapper eval(List<JenaOperandWrapper> datasets) {
        return null;
    }


    public List<String> getTvgNames() {
        return tvgNames;
    }

    public String getResName() {
        return null;
    }


    public String getUnaryOpName() {
        return unaryOpName;
    }

    public JenaOperandWrapper eval(JenaOperandWrapper dataset) {

        QueryIterator exec = Algebra.exec(bgp, dataset.getContent().content);

        List<Binding> res = new ArrayList<>();

        while (exec.hasNext()) {
            res.add(exec.next());
        }

        dataset.setResult(res);
        return dataset;
    }

    @Override
    public JenaOperandWrapper apply(JenaOperandWrapper bindings) {
        return null;
    }

    public Node getProperty() {
        return bgp.getTriple().getPredicate();
    }

    public Node getObject() {
        return bgp.getTriple().getObject();
    }
}