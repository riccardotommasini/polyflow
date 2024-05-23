package org.streamreasoning.rsp4j.api.secret.time;

public class TimeInstant {

    public final long t;
    //Number of times this time instant has been added
    public int duplicates;

    public TimeInstant(long t) {
        this.t = t;
        this.duplicates = 1;
    }

    @Override
    public boolean equals(Object o){
        return (o instanceof TimeInstant) && this.t == ((TimeInstant) o).t;
    }

    @Override
    public int hashCode(){
        return (int)(t ^ (t >>> 32));
    }
}
