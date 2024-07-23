package custom.customdatatypes;

public class Banana extends Fruit {
    public Banana(float weight, String status) {
        super(weight, status);
    }

    @Override
    public String getName() {
        return "Banana";
    }

    @Override
    public String toString(){
        return "name: Banana, status: "+ getStatus() +", weight: "+  getWeight();
    }

}
