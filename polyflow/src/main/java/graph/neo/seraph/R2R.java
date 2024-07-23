package graph.neo.seraph;

public class R2R {

    public String query;

    public R2R(String query) {
        this.query = query;
    }

    @Override
    public String toString() {
        return query;
    }
}
