package com.focusflow.core.gameify;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.focusflow.core.task.Task;

/**
 * Represents a Quest which acts as a container for multiple related Tasks.
 * Similar to a Project, a Quest is completed when all of its Tasks are completed.
 * Quests award XP based on their complexity (number of tasks and subtasks).
 */
public class Quest {
    private final UUID id;
    private String title;
    private String description;
    private final List<Task> tasks;
    private boolean completed;
    private final Achievement rewardAchievement; // Optional achievement reward
    private int baseXpReward;

    /**
     * Creates a new Quest with the specified title, description, achievement reward and base XP.
     * 
     * @param title The title of the quest
     * @param description The description of the quest
     * @param rewardAchievement The achievement to award when completed (can be null)
     * @param baseXpReward The base XP reward for completing this quest
     */
    public Quest(String title, String description, Achievement rewardAchievement, int baseXpReward) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.tasks = new ArrayList<>();
        this.completed = false;
        this.rewardAchievement = rewardAchievement;
        this.baseXpReward = baseXpReward;
    }

    /**
     * Adds a task to this quest
     * 
     * @param task The task to add
     * @return true if added successfully
     */
    public boolean addTask(Task task) {
        if (task != null) {
            tasks.add(task);
            return true;
        }
        return false;
    }

    /**
     * Removes a task from this quest
     * 
     * @param task The task to remove
     * @return true if removed successfully
     */
    public boolean removeTask(Task task) {
        return tasks.remove(task);
    }
    
    /**
     * Removes a task from this quest by its index
     * 
     * @param index The index of the task to remove
     * @return The removed task, or null if index is invalid
     */
    public Task removeTask(int index) {
        if (index >= 0 && index < tasks.size()) {
            return tasks.remove(index);
        }
        return null;
    }

    /**
     * Updates the completion status of the quest based on its tasks
     * 
     * @return true if the quest is now complete
     */
    public boolean updateCompletionStatus() {
        // Quest is complete if all tasks are complete
        if (!completed && !tasks.isEmpty()) {
            boolean allTasksCompleted = true;
            for (Task task : tasks) {
                if (!task.isComplete()) {
                    allTasksCompleted = false;
                    break;
                }
            }

            if (allTasksCompleted) {
                this.completed = true;
                return true; // Quest newly completed
            }
        }
        return false; // No change in completion status
    }
    
    /**
     * Marks the quest as completed regardless of task status
     * 
     * @return true if quest is successfully marked as completed
     */
    public boolean markAsCompleted() {
        if (!completed) {
            this.completed = true;
            return true; // Quest newly completed
        }
        return false; // No change in completion status
    }
    /**
     * Gets the current progress percentage of the quest
     * 
     * @return Percentage from 0-100
     */
    public int getProgressPercentage() {
        if (tasks.isEmpty()) {
            return 0;
        }
        
        int completedTasks = 0;
        for (Task task : tasks) {
            if (task.isComplete()) {
                completedTasks++;
            }
        }
        
        return (int)((double)completedTasks / tasks.size() * 100);
    }
    
    /**
     * Calculates the total XP reward for completing this quest
     * based on the number of tasks and subtasks
     * 
     * @return Total XP reward value
     */
    public int calculateXpReward() {
        int taskCount = tasks.size();
        int subtaskCount = 0;
        
        // Count all subtasks across all tasks
        for (Task task : tasks) {
            subtaskCount += task.getSubtasks().size();
        }
        
        // Formula: Base XP + (Task Bonus * Number of Tasks) + (Subtask Bonus * Number of Subtasks)
        int taskBonus = 10;  // XP per task
        int subtaskBonus = 2; // XP per subtask
        
        return baseXpReward + (taskBonus * taskCount) + (subtaskBonus * subtaskCount);
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Task> getTasks() {
        return new ArrayList<>(tasks); // Return a copy for safety
    }
    
    /**
     * Gets a task by index
     * 
     * @param index The index of the task
     * @return The task, or null if index is invalid
     */
    public Task getTask(int index) {
        if (index >= 0 && index < tasks.size()) {
            return tasks.get(index);
        }
        return null;
    }
    
    /**
     * Gets a task by its ID
     * 
     * @param taskId The ID of the task to find
     * @return The task, or null if not found
     */
    public Task getTaskById(UUID taskId) {
        for (Task task : tasks) {
            if (task.getId().equals(taskId)) {
                return task;
            }
        }
        return null;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Achievement getRewardAchievement() {
        return rewardAchievement;
    }
    
    public int getBaseXpReward() {
        return baseXpReward;
    }
    
    public void setBaseXpReward(int baseXpReward) {
        this.baseXpReward = baseXpReward;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Quest quest = (Quest) o;
        return id.equals(quest.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}