package graph.neo.stream.data;


import com.google.gson.Gson;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PGraphImpl implements PGraph {

    private static final Gson gson = new Gson();
    protected long timestamp;
    private NodeImpl[] nodes;
    private EdgeImpl[] edges;

    public PGraphImpl(NodeImpl[] nodes, EdgeImpl[] edges) {
        this.nodes = nodes;
        this.edges = edges;
        this.timestamp = System.currentTimeMillis();
    }

    public static PGraph createEmpty() {
        return new PGraphImpl(new NodeImpl[]{}, new EdgeImpl[]{});
    }

    //Create a property graph from a json file and add the current system time as a timestamp
    public static PGraph fromJson(FileReader fileReader) {
        PGraphImpl pGraph = gson.fromJson(fileReader, PGraphImpl.class);
        pGraph.timestamp = System.currentTimeMillis();
        return pGraph;
    }

    @Override
    public Node[] nodes() {
        return nodes;
    }

    @Override
    public Edge[] edges() {
        return edges;
    }

    @Override
    public long timestamp() {
        return timestamp;
    }

    @Override
    public void union(PGraph g2) {


        List<Node> nodes1 = new ArrayList<>(Arrays.stream(this.nodes()).toList());
        nodes1.addAll(Arrays.stream(g2.nodes()).toList());
        this.nodes = nodes1.toArray(new NodeImpl[this.nodes.length + g2.nodes().length]);

        List<Edge> edges1 = new ArrayList<>(Arrays.stream(this.edges()).toList());
        edges1.addAll(Arrays.stream(g2.edges()).toList());
        this.edges = edges1.toArray(new EdgeImpl[this.edges().length + g2.edges().length]);

    }

    @Override
    public String toString() {
        return "(PGraphImpl){" +
               "nodes=" + Arrays.toString(nodes) +
               ", edges=" + Arrays.toString(edges) +
               ", timestamp=" + timestamp +
               '}';
    }

    private class NodeImpl implements Node {
        long id;
        String[] labels;
        Map<String, Object> properties;

        public NodeImpl(long id, String[] labels, Map<String, Object> properties) {
            this.id = id;
            this.labels = labels;
            this.properties = properties;
        }

        public NodeImpl() {
        }

        @Override
        public String toString() {
            return "(" + id +
                   ":" + Arrays.toString(labels) +
                   "{" + properties +
                   "})";
        }

        @Override
        public long id() {
            return id;
        }

        @Override
        public String[] labels() {
            return labels;
        }

        @Override
        public String[] properties() {
            return properties.keySet().toArray(new String[properties.size()]);
        }

        @Override
        public Object property(String p) {
            Object o = properties.get(p);
            return o instanceof ArrayList ? ((ArrayList<?>) o).get(0) : o;
        }
    }

    private class EdgeImpl extends NodeImpl implements Edge {
        long from, to;

        public EdgeImpl(long id, String[] labels, Map<String, Object> properties, long from, long to) {
            super(id, labels, properties);
            this.from = from;
            this.to = to;
        }

        public EdgeImpl() {
        }


        @Override
        public String toString() {
            return "(" + from +
                   "," + to +
                   ")";
        }

        @Override
        public String to() {
            return to + "";
        }

        @Override
        public String from() {
            return from + "";
        }

        @Override
        public long id() {
            return from;
        }
    }


}
