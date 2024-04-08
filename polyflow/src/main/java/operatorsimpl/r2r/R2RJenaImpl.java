package operatorsimpl.r2r;

import datatypes.JenaOperandWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.*;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.core.ResultBinding;
import org.apache.jena.sparql.core.mem.DatasetGraphInMemory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.querying.result.SolutionMapping;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class R2RJenaImpl implements RelationToRelationOperator<JenaOperandWrapper> {


    private Query query;

    public R2RJenaImpl(String query) {
        this.query = QueryFactory.create(query);
        this.query.getProjectVars();

    }

    @Override
    public JenaOperandWrapper evalUnary(JenaOperandWrapper dataset) {

        Node aDefault = NodeFactory.createURI("default");
        DatasetGraph dg = new DatasetGraphInMemory();
        dg.addGraph(aDefault, dataset.getContent().content);

        QueryExecution queryExecution = QueryExecutionFactory.create(query, DatasetImpl.wrap(dg));
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
    public JenaOperandWrapper evalBinary(JenaOperandWrapper dataset1, JenaOperandWrapper dataset2) {
        return null;
    }

    @Override
    public TimeVarying<Collection<JenaOperandWrapper>> apply(SDS<JenaOperandWrapper> sds) {
        return null;
    }

    @Override
    public SolutionMapping<JenaOperandWrapper> createSolutionMapping(JenaOperandWrapper result) {
        return null;
    }
}
