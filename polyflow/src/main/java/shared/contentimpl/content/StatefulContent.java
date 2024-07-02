package shared.contentimpl.content;

import org.streamreasoning.rsp4j.api.secret.content.Content;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class StatefulContent<I, W, R> implements Content<I, W, R> {


    private Object state;
    private List<W> content = new ArrayList<>();

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

    private Function<I, W> f1;
    private Function<W, R> f2;
    private BiFunction<R,R, R> sumR;


    public StatefulContent(Supplier<Object> stateCreator, BiFunction<W, Object, Object> stateUpdater, Function<Object, Boolean> conditionChecker, Function<I, W> f1,
                           Function<W, R> f2, BiFunction<R, R, R> sumR, R emptyContent){
        this.stateCreator = stateCreator;
        this.stateUpdater = stateUpdater;
        this.conditionChecker = conditionChecker;
        this.state = stateCreator.get();
        this.f1 = f1;
        this.f2 = f2;
        this.sumR = sumR;
        this.emptyContent = emptyContent;
    }

    @Override
    public int size() {
        return content.size();
    }

    @Override
    public void add(I e) {
        W el = f1.apply(e);
        state = stateUpdater.apply(el, state);
        //Here it's an implementation choice to NOT add the element that made the content ready to be reported
        if(!toReport())
            content.add(el);
    }

    @Override
    public R coalesce() {
        return content.stream().map(f2).reduce(emptyContent,  (x, y) -> sumR.apply(x,y));
    }

    @Override
    public boolean toReport(){
        return conditionChecker.apply(state);
    }
}
