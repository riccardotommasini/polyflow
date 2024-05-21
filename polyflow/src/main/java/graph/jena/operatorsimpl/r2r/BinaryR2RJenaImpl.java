package graph.jena.operatorsimpl.r2r;

import graph.jena.datatypes.JenaOperandWrapper;
import org.apache.jena.Jena;
import org.streamreasoning.rsp4j.api.operators.r2r.DAG.DAGNode;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.LazyTimeVarying;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BinaryR2RJenaImpl implements RelationToRelationOperator<JenaOperandWrapper> {


    private String query;

    private List<String> tvgNames;

    private String resName;


    public BinaryR2RJenaImpl(String query, List<String> tvgNames, String resName) {
        this.query = query;
        this.tvgNames = tvgNames;
        this.resName = resName;

    }



    @Override
    public JenaOperandWrapper eval(List<JenaOperandWrapper> datasets) {
        JenaOperandWrapper dataset1 =  datasets.get(0);
        JenaOperandWrapper dataset2 =  datasets.get(1);
        JenaOperandWrapper result = new JenaOperandWrapper();
        result.setResult(Stream.concat(dataset1.getResult().stream(), dataset2.getResult().stream()).collect(Collectors.toList()));

        return result;
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
