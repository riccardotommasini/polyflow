package custom.customdatatypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
 * This class is our 'R' data type, which is algebraically a monoid:
 *  - The identity element is the Empty Basket
 *  - The associative binary operation is the union of two fruit baskets
 */
public class FruitBasket implements Iterable<Fruit>{

    private List<Fruit> fruits = new ArrayList<>();


    public void addFruit(Fruit f){
        this.fruits.add(f);
    }

    public void addAll(FruitBasket basket){
        basket.forEach(f->fruits.add(f));
    }

    public int getSize(){
        return fruits.size();
    }

    @Override
    public Iterator<Fruit> iterator() {
        return fruits.iterator();
    }
}
