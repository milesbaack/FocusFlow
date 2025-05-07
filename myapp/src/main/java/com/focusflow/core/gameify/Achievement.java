/**
 * Enumeration of achievements available in the FocusFlow gamification system.
 * Each achievement has a unique integer ID, description, name, and unlock status.
 * <p>
 * Achievements track various user milestones including:
 * <ul>
 *   <li>Time-based achievements (weekend sessions, early morning sessions)</li>
 *   <li>Task completion milestones</li>
 *   <li>Quest completion milestones</li>
 *   <li>Session streak achievements</li>
 *   <li>Feature usage achievements</li>
 * </ul>
 * </p>
 * 
 * @author Miles Baack
 * @version 1.1
 */
package com.focusflow.core.gameify;

public enum Achievement {

    /**
     * Achievement for completing a session on a weekend.
     * Description: "Complete a session on a weekend"
     * Name: "No Days Off"
     */
    WEEKEND_WARRIOR(1001, "Complete a session on a weekend", "No Days Off"),
    
    /**
     * Achievement for completing a session after 10pm.
     * Description: "Complete a session after 10pm"
     * Name: "Night Owl"
     */
    NIGHT_OWL(1002, "Complete a session after 10pm", "Night Owl"),
            
    /**
     * Achievement for starting a session before 8am.
     * Description: "Start a session before 8am"
     * Name: "Eating worms"
     */
    EARLY_BIRD(1003, "Start a session before 8am", "Eating worms"),
            
    /**
     * Achievement for starting a session before 6am.
     * Description: "Start a session before 6am"
     * Name: "Morning Hero"
     */
    MORNING_HERO(1004, "Start a session before 6am", "Morning Hero"),
            
    /**
     * Achievement for completing all subtasks of a task.
     * Description: "Complete all subtasks of a task"
     * Name: "Conqor and Divide"
     */
    CONQOR_AND_DIVIDE(2001, "Complete all subtasks of a task", "Conqor and Divide"),
    
    /**
     * Achievemnt for completeing 10 subtasks
     * Description: "Complete 10 subtasks"
     * Name: "Submariner"
     */
    COMPLETED_10_SUBTASKS(2002, "Complete 10 subtasks", "Submariner"),
            
    /**
     * Achievement for completing 50 subtasks.
     * Description: "Complete 50 subtasks"
     * Name: "Subspicious"
     */
    COMPLETED_50_SUBTASKS(2003, "Complete 50 subtasks", "Subspicious"),
            
    /**
     * Achievement for completing 100 subtasks.
     * Description: "Complete 100 subtasks"
     * Name: "Substantial"
     */
    COMPLETED_100_SUBTASKS(2004, "Complete 100 subtasks", "Substantial"),
            
    /**
     * Achievement for completing 500 subtasks.
     * Description: "Complete 500 subtasks"
     * Name: "Substance"
     */

    COMPLETED_500_SUBTASKS(2005, "Complete 500 subtasks", "Substance"),
            
    /**
     * Achievement for completing 1000 subtasks.
     * Description: "Complete 1000 subtasks"
     * Name: "Sublime"
     */
    COMPLETED_1000_SUBTASKS(2006, "Complete 1000 subtasks", "Sublime"),
            
    /**
     * Achievement for completing 5000 subtasks.
     * Description: "Complete 5000 subtasks"
     * Name: "Subliminal"
     */

    COMPLETED_5000_SUBTASKS(2007, "Complete 5000 subtasks", "Subliminal"),
            
    /**
     * Achievement for completing 9001 subtasks.
     * Description: "Complete 9001 subtasks"
     * Name: "It's over 9000!"
     */
    COMPLETED_9001_SUBTASKS(2008, "Complete 9001 subtasks", "It's over 9000!"),
            
    /**
     * Achievement for completing 10,000 subtasks.
     * Description: "Complete 10,000 subtasks"
     * Name: "Subtle"
     */
    COMPLETED_10000_SUBTASKS(2009, "Complete 10,000 subtasks", "Subtle"),


