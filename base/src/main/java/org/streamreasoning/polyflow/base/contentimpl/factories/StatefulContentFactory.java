package org.streamreasoning.polyflow.base.contentimpl.factories;

import org.streamreasoning.polyflow.base.contentimpl.EmptyContent;
import org.streamreasoning.polyflow.api.secret.content.Content;
import org.streamreasoning.polyflow.api.secret.content.ContentFactory;
import org.streamreasoning.polyflow.base.contentimpl.content.StatefulContent;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class StatefulContentFactory<I, W, R> implements ContentFactory<I, W, R> {


    private Function<I, W> f1;
    private Function<W, R> f2;
    private BiFunction<R, R, R> sumR;
    private R emptyContent;
    /**
     * Function to create the state object
     */
    private Supplier<Object> stateCreator;
    /**
     * Function to update the state given the new received element
     */
    private BiFunction<W, Object, Object> stateUpdater;
    /**
     * Function to check if a condition on the state is met
     */
    private Function<Object, Boolean> conditionChecker;

    public StatefulContentFactory(Supplier<Object> stateCreator, BiFunction<W, Object, Object> stateUpdater, Function<Object, Boolean> conditionChecker, Function<I, W> f1,
                                  Function<W, R> f2, BiFunction<R, R, R> sumR, R emptyContent){
        this.stateCreator = stateCreator;
        this.stateUpdater = stateUpdater;
        this.conditionChecker = conditionChecker;
        this.f1 = f1;
        this.f2 = f2;
        this.sumR = sumR;
        this.emptyContent = emptyContent;
    }
    @Override
    public Content<I, W, R> createEmpty() {
        return new EmptyContent<>(emptyContent);
    }

    @Override
    public Content<I, W, R> create() {
        return new StatefulContent<>(stateCreator, stateUpdater, conditionChecker, f1, f2, sumR, emptyContent);
    }
}
