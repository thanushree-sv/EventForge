// One need of the event (e.g. "Main Venue") with a primary and a backup resource.
// Implements two interfaces: Backupable and Reportable
public class Allocation implements Backupable, Reportable {
    public static final double RESERVE_RATE = 0.10;   // 10% fee to keep backup on standby

    private String role;
    private Resource primary;
    private Resource backup;
    private boolean backupActive;

    public Allocation(String role, Resource primary) {
        this.role = role;
        this.primary = primary;
        this.backup = null;
        this.backupActive = false;
    }

    public String getRole() { return role; }
    public Resource getPrimary() { return primary; }
    public Resource getBackup() { return backup; }
    public boolean isBackupActive() { return backupActive; }

    public boolean hasBackup() {
        return backup != null;
    }

    // Returns whichever resource is currently being used
    public Resource getActiveResource() {
        if (backupActive) {
            return backup;
        }
        return primary;
    }

    public void setBackup(Resource backup) throws InvalidBackupException {
        if (backup == primary) {
            throw new InvalidBackupException("Backup cannot be the same as the primary");
        }
        if (!backup.getType().equals(primary.getType())) {
            throw new InvalidBackupException("Backup must be of type " + primary.getType());
        }
        this.backup = backup;
    }

    public void activateBackup() throws NoBackupException {
        if (backup == null) {
            throw new NoBackupException("No backup planned for " + role);
        }
        if (!backup.isAvailable()) {
            throw new NoBackupException("Backup " + backup.getName() + " is also unavailable");
        }
        backupActive = true;
    }

    // Standby fee for keeping the backup reserved
    public double getBackupFee() {
        if (backup == null || backupActive) {
            return 0;
        }
        return backup.getCost() * RESERVE_RATE;
    }

    // Money committed right now
    public double getCommittedCost() {
        if (backupActive) {
            return backup.getCost();
        }
        return primary.getCost() + getBackupFee();
    }

    public double costIfBackupActivated() {
        if (backup == null) {
            return 0;
        }
        return backup.getCost();
    }

    public String summary() {
        String text = role.toUpperCase() + "\n";
        text = text + "     Primary : " + primary.describe() + "\n";
        if (backup == null) {
            text = text + "     Backup  : -- NONE (risk!) --\n";
        } else {
            text = text + "     Backup  : " + backup.describe() + "\n";
        }
        text = text + "     In use  : " + getActiveResource().getName();
        if (backupActive) {
            text = text + "  <== BACKUP ACTIVE";
        }
        text = text + "\n     Cost    : Rs." + getCommittedCost();
        return text;
    }
}
