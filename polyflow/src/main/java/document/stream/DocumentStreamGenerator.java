package document.stream;

import org.apache.jena.atlas.json.io.parser.JSONParser;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Tuple;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;
import relational.stream.RowStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.*;

public class DocumentStreamGenerator {

    private static final Long TIMEOUT = 1000l;
    private final Map<String, DataStream<String>> activeStreams;

    private File f1 = new File(DocumentStreamGenerator.class.getResource("/test.json").getPath());
    private Scanner s1;
    private final AtomicBoolean isStreaming;
    private StringBuilder stringBuilder = new StringBuilder();
    private final JSONArray jsonArray;
    int count = 0;

    public DocumentStreamGenerator() {
        this.activeStreams = new HashMap<>();
        this.isStreaming = new AtomicBoolean(false);
        try{
            s1 = new Scanner(f1);
        }catch(FileNotFoundException e){
        }
        while(s1.hasNext()){
            stringBuilder.append(s1.nextLine());
        }
        this.jsonArray = new JSONArray(stringBuilder.toString());

    }



    public DataStream<String> getStream(String streamURI) {
        if (!activeStreams.containsKey(streamURI)) {
            DocumentStream stream = new DocumentStream(streamURI);
            activeStreams.put(streamURI, stream);
        }
        return activeStreams.get(streamURI);
    }

    public void startStreaming() {
        if (!this.isStreaming.get()) {
            this.isStreaming.set(true);
            Runnable task = () -> {
                long ts = 0;
                while (this.isStreaming.get() && count < jsonArray.length()) {
                    long finalTs = ts;
                    activeStreams.entrySet().forEach(e -> generateDataAndAddToStream(e.getValue(), finalTs));
                    ts += 300;
                    count+=1;
                    try {
                        Thread.sleep(TIMEOUT);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                stopStreaming();

            };


            Thread thread = new Thread(task);
            thread.start();
        }
    }

    private void generateDataAndAddToStream(DataStream<String> stream, long ts) {
        String json;
        if(stream.getName().equals("http://test/stream1")) {
            stream.put(jsonArray.get(count).toString(), ts);
        }

    }


    public void stopStreaming() {
        this.isStreaming.set(false);
    }

}
