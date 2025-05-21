/**
 * Manages notifications in the FocusFlow system.
 * This class is thread-safe and handles notification queuing, delivery, and listener notifications.
 * 
 * Technical Notes:
 * - Uses ConcurrentLinkedQueue for lock-free thread-safe operations
 * - CopyOnWriteArrayList prevents ConcurrentModificationException during iteration
 * - AtomicReference ensures atomic preference updates across threads
 * 
 * @author Emilio Lopez
 * @version 1.0
 */
package com.focusflow.core.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class NotificationManager {
    // Thread-safe queue for notifications
    // ConcurrentLinkedQueue provides lock-free thread-safe operations
    // Ideal for high-concurrency scenarios with multiple producers/consumers
    private final Queue<Notification> notificationQueue;
    
    // Thread-safe list for listeners using copy-on-write semantics
    // Creates a new copy of the array on each modification
    // Perfect for scenarios where reads vastly outnumber writes
    private final List<NotificationListener> listeners;
    
    // Atomic reference for thread-safe preference updates
    // Ensures all threads see the same preference state
    // Prevents race conditions during preference updates
    private final NotificationPreferences preferences;

    /**
     * Creates a new NotificationManager with default preferences.
     */
    public NotificationManager() {
        this.notificationQueue = new ConcurrentLinkedQueue<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.preferences = new NotificationPreferences();
    }

    /**
     * Sends a notification if it meets the current preferences criteria.
     * This method is thread-safe and can be called from any thread.
     *
     * @param notification The notification to send
     * @return true if the notification was queued, false if it was filtered out
     */
    public boolean sendNotification(Notification notification) {
        if (shouldSendNotification(notification)) {
            notificationQueue.offer(notification);
            notifyListeners(listener -> listener.onNotificationCreated(notification));
            return true;
        }
        return false;
    }

    /**
     * Updates the preferences for this notification manager.
     * This method is thread-safe.
     *
     * @param newPreferences The new preferences to use
     */
    public void updatePreferences(NotificationPreferences newPreferences) {
        preferences.update(newPreferences);
    }

    /**
     * Adds a listener for notification events.
     * This method is thread-safe.
     *
     * @param listener The listener to add
     */
    public void addListener(NotificationListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener for notification events.
     * This method is thread-safe.
     *
     * @param listener The listener to remove
     */
    public void removeListener(NotificationListener listener) {
        listeners.remove(listener);
    }

    /**
     * Marks a notification as read.
     * This method is thread-safe.
     *
     * @param notification The notification to mark as read
     */
    public void markAsRead(Notification notification) {
        Notification readNotification = notification.withReadStatus(true);
        notifyListeners(listener -> listener.onNotificationRead(readNotification));
    }

    /**
     * Clears all notifications from the queue.
     * This method is thread-safe.
     */
    public void clearNotifications() {
        notificationQueue.clear();
        
        // Notify listeners
        notifyListeners(NotificationListener::onNotificationsCleared);
    }

    /**
     * Clears all notifications and notifies listeners.
     */
    public void clearAllNotifications() {
        notificationQueue.clear();
        notifyListeners(NotificationListener::onNotificationsCleared);
    }

    /**
     * Gets all pending notifications.
     * This method is thread-safe.
     *
     * @return A new list containing all pending notifications
     */
    public List<Notification> getNotifications() {
        return new ArrayList<>(notificationQueue);
    }

    /**
     * Gets the current notification preferences.
     * This method is thread-safe.
     *
     * @return The current preferences
     */
    public NotificationPreferences getPreferences() {
        return preferences;
    }

    private boolean shouldSendNotification(Notification notification) {
        // Check if notifications are enabled
        if (!preferences.isEnabled()) {
            return false;
        }
        
        // Check if the notification type is enabled
        if (!preferences.isTypeEnabled(notification.getType())) {
            return false;
        }
        
        // Check quiet hours
        if (preferences.isQuietHoursEnabled() && preferences.isInQuietHours()) {
            return false;
        }
        
        return true;
    }

    private void notifyListeners(java.util.function.Consumer<NotificationListener> action) {
        for (NotificationListener listener : listeners) {
            try {
                action.accept(listener);
            } catch (Exception e) {
                // Log error but continue notifying other listeners
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }
} 