package graph.neo.opeators;

import graph.neo.stream.data.PGraph;
import graph.neo.stream.data.PGraphOrTable;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestDatabaseManagementServiceBuilder;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;

import java.util.*;

public class FullQueryUnaryCypher implements RelationToRelationOperator<PGraphOrTable> {

    private String query;

    private List<String> tvgNames;

    private String resName;

    protected GraphDatabaseService db;

    public FullQueryUnaryCypher(String query, List<String> tvgNames, String resName) {
        this.query = query;
        this.tvgNames = tvgNames;
        this.resName = resName;
        TestDatabaseManagementServiceBuilder builder = new TestDatabaseManagementServiceBuilder();
        DatabaseManagementService dbm = builder.impermanent().build();
        this.db = dbm.database(GraphDatabaseSettings.DEFAULT_DATABASE_NAME);
        Transaction transaction = db.beginTx();

    }

    @Override
    public PGraphOrTable eval(List<PGraphOrTable> elements) {
        if (db == null || elements == null)
            return new PGraphOrTable();

        Transaction txd = db.beginTx();
        txd.getAllNodes().forEach(node -> {
            node.getRelationships().forEach(Relationship::delete);
            node.delete();
        });
        txd.commit();

        //TODO First run query (delete n when n.prov == stream(name)) | added the execute delete query
        //TODO create a query that adds all the information into the elements
        Transaction tx = db.beginTx();
        for (PGraphOrTable g : elements) {
            Map<String, Node> ids = new HashMap<>();
            Arrays.stream(g.getContent().nodes()).forEach(n1 -> {
                Node n = tx.createNode();
                Arrays.stream(n1.labels()).forEach(s -> n.addLabel(Label.label(s)));
                Arrays.stream(n1.properties()).forEach(p -> {
                    Object property = getProperty(n1, p);
                    n.setProperty(p, property);
                });
                ids.put(n1.id() + "", n);
            });
            //TODO Assumption on EDGES, they only refer to nodes in the current graph because we better use internal ids
            Arrays.stream(g.getContent().edges()).forEach(e -> {
                Node from = ids.computeIfAbsent(e.from(), l -> tx.createNode());
                Node to = ids.computeIfAbsent(e.to(), l -> tx.createNode());
                Arrays.stream(e.labels()).forEach(l -> {
                    Relationship r = from.createRelationshipTo(to, RelationshipType.withName(l));
                    Arrays.stream(e.properties()).forEach(p -> {
                        Object property = getProperty(e, p);
                        r.setProperty(p, property);
                    });
                });
            });
            ids.clear();
        }

        tx.commit();
        tx.close();


        Transaction tx2 = db.beginTx();
        //execute the cypher query which is stored in the r2r parameter of the query
        Result result = tx2.execute(query);

        List<Map<String, Object>> res = new ArrayList<>();
        while (result.hasNext()) {
            Map<String, Object> next = result.next();
            res.add(next);
        }

        tx2.commit();
        tx2.close();

        return new PGraphOrTable(res);

    }

    private Object getProperty(PGraph.Node n1, String p) {
        Object property = n1.property(p);
        if (property instanceof Map) {
            return ((Map<?, ?>) property).entrySet().stream().findFirst().get().getValue();
        }
        return property;
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
