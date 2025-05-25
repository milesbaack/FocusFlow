/**
 * Manages the tracking and state of achievements within the FocusFlow gameification system.
 * 
 * This class provides functionality to add, unlock, and query achievements. It maintains
 * separate collections for locked, unlocked, and all achievements using HashSets for
 * efficient lookup operations.
 *
 * @author FocusFlow
 * @version 1.0
 * @since 1.0
 */
package com.focusflow.core.gameify;

import java.util.HashSet;

public class AchievementManager {

    /** Set containing all currently locked achievements. */
    private HashSet<Achievement> lockedAchievements = new HashSet<>();
    
    /** Set containing all currently unlocked achievements. */
    private HashSet<Achievement> unlockedAchievements = new HashSet<>();
    
    /** Set containing all achievements in the system, whether locked or unlocked. */
    private final HashSet<Achievement> allAchievements = new HashSet<>();
    
    /**
     * Constructs a new AchievementManager instance.
     * Initializes the achievement tracking system with all achievements in the locked state.
     */
    public AchievementManager() {
        // Initially, all achievements are locked
        lockedAchievements = new HashSet<>(allAchievements);
    }
    
    /**
     * Adds a new achievement to the system.
     * The achievement is automatically added to the locked achievements collection.
     * Duplicate achievements are ignored.
     * 
     * @param achievement The achievement to add to the system
     * @throws NullPointerException if achievement is null
     */
    public void addAchievement(Achievement achievement) {
        if (achievement != null && !allAchievements.contains(achievement)) {
            allAchievements.add(achievement);
            lockedAchievements.add(achievement);
        }
    }

    /**
     * Retrieves the current set of locked achievements.
     * This method recalculates the locked set to ensure it's synchronized with
     * the current state of all achievements.
     * 
     * @return A HashSet containing all currently locked achievements
     */
    public HashSet<Achievement> getLockedAchievements() {
        // Recalculate locked achievements to ensure it's up to date
        lockedAchievements = new HashSet<>(allAchievements);
        lockedAchievements.removeAll(unlockedAchievements);
        return lockedAchievements;
    }

    /**
     * Retrieves all achievements registered in the system.
     * This includes both locked and unlocked achievements.
     * 
     * @return A HashSet containing all achievements in the system
     */
    public HashSet<Achievement> getAllAchievements() {
        return allAchievements;
    }

    /**
     * Unlocks an achievement and updates its status in the system.
     * This method updates the achievement's internal state and moves it
     * from the locked to the unlocked collection.
     * 
     * @param achievement The achievement to unlock
     * @return {@code true} if the achievement was newly unlocked, {@code false} if it was already unlocked
     * @throws NullPointerException if achievement is null
     */
    public boolean unlockAchievement(Achievement achievement) {
        if (achievement != null && !unlockedAchievements.contains(achievement)) {
            achievement.unlock(); // Assuming Achievement class has this method
            unlockedAchievements.add(achievement);
            lockedAchievements.remove(achievement);
            return true;
        }
        return false;
    }

    /**
     * Retrieves the current set of unlocked achievements.
     * 
     * @return A HashSet containing all currently unlocked achievements
     */
    public HashSet<Achievement> getUnlockedAchievements() {
        return unlockedAchievements;
    }
    
    /**
     * Checks if a specific achievement has been unlocked.
     * 
     * @param achievement The achievement to check
     * @return {@code true} if the achievement is unlocked, {@code false} otherwise
     * @throws NullPointerException if achievement is null
     */
    public boolean isAchievementUnlocked(Achievement achievement) {
        return unlockedAchievements.contains(achievement);
    }
}