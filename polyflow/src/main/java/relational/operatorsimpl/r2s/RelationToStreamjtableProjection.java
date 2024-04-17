package relational.operatorsimpl.r2s;

import org.javatuples.Pair;
import org.javatuples.Septet;
import org.javatuples.Tuple;
import org.javatuples.Unit;
import org.streamreasoning.rsp4j.api.operators.r2s.RelationToStreamOperator;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

public class RelationToStreamjtableProjection implements RelationToStreamOperator<Table, Tuple> {

    public Tuple transform(Object sm, long ts) {

        Row row = (Row) sm;
        Tuple res = new Pair<>(
                row.getString(0),
                row.getString(1));
        return res;
    }

    public Stream<Tuple> eval(Table sml, long ts) {
        Collection<Tuple> result = new ArrayList<>();
        sml.forEach(e -> result.add(transform(e, ts)));
        return result.stream();
    }
}
