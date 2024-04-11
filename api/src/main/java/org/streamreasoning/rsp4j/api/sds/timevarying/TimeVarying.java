package org.streamreasoning.rsp4j.api.sds.timevarying;

public interface TimeVarying<E> {

    void materialize(long ts);

    E get();

    String iri();

    //Empty string is considered a name?
    default boolean named() {
        return iri() != null;
    }

}
