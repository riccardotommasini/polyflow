package relational.datatypes;

import org.javatuples.Quartet;
import org.streamreasoning.rsp4j.api.operators.s2r.Convertible;
import tech.tablesaw.api.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class TableWrapper implements Convertible<Table> {

    Table table;

    public TableWrapper(Collection<Quartet<Long, String, Integer, Boolean>> rows){

        List<Long> column_1 = rows.stream().map(quartet -> quartet.getValue0()).collect(Collectors.toList());
        List<String> column_2 = rows.stream().map(quartet -> quartet.getValue1()).collect(Collectors.toList());
        List<Integer> column_3 = rows.stream().map(quartet -> quartet.getValue2()).collect(Collectors.toList());
        List<Boolean> column_4 = rows.stream().map(quartet -> quartet.getValue3()).collect(Collectors.toList());

        LongColumn c1 = LongColumn.create("c1");
        for(Long l : column_1){
            c1.append(l);
        }

        StringColumn c2 = StringColumn.create("c2");
        for(String s : column_2){
            c2.append(s);
        }

        IntColumn c3 = IntColumn.create("c3");
        for(Integer i : column_3){
            c3.append(i);
        }

        BooleanColumn c4 = BooleanColumn.create("c4");
        for(Boolean b : column_4){
            c4.append(b);
        }

        this.table = Table.create("window").addColumns(c1, c2, c3, c4);
    }


    @Override
    public void compute() {

    }

    @Override
    public Table convertToR() {
        return table;
    }
}
