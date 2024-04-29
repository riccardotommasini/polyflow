/*
package relational.content;

import graph.jena.content.EmptyContent;
import org.javatuples.Quartet;
import org.javatuples.Tuple;
import org.streamreasoning.rsp4j.api.secret.content.Content;
import org.streamreasoning.rsp4j.api.secret.content.ContentFactory;
import relational.datatypes.TableWrapper;

import java.util.Collections;

public class WindowContentFactory implements ContentFactory<Tuple, TableWrapper> {
    @Override
    public Content<Tuple, TableWrapper> createEmpty() {
        Tuple q = new Quartet<>(-1L, "Empty", -1, false);
        return new EmptyContent<>(new TableWrapper(Collections.singleton(q)));
    }

    @Override
    public Content<Tuple, TableWrapper> create() {
        return new WindowContent();
    }
}
*/
