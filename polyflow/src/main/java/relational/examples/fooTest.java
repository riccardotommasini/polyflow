package relational.examples;

import org.streamreasoning.rsp4j.api.stream.data.DataStream;
import relational.stream.RowStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class fooTest {

    public static void main(String [] args){

        DataStream<?> a = new RowStream<>("ciao");
        DataStream<?> b = new RowStream<>("bello");

        Map<DataStream<Integer>, Integer> m1 = new HashMap<>();
        //Map<List<String>, Integer> m2 = new HashMap<>();
        m1.put((DataStream<Integer>) a, 3);


        System.out.println(m1.containsKey(a));
        System.out.println(m1.containsKey(b));
        //m1.put(b, 2);
        //m1.put(a, 3);
        System.out.println(m1.get(b));
        System.out.println(m1.get(a));
    }
}
