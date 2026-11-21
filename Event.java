import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

// An event with a fixed budget and a list of allocations
public class Event implements Reportable {
    private String name;
    private String date;
    private double budget;
    private ArrayList<Allocation> allocations = new ArrayList<Allocation>();

    public Event(String name, String date, double budget) {
        if (budget <= 0) {
            throw new IllegalArgumentException("Budget must be positive");
        }
        this.name = name;
        this.date = date;
        this.budget = budget;
    }

    public String getName() { return name; }
    public double getBudget() { return budget; }
    public ArrayList<Allocation> getAllocations() { return allocations; }

    public double getTotalCost() {
        double total = 0;
        for (Allocation a : allocations) {
            total = total + a.getCommittedCost();
        }
        return total;
    }

    public double getRemainingBudget() {
        return budget - getTotalCost();
    }

    private Allocation getAllocation(int index) {
        if (index < 0 || index >= allocations.size()) {
            throw new IllegalArgumentException("Invalid item number");
        }
        return allocations.get(index);
    }

    public void addAllocation(String role, Resource primary) throws BudgetExceededException {
        Allocation a = new Allocation(role, primary);
        if (getTotalCost() + a.getCommittedCost() > budget) {
            throw new BudgetExceededException("Not enough budget! Need Rs."
                    + a.getCommittedCost() + " but only Rs." + getRemainingBudget() + " left");
        }
        allocations.add(a);
    }

    public void assignBackup(int index, Resource backup)
            throws BudgetExceededException, InvalidBackupException {
        Allocation a = getAllocation(index);
        double extraFee = backup.getCost() * Allocation.RESERVE_RATE - a.getBackupFee();
        if (getTotalCost() + extraFee > budget) {
            throw new BudgetExceededException("Backup standby fee Rs." + extraFee
                    + " is more than remaining budget Rs." + getRemainingBudget());
        }
        a.setBackup(backup);
    }

    // Simulates a failure: the resource in use becomes unavailable
    public String reportUnavailable(int index) {
        Allocation a = getAllocation(index);
        a.getActiveResource().setAvailable(false);
        String message = a.getActiveResource().getName() + " is now UNAVAILABLE.";
        if (a.hasBackup()) {
            message = message + " Backup '" + a.getBackup().getName() + "' is ready to activate.";
        } else {
            message = message + " WARNING: no backup planned!";
        }
        return message;
    }

    public void activateBackup(int index) throws NoBackupException, BudgetExceededException {
        Allocation a = getAllocation(index);
        double newTotal = getTotalCost() - a.getCommittedCost() + a.costIfBackupActivated();
        if (a.hasBackup() && newTotal > budget) {
            throw new BudgetExceededException("Switching to backup would exceed the budget!");
        }
        a.activateBackup();
    }

    public EventStatus getStatus() {
        boolean missingBackup = false;
        boolean onBackup = false;
        for (Allocation a : allocations) {
            if (!a.getActiveResource().isAvailable()) {
                return EventStatus.ACTION_REQUIRED;
            }
            if (!a.hasBackup()) {
                missingBackup = true;
            }
            if (a.isBackupActive()) {
                onBackup = true;
            }
        }
        if (missingBackup) {
            return EventStatus.INCOMPLETE_BACKUPS;
        }
        if (onBackup) {
            return EventStatus.RUNNING_ON_BACKUP;
        }
        return EventStatus.FULLY_PROTECTED;
    }

    public String summary() {
        String text = "==================================================\n";
        text = text + " EVENT: " + name + "   (Date: " + date + ")\n";
        text = text + "==================================================\n";

        if (allocations.size() == 0) {
            text = text + " (no resources allocated yet)\n";
        }
        int number = 1;
        for (Allocation a : allocations) {
            text = text + " " + number + ". " + a.summary() + "\n\n";
            number++;
        }

        // TreeMap: keeps keys (resource types) sorted
        Map<String, Double> costByType = new TreeMap<String, Double>();
        for (Allocation a : allocations) {
            String type = a.getActiveResource().getType();
            double cost = a.getCommittedCost();
            if (costByType.containsKey(type)) {
                cost = cost + costByType.get(type);
            }
            costByType.put(type, cost);
        }

        text = text + "--------------- COST BREAKDOWN ---------------\n";
        for (String type : costByType.keySet()) {
            text = text + " " + type + ": Rs." + costByType.get(type) + "\n";
        }
        text = text + " Budget: Rs." + budget + " | Spent: Rs." + getTotalCost()
                + " | Left: Rs." + getRemainingBudget() + "\n";
        text = text + " STATUS : " + getStatus() + " - " + getStatus().getMessage() + "\n";
        return text;
    }
}
