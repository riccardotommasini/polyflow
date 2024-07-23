package graph.neo.seraph;


import graph.neo.stream.PGraphStream;
import org.streamreasoning.rsp4j.api.RDFUtils;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.secret.time.TimeFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

//Seraph query
public class SeraphQuery {

    private List<String> projections;
    private R2R r2r;
    private Map<String, S2R> inputs = new HashMap<>();
    private Map<String, R2S> outputs = new HashMap<>();
    private Map<WindowNode, PGraphStream> map = new HashMap<>();
    private String id;

    public SeraphQuery(String id, List<String> projections) {
        this.id = id;
        this.projections = projections;
    }

    public SeraphQuery(String id, R2R r2r, Map<String, S2R> inputs, Map<String, R2S> outputs, List<String> projections) {
        this.id = id;
        this.r2r = r2r;
        this.inputs = inputs;
        this.outputs = outputs;
        this.projections = projections;
    }


    public void setInputStream(String uri) {

        S2R s2r = inputs.entrySet().iterator().next().getValue();

        inputs.clear();
        inputs.put(uri, s2r);

    }


    public void setOutputStream(String uri) {

        R2S r2s = outputs.entrySet().iterator().next().getValue();

        //TODO change map to list data structure because of possible duplicated keys
        outputs.clear();
        outputs.put(uri, r2s);
    }


    public String getID() {
        return id;
    }

    public Map<WindowNode, PGraphStream> getWindowMap() {
        AtomicInteger i = new AtomicInteger();
        outputs.forEach((out, r2S) -> {
            inputs.forEach((k, v) -> {
                i.getAndIncrement();
                PGraphStream webStream = new PGraphStream(k);
                WindowNode windowNode = new WindowNode(RDFUtils.createIRI(k + "/w" + i), v.range, r2S.period, 0);
                //add windownode and webstream to the map, if the key value pair doesnt already exist
                map.putIfAbsent(windowNode, webStream);
            });
        });
        return map;
    }

    public List<String> getInputStreams() {
        return new ArrayList<>(inputs.keySet());
    }

    public Time getTime() {
        return TimeFactory.getInstance();
    }


    public String getR2R() {
        return r2r.toString();
    }

    public List<String> getResultVars() {
        return projections;
    }

}
