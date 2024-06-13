package graph.jena.operatorsimpl.r2r.jena;

import graph.jena.datatypes.JenaGraphOrBindings;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.*;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.core.ResultBinding;
import org.apache.jena.sparql.core.mem.DatasetGraphInMemory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JoinRSPQLstarQueryJena implements RelationToRelationOperator<JenaGraphOrBindings> {


    private String query;

    private List<String> tvgNames;

    private String resName;
    DatasetGraph knowledge = new DatasetGraphInMemory();
    List<Binding> knowledge_res = new ArrayList<>();

    public JoinRSPQLstarQueryJena(List<String> tvgNames, String resName) {
        this.tvgNames = tvgNames;
        this.resName = resName;
        RDFParser.create()
                .base("http://base/")
                .source(FullQueryUnaryJena.class.getResourceAsStream("/base-data.ttl"))
                .checking(false)
                .lang(RDFLanguages.TRIG)
                .parse(knowledge);

        initializeKnowledge();



    }

    private void initializeKnowledge(){
        Query q = QueryFactory.create("BASE <http://base/>\n" +
                "PREFIX ex: <http://www.example.org/ontology#>\n" +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX sosa: <http://www.w3.org/ns/sosa/> " +
                "SELECT * WHERE {\n" +
                "    ?person a foaf:Person ;\n" +
                "       foaf:name \"Rut\" ;\n" +
                "       ex:home ?home ;\n" +
                "       ex:partner ?partner .\n" +
                "\n" +
                "    [] a ex:NormalSituation ;\n" +
                "       ex:forPerson ?person ;\n" +
                "       ex:forActivity ?activity ;\n" +
                "       ex:expectedHeartRate [ ex:upperBound ?hrMax ] ;\n" +
                "       ex:expectedBreathingRate [ ex:lowerBound ?brMin ] ;\n" +
                "       ex:expectedOxygenSaturation [ ex:lowerBound ?oxMin ; ex:upperBound ?oxMax ] .}");

        q.getProjectVars();
        Node aDefault = NodeFactory.createURI("default");
        DatasetGraph dg = new DatasetGraphInMemory();
        dg.addAll(knowledge);

        QueryExecution queryExecution = QueryExecutionFactory.create(q, DatasetImpl.wrap(dg));
        ResultSet resultSet = queryExecution.execSelect();
        while (resultSet.hasNext()) {

            ResultBinding rb = (ResultBinding) resultSet.next();
            knowledge_res.add(rb.getBinding());
        }

    }
    @Override
    public JenaGraphOrBindings eval(List<JenaGraphOrBindings> datasets) {

        List<Binding> collect = datasets.get(0).getResult().stream().flatMap(l -> knowledge_res.stream().map(r -> Algebra.merge(l, r)))
                .filter(b -> b != null).collect(Collectors.toList());

        JenaGraphOrBindings result = new JenaGraphOrBindings();
        result.setResult(collect);

        return result;

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
