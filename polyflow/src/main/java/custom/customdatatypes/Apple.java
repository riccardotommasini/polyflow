package custom.customdatatypes;

public class Apple extends Fruit {
    public Apple(float weight, String status) {
        super(weight, status);
    }

    @Override
    public String getName() {
        return "Apple";
    }

    @Override
    public String toString(){
        return "name: Apple, status: "+ getStatus() +", weight: "+  getWeight();
    }
}
