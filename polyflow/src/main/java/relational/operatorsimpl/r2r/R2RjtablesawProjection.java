package relational.operatorsimpl.r2r;

import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.querying.result.SolutionMapping;
import org.streamreasoning.rsp4j.api.sds.SDS;
import org.streamreasoning.rsp4j.api.sds.timevarying.TimeVarying;
import tech.tablesaw.api.Table;

import java.util.Collection;
import java.util.List;

public class R2RjtablesawProjection implements RelationToRelationOperator<Table> {

    CustomRelationalQuery query;

    private List<String> tvgNames;

    private String resName;



    public R2RjtablesawProjection(CustomRelationalQuery query, List<String> tvgNames, String resName){
        this.query = query;
        this.tvgNames = tvgNames;
        this.resName = resName;

    }
    @Override
    public Table eval(List<Table> dataset) {
        Table res = dataset.get(0);
        if(!res.isEmpty())
            return res.selectColumns(query.projectionColumns);
        return res;
    }


    @Override
    public List<String> getTvgNames() {
        return tvgNames;
    }

    @Override
    public String getResName() {
        return resName;
    }


}
