package relational.operatorsimpl.r2s;

import org.javatuples.Septet;
import org.streamreasoning.rsp4j.api.operators.r2s.RelationToStreamOperator;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

public class RelationToStreamjtableJoin implements RelationToStreamOperator<Table, Septet<Long, String, Integer, Boolean, Long, String, Boolean>> {

    public Septet<Long, String, Integer, Boolean, Long, String, Boolean> transform(Object sm, long ts) {

        Row row = (Row) sm;
        Septet<Long, String, Integer, Boolean, Long, String, Boolean> res = new Septet<>(
                row.getLong(0),
                row.getString(1),
                row.getInt(2),
                row.getBoolean(3),
                row.getLong(4),
                row.getString(5),
                row.getBoolean(6));
        return res;
    }

    public Stream<Septet<Long, String, Integer, Boolean, Long, String, Boolean>> eval(Table sml, long ts) {
        Collection<Septet<Long, String, Integer, Boolean, Long, String, Boolean>> result = new ArrayList<>();
        sml.forEach(e -> result.add(transform(e, ts)));
        return result.stream();
    }

}
