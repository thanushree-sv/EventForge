// Abstract base class for everything an event needs.
// Shows: abstraction, encapsulation (private fields + getters/setters)
public abstract class Resource {
    private static int counter = 1;   // static: shared by all objects, used for auto IDs
    private int id;
    private String name;
    private double cost;
    private boolean available;

    public Resource(String name, double cost) {
        this.id = counter;
        counter++;
        this.available = true;
        setName(name);
        setCost(cost);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getCost() { return cost; }
    public boolean isAvailable() { return available; }

    public void setName(String name) {
        if (name == null || name.trim().equals("")) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.name = name.trim();
    }

    public void setCost(double cost) {
        if (cost < 0) {
            throw new IllegalArgumentException("Cost cannot be negative");
        }
        this.cost = cost;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
    // Formats money with 2 decimal places, e.g. Rs.50000.00
    public static String money(double amount) {
        return "Rs." + String.format("%.2f", amount);
    }

    // Abstract method: every subclass MUST write its own version
    public abstract String getType();

    // Subclasses override this and add their own details (polymorphism)
    public String describe() {
        String status = "Available";
        if (!available) {
            status = "UNAVAILABLE";
        }
        return "[" + getType() + " #" + id + "] " + name + " - " + money(getCost()) + " (" + status + ")";    }
}
