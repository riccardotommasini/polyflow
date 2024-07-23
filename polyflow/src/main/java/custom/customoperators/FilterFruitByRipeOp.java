package custom.customoperators;

import custom.customdatatypes.Fruit;
import custom.customdatatypes.FruitBasket;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;

import java.util.List;

public class FilterFruitByRipeOp implements RelationToRelationOperator<FruitBasket> {

    // Name of the operands (one operand in this case)
    List<String> tvgNames;
    //Name of the result
    String resName;
    //Attribute to filter out
    String query;

    public FilterFruitByRipeOp(String query, List<String> tvgNames, String resName){
        this.query = query;
        this.tvgNames = tvgNames;
        this.resName = resName;
    }

    @Override
    public FruitBasket eval(List<FruitBasket> datasets) {
        FruitBasket op = datasets.get(0);
        FruitBasket res = new FruitBasket();
        //Add only the fruits with a status different from the one passed to the query
        for(Fruit fruit : op){
            if(!fruit.getStatus().equals(query))
                res.addFruit(fruit);
        }
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
