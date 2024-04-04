package org.streamreasoning.rsp4j.api.coordinators;

/**
 * Interface that should be extended by a Continuous Program object, it manages a list of tasks, each of which represents a query
 */
public interface ContinuousProgramInterface<I, W, R, O>{

    /**
     * Passes the query that will be used to build the various components needed to answer it(tasks, operators etc..)
     */
    public void buildTask(String query);

}
