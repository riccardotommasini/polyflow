package graph.neo.seraph;

import org.apache.commons.rdf.api.IRI;
import org.streamreasoning.rsp4j.api.RDFUtils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class WindowNode {
    private final IRI windowUri;
    private final Duration logicalRange;
    private Duration logicalStep;
    private final Integer t0;

    public WindowNode(String id, Integer range, Integer step, Integer t0) {
        this.windowUri = RDFUtils.createIRI(id);
        this.logicalRange = Duration.ofSeconds(range);
        this.logicalStep = Duration.ofSeconds(step);
        this.t0 = t0;
    }

    public WindowNode(IRI windowUri, Duration logicalRange, Integer t0) {
        this.windowUri = windowUri;
        this.logicalRange = logicalRange;
        this.t0 = t0;
    }

    public WindowNode(IRI windowUri, Duration logicalRange, Duration logicalStep, Integer t0) {
        this.windowUri = windowUri;
        this.logicalRange = logicalRange;
        this.logicalStep = logicalStep;
        this.t0 = t0;
    }


    public long getT0() {
        return t0;
    }

    public long getRange() {
        return logicalRange.getSeconds() * 1000;
    }

    public long getStep() {
        return logicalStep != null ? logicalStep.getSeconds() * 1000 : -1;
    }

    public String getUnitRange() {
        return ChronoUnit.SECONDS.toString();
    }

    public String getUnitStep() {
        return ChronoUnit.SECONDS.toString();
    }

    public String iri() {
        return windowUri.getIRIString();
    }

    public boolean named() {
        return windowUri != null;
    }
}
