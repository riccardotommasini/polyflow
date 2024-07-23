package custom.customdatatypes;

public class Pineapple extends Fruit {
    public Pineapple(float weight, String status) {
        super(weight, status);
    }

    @Override
    public String getName() {
        return "Pineapple";
    }

    @Override
    public String toString(){
        return "name: Pineapple, status: "+ getStatus() +", weight: "+  getWeight();
    }
}
