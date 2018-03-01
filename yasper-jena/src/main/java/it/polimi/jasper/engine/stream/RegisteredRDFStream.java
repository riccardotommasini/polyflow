package it.polimi.jasper.engine.stream;

import com.espertech.esper.client.EPStatement;
import it.polimi.rspql.RSPEngine;
import it.polimi.rspql.Stream;
import it.polimi.spe.stream.rdf.RDFStream;
import lombok.Getter;

/**
 * Created by riccardo on 10/07/2017.
 */
@Getter
public class RegisteredRDFStream extends RDFStream implements Stream {

    protected RDFStream stream;
    protected EPStatement e;
    protected RSPEngine engine;

    public RegisteredRDFStream(String uri, RDFStream s, EPStatement epl, RSPEngine engine) {
        super(uri);
        this.stream = s;
        this.engine = engine;
    }


}