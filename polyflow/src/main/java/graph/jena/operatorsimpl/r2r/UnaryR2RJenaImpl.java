package graph.jena.operatorsimpl.r2r;

import graph.jena.datatypes.JenaOperandWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.*;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.core.ResultBinding;
import org.apache.jena.sparql.core.mem.DatasetGraphInMemory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.streamreasoning.rsp4j.api.operators.r2r.DAG.DAGNode;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.LazyTimeVarying;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UnaryR2RJenaImpl implements RelationToRelationOperator<JenaOperandWrapper> {


    private String query;

    private List<String> tvgNames;

    private String resName;

    public UnaryR2RJenaImpl(String query, List<String> tvgNames, String resName) {
        this.query = query;
        this.tvgNames = tvgNames;
        this.resName = resName;

    }

    @Override
    public JenaOperandWrapper eval(List<JenaOperandWrapper> datasets) {
        JenaOperandWrapper dataset = datasets.get(0);
        Query q = QueryFactory.create(query);
        q.getProjectVars();
        Node aDefault = NodeFactory.createURI("default");
        DatasetGraph dg = new DatasetGraphInMemory();
        dg.addGraph(aDefault, dataset.getContent().content);

        QueryExecution queryExecution = QueryExecutionFactory.create(q, DatasetImpl.wrap(dg));
        ResultSet resultSet = queryExecution.execSelect();

        List<Binding> res = new ArrayList<>();

        while (resultSet.hasNext()) {

            ResultBinding rb = (ResultBinding) resultSet.next();
            res.add(rb.getBinding());

        }

        dataset.setResult(res);
        return dataset;
    }

    @Override
    public TimeVarying<JenaOperandWrapper> apply(DAGNode<JenaOperandWrapper> node) {
        return new LazyTimeVarying<>(node);
    }

    @Override
    public List<String> getTvgNames() {
        return tvgNames;
    }

    @Override
    public String getResName() {
        return resName;
    }
}
