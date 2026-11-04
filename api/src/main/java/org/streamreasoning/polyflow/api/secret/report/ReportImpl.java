package org.streamreasoning.polyflow.api.secret.report;


import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.Window;
import org.streamreasoning.polyflow.api.operators.s2r.execution.state.Segment;
import org.streamreasoning.polyflow.api.secret.report.strategies.ReportingStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ReportImpl implements Report {

    List<ReportingStrategy> strategies = new ArrayList<>();

    @Override
    public boolean report(Window w, Segment<?, ?> c, long tapp, long tsys) {
        return strategies.stream().allMatch(strategy -> strategy.match(w, c, tapp, tsys));
    }

    /** Evaluate in the original order, constructing content at most once per check. */
    @Override
    public boolean reportLazy(Window w, Supplier<? extends Segment<?, ?>> content, long tapp, long tsys) {
        // A legacy subclass may override report(); preserve that custom decision path.
        if (getClass() != ReportImpl.class) {
            return Report.super.reportLazy(w, content, tapp, tsys);
        }
        Segment<?, ?> segment = null;
        boolean materialized = false;
        for (ReportingStrategy strategy : strategies) {
            if (strategy.requiresContent() && !materialized) {
                segment = content.get();
                materialized = true;
            }
            // Preserve short-circuiting: time strategies can update their internal clocks.
            if (!strategy.match(w, segment, tapp, tsys)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void add(ReportingStrategy r) {
        strategies.add(r);
    }

    @Override
    public ReportingStrategy[] strategies() {
        return strategies.toArray(new ReportingStrategy[strategies.size()]);
    }


    public static Report fromStrategies(ReportingStrategy... strategies){
        Report report = new ReportImpl();
        Arrays.stream(strategies).forEach(s -> report.add(s));
        return report;
    }

}
