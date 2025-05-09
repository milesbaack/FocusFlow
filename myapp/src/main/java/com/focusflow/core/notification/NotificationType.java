/**
 * Represents the different types of notifications in the FocusFlow system.
 * Each type has specific use cases and may be handled differently by the UI.
 * 
 * @author Emilio Lopez
 * @version 1.0
 */

package com.focusflow.core.notification;

/**
 * Enum representing different types of notifications in the FocusFlow application.
 * Each type corresponds to a specific event or state change in the system.
 */
public enum NotificationType {
    // System notifications
    INFO("System Information"),
    WARNING("Warning"),
    ERROR("Error"),
    
    // Task-related notifications
    TASK_CREATED("Task Created"),
    TASK_UPDATED("Task Updated"),
    TASK_COMPLETED("Task Completed"),
    TASK_SELECTED("Task Selected"),
    
    // Session-related notifications
    SESSION_STARTED("Session Started"),
    SESSION_PAUSED("Session Paused"),
    SESSION_RESUMED("Session Resumed"),
    SESSION_ENDED("Session Ended"),
    SESSION_STOPPED("Session Stopped"),
    
    // Timer-related notifications
    TIMER_UPDATED("Timer Updated"),
    TIMER_RESET("Timer Reset"),
    BREAK_REMINDER("Break Reminder");
    
    private final String displayName;
    
    NotificationType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}