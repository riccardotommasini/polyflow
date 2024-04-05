package org.streamreasoning.rsp4j.api.containers;

import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;

import java.util.Collections;
import java.util.List;


public  class R2RContainer<R extends Iterable<?>> {
        private List<String> tvgName;
        private RelationToRelationOperator<R> r2rOperator;

        private boolean isBinary;

    public R2RContainer() {
    }

    public R2RContainer(List<String> tvgName, RelationToRelationOperator<R> r2rOperator, boolean isBinary) {
        this.tvgName = tvgName;
        this.r2rOperator = r2rOperator;
        this.isBinary = isBinary;
    }
    public R2RContainer(String tvgName, RelationToRelationOperator<R> r2rOperator, boolean isBinary) {
        this(Collections.singletonList(tvgName),r2rOperator, isBinary);
    }

    public List<String> getTvgNames() {
        return this.tvgName;
    }

    public RelationToRelationOperator<R> getR2rOperator() {
        return this.r2rOperator;
    }

    public boolean isBinary (){ return this.isBinary;}
}

