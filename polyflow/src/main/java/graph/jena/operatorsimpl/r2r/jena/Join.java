package graph.jena.operatorsimpl.r2r.jena;

import graph.jena.datatypes.JenaGraphOrBindings;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.engine.binding.Binding;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.querying.result.SolutionMapping;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Join implements RelationToRelationOperator<JenaGraphOrBindings> {


    private List<String> tvgNames;
    private String resName;


    public Join(List<String> tvgNames, String resName) {
        this.tvgNames = tvgNames;
        this.resName = resName;
    }


    @Override
    public List<String> getTvgNames() {
        return tvgNames;
    }

    @Override
    public String getResName() {
        return resName;
    }


    @Override
    public JenaGraphOrBindings eval(List<JenaGraphOrBindings> datasets) {

        List<Binding> collect = datasets.get(0).getResult().stream().flatMap(l -> datasets.get(1).getResult().stream().map(r -> Algebra.merge(l, r)))
                .filter(b -> b != null).collect(Collectors.toList());

        JenaGraphOrBindings result = new JenaGraphOrBindings();
        result.setResult(collect);

        return result;

    }


    public TimeVarying<Collection<JenaGraphOrBindings>> apply(SDS<JenaGraphOrBindings> sds) {
        return null;
    }

}
