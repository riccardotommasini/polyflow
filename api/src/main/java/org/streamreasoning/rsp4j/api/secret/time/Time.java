package org.streamreasoning.rsp4j.api.secret.time;

import java.util.Optional;

public interface Time {

    long getScope();

    long getAppTime();

    void setAppTime(long now);

    default long getSystemTime() {
        return System.currentTimeMillis();
    }

    ET getEvaluationTimeInstants();

    void addEvaluationTimeInstants(TimeInstant i);

    /**
     * Returns the next time instant to evaluate in a computation
     */
    public TimeInstant getEvaluationTime();

    public boolean hasEvaluationInstant();

}