    /**
     * Achievement for completing the first task.
     * Description: "Complete your first task"
     * Name: "First Task"
     */
    COMPLETED_FIRST_TASK(2002, "Complete your first task", "First Task"),
    
    /**
     * Achievement for completing 10 tasks.
     * Description: "Complete 10 tasks"
     * Name: "Tenacious"
     */
    COMPLETED_10_TASKS(2003, "Complete 10 tasks", "Tenacious"),
    
    /**
     * Achievement for completing 50 tasks.
     * Description: "Complete 50 tasks"
     * Name: "Fifty Shades of Focus"
     */
    COMPLETED_50_TASKS(2004, "Complete 50 tasks", "Fifty Shades of Focus"),
    
    /**
     * Achievement for completing 100 tasks.
     * Description: "Complete 100 tasks"
     * Name: "Centurion"
     */
    COMPLETED_100_TASKS(2005, "Complete 100 tasks", "Centurion"),
    
    /**
     * Achievement for completing 500 tasks.
     * Description: "Complete 500 tasks"
     * Name: "Half a Thousand"
     */
    COMPLETED_500_TASKS(2006, "Complete 500 tasks", "Half a Thousand"),
    
    /**
     * Achievement for completing 1000 tasks.
     * Description: "Complete 1000 tasks"
     * Name: "Thousandaire"
     */
    COMPLETED_1000_TASKS(2007, "Complete 1000 tasks", "Thousandaire"),
    
    /**
     * Achievement for completing 5000 tasks.
     * Description: "Complete 5000 tasks"
     * Name: "Five Thousand"
     */
    COMPLETED_5000_TASKS(2008, "Complete 5000 tasks", "Five Thousand"),
    
    /**
     * Achievement for completing 9001 tasks.
     * Description: "Complete 9001 tasks"
     * Name: "It's over 9000!"
     */
    COMPLETED_9001_TASKS(2009, "Complete 9001 tasks", "It's over 9000!"),
            
    /**
     * Achievement for completing first quest.
     * Description: "Complete your first quest"
     * Name: "The First of Many"
     */
    COMPLETED_FIRST_QUEST(3001, "Complete your first quest", "The First of Many"),
    
    /**
     * Achievement for completing 10 quests.
     * Description: "Complete 10 quests"
     * Name: "Quester"
     */
    COMPLETED_10_QUESTS(3002, "Complete 10 quests", "Quester"),
    
    /**
     * Achievement for completing 50 quests.
     * Description: "Complete 50 quests"
     * Name: "Questiciser"
     */
    COMPLETED_50_QUESTS(3003, "Complete 50 quests", "Questiciser"),
    
    /**
     * Achievement for completing 100 quests.
     * Description: "Complete 100 quests"
     * Name: "Questilicious"
     */
    COMPLETED_100_QUESTS(3004, "Complete 100 quests", "Questilicious"),
    
    /**
     * Achievement for completing 500 quests.
     * Description: "Complete 500 quests"
     * Name: "Questacious"
     */
    COMPLETED_500_QUESTS(3005, "Complete 500 quests", "Questacious"),
    
    /**
     * Achievement for completing 1000 quests.
     * Description: "Complete 1000 quests"
     * Name: "My middle name is Quest"
     */
    COMPLETED_1000_QUESTS(3006, "Complete 1000 quests", "Middle name is Quest"),
    
    /**
     * Achievement for completing 5000 quests.
     * Description: "Complete 5000 quests"
     * Name: "Questinator"
     */
    COMPLETED_5000_QUESTS(3007, "Complete 5000 quests", "Questinator"),
    
    /**
     * Achievement for completing 9001 quests.
     * Description: "Complete 9001 quests"
     * Name: "It's over 9000!"
     */
    COMPLETED_9001_QUESTS(3008, "Complete 9001 quests", "It's over 9000!"),
            
