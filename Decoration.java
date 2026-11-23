public class Decoration extends Resource {
    private String theme;

    public Decoration(String name, double cost, String theme) {
        super(name, cost);
        this.theme = theme;
    }

    public String getTheme() { return theme; }

    public String getType() { return "Decoration"; }

    public String describe() {
        return super.describe() + " | Theme: " + theme;
    }
}