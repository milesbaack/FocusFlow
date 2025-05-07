package com.focusflow.core.gameify;

/**
 * Manages user experience points, levels, and progression in a gamified system.
 * <p>
 * This class tracks a user's XP, handles level progression, and provides methods
 * to query level information and progression status. It uses a fully dynamic XP 
 * calculation system allowing for unlimited level progression without hardcoded values.
 * </p>
 * 
 * @author FocusFlow
 * @version 1.2
 * @since 1.0
 */
public class XpManager {
    private int currentXp;
    private int currentLevel;
    
    /**
     * Base XP required for level 1.
     * This serves as the foundation for all dynamic XP calculations.
     */
    private static final int BASE_XP = 100;
    
    /**
     * Growth factor that determines how XP requirements scale with level.
     * Higher values create steeper progression curves.
     */
    private static final double GROWTH_FACTOR = 1.1;
    
    /**
     * Creates a new XP manager with the user starting at level 1 with 0 XP.
     * <p>
     * This is the default constructor for new users who are just beginning.
     * </p>
     */
    public XpManager() {
        this.currentXp = 0;
        this.currentLevel = 1;
    }
    
    /**
     * Creates a new XP manager with specified initial XP and level.
     * <p>
     * This constructor is useful for initializing users with existing progression
     * data, such as when loading saved user profiles.
     * </p>
     * 
     * @param initialXp Initial XP value for the user
     * @param initialLevel Initial level for the user
     * @throws IllegalArgumentException if initialXp is negative or initialLevel is less than 1
     */
    public XpManager(int initialXp, int initialLevel) {
        if (initialXp < 0) {
            throw new IllegalArgumentException("Initial XP cannot be negative");
        }
        if (initialLevel < 1) {
            throw new IllegalArgumentException("Initial level cannot be less than 1");
        }
        
        this.currentXp = initialXp;
        this.currentLevel = initialLevel;
    }
    
    /**
     * Adds XP to the user's total and handles level-ups.
     * <p>
     * This method will check if the new XP total qualifies the user for 
     * one or more level advancements after adding the specified amount.
     * Users can continue to progress indefinitely with dynamically calculated
     * XP requirements for each level.
     * </p>
     * 
     * @param xpToAdd Amount of XP to add to the user's total
     * @return {@code true} if adding XP resulted in a level up, {@code false} otherwise
     * @throws IllegalArgumentException if xpToAdd is negative
     */
    public boolean addXp(int xpToAdd) {
        if (xpToAdd < 0) {
            throw new IllegalArgumentException("Cannot add negative XP");
        }
        
        if (xpToAdd == 0) {
            return false;
        }
        
        int oldLevel = currentLevel;
        currentXp += xpToAdd;
        
        // Check for level up - continuing indefinitely with dynamic XP requirements
        while (currentXp >= getXpForLevel(currentLevel + 1)) {
            currentLevel++;
        }
        
        return currentLevel > oldLevel;
    }
    
    /**
     * Gets the current user level.
     * <p>
     * There is no maximum level - users can progress indefinitely
     * with dynamically calculated XP requirements.
     * </p>
     * 
     * @return The current level of the user
     */
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    /**
     * Gets the current total XP accumulated by the user.
     * 
     * @return The current XP total
     */
    public int getCurrentXp() {
        return currentXp;
    }
    
    /**
     * Gets the XP threshold required to advance to the next level.
     * <p>
     * XP requirements are calculated dynamically using a mathematical formula
     * that produces a smooth progression curve.
     * </p>
     * 
     * @return XP required to reach the next level
     */
    public int getXpForNextLevel() {
        return getXpForLevel(currentLevel + 1);
    }
    
    /**
     * Gets the XP threshold that was required to reach the current level.
     * <p>
     * For level 1, this returns 0 since no XP is needed for the starting level.
     * For all other levels, requirements are calculated dynamically.
     * </p>
     * 
     * @return XP threshold for the current level
     */
    public int getXpForCurrentLevel() {
        if (currentLevel == 1) {
            return 0;
        }
        
        return getXpForLevel(currentLevel);
    }
    
    /**
     * Gets the total XP needed to reach a specific level.
     * <p>
     * XP requirements are calculated using a formula that produces exponential growth:
     * XP = BASE_XP * ((GROWTH_FACTOR^(level-1) - 1) / (GROWTH_FACTOR - 1))
     * </p>
     * <p>
     * This formula creates a smooth progression curve that becomes increasingly 
     * challenging at higher levels without requiring hardcoded values.
     * </p>
     * 
     * @param level The level to check
     * @return XP required to reach the specified level
     * @throws IllegalArgumentException if level is less than 1
     */
    public int getXpForLevel(int level) {
        if (level <= 0) {
            throw new IllegalArgumentException("Level must be positive");
        }
        
        if (level == 1) {
            return 0;
        }
        
        // Mathematical formula for calculating XP requirements
        // This creates a smooth exponential curve
        double levelFactor = (Math.pow(GROWTH_FACTOR, level - 1) - 1) / (GROWTH_FACTOR - 1);
        return (int)(BASE_XP * levelFactor);
    }
    
    /**
     * Gets the progress percentage toward the next level.
     * <p>
     * This can be used for displaying progress bars or similar UI elements.
     * </p>
     * 
     * @return Percentage completion toward the next level, from 0-100
     */
    public int getLevelProgressPercentage() {
        int currentLevelXp = getXpForCurrentLevel();
        int nextLevelXp = getXpForNextLevel();
        int xpInCurrentLevel = currentXp - currentLevelXp;
        int xpRequiredForLevel = nextLevelXp - currentLevelXp;
        
        return (int)((double)xpInCurrentLevel / xpRequiredForLevel * 100);
    }
    
    /**
     * Gets the amount of XP still needed to reach the next level.
     * 
     * @return XP remaining until next level up
     */
    public int getXpRemainingForNextLevel() {
        return getXpForNextLevel() - currentXp;
    }
    
    /**
     * Resets the user's XP and level to initial values.
     * <p>
     * This sets XP to 0 and level to 1, effectively resetting all progression.
     * Useful for implementing prestige systems or handling user account resets.
     * </p>
     */
    public void reset() {
        this.currentXp = 0;
        this.currentLevel = 1;
    }
}