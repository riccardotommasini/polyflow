package org.streamreasoning.polyflow.api.secret.time;

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
     * Returns the next time instant to evaluate in a computation, removes it from the list of evaluation times and adds it to the list of evaluated times
     */
    public TimeInstant getEvaluationTime();

     boolean hasEvaluationInstant();

}
