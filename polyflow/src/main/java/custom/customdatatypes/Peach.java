package custom.customdatatypes;

public class Peach extends Fruit {
    public Peach(float weight, String status) {
        super(weight, status);
    }

    @Override
    public String getName() {
        return "Peach";
    }

    @Override
    public String toString(){
        return "name: Peach, status: "+ getStatus() +", weight: "+  getWeight();
    }
}
