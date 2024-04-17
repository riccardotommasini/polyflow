package relational.operatorsimpl.r2r;

import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.querying.result.SolutionMapping;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class R2RjtablesawImpl implements RelationToRelationOperator<Table> {

    long query;

    private List<String> tvgNames;

    private boolean isBinary;

    private String unaryOpName;
    private String binaryOpName;

    public R2RjtablesawImpl(long query, List<String> tvgNames, boolean isBinary, String unaryOpName, String binaryOpName){
        this.query = query;
        this.tvgNames = tvgNames;
        this.isBinary = isBinary;
        this.unaryOpName = unaryOpName;
        this.binaryOpName = binaryOpName;
    }

    @Override
    public List<String> getTvgNames() {
        return tvgNames;
    }

    @Override
    public boolean isBinary() {
        return isBinary;
    }

    @Override
    public String getUnaryOpName() {
        return unaryOpName;
    }

    @Override
    public String getBinaryOpName() {
        return binaryOpName;
    }

    @Override
    public Table evalUnary(Table dataset) {
        if(dataset.isEmpty())
            return dataset;
        return dataset.where(dataset.intColumn("c3").isGreaterThan(query));
    }

    @Override
    public Table evalBinary(Table dataset1, Table dataset2) {
        if(dataset1.isEmpty())
            return dataset1;
        if(dataset2.isEmpty())
            return dataset2;
        dataset2.column(1).setName("t2.c2");
        return dataset1.joinOn("c1").inner(dataset2);

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
