public class Equipment extends Resource {
    private int quantity;

    public Equipment(String name, double unitCost, int quantity) {
        super(name, unitCost);
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = quantity;
    }

    // Method overriding (polymorphism): total cost = unit cost x quantity
    public double getCost() {
        return super.getCost() * quantity;
    }

    public String getType() { return "Equipment"; }

    public String describe() {
        return super.describe() + " | Qty: " + quantity;
    }
}
