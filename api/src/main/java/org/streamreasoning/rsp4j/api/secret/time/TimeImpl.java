package org.streamreasoning.rsp4j.api.secret.time;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Central control time. This implementation of time, represents
 * a global clock, streams must be aligned w.r.t. to app_time.
 **/
public class TimeImpl implements Time {

    public final long tc0;
    List<Long> et = new ArrayList<>();
    private Long app_time = 0L;
    ET timeInstants = new ET();

    ET computedTimeInstants = new ET();

    public TimeImpl(long tc0) {
        this.tc0 = tc0;
    }

    @Override
    public long getScope() {
        return tc0;
    }

    @Override
    public synchronized long getAppTime() {
        return this.app_time;
    }

    @Override
    public synchronized void setAppTime(long now) {
        et.add(now);
        this.app_time = this.app_time < now ? now : this.app_time;
    }

    @Override
    public ET getEvaluationTimeInstants() {
        return timeInstants;
    }

    @Override
    public void addEvaluationTimeInstants(TimeInstant et) {

        //If we already inserted a 'computation time' bigger or equal than the one we want to insert, we avoid inserting the new one
        if(timeInstants.isEmpty() || timeInstants.getLast().t < et.t)
            timeInstants.add(et);
        else if(timeInstants.getLast().t == et.t)
            timeInstants.getLast().duplicates+=1;
        else{
            throw new UnsupportedOperationException("Out of order not handled");
        }
    }

    public TimeInstant getEvaluationTime(){

        TimeInstant t = timeInstants.poll();
        computedTimeInstants.add(t);
        return t;

    }

    @Override
    public boolean hasEvaluationInstant(){
        return !timeInstants.isEmpty();
    }

    public static Time forStartTime(long startTime){
        return new TimeImpl(startTime);
    }
}
