package relational.datatypes;

import org.javatuples.Quartet;
import org.javatuples.Tuple;
import org.streamreasoning.rsp4j.api.operators.s2r.Convertible;
import tech.tablesaw.api.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class TableWrapper implements Convertible<Table> {

    Table table = Table.create();

    public TableWrapper(Collection<Tuple> rows){

        //Here it's hardcoded, a custom implementation of Tuples that can return the size and type of fields can help in make this code generic
        if(rows.isEmpty())
            return;
        int size = rows.iterator().next().getSize();
        if(size == 4) {
            List<Long> column_1 = rows.stream().map(quartet -> (Long) quartet.getValue(0)).collect(Collectors.toList());
            List<String> column_2 = rows.stream().map(quartet -> (String) quartet.getValue(1)).collect(Collectors.toList());
            List<Integer> column_3 = rows.stream().map(quartet -> (Integer) quartet.getValue(2)).collect(Collectors.toList());
            List<Boolean> column_4 = rows.stream().map(quartet -> (Boolean) quartet.getValue(3)).collect(Collectors.toList());

            LongColumn c1 = LongColumn.create("c1");
            for (Long l : column_1) {
                c1.append(l);
            }

            StringColumn c2 = StringColumn.create("c2");
            for (String s : column_2) {
                c2.append(s);
            }

            IntColumn c3 = IntColumn.create("c3");
            for (Integer i : column_3) {
                c3.append(i);
            }

            BooleanColumn c4 = BooleanColumn.create("c4");
            for (Boolean b : column_4) {
                c4.append(b);
            }

            this.table = Table.create("window_1").addColumns(c1, c2, c3, c4);
        }
        else if (size == 2){
            List<Long> column_1 = rows.stream().map(couple -> (Long) couple.getValue(0)).collect(Collectors.toList());
            List<String> column_2 = rows.stream().map(couple -> (String) couple.getValue(1)).collect(Collectors.toList());
            LongColumn c1 = LongColumn.create("c1");
            for (Long l : column_1) {
                c1.append(l);
            }
            StringColumn c2 = StringColumn.create("c2");
            for (String s : column_2) {
                c2.append(s);
            }
            this.table = Table.create("window_2").addColumns(c1, c2);

        }
    }


    @Override
    public void compute() {

    }

    @Override
    public Table convertToR() {
        return table;
    }
}
