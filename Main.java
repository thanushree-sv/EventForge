import java.util.ArrayList;
import java.util.Scanner;

// Console user interface for EventForge
public class Main {
    static Scanner sc = new Scanner(System.in);
    static ArrayList<Event> events = new ArrayList<Event>();

    public static void main(String[] args) {
        System.out.println("*** EventForge - Smart Event Planning & Backup Management ***");
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readInt("Choose: ");

            try {
                if (choice == 1) {
                    createEvent();
                } else if (choice == 2) {
                    addResource();
                } else if (choice == 3) {
                    assignBackup();
                } else if (choice == 4) {
                    viewPlan();
                } else if (choice == 5) {
                    reportUnavailable();
                } else if (choice == 6) {
                    activateBackup();
                } else if (choice == 7) {
                    loadDemo();
                } else if (choice == 0) {
                    System.out.println("Goodbye!");
                    running = false;
                } else {
                    System.out.println("Invalid option.");
                }
            } catch (BudgetExceededException e) {
                System.out.println("[BUDGET ERROR] " + e.getMessage());
            } catch (InvalidBackupException e) {
                System.out.println("[BACKUP ERROR] " + e.getMessage());
            } catch (NoBackupException e) {
                System.out.println("[NO BACKUP] " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("[INPUT ERROR] " + e.getMessage());
            }
        }
    }

    static void printMenu() {
        System.out.println();
        System.out.println("1. Create event          5. Report resource unavailable");
        System.out.println("2. Add primary resource  6. Activate backup");
        System.out.println("3. Assign backup         7. Load demo event");
        System.out.println("4. View plan & status    0. Exit");
    }

    static void createEvent() {
        String name = readLine("Event name: ");
        String date = readLine("Date: ");
        double budget = readDouble("Total budget (Rs.): ");
        events.add(new Event(name, date, budget));
        System.out.println("Event created.");
    }

    static void addResource() throws BudgetExceededException {
        Event e = selectEvent();
        if (e == null) {
            return;
        }
        String role = readLine("What is this for (e.g. Main Venue, Catering): ");
        Resource r = readResource("Primary");
        e.addAllocation(role, r);
        System.out.println("Added. Remember to assign a backup (option 3)!");
    }

    static void assignBackup() throws BudgetExceededException, InvalidBackupException {
        Event e = selectEvent();
        if (e == null) {
            return;
        }
        listItems(e);
        int index = readInt("Item number to back up: ") - 1;
        Resource r = readResource("Backup");
        e.assignBackup(index, r);
        System.out.println("Backup assigned.");
    }

    static void viewPlan() {
        Event e = selectEvent();
        if (e == null) {
            return;
        }
        System.out.println(e.summary());
    }

    static void reportUnavailable() {
        Event e = selectEvent();
        if (e == null) {
            return;
        }
        listItems(e);
        int index = readInt("Item number that failed: ") - 1;
        System.out.println(e.reportUnavailable(index));
    }

    static void activateBackup() throws NoBackupException, BudgetExceededException {
        Event e = selectEvent();
        if (e == null) {
            return;
        }
        listItems(e);
        int index = readInt("Item number to switch to backup: ") - 1;
        e.activateBackup(index);
        System.out.println("Backup activated!");
    }

    // Lets the user pick an event from the list
    static Event selectEvent() {
        if (events.size() == 0) {
            System.out.println("No events yet. Create one first.");
            return null;
        }
        if (events.size() == 1) {
            return events.get(0);
        }
        for (int i = 0; i < events.size(); i++) {
            System.out.println((i + 1) + ". " + events.get(i).getName());
        }
        int n = readInt("Select event: ");
        if (n < 1 || n > events.size()) {
            throw new IllegalArgumentException("Invalid event number");
        }
        return events.get(n - 1);
    }

    static void listItems(Event e) {
        int number = 1;
        for (Allocation a : e.getAllocations()) {
            System.out.println(number + ". " + a.getRole() + " -> " + a.getActiveResource().getName());
            number++;
        }
    }

    // Polymorphism: returns a Resource variable, but the real object
    // is a Venue, Vendor, Volunteer or Equipment
        static Resource readResource(String label) {
        System.out.println(label + " type: 1.Venue 2.Vendor 3.Volunteer 4.Equipment(tech) 5.Decoration");
        int type = readInt("Type: ");
        String name = readLine("Name: ");

        double cost;
        if (type == 4) {
            cost = readDouble("Cost per item (Rs.): ");
        } else {
            cost = readDouble("Cost (Rs.): ");
        }

        if (type == 1) {
            int capacity = readInt("Capacity: ");
            return new Venue(name, cost, capacity);
        } else if (type == 2) {
            String service = readLine("Service (Catering/Decor/...): ");
            return new Vendor(name, cost, service);
        } else if (type == 3) {
            String role = readLine("Role: ");
            return new Volunteer(name, cost, role);
        } else if (type == 4) {
            int qty = readInt("Quantity: ");
            return new Equipment(name, cost, qty);
        } else if (type == 5) {
            String theme = readLine("Theme (Floral/Balloon/Lighting/...): ");
            return new Decoration(name, cost, theme);
        } else {
            throw new IllegalArgumentException("Unknown resource type");
        }
    }
    static void loadDemo() {
        try {
            Event e = new Event("Tech Fest 2026", "15-Dec-2026", 200000);
            e.addAllocation("Main Venue", new Venue("Main Auditorium", 50000, 500));
            e.assignBackup(0, new Venue("Open Air Arena", 40000, 400));
            e.addAllocation("Catering", new Vendor("Spice Caterers", 60000, "Catering"));
            e.assignBackup(1, new Vendor("Royal Foods", 55000, "Catering"));
            e.addAllocation("Registration Desk", new Volunteer("Anita", 500, "Registration"));
            e.assignBackup(2, new Volunteer("Ravi", 500, "Registration"));
            e.addAllocation("Projectors", new Equipment("HD Projector", 2000, 4));
            e.addAllocation("Stage Decor", new Decoration("Floral Stage Setup", 15000, "Floral"));
            e.assignBackup(4, new Decoration("Balloon Arch Setup", 12000, "Balloon"));
            e.assignBackup(3, new Equipment("Spare Projector", 2000, 2));
            events.add(e);
            System.out.println("Demo event loaded: " + e.getName());
        } catch (Exception ex) {
            System.out.println("Demo failed: " + ex.getMessage());
        }
    }

    // ----- input helpers (exception handling for bad numbers) -----
    static String readLine(String prompt) {
        System.out.print(prompt);
        if (!sc.hasNextLine()) {
            System.exit(0);   // no more input, stop the program
        }
        return sc.nextLine().trim();
    }

    static int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    static double readDouble(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}