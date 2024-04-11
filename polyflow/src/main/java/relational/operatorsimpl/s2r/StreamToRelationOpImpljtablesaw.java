package relational.operatorsimpl.s2r;

import graph.jena.sds.TimeVaryingObject;
import operatorsimpl.s2r.StreamToRelationOpImpl;
import org.streamreasoning.rsp4j.api.RDFUtils;
import org.streamreasoning.rsp4j.api.enums.ReportGrain;
import org.streamreasoning.rsp4j.api.enums.Tick;
import org.streamreasoning.rsp4j.api.operators.s2r.Convertible;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;
import org.streamreasoning.rsp4j.api.secret.content.ContentFactory;
import org.streamreasoning.rsp4j.api.secret.report.Report;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import relational.sds.TimeVaryingObjectjtablesaw;

public class StreamToRelationOpImpljtablesaw<I, W extends Convertible<?>> extends StreamToRelationOpImpl<I, W> {
    public StreamToRelationOpImpljtablesaw(Tick tick, Time time, String name, ContentFactory<I, W> cf, ReportGrain grain, Report report, long width, long slide) {
        super(tick, time, name, cf, grain, report, width, slide);
    }

    @Override
    public TimeVarying<W> apply() {
        return new TimeVaryingObjectjtablesaw<W>(this, name);
    }

    @Override
    public TimeVarying<W> get() {
        return new TimeVaryingObjectjtablesaw<W>(this, name);
    }

}
