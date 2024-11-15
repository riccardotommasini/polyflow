package org.streamreasoning.polyflow.api.sds.timevarying;

import java.util.function.Function;

public interface TimeVarying<E> {

    void materialize(long ts);

    E get();

    String iri();

    default boolean named() {
        return iri() != null;
    }

    default <H> TimeVarying<H> compose(Function<E, H> tvh) {
        return new TimeVarying<H>() {
            @Override
            public void materialize(long ts) {
                TimeVarying.this.materialize(ts);
            }

            @Override
            public H get() {
                return tvh.apply(TimeVarying.this.get());
            }

            @Override
            public String iri() {
                return TimeVarying.this.iri();
            }
        };
    }

}
