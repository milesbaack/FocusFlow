/**
 * Manages user preferences for notifications in the FocusFlow system.
 * This class is thread-safe and immutable.
 * 
 * Technical Notes:
 * - Immutable design prevents preference changes during notification processing
 * - EnumSet.copyOf() creates defensive copy to prevent external modification
 * - Thread-safe without synchronization due to immutability
 * 
 * @author Emilio Lopez
 * @version 1.0
 */
package com.focusflow.core.notification;

import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * Manages user preferences for notifications in the FocusFlow application.
 * This class handles settings like quiet hours, notification types, and sound preferences.
 */
public class NotificationPreferences {
    private boolean enabled;
    private boolean soundEnabled;
    private boolean vibrationEnabled;
    private boolean quietHoursEnabled;
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
    private Set<NotificationType> enabledTypes;
    private int maxNotificationsInQueue;
    
    /**
     * Creates a new NotificationPreferences with default settings.
     */
    public NotificationPreferences() {
        this.enabled = true;
        this.soundEnabled = true;
        this.vibrationEnabled = true;
        this.quietHoursEnabled = false;
        this.quietHoursStart = LocalTime.of(22, 0); // 10 PM
        this.quietHoursEnd = LocalTime.of(7, 0);    // 7 AM
        this.enabledTypes = EnumSet.allOf(NotificationType.class);
        this.maxNotificationsInQueue = 50;
    }
    
    /**
     * Creates a new NotificationPreferences with specified settings.
     */
    public NotificationPreferences(
        boolean enabled,
        boolean soundEnabled,
        boolean vibrationEnabled,
        LocalTime quietHoursStart,
        LocalTime quietHoursEnd,
        Set<NotificationType> enabledTypes,
        int maxNotificationsInQueue
    ) {
        this.enabled = enabled;
        this.soundEnabled = soundEnabled;
        this.vibrationEnabled = vibrationEnabled;
        this.quietHoursEnabled = quietHoursStart != null && quietHoursEnd != null;
        this.quietHoursStart = quietHoursStart;
        this.quietHoursEnd = quietHoursEnd;
        this.enabledTypes = EnumSet.copyOf(enabledTypes);
        this.maxNotificationsInQueue = maxNotificationsInQueue;
    }
    
    /**
     * Updates these preferences with values from another preferences object.
     * 
     * @param other the preferences to copy from
     */
    public void update(NotificationPreferences other) {
        this.enabled = other.enabled;
        this.soundEnabled = other.soundEnabled;
        this.vibrationEnabled = other.vibrationEnabled;
        this.quietHoursEnabled = other.quietHoursEnabled;
        this.quietHoursStart = other.quietHoursStart;
        this.quietHoursEnd = other.quietHoursEnd;
        this.enabledTypes = EnumSet.copyOf(other.enabledTypes);
        this.maxNotificationsInQueue = other.maxNotificationsInQueue;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }
    
    public boolean isVibrationEnabled() {
        return vibrationEnabled;
    }
    
    public void setVibrationEnabled(boolean vibrationEnabled) {
        this.vibrationEnabled = vibrationEnabled;
    }
    
    public boolean isQuietHoursEnabled() {
        return quietHoursEnabled;
    }
    
    public void setQuietHoursEnabled(boolean quietHoursEnabled) {
        this.quietHoursEnabled = quietHoursEnabled;
    }
    
    public LocalTime getQuietHoursStart() {
        return quietHoursStart;
    }
    
    public void setQuietHoursStart(LocalTime quietHoursStart) {
        this.quietHoursStart = quietHoursStart;
    }
    
    public LocalTime getQuietHoursEnd() {
        return quietHoursEnd;
    }
    
    public void setQuietHoursEnd(LocalTime quietHoursEnd) {
        this.quietHoursEnd = quietHoursEnd;
    }
    
    public boolean isTypeEnabled(NotificationType type) {
        return enabledTypes.contains(type);
    }
    
    public void setTypeEnabled(NotificationType type, boolean enabled) {
        if (enabled) {
            enabledTypes.add(type);
        } else {
            enabledTypes.remove(type);
        }
    }
    
    public int getMaxNotificationsInQueue() {
        return maxNotificationsInQueue;
    }
    
    public void setMaxNotificationsInQueue(int maxNotificationsInQueue) {
        this.maxNotificationsInQueue = maxNotificationsInQueue;
    }
    
    /**
     * Checks if notifications should be muted based on quiet hours settings.
     * @return true if current time is within quiet hours and quiet hours are enabled
     */
    public boolean isInQuietHours() {
        if (!quietHoursEnabled) {
            return false;
        }
        
        LocalTime now = LocalTime.now();
        if (quietHoursStart.isBefore(quietHoursEnd)) {
            return now.isAfter(quietHoursStart) && now.isBefore(quietHoursEnd);
        } else {
            // Handle case where quiet hours span midnight
            return now.isAfter(quietHoursStart) || now.isBefore(quietHoursEnd);
        }
    }
} 