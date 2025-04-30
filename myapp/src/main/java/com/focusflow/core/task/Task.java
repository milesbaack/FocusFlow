/**
 // TODO: Fix JavaDoc
 * Task handling 
 * @author Emilio Lopez
 * @version 1.0
 */

package com.focusflow.core.task;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// TODO: Check how to illustrate return type
/**
 * Task class that implements Serializable for ...
 */
public class Task implements Serializable{
    // Task ID
    private final UUID id;

    // Title, description, priority, isCompleted 
    private String title;
    private String description;
    private Priority priority;
    private boolean completed;

    // Date created, due date
    private LocalDateTime creationDate;
    private LocalDateTime dueDate;

    // Create checklist
    private List<ChecklistItem> checklistItems = new ArrayList<>();

    // Public class to represent group of priorities
    public enum Priority{
        LOW, MEDIUM, HIGH
    }

    /**
     * Public constructor for Task
     * @param title Task title
     */
    // TODO: Priority should be one of the inputs
    // TODO: Select new time for task.
    public Task(String title){
        this.id = UUID.randomUUID();
        this.title = title;
        this.creationDate = LocalDateTime.now();
        this.priority = Priority.MEDIUM;
        this.completed = false;
    }
    
    /**
     * Get Task ID
     * @return UUID of task
     */
    public UUID getId(){
        return id;
    }

    /**
     * Get task title
     * @return Task title
     */
    public String getTitle(){
        return title;
    }

    /**
     * Set task title
     * @param title String containing task title
     */
    public void setTitle(String title){
        this.title = title;
    }

    /**
     * Returns whether this task is completed or not.
     * @return {@code true} if the task is completed; {@code false} otherwise
     */
    public boolean isCompleted(){
        return completed;
    }

    // TODO: Check logic for this
    /**
     * Set task completion status
     * @param completed {@code true} if task is completed; {@code false} otherwise
     */
    public void setCompleted(boolean completed){
        this.completed = completed;
    }

    // TODO: Java Doc for this ( ? ) 
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Append the task title
        sb.append("Task: ").append(title);

        // Append the description if present
        if (description != null && !description.isEmpty()) {
            sb.append("\nDescription: ").append(description);
        }

        // Append the priority
        sb.append("\nPriority: ").append(priority);

        // Append the completion status
        sb.append("\nStatus: ").append(completed ? "Completed" : "Pending");

        // Append the creation date
        sb.append("\nCreated: ").append(creationDate);

        // Append the due date if present
        if (dueDate != null) {
            sb.append("\nDue: ").append(dueDate);
        }

        // Append checklist items if any exist
        if (checklistItems != null && !checklistItems.isEmpty()) {
            sb.append("\nChecklist:");
            for (int i = 0; i < checklistItems.size(); i++) {
                ChecklistItem item = checklistItems.get(i);
                // Format: 1. [x] Item text
                sb.append(String.format("\n  %d. [%s] %s",
                    i + 1,
                    item.isChecked() ? "x" : " ",
                    item.getText()));
            }
        }

        return sb.toString();
    }
}
