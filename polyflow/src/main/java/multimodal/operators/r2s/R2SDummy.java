package multimodal.operators.r2s;

import org.javatuples.Tuple;
import org.streamreasoning.rsp4j.api.operators.r2s.RelationToStreamOperator;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class R2SDummy implements RelationToStreamOperator<Table, Boolean> {

    public Stream<Boolean> eval(Table sml, long ts) {

        List<Boolean> result = new ArrayList<>();
        sml.forEach(r->result.add(r.getBoolean(2)));
        return result.stream();
    }
    public Boolean transform(Object sm, long ts) {
        return null;
    }
}
