package relational.operatorsimpl.r2r;

import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.querying.result.SolutionMapping;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;
import tech.tablesaw.api.Table;

import java.util.Collection;

public class R2RjtablesawImpl implements RelationToRelationOperator<Table> {

    long query;

    public R2RjtablesawImpl(long query){
        this.query = query;
    }
    @Override
    public Table evalUnary(Table dataset) {
        return dataset.where(dataset.longColumn("c1").isGreaterThan(query));
    }

    @Override
    public Table evalBinary(Table dataset1, Table dataset2) {
        dataset2.column(0).setName("t2.c1");
        dataset2.column(1).setName("t2.c2");
        dataset2.column(3).setName("t2.c4");
        return dataset1.joinOn("c3").inner(dataset2);
    }

    @Override
    public TimeVarying<Collection<Table>> apply(SDS<Table> sds) {
        return null;
    }

    @Override
    public SolutionMapping<Table> createSolutionMapping(Table result) {
        return null;
    }
}
