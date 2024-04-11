package relational.content;

import graph.jena.content.EmptyContent;
import org.javatuples.Quartet;
import org.streamreasoning.rsp4j.api.secret.content.Content;
import org.streamreasoning.rsp4j.api.secret.content.ContentFactory;
import relational.datatypes.TableWrapper;

import java.util.Collections;

public class WindowContentFactory implements ContentFactory<Quartet<Long, String, Integer, Boolean>, TableWrapper> {
    @Override
    public Content<Quartet<Long, String, Integer, Boolean>, TableWrapper> createEmpty() {
        Quartet<Long, String, Integer, Boolean> q = new Quartet<>(-1L, "Empty", -1, false);
        return new EmptyContent<>(new TableWrapper(Collections.singleton(q)));
    }

    @Override
    public Content<Quartet<Long, String, Integer, Boolean>, TableWrapper> create() {
        return new WindowContent();
    }
}
