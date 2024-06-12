package graph.jena.stream;

import org.apache.commons.rdf.api.RDF;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.mem.DatasetGraphInMemory;
import org.streamreasoning.rsp4j.api.RDFUtils;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class JenaStreamRDFStarGenerator {

    private static final Long TIMEOUT = 1000l;
    private final Map<String, DataStream<Graph>> activeStreams;
    private final AtomicBoolean isStreaming;
    private String prefixes;
    private String [] fileNames = {"/activity.trig", "/location.trig", "/heart.trig", "/breathing.trig", "/oxygen.trig"};
    private List<Scanner> scanners = new ArrayList<>();

    public JenaStreamRDFStarGenerator() {
        this.activeStreams = new HashMap<String, DataStream<Graph>>();
        this.isStreaming = new AtomicBoolean(false);
        try {
            for(int i = 0; i< fileNames.length; i++){
                scanners.add(new Scanner(new File(JenaStreamGenerator.class.getResource(fileNames[i]).getPath())));
                //Read prefixes from all files
                prefixes = scanners.get(i).nextLine();
            }
        }catch(FileNotFoundException ignored){}

    }

    public DataStream<Graph> getStream(String streamURI) {
        if (!activeStreams.containsKey(streamURI)) {
            JenaRDFStream stream = new JenaRDFStream(streamURI);
            activeStreams.put(streamURI, stream);
        }
        return activeStreams.get(streamURI);
    }

    public void startStreaming() {
        if (!this.isStreaming.get()) {
            this.isStreaming.set(true);
            Runnable task = () -> {
                long ts = 0;
                while (this.isStreaming.get()) {
                    long finalTs = ts;
                    activeStreams.entrySet().forEach(e -> generateDataAndAddToStream(e.getValue(), finalTs));
                    ts += 200;
                    try {
                        Thread.sleep(TIMEOUT);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            };


            Thread thread = new Thread(task);
            thread.start();
        }
    }

    private void generateDataAndAddToStream(DataStream<Graph> stream, long ts) {

        Scanner s;
        switch (stream.getName()){
            case "http://test/activity":
                s = scanners.get(0);
                break;
            case "http://test/location":
                s = scanners.get(1);
                break;
            case "http://test/heart":
                s = scanners.get(2);
                break;
            case "http://test/breathing":
                s = scanners.get(3);
                break;
            case "http://test/oxygen":
                s = scanners.get(4);
                break;
            default:
                throw new RuntimeException("no scanner defined");
        }
        String data = s.nextLine();
        Graph tmp = GraphMemFactory.createGraphMem();
        DatasetGraph ds = new DatasetGraphInMemory();
        RDFParser.create()
                .base("http://base/")
                .source(new ByteArrayInputStream((prefixes + data).getBytes()))
                .checking(false)
                .lang(RDFLanguages.TRIG)
                .parse(ds);
        ds.stream().forEach(g -> tmp.add(g.asTriple()));
        stream.put(tmp, ts);

    }


    public void stopStreaming() {
        this.isStreaming.set(false);
    }
}
