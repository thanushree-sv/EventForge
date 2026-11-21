/** Interface: anything that supports a pre-planned backup resource. */
public interface Backupable {
    void setBackup(Resource backup) throws InvalidBackupException;
    Resource getBackup();
    boolean hasBackup();
    void activateBackup() throws NoBackupException;
}