    /**
     * Achievement for completing two consecutive sessions.
     * Description: "Complete two sessions in a row"
     * Name: "Two for the Price of One"
     */
    TWO_SESSIONS_IN_A_ROW(4001, "Complete two sessions in a row", "Two for the Price of One"),
    
    /**
     * Achievement for completing three consecutive sessions.
     * Description: "Complete three sessions in a row"
     * Name: "Three's Company"
     */
    THREE_SESSIONS_IN_A_ROW(4002, "Complete three sessions in a row", "Three's Company"),
    
    /**
     * Achievement for completing five consecutive sessions.
     * Description: "Complete five sessions in a row"
     * Name: "Five Alive"
     */
    FIVE_SESSIONS_IN_A_ROW(4003, "Complete five sessions in a row", "Five Alive"),
    
    /**
     * Achievement for completing twelve consecutive sessions.
     * Description: "Complete twelve sessions in a row"
     * Name: "Six hours later"
     */
    TWELVE_SESSIONS_IN_A_ROW(4004, "Complete twelve sessions in a row", "Six hours later"),

    /**
     * Achievement for completing twenty-four consecutive sessions.
     * Description: "Complete twenty-four sessions in a row"
     * Name: "Twelve hours later"
     */
    TWENTY_FOUR_SESSIONS_IN_A_ROW(4005, "Complete twenty-four sessions in a row", "Twelve hours later"),
            
    /**
     * Achievement for completing thirty-two consecutive sessions.
     * Description: "Complete thirty-two sessions in a row... time to go to sleep..."
     * Name: "Sixteen hours later"
     */
    THIRTY_TWO_SESSIONS_IN_A_ROW(4006, "Complete thirty-two sessions in a row... time to go to sleep...", "Sixteen hours later"),
    
    /**
     * Achievement for watching the tutorial.
     * Description: "Watch the Tutorial"
     * Name: "Quick Learner"
     */
    WATCH_THE_TUTORIAL(5001, "Watch the Tutorial", "Quick Learner"),
            
    /**
     * Achievement for using the Distraction List feature.
     * Description: "Use the Distraction List feature"
     * Name: "Undestractable"
     */
    USE_THE_DISTRACTION_LIST_FEATURE(5002, "Use the Distraction List feature", "Undestractable"),

    /**
     * Achievement for returning from a break.
     * Description: "Return from a break"
     * Name: "Back so soon?"
     */
    RETURN_FROM_BREAK(5003, "Return from a break", "Back so soon?");
            
    
            

    /** The unique identifier for this achievement */
    private final int id;
    
    /** The achievement description */
    private final String description;
    
    /** The display name of the achievement */
    private final String name;
    
    /** Whether the achievement has been unlocked by the user */
    private boolean unlocked = false;

    /**
     * Constructs a new Achievement with the specified ID, description, and name.
     * All achievements are initially locked (unlocked = false).
     *
     * @param id the unique integer identifier for this achievement
     * @param description the textual description of what needs to be done to unlock this achievement
     * @param name the display name of the achievement
     */
    Achievement(int id, String description, String name) {
        this.id = id;
        this.description = description;
        this.name = name;
    }
    
    /**
     * Gets the unique identifier of this achievement.
     *
     * @return the achievement ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the description of this achievement.
     *
     * @return the achievement description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the display name of this achievement.
     *
     * @return the achievement name
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if this achievement has been unlocked.
     *
     * @return true if the achievement is unlocked, false otherwise
     */
    public boolean isUnlocked() {
        return unlocked;
    }

    /**
     * Unlocks this achievement by setting its unlocked status to true.
     */
    public void unlock() {
        this.unlocked = true;
    }
    
    /**
     * Finds an achievement by its ID.
     *
     * @param id the ID to search for
     * @return the Achievement with the matching ID, or null if not found
     */
    public static Achievement findById(int id) {
        for (Achievement achievement : values()) {
            if (achievement.getId() == id) {
                return achievement;
            }
        }
        return null;
    }
}