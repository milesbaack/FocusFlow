/**
 * Represents a notification in the FocusFlow system.
 * This class is immutable and thread-safe by design.
 * 
 * Technical Notes:
 * - Immutable design eliminates need for synchronization
 * - All fields are final to ensure thread safety
 * - withReadStatus() creates new instance instead of modifying state
 * 
 * @author Emilio Lopez
 * @version 1.0
 */

package com.focusflow.core.notification;

import java.time.LocalDateTime;
import java.util.UUID;

public class Notification {
    private final UUID id;
    private final String title;
    private final String message;
    private final NotificationType type;
    private final LocalDateTime timestamp;
    private final String taskId;
    private final boolean isRead;

    /**
     * Constructs a new Notification with the specified parameters.
     *
     * @param title The title of the notification
     * @param message The detailed message of the notification
     * @param type The type of notification
     * @param taskId Optional task ID associated with this notification (can be null)
     */
    public Notification(String title, String message, NotificationType type, String taskId) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.taskId = taskId;
        this.isRead = false;
    }

    /**
     * Creates a new Notification with the read status changed.
     * Since Notification is immutable, this returns a new instance.
     *
     * @param read The new read status
     * @return A new Notification instance with updated read status
     */
    public Notification withReadStatus(boolean read) {
        return new Notification(this.title, this.message, this.type, this.taskId, this.id, this.timestamp, read);
    }

    // Private constructor for creating notifications with specific ID and timestamp
    private Notification(String title, String message, NotificationType type, String taskId, 
                        UUID id, LocalDateTime timestamp, boolean isRead) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
        this.taskId = taskId;
        this.isRead = isRead;
    }

    /**
     * Gets the unique identifier of this notification.
     * @return The UUID of this notification
     */
    public UUID getId() {
        return id;
    }

    /**
     * Gets the title of this notification.
     * @return The notification title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the message content of this notification.
     * @return The notification message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the type of this notification.
     * @return The notification type
     */
    public NotificationType getType() {
        return type;
    }

    /**
     * Gets the timestamp when this notification was created.
     * @return The creation timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the associated task ID, if any.
     * @return The task ID or null if none
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * Checks if this notification has been read.
     * @return true if the notification has been read, false otherwise
     */
    public boolean isRead() {
        return isRead;
    }

    @Override
    public String toString() {
        return String.format("Notification{id='%s', title='%s', message='%s', type=%s, timestamp=%s, taskId=%s, isRead=%s}",
                id, title, message, type, timestamp, taskId, isRead);
    }
}