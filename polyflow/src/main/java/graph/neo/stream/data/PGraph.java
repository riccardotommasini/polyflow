package graph.neo.stream.data;

public interface PGraph  {


    Node[] nodes();

    Edge[] edges();

    default long timestamp() {
        return System.currentTimeMillis();
    }

    void union(PGraph r2);

    interface Node {

        long id();

        String[] labels();

        String[] properties();

        Object property(String p);

    }

    interface Edge extends Node {

        String to();

        String from();

    }
}
