package multimodal.operators.m2m;

import org.streamreasoning.rsp4j.api.operators.multimodal.m2m.ModelToModelOperator;

import java.util.List;

public class M2MDummy<R1, R2> implements ModelToModelOperator<R1,R2> {



    @Override
    public R2 eval(R1 dataset) {
        return (R2) dataset;
    }

    @Override
    public List<String> getTvgNames() {
        return null;
    }
}
