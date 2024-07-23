package custom.stream;

import custom.customdatatypes.*;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FruitStreamGenerator {

    private enum fruit {APPLE, BANANA, PEACH, PEAR, PINEAPPLE, TOMATO}
    private String[] fruitStatus = {"underripe", "ripe", "overripe"};
    private final Map<String, DataStream<Fruit>> activeStreams;
    private final long TIMEOUT = 1000l;
    private final Random randomGenerator;
    private boolean isStreaming = false;

    public FruitStreamGenerator(){
        this.activeStreams = new HashMap<>();
        this.randomGenerator = new Random(1336);
    }

    public DataStream<Fruit> getStream(String streamURI) {
        if (!activeStreams.containsKey(streamURI)) {
            FruitDataStream stream = new FruitDataStream(streamURI);
            activeStreams.put(streamURI, stream);
        }
        return activeStreams.get(streamURI);
    }

    public void startStreaming() {
        if (!this.isStreaming) {
            this.isStreaming = true;
            Runnable task = () -> {
                long ts = 0;
                while (this.isStreaming) {
                    long finalTs = ts;
                    activeStreams.entrySet().forEach(e -> generateDataAndAddToStream(e.getValue(), finalTs));
                    ts += 400;
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

    public void stopStreaming() {
        this.isStreaming = false;
    }

    private void generateDataAndAddToStream(DataStream<Fruit> stream, long ts) {

        switch (fruit.values()[randomGenerator.nextInt(0, 6)]){
            case APPLE:
                stream.put(new Apple(randomGenerator.nextFloat(0, 5), fruitStatus[randomGenerator.nextInt(0, 3)]), ts);
                break;
            case BANANA:
                stream.put(new Banana(randomGenerator.nextFloat(0, 5), fruitStatus[randomGenerator.nextInt(0, 3)]), ts);
                break;
            case PEACH:
                stream.put(new Peach(randomGenerator.nextFloat(0, 5), fruitStatus[randomGenerator.nextInt(0, 3)]), ts);
                break;
            case PEAR:
                stream.put(new Pear(randomGenerator.nextFloat(0, 5), fruitStatus[randomGenerator.nextInt(0, 3)]), ts);
                break;
            case PINEAPPLE:
                stream.put(new Pineapple(randomGenerator.nextFloat(0, 5), fruitStatus[randomGenerator.nextInt(0, 3)]), ts);
                break;
            case TOMATO:
                stream.put(new Tomato(randomGenerator.nextFloat(0, 5), fruitStatus[randomGenerator.nextInt(0, 2)]), ts);
                break;

        }

    }


}
