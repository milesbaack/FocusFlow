package com.focusflow.core.task;

import java.util.UUID;

/**
 * Interface for observers of task changes in the FocusFlow application.
 * 
 * Implementing this interface allows objects to be notified when tasks are
 * created, updated, or deleted. This provides support for event-driven
 * architecture within the application.
 * 
 * @author Miles Baack
 * @version 1.0
 * @see com.focusflow.core.task.Task
 * @see com.focusflow.core.task.TaskManager
 */
public interface TaskObserver {
    
    /**
     * Called when a new task is created.
     * 
     * @param taskId The UUID of the created task
     */
    void onTaskCreated(UUID taskId);
    
    /**
     * Called when a task is updated.
     * 
     * @param taskId The UUID of the updated task
     * @param propertyName The name of the property that was updated
     * @param oldValue The previous value of the property
     * @param newValue The new value of the property
     */
    void onTaskUpdated(UUID taskId, String propertyName, Object oldValue, Object newValue);
    
    /**
     * Called when a task is completed.
     * 
     * @param taskId The UUID of the completed task
     */
    void onTaskCompleted(UUID taskId);
    
    /**
     * Called when a task is deleted.
     * 
     * @param taskId The UUID of the deleted task
     */
    void onTaskDeleted(UUID taskId);
    
    /**
     * Called when a subtask is added to a task.
     * 
     * @param taskId The UUID of the parent task
     * @param subTaskId The UUID of the added subtask
     */
    void onSubTaskAdded(UUID taskId, UUID subTaskId);
    
    /**
     * Called when a subtask is updated.
     * 
     * @param taskId The UUID of the parent task
     * @param subTaskId The UUID of the updated subtask
     * @param propertyName The name of the property that was updated
     * @param oldValue The previous value of the property
     * @param newValue The new value of the property
     */
    void onSubTaskUpdated(UUID taskId, UUID subTaskId, String propertyName, Object oldValue, Object newValue);
    
    /**
     * Called when a subtask is completed.
     * 
     * @param taskId The UUID of the parent task
     * @param subTaskId The UUID of the completed subtask
     */
    void onSubTaskCompleted(UUID taskId, UUID subTaskId);
    
    /**
     * Called when a subtask is removed from a task.
     * 
     * @param taskId The UUID of the parent task
     * @param subTaskId The UUID of the removed subtask
     */
    void onSubTaskRemoved(UUID taskId, UUID subTaskId);
    
    /**
     * Base implementation that does nothing on all events.
     * This allows implementing classes to override only the methods they need.
     */
    class Adapter implements TaskObserver {
        @Override
        public void onTaskCreated(UUID taskId) {}
        
        @Override
        public void onTaskUpdated(UUID taskId, String propertyName, Object oldValue, Object newValue) {}
        
        @Override
        public void onTaskCompleted(UUID taskId) {}
        
        @Override
        public void onTaskDeleted(UUID taskId) {}
        
        @Override
        public void onSubTaskAdded(UUID taskId, UUID subTaskId) {}
        
        @Override
        public void onSubTaskUpdated(UUID taskId, UUID subTaskId, String propertyName, Object oldValue, Object newValue) {}
        
        @Override
        public void onSubTaskCompleted(UUID taskId, UUID subTaskId) {}
        
        @Override
        public void onSubTaskRemoved(UUID taskId, UUID subTaskId) {}
    }
} 