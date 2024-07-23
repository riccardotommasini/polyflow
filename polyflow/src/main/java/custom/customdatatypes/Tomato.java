package custom.customdatatypes;

/*
 * Yes, Tomatoes are considered a Fruit
 */
public class Tomato extends Fruit {

    public Tomato(float weight, String status) {
        super(weight, status);
    }

    @Override
    public String getName() {
        return "Tomato";
    }

    @Override
    public String toString(){
        return "name: Tomato, status: "+ getStatus() +", weight: "+  getWeight();
    }
}
