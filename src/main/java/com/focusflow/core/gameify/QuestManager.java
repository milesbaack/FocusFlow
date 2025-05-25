package com.focusflow.core.gameify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.focusflow.core.task.Task;
import com.focusflow.core.task.TaskListener;

/**
 * Manages quests and coordinates with the AchievementManager
 * to award achievements when quests are completed.
 * 
 * Quests are collections of tasks that can be completed,
 * and/or grouped together.
 * 
 * Quests are essentially projects, and tasks are the individual
 * steps to complete the project.
 * 
 * This incentivizes users to complete tasks by breaking them
 * down into smaller more manageable tasks.
 */
public class QuestManager implements TaskListener {
    private final Map<UUID, Quest> quests;
    private final AchievementManager achievementManager;
    private final XpManager xpManager;

    /**
     * Creates a new QuestManager with the specified achievement and XP managers.
     * 
     * @param achievementManager The achievement manager to use
     * @param xpManager          The XP manager to use
     */
    public QuestManager(AchievementManager achievementManager, XpManager xpManager) {
        this.quests = new HashMap<>();
        this.achievementManager = achievementManager;
        this.xpManager = xpManager;
    }

    /**
     * Adds a new quest
     * 
     * @param quest The quest to add
     * @return true if added successfully
     */
    public boolean addQuest(Quest quest) {
        if (quest != null && !quests.containsKey(quest.getId())) {
            quests.put(quest.getId(), quest);

            // Register as a listener for all tasks in this quest
            for (Task task : quest.getTasks()) {
                task.addListener(this);
                // Register for subtasks too
                for (Task subtask : task.getSubtasks()) {
                    subtask.addListener(this);
                }
            }

            return true;
        }
        return false;
    }

    /**
     * Removes a quest
     * 
     * @param questId The ID of the quest to remove
     * @return The removed quest, or null if not found
     */
    public Quest removeQuest(UUID questId) {
        Quest quest = quests.remove(questId);

        // Remove listener registration from all tasks
        if (quest != null) {
            for (Task task : quest.getTasks()) {
                task.removeListener(this);
                // Remove from subtasks too
                for (Task subtask : task.getSubtasks()) {
                    subtask.removeListener(this);
                }
            }
        }

        return quest;
    }

    /**
     * Gets a quest by ID
     * 
     * @param questId The ID of the quest
     * @return The quest, or null if not found
     */
    public Quest getQuest(UUID questId) {
        return quests.get(questId);
    }

    /**
     * Gets all quests
     * 
     * @return Map of quest IDs to quests
     */
    public Map<UUID, Quest> getAllQuests() {
        return new HashMap<>(quests); // Return a copy for safety
    }

    /**
     * Gets all completed quests
     * 
     * @return List of completed quests
     */
    public List<Quest> getCompletedQuests() {
        List<Quest> completedQuests = new ArrayList<>();
        for (Quest quest : quests.values()) {
            if (quest.isCompleted()) {
                completedQuests.add(quest);
            }
        }
        return completedQuests;
    }

    /**
     * Gets all incomplete quests
     * 
     * @return List of incomplete quests
     */
    public List<Quest> getIncompleteQuests() {
        List<Quest> incompleteQuests = new ArrayList<>();
        for (Quest quest : quests.values()) {
            if (!quest.isCompleted()) {
                incompleteQuests.add(quest);
            }
        }
        return incompleteQuests;
    }

    /**
     * Registers this QuestManager as a listener for all tasks in all quests
     */
    public void registerAsListenerForAllTasks() {
        for (Quest quest : quests.values()) {
            for (Task task : quest.getTasks()) {
                task.addListener(this);
                // Register for subtasks too
                for (Task subtask : task.getSubtasks()) {
                    subtask.addListener(this);
                }
            }
        }
    }

