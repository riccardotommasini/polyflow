/*
package relational.content;

import org.javatuples.Quartet;
import org.javatuples.Tuple;
import org.streamreasoning.rsp4j.api.secret.content.Content;
import relational.datatypes.TableWrapper;

import java.util.HashSet;
import java.util.Set;

public class WindowContent implements Content<Tuple, TableWrapper> {

    Set<Tuple> rows = new HashSet<>();


    @Override
    public int size() {
        return rows.size();
    }

    @Override
    public void add(Tuple e) {
        rows.add(e);
    }

    @Override
    public Long getTimeStampLastUpdate() {
        return null;
    }

    @Override
    public TableWrapper coalesce() {
        return new TableWrapper(rows);
    }
}
*/
