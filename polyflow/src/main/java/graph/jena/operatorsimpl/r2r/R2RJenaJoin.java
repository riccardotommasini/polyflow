package graph.jena.operatorsimpl.r2r;

import graph.jena.datatypes.JenaOperandWrapper;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.engine.binding.Binding;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.querying.result.SolutionMapping;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class R2RJenaJoin implements RelationToRelationOperator<JenaOperandWrapper> {


    private List<String> tvgNames;

    private boolean isBinary;

    private String unaryOpName;
    private String binaryOpName;

    public R2RJenaJoin(List<String> tvgNames, boolean isBinary, String unaryOpName, String binaryOpName) {
        this.tvgNames = tvgNames;
        this.isBinary = isBinary;
        this.unaryOpName = unaryOpName;
        this.binaryOpName = binaryOpName;

    }

    @Override
    public List<String> getTvgNames() {
        return tvgNames;
    }

    @Override
    public boolean isBinary() {
        return isBinary;
    }

    @Override
    public String getUnaryOpName() {
        return unaryOpName;
    }

    @Override
    public String getBinaryOpName() {
        return binaryOpName;
    }

    @Override
    public JenaOperandWrapper evalUnary(JenaOperandWrapper dataset) {
        return dataset;
    }

    public JenaOperandWrapper eval(JenaOperandWrapper... datasets) {

        List<Binding> collect = datasets[0].getResult().stream().flatMap(l -> datasets[1].getResult().stream().map(r -> Algebra.merge(l, r)))
                .filter(b -> b != null).collect(Collectors.toList());

        JenaOperandWrapper result = new JenaOperandWrapper();
        result.setResult(collect);

        return result;

    }

    @Override
    public JenaOperandWrapper evalBinary(JenaOperandWrapper dataset1, JenaOperandWrapper dataset2) {

        List<Binding> collect = dataset1.getResult().stream().flatMap(l -> dataset2.getResult().stream().map(r -> Algebra.merge(l, r)))
                .filter(b -> b != null).collect(Collectors.toList());

        JenaOperandWrapper result = new JenaOperandWrapper();
        result.setResult(collect);

        return result;

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
