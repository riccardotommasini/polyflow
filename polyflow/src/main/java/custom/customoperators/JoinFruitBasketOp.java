package custom.customoperators;

import custom.customdatatypes.FruitBasket;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;

import java.util.List;

public class JoinFruitBasketOp implements RelationToRelationOperator<FruitBasket> {
    List<String> tvgNames;
    String resName;

    public JoinFruitBasketOp(List<String> tvgNames, String resName){
        this.tvgNames = tvgNames;
        this.resName = resName;
    }

    @Override
    public FruitBasket eval(List<FruitBasket> datasets) {
        FruitBasket res = new FruitBasket();
        res.addAll(datasets.get(0));
        res.addAll(datasets.get(1));
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
