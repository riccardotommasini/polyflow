package custom.customoperators;

import graph.jena.sds.TimeVaryingObject;
import org.streamreasoning.rsp4j.api.RDFUtils;
import org.streamreasoning.rsp4j.api.enums.ReportGrain;
import org.streamreasoning.rsp4j.api.enums.Tick;
import org.streamreasoning.rsp4j.api.exceptions.OutOfOrderElementException;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.instance.Window;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.instance.WindowImpl;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;
import org.streamreasoning.rsp4j.api.secret.content.Content;
import org.streamreasoning.rsp4j.api.secret.content.ContentFactory;
import org.streamreasoning.rsp4j.api.secret.report.Report;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.secret.time.TimeInstant;

import java.util.Collections;
import java.util.List;



/*
 * This class contains a custom implementation of a Stream To Relation Operator, in particular it's a time-based Tumbling Window.
 * We will give a brief explanation of the various components used by the operator, a more in-depth one can be found in the Official Documentation.
 *
 * Since (for the sake of simplicity) late arrivals are not treated in this example, only one window can be active at each instant in time,
 * represented by the "active_window" object alongside its "active_content": the first represents an interval (opening time and closing time),
 * while the latter represents the content of that window.
 *
 * A window closes (and a new one opens) if an event with timestamp greater than the closing time of the active window arrives.
 * According to this logic, we decide to report the content of a window when it closes (reporting strategy: on window close).
 * Notice how the fact that a window closes can be logically decoupled from the time at which that window is reported to the user: indeed the opening and closing
 * of windows are managed by the Stream To Relation Operator, while the reporting is managed by the Reporting Strategy (report object).
 *
 * Since we use an "On window close" reporting strategy and a tumbling window, the logic will be the following:
 * when a new event arrives and its timestamp is greater than the closing time of the current window, we need to both
 * open a new window and report+close the old one (opening a new window comes from the Tumbling Window logic, while reporting the old one comes
 * from our Reporting Strategy).
 *
 * We achieve this by copying the active_window and the active_content objects into the reported_window and reported_content objects, and assign
 * new instances to the active_window and active_content objects (to represent a newly open window). At this point we add the new event to the fresh
 * active_content, and the operator is ready to report the old window.
 *
 * For the sake of the example, the operator has been simplified a lot (no Ticker o ReportGrain are being used), refer to the official documentation
 * to have more information about advanced features
 */


public class CustomTumblingWindow<I, W, R extends Iterable<?>> implements StreamToRelationOperator<I, W, R> {

    protected final Time time;
    protected final String name;
    protected final ContentFactory<I, W, R> cf;
    protected Report report;
    private final long width;
    private Window active_window;
    private Window reported_window;
    private Content<I, W, R> active_content;
    private Content<I, W, R> reported_content;
    private long t0;
    public CustomTumblingWindow(Time time, String name, ContentFactory<I, W, R> cf, Report report,
                                         long width) {

        this.time = time;
        this.name = name;
        this.cf = cf;
        this.report = report;
        this.width = width;
        this.t0 = time.getScope();
    }


    @Override
    public Report report() {
        return report;
    }

    @Override
    public Tick tick() {
        return null;
    }

    @Override
    public Time time() {
        return time;
    }

    @Override
    public ReportGrain grain() {
        return null;
    }

    @Override
    public Content<I, W, R> content(long t_e) {
        if(reported_content!=null)
            return reported_content;
        //If I need the content when the reported_content is null, it means that someone else triggered the computation, so we just return the active content if present
        if(active_content != null)
            return active_content;
        return cf.createEmpty();
    }

    @Override
    public List<Content<I, W, R>> getContents(long t_e) {
        if(reported_content!= null)
            return Collections.singletonList(reported_content);
        else return Collections.singletonList(cf.createEmpty());
    }

    //Helper method to open a window given a timestamp
    private Window scope(long t_e) {
        long c_sup = (long) Math.ceil(((double) Math.abs(t_e - t0) / (double) width)) * width;
        long o_i = c_sup - width;
        return new WindowImpl(o_i, c_sup);
    }

    @Override
    public void compute(I arg, long ts) {

        if (time.getAppTime() > ts) {
            throw new OutOfOrderElementException("(" + arg + "," + ts + ")");
        }
        System.out.println("Received element (" + arg + ") at time " + ts + " ms at window "+name);

        //We received an element at time ts, advance the application time
        time.setAppTime(ts);

        if(active_window == null){
            active_window = scope(ts);
            active_content = cf.create();
        }

        if(active_window.getO()<=ts && active_window.getC()>ts){
            active_content.add(arg);
        }

        //If the report strategy matches (in this case, onWindowClose) then we need to report the current window and content
        if(report.report(active_window, active_content, ts, System.currentTimeMillis())){
            reported_window = active_window;
            reported_content = active_content;
            //Adding an evaluation Time Instant to the Time object will tell the system that a computation needs to happen
            time.addEvaluationTimeInstants(new TimeInstant(ts));
        }

        if (active_window.getC()<ts){
            active_window = scope(ts);
            active_content = cf.create();
            active_content.add(arg);
        }
    }

    @Override
    public TimeVarying<R> get() {
        return new TimeVaryingObject<>(this, RDFUtils.createIRI(name));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void evict() {

    }

    @Override
    public void evict(long ts) {
        reported_window = null;
        reported_content = null;
        if(active_window.getC() < ts){
            active_window = null;
            reported_content = null;
        }

    }
}
