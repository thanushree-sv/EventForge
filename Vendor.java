public class Vendor extends Resource {
    private String service;

    public Vendor(String name, double cost, String service) {
        super(name, cost);
        this.service = service;
    }

    public String getService() { return service; }

    public String getType() { return "Vendor"; }

    public String describe() {
        return super.describe() + " | Service: " + service;
    }
}
