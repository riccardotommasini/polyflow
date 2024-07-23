package custom.customdatatypes;

/*
 * Fruit Abstract class!
 * All of our custom Fruits will extend it.
 * We have methods to return the weight of the fruit, its name (banana, apple etc...) and its status (underripe, ripe, overripe)
 */
public abstract class Fruit {

    private float weight;
    private String status;

    public Fruit(float weight, String status){
        this.weight = weight;
        this.status = status;
    }


    public float getWeight() {
        return weight;
    }

    public String getName() {
        return "Fruit";
    }

    public String getStatus() {
        return status;
    }



}
