package graph.neo.seraph;

//import org.streamreasoning.rsp4j.yasper.querying.syntax.QueryFactory;

import org.apache.commons.configuration.ConfigurationException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


public class TestParsing {


    public static void main(String[] args) throws IOException, ConfigurationException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        try {

            SeraphQuery studentTrick = (SeraphQuery) QueryFactory.parse(
                    "" +
                    "REGISTER QUERY student_trick STARTING AT NOW {\n" +
                    "MATCH (:Bike)-[r:rentedAt]->(s:Station),\n" +
                    "q = (b)-[:returnedAt|rentedAt*3..]-(o:Station)\n" +
                    "WITHIN PT1H\n" +
                    "WITH r, s, q, relationships(q) AS rels,\n" +
                    "[n IN nodes(q) WHERE 'Station' IN labels(n) | n.id] AS hs\n" +
                    "WHERE ALL(e IN rels WHERE e.user_id = r.user_id AND e.\n" +
                    "val_time > r.val_time AND e.duration < 20 )\n" +
                    "EMIT r.user_id, s.id, r.val_time, hs\n" +
                    "ON ENTERING EVERY PT5M }" +
                    "");

            System.out.println(studentTrick.getR2R());

        } catch (RuntimeException e) {
            System.out.println(e);
        }


    }
}

