/** Enum: overall health of an event plan. */
public enum EventStatus {
    FULLY_PROTECTED("All resources have a ready backup"),
    INCOMPLETE_BACKUPS("Some resources have NO backup yet"),
    RUNNING_ON_BACKUP("One or more backups are currently active"),
    ACTION_REQUIRED("A resource is unavailable - activate its backup!");

    private final String message;
    EventStatus(String message) { this.message = message; }
    public String getMessage() { return message; }
}
