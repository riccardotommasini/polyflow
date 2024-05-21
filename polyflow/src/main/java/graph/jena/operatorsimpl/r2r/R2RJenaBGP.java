package graph.jena.operatorsimpl.r2r;

import graph.jena.datatypes.JenaOperandWrapper;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.querying.result.SolutionMapping;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class R2RJenaBGP implements RelationToRelationOperator<JenaOperandWrapper> {


    private OpBGP bgp;

    private List<String> tvgNames;

    private boolean isBinary;

    private String unaryOpName;
    private String binaryOpName;

    public R2RJenaBGP(OpBGP bgp, List<String> tvgNames, boolean isBinary, String unaryOpName, String binaryOpName) {
        this.bgp = bgp;
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
    public JenaOperandWrapper eval(JenaOperandWrapper... datasets) {
        return null;
    }

    @Override
    public JenaOperandWrapper evalUnary(JenaOperandWrapper dataset) {

        QueryIterator exec = Algebra.exec(bgp, dataset.getContent().content);

        List<Binding> res = new ArrayList<>();

        while (exec.hasNext()) {
            res.add(exec.next());
        }

        dataset.setResult(res);
        return dataset;
    }

    @Override
    public JenaOperandWrapper evalBinary(JenaOperandWrapper dataset1, JenaOperandWrapper dataset2) {

        JenaOperandWrapper result = new JenaOperandWrapper();
        result.setResult(Stream.concat(dataset1.getResult().stream(), dataset2.getResult().stream()).collect(Collectors.toList()));

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

    @Override
    public Map<String, RelationToRelationOperator<JenaOperandWrapper>> getR2RComponents() {
        return RelationToRelationOperator.super.getR2RComponents();
    }
}
