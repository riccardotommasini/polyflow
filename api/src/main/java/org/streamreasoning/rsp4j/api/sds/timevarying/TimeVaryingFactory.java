package org.streamreasoning.rsp4j.api.sds.timevarying;

import org.streamreasoning.rsp4j.api.operators.s2r.Convertible;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOperator;

/**
 * Factory to use in order to instantiate different TimeVarying implementations at runtime.
 * The Factory implementation must override the create method, which have to return an object that implements the TimeVarying interface.
 */
public interface TimeVaryingFactory<W extends Convertible<?>> {


    TimeVarying<W> create(StreamToRelationOperator<?, W> s2r, String name);

}
