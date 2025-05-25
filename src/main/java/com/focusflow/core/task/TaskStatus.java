package com.focusflow.core.task;

/**
 * Represents the status of a task in the FocusFlow application.
 * 
 * This enum provides a richer set of status values beyond simple completion,
 * including states like OVERDUE, IN_PROGRESS, and POSTPONED. Each status
 * includes a display name and a color code for UI representation.
 * 
 * @author Miles Baack
 * @version 1.0
 * @see com.focusflow.core.task.Task
 */
public enum TaskStatus {
    /**
     * Task that has not been started and is not yet due.
     */
    NOT_STARTED("Not Started", "#808080"), // Gray

    /**
     * Task that is in progress but not yet completed.
     */
    IN_PROGRESS("In Progress", "#3498db"), // Blue

    /**
     * Task that is completed.
     */
    COMPLETED("Completed", "#2ecc71"), // Green

    /**
     * Task that is past its due date but not completed.
     */
    OVERDUE("Overdue", "#e74c3c"), // Red

    /**
     * Task that is due today.
     */
    DUE_TODAY("Due Today", "#f1c40f"), // Yellow

    /**
     * Task that is due soon (within a configurable timeframe).
     */
    DUE_SOON("Due Soon", "#f39c12"), // Orange

    /**
     * Task that has been postponed.
     */
    POSTPONED("Postponed", "#9b59b6"), // Purple

    /**
     * Task that has been canceled.
     */
    CANCELED("Canceled", "#7f8c8d"); // Slate

    private final String displayName;
    private final String colorCode;

    /**
     * Constructs a TaskStatus with the specified display name and color code.
     * 
     * @param displayName The human-readable name for the status
     * @param colorCode   The hex color code for UI representation
     */
    TaskStatus(String displayName, String colorCode) {
        this.displayName = displayName;
        this.colorCode = colorCode;
    }

    /**
     * Gets the display name of this status.
     * 
     * @return The human-readable name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the color code of this status.
     * 
     * @return The hex color code for UI representation
     */
    public String getColorCode() {
        return colorCode;
    }

    /**
     * Returns the display name of this status.
     * 
     * @return The display name
     */
    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Determines the appropriate status for a task based on its properties.
     * 
     * @param task The task to evaluate
     * @return The appropriate TaskStatus
     */
    public static TaskStatus fromTask(Task task) {
        if (task == null) {
            return null;
        }

        if (task.isComplete()) {
            return COMPLETED;
        }

        // Check for custom status flags (implemented in Task class)
        if (task instanceof TaskWithStatus) {
            TaskWithStatus taskWithStatus = (TaskWithStatus) task;
            if (taskWithStatus.isPostponed()) {
                return POSTPONED;
            }
            if (taskWithStatus.isCanceled()) {
                return CANCELED;
            }
            if (taskWithStatus.isInProgress()) {
                return IN_PROGRESS;
            }
        }

        // Check due date related statuses
        if (!task.hasDueDateTime()) {
            return NOT_STARTED;
        }

        // This logic should use DateTimeUtils in a real implementation
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime dueDateTime = task.getDueDateTime();

        if (now.isAfter(dueDateTime)) {
            return OVERDUE;
        }

        if (dueDateTime.toLocalDate().equals(now.toLocalDate())) {
            return DUE_TODAY;
        }

        if (dueDateTime.isBefore(now.plusDays(3))) {
            return DUE_SOON;
        }

        return NOT_STARTED;
    }

    /**
     * Interface to be implemented by Task classes that support advanced status
     * tracking.
     */
    public interface TaskWithStatus {
        boolean isPostponed();

        boolean isCanceled();

        boolean isInProgress();
    }
}