package custom.customdatatypes;

public class Pear extends Fruit {

    public Pear(float weight, String status) {
        super(weight, status);
    }

    @Override
    public String getName() {
        return "Pear";
    }

    @Override
    public String toString(){
        return "name: Pear, status: "+ getStatus() +", weight: "+  getWeight();
    }
}
