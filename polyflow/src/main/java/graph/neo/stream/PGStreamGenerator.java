package graph.neo.stream;

import graph.neo.stream.data.PGraph;
import graph.neo.stream.data.PGraphImpl;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class PGStreamGenerator {

    private final Map<String, DataStream<PGraph>> activeStreams;
    private final AtomicBoolean isStreaming;

    public PGStreamGenerator() {
        this.activeStreams = new HashMap<>();
        this.isStreaming = new AtomicBoolean(false);
    }


    public void addStream(DataStream<PGraph> stream){
        activeStreams.put(stream.getName(), stream);
    }

    public DataStream<PGraph> getStream(String streamURI) {
        if (!activeStreams.containsKey(streamURI)) {
            PGraphStream stream = new PGraphStream(streamURI);
            activeStreams.put(streamURI, stream);
        }
        return activeStreams.get(streamURI);
    }

    public void startStreaming() {
        if (!this.isStreaming.get()) {
            this.isStreaming.set(true);

            Runnable task = () -> {
                int count = 0;
                while (this.isStreaming.get()) {
                    try {
                        String fileName = "testGraph" + (count % 5 + 1) + ".json";
                        //Create a property graph using the test.json as a base
                        URL url = PGStreamGenerator.class.getClassLoader().getResource(fileName);
                        FileReader fileReader = new FileReader(url.getPath());
                        PGraph pGraph = PGraphImpl.fromJson(fileReader);
                        activeStreams.values().forEach(s -> s.put(pGraph, pGraph.timestamp()));
                        Thread.sleep(1000);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    count++;

                }
            };


            Thread thread = new Thread(task);
            thread.start();
        }
    }

    public void stopStreaming() {

    }
}
