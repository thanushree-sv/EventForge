public class Volunteer extends Resource {
    private String role;

    public Volunteer(String name, double cost, String role) {
        super(name, cost);
        this.role = role;
    }

    public String getRole() { return role; }

    public String getType() { return "Volunteer"; }

    public String describe() {
        return super.describe() + " | Role: " + role;
    }
}
