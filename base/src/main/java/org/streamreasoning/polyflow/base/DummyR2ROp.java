package org.streamreasoning.polyflow.base;

import org.streamreasoning.polyflow.api.operators.r2r.RelationToRelationOperator;

import java.util.List;

public class DummyR2ROp<T extends Iterable<?>> implements RelationToRelationOperator<T> {

    // Name of the operands (one operand in this case)
    List<String> tvgNames;
    //Name of the result
    String resName;

    public DummyR2ROp(List<String> tvgNames, String resName) {
        this.tvgNames = tvgNames;
        this.resName = resName;
    }

    @Override
    public T eval(List<T> datasets) {
        T op = datasets.get(0);
        return op;
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
