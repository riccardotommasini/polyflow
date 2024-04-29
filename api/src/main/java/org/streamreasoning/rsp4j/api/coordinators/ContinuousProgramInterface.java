package org.streamreasoning.rsp4j.api.coordinators;

import org.streamreasoning.rsp4j.api.operators.s2r.Convertible;

/**
 * Interface that should be extended by a Continuous Program object, it manages a list of tasks, each of which represents a query
 */
public interface ContinuousProgramInterface<I, W, R extends Iterable<?>, O>{

    /**
     * Passes the query that will be used to build the various components needed to answer it(tasks, operators etc..)
     */
    void buildTask(String query);

}
