package com.focusflow.core.gameify;

/**
 * This class implements the gamification system for the FocusFlow application.
 * 
 * It tracks user progress and provides XP and level-up rewards.
 * The system includes achievements and quests that users can complete to earn rewards.
 *
 * @author Miles Baack
 * @version 1.0
 * @since 2025-05-06
 */

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GameifySystem {

    /** Set of all achievements in the system */
    private HashSet<Achievement> achievements;

    /** Set of currently active quests */
    private HashSet<Quest> activeQuests;

    /** Set of completed quests */
    private HashSet<Quest> completedQuests;

    /**
     * Constructor for the GameifySystem class.
     * Initializes empty collections for achievements and quests.
     */
    public GameifySystem() {
        this.achievements = new HashSet<>();
        this.activeQuests = new HashSet<>();
        this.completedQuests = new HashSet<>();
    }

    /**
     * Retrieves all achievements in the system.
     * 
     * @return A HashSet containing all achievements
     */
    public HashSet<Achievement> getAchievements() {
        return achievements;
    }

    /**
     * Retrieves all unlocked achievements.
     * 
     * @return A List of unlocked achievements
     */
    public List<Achievement> getUnlockedAchievements() {
        return achievements.stream()
                .filter(Achievement::isUnlocked)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all locked (not yet unlocked) achievements.
     * 
     * @return A List of locked achievements
     */
    public List<Achievement> getLockedAchievements() {
        return achievements.stream()
                .filter(achievement -> !achievement.isUnlocked())
                .collect(Collectors.toList());
    }

    /**
     * Unlocks a specific achievement by its ID.
     * 
     * @param achievementId The int of the achievement to unlock
     * @return true if the achievement was successfully unlocked, false if already
     *         unlocked or not found
     */
    public boolean unlockAchievement(int achievementId) {
        for (Achievement achievement : achievements) {
            if (achievement.getId() == achievementId) {
                if (!achievement.isUnlocked()) {
                    achievement.unlock();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves all currently active quests.
     * 
     * @return A HashSet containing all active quests
     */
    public HashSet<Quest> getActiveQuests() {
        return activeQuests;
    }

    /**
     * Retrieves all completed quests.
     * 
     * @return A HashSet containing all completed quests
     */
    public HashSet<Quest> getCompletedQuests() {
        return completedQuests;
    }

    /**
     * Marks a quest as completed and moves it from active to completed quests.
     * 
     * @param questId The UUID of the quest to complete
     * @return true if the quest was successfully completed, false if not found in
     *         active quests
     */
    public boolean completeQuest(UUID questId) {
        for (Quest quest : activeQuests) {
            if (quest.getId().equals(questId)) {
                quest.markAsCompleted();
                completedQuests.add(quest);
                activeQuests.remove(quest);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the user has unlocked a specific achievement.
     * 
     * @param achievementId The UUID of the achievement to check
     * @return true if the achievement is unlocked, false if locked or not found
     */
    public boolean hasUnlockedAchievement(int achievementId) {
        for (Achievement achievement : achievements) {
            if (achievement.getId() == achievementId) {
                return achievement.isUnlocked();
            }
        }
        return false;
    }

    /**
     * Calculates the user level based on their total experience points.
     * 
     * @param experiencePoints The user's total experience points
     * @return The calculated level
     * @throws IllegalArgumentException if experiencePoints is negative
     */
    public int calculateLevelFromXP(int experiencePoints) {
        if (experiencePoints < 0) {
            throw new IllegalArgumentException("Experience points cannot be negative");
        }

        int level = 1;
        int requiredXP = getRequiredXPForNextLevel(level);
        while (experiencePoints >= requiredXP) {
            level++;
            requiredXP = getRequiredXPForNextLevel(level);
        }
        return level;
    }

    /**
     * Calculates the required experience points to reach the next level.
     * Each successive level requires 10% more XP than the previous level.
     * This is a simple exponential growth formula.
     * The formula used is: XP = 100 * (1.1 ^ (level - 1))
     * 
     * 
     * @param currentLevel The current level
     * @return The required experience points for the next level
     * @throws IllegalArgumentException if currentLevel is less than 1
     */
    public int getRequiredXPForNextLevel(int currentLevel) {
        if (currentLevel < 1) {
            throw new IllegalArgumentException("Level must be at least 1");
        }
        return (int) (100 * Math.pow(1.1, currentLevel - 1));
    }
}