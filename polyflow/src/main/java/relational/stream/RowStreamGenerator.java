package relational.stream;


import org.javatuples.Quartet;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class RowStreamGenerator {

    private static final String PREFIX = "http://test/";
    private static final Long TIMEOUT = 1000l;
    private final Map<String, DataStream<Quartet<Long, String, Integer, Boolean>>> activeStreams;

    private final String[] colors = new String[]{"Blue", "Green", "Red", "Yellow", "Black", "Grey", "White"};
    private final AtomicBoolean isStreaming;


    public RowStreamGenerator() {
        this.activeStreams = new HashMap<String, DataStream<Quartet<Long, String, Integer, Boolean>>>();
        this.isStreaming = new AtomicBoolean(false);
    }


    public DataStream<Quartet<Long, String, Integer, Boolean>> getStream(String streamURI) {
        if (!activeStreams.containsKey(streamURI)) {
            RowStream stream = new RowStream(streamURI);
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
                    ts += 1000;
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

    private void generateDataAndAddToStream(DataStream<Quartet<Long, String, Integer, Boolean>> stream, long ts) {


        Quartet<Long, String, Integer, Boolean> row;

        if(stream.getName().equals("http://test/stream1")) {
            row = new Quartet<>(ThreadLocalRandom.current().nextLong(10), "stream_1", ThreadLocalRandom.current().nextInt(10), ThreadLocalRandom.current().nextBoolean());
        }
        else{
            row = new Quartet<>(ThreadLocalRandom.current().nextLong(10), "stream_2", ThreadLocalRandom.current().nextInt(10), ThreadLocalRandom.current().nextBoolean());
        }
        stream.put(row, ts);
    }


    public void stopStreaming() {
        this.isStreaming.set(false);
    }

}
