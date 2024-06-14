package graph.jena.operatorsimpl.r2r.jena;

import graph.jena.datatypes.JenaGraphOrBindings;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.query.*;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.core.ResultBinding;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.mem.DatasetGraphInMemory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.Binding4;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.jena.datatypes.xsd.XSDDatatype.*;

public class JoinRSPQLstarQueryJena implements RelationToRelationOperator<JenaGraphOrBindings> {


    private String query;

    private List<String> tvgNames;

    private String resName;
    DatasetGraph knowledge = new DatasetGraphInMemory();
    List<Binding> knowledge_res = new ArrayList<>();
    List<String> outputVars = new ArrayList<>();

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
        outputVars.add("activity");
        outputVars.add("avgHr");
        outputVars.add("avgBr");
        outputVars.add("avgOx");


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

        List<Binding> res = new ArrayList<>();
        for(Binding b : collect){

            double hrMax = ((Integer)b.get("hrMax").getLiteralValue()).doubleValue();
            double brMin = ((Integer)b.get("brMin").getLiteralValue()).doubleValue();
            double oxMin = ((BigDecimal)b.get("oxMin").getLiteralValue()).doubleValue();
            double oxMax = ((Integer)b.get("oxMax").getLiteralValue()).doubleValue();
            double avgHr = b.get("avgHr").getLiteralValue() instanceof BigDecimal? ((BigDecimal)b.get("avgHr").getLiteralValue()).doubleValue():((Integer)b.get("avgHr").getLiteralValue()).doubleValue();
            double avgBr = b.get("avgBr").getLiteralValue() instanceof BigDecimal? ((BigDecimal)b.get("avgBr").getLiteralValue()).doubleValue():((Integer)b.get("avgBr").getLiteralValue()).doubleValue();
            double avgOx = b.get("avgOx").getLiteralValue() instanceof BigDecimal? ((BigDecimal)b.get("avgOx").getLiteralValue()).doubleValue():((Integer)b.get("avgOx").getLiteralValue()).doubleValue();

            if(!b.get("loc").equals(b.get("home")) && avgHr>hrMax && avgBr<brMin && avgOx>=oxMin && avgOx<=oxMax){
                List<Binding> tmp = new ArrayList<>();
                b.forEach(((var, node) -> {
                    if(outputVars.contains(var.getVarName()))
                        tmp.add(BindingFactory.binding(var, node));
                }));
                res.add(tmp.stream().reduce(Algebra::merge).get());
            }
        }

        JenaGraphOrBindings result = new JenaGraphOrBindings();
        result.setResult(res);

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
