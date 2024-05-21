package graph.jena.operatorsimpl.r2r;

import graph.jena.datatypes.JenaOperandWrapper;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.streamreasoning.rsp4j.api.operators.r2r.DAG.DAGNode;
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
    public JenaOperandWrapper eval(List<JenaOperandWrapper> datasets) {
        return null;
    }


    @Override
    public List<String> getTvgNames() {
        return tvgNames;
    }

    @Override
    public String getResName() {
        return null;
    }

    public boolean isBinary() {
        return isBinary;
    }

    public String getUnaryOpName() {
        return unaryOpName;
    }

    public String getBinaryOpName() {
        return binaryOpName;
    }

    public JenaOperandWrapper eval(JenaOperandWrapper... datasets) {
        return null;
    }

    public JenaOperandWrapper evalUnary(JenaOperandWrapper dataset) {

        QueryIterator exec = Algebra.exec(bgp, dataset.getContent().content);

        List<Binding> res = new ArrayList<>();

        while (exec.hasNext()) {
            res.add(exec.next());
        }

        dataset.setResult(res);
        return dataset;
    }
}