/**
 * Interface for listening to notification events in the FocusFlow system.
 * Implementations can handle notifications as they are created, updated, or removed.
 * 
 * @author Emilio Lopez
 * @version 1.0
 */
package com.focusflow.core.notification;

/**
 * Interface for handling notification events.
 * This interface is used to receive notifications from the NotificationManager.
 */
public interface NotificationListener {
    /**
     * Called when a new notification is created.
     * 
     * @param notification the notification that was created
     */
    void onNotificationCreated(Notification notification);
    
    /**
     * Called when a notification is marked as read.
     * 
     * @param notification the notification that was marked as read
     */
    void onNotificationRead(Notification notification);
    
    /**
     * Called when all notifications are cleared.
     */
    void onNotificationsCleared();
} 