package org.streamreasoning.polyflow.base.operatorsimpl.r2r;

import org.streamreasoning.polyflow.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.polyflow.api.sds.timevarying.TimeVarying;

import java.util.List;

public class R2RBase<T extends Iterable<?>> implements RelationToRelationOperator<T> {

    private final List<String> tvgNames;
    private final String resName;

    public R2RBase(List<String> tvgNames, String resName) {
        this.tvgNames = tvgNames;
        this.resName = resName;
    }

    @Override
    public T eval(List<T> list) {
        return list.get(0);
    }

    @Override
    public TimeVarying<T> apply(TimeVarying<T> node) {
        return RelationToRelationOperator.super.apply(node);
    }

    @Override
    public List<String> getTvgNames() {
        return tvgNames;
    }

    @Override
    public String getResName() {
        return resName;
    }
}
