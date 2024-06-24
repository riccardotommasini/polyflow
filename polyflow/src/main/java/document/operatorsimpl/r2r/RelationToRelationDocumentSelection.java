package document.operatorsimpl.r2r;

import com.jayway.jsonpath.JsonPath;
import document.datatypes.DocumentCollection;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;

import java.util.ArrayList;
import java.util.List;

public class RelationToRelationDocumentSelection implements RelationToRelationOperator<DocumentCollection> {

    List<String> tvgNames;
    String resName;
    public RelationToRelationDocumentSelection(List<String> tvgNames, String resName){
        this.tvgNames = tvgNames;
        this.resName = resName;
    }

    @Override
    public DocumentCollection eval(List<DocumentCollection> datasets) {
        DocumentCollection res = new DocumentCollection();
        datasets.get(0).forEach(s->res.addElement(JsonPath.parse(s).read("$.name")));
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