    /**
     * 
     * Called when a task is completed.
     * 
     * -- IMPORTANT --
     * 
     * Might want to put this in a QuestListener class.
     * 
     * @param task The task that was completed
     */
    @Override
    public void onTaskCompleted(Task task) {
        // Check if any quest contains this task
        boolean taskFound = false;

        for (Quest quest : quests.values()) {
            // Check if this task is directly in the quest
            if (quest.getTasks().contains(task)) {
                taskFound = true;
                boolean wasAlreadyCompleted = quest.isCompleted();
                boolean newlyCompleted = quest.updateCompletionStatus();

                // If quest was newly completed
                if (newlyCompleted) {
                    handleQuestCompletion(quest);
                }
            } else {
                // Check if this task is a subtask in any of the quest's tasks
                for (Task mainTask : quest.getTasks()) {
                    if (mainTask.getSubtasks().contains(task)) {
                        taskFound = true;
                        // Check if completing this subtask completes the main task
                        if (mainTask.updateCompletionStatusFromSubtasks()) {
                            // Main task was newly completed - will trigger onTaskCompleted for main task
                            // so no need to do anything here
                        }
                        break;
                    }
                }
            }
        }

        // If task wasn't found in any quest, it might be a standalone task
        if (!taskFound) {
            // Handle standalone task completion if needed
            // For example, you could notify the user or update UI
        }
    }

    /**
     * Called when a task's progress is updated
     * 
     * @param task The task that was updated
     */
    @Override
    public void onTaskProgressUpdated(Task task) {
        // Optional: You can add behavior here if needed
        // For example, update UI or trigger other events
    }

    /**
     * Handles the completion of a quest, awarding achievements and XP
     * 
     * @param quest The completed quest
     */
    private void handleQuestCompletion(Quest quest) {
        // Award achievement
        if (quest.getRewardAchievement() != null) {
            achievementManager.unlockAchievement(quest.getRewardAchievement());
        }

        // Award XP
        int xpReward = quest.calculateXpReward();
        boolean leveledUp = xpManager.addXp(xpReward);

        // You could trigger events here for UI notifications
        // For example: notifyQuestCompleted(quest, xpReward, leveledUp);
    }

    /**
     * Adds a task to a quest
     * 
     * @param questId The ID of the quest
     * @param task    The task to add
     * @return true if added successfully
     */
    public boolean addTaskToQuest(UUID questId, Task task) {
        Quest quest = quests.get(questId);
        if (quest != null && task != null) {
            boolean added = quest.addTask(task);
            if (added) {
                task.addListener(this);
                // Register for subtasks too
                for (Task subtask : task.getSubtasks()) {
                    subtask.addListener(this);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a subtask to a task in a quest
     * 
     * @param questId The ID of the quest
     * @param taskId  The ID of the task
     * @param subtask The subtask to add
     * @return true if added successfully
     */
    public boolean addSubtaskToTask(UUID questId, UUID taskId, Task subtask) {
        Quest quest = quests.get(questId);
        if (quest != null && subtask != null) {
            Task task = quest.getTaskById(taskId);
            if (task != null) {
                boolean added = task.addSubtask(subtask);
                if (added) {
                    subtask.addListener(this);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the total XP reward for a quest
     * 
     * @param questId The ID of the quest
     * @return The XP reward, or 0 if quest not found
     */
    public int getQuestXpReward(UUID questId) {
        Quest quest = quests.get(questId);
        if (quest != null) {
            return quest.calculateXpReward();
        }
        return 0;
    }

    /**
     * Calculates the total XP available from all incomplete quests
     * 
     * @return Total available XP
     */
    public int getTotalAvailableXp() {
        int totalXp = 0;
        for (Quest quest : quests.values()) {
            if (!quest.isCompleted()) {
                totalXp += quest.calculateXpReward();
            }
        }
        return totalXp;
    }

    /**
     * Calculates the total XP earned from completed quests
     * 
     * @return Total earned XP
     */
    public int getTotalEarnedXp() {
        int totalXp = 0;
        for (Quest quest : quests.values()) {
            if (quest.isCompleted()) {
                totalXp += quest.calculateXpReward();
            }
        }
        return totalXp;
    }
}