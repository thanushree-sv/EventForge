// Inheritance: Venue IS-A Resource
public class Venue extends Resource {
    private int capacity;

    public Venue(String name, double cost, int capacity) {
        super(name, cost);
        this.capacity = capacity;
    }

    public int getCapacity() { return capacity; }

    public String getType() { return "Venue"; }

    public String describe() {
        return super.describe() + " | Capacity: " + capacity;
    }
}
