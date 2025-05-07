package com.focusflow.app;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

import com.focusflow.core.gameify.Achievement;
import com.focusflow.core.gameify.AchievementManager;
import com.focusflow.core.gameify.GameifySystem;
import com.focusflow.core.gameify.Quest;
import com.focusflow.core.gameify.QuestManager;
import com.focusflow.core.gameify.XpManager;
import com.focusflow.core.session.FocusSession;
import com.focusflow.core.session.SessionManager;
import com.focusflow.core.task.Task;
import com.focusflow.core.timer.PomodoroTimer;
import com.focusflow.core.timer.Timer;
import com.focusflow.core.timer.TimerEventListener;
import com.focusflow.core.timer.TimerState;
import com.focusflow.core.timer.TimerType;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Main application class for FocusFlow.
 * 
 * This class implements the main user interface for the FocusFlow application,
 * integrating the timer, task management, session tracking features, and
 * gamification elements.
 * 
 * @author Emilio Lopez & Miles Baack
 * @version 1.2.0
 */
public class App extends Application {
    private PomodoroTimer timer;
    private SessionManager sessionManager;
    private Task currentTask;
    private ListView<Task> taskListView;
    private TextField taskNameField;
    private TextArea taskDescriptionField;
    private Label timerLabel;
    private TextArea sessionHistoryArea;
    private Button startButton;
    private Button pauseButton;
    private Button resetButton;
    private Button createTaskButton;
    private ComboBox<TimerType> timerTypeComboBox;
    private CheckBox taskComplete;
    private Label currentTaskLabel;
    private Label sessionStatusLabel;

    // Gamification components
    private AchievementManager achievementManager;
    private XpManager xpManager;
    private QuestManager questManager;
    private GameifySystem gameifySystem;

    // Gamification UI elements
    private Label levelLabel;
    private ProgressBar xpProgressBar;
    private Label xpLabel;
    private ListView<Achievement> achievementsListView;
    private ListView<Quest> questsListView;
    private Button createQuestButton;
    private TextField questNameField;
    private TextArea questDescriptionField;
    private Label currentQuestLabel;
    private ProgressBar questProgressBar;
    private Button addTaskToQuestButton;

    @Override
    public void start(Stage stage) {
        // Initialize components
        sessionManager = new SessionManager();
        timer = new PomodoroTimer(TimerType.WORK);
        currentTask = null;

        // Initialize gamification system
        initializeGameification();

        // Create main layout with tabs
        TabPane tabPane = new TabPane();

        // Create tabs
        Tab focusTab = new Tab("Focus");
        focusTab.setClosable(false);

        Tab questsTab = new Tab("Quests");
        questsTab.setClosable(false);

        Tab achievementsTab = new Tab("Achievements");
        achievementsTab.setClosable(false);

        Tab statsTab = new Tab("Stats");
        statsTab.setClosable(false);

        // Build each tab's content
        focusTab.setContent(createFocusTabContent());
        questsTab.setContent(createQuestsTabContent());
        achievementsTab.setContent(createAchievementsTabContent());
        statsTab.setContent(createStatsTabContent());

        // Add tabs to the TabPane
        tabPane.getTabs().addAll(focusTab, questsTab, achievementsTab, statsTab);

        // Create header with level and XP
        HBox headerBox = createGameificationHeader();

        // Create main layout
        BorderPane root = new BorderPane();
        root.setTop(headerBox);
        root.setCenter(tabPane);

        // Create scene
        Scene scene = new Scene(root, 700, 600);
        stage.setTitle("FocusFlow");
        stage.setScene(scene);
        stage.show();

        // Initialize UI state
        updateUIState();
        updateGameificationDisplay();

        // Check for achievements based on time of day
        checkTimeBasedAchievements();
    }

    /**
     * Initializes all gamification-related components
     */
    private void initializeGameification() {
        // Create XP manager starting at level 1 with 0 XP
        xpManager = new XpManager();

        // Initialize achievement manager
        achievementManager = new AchievementManager();

        // Add all achievements to the achievement manager
        for (Achievement achievement : Achievement.values()) {
            achievementManager.addAchievement(achievement);
        }

        // Initialize quest manager
        questManager = new QuestManager(achievementManager, xpManager);

        // Initialize gamify system
        gameifySystem = new GameifySystem();
    }

    /**
     * Creates the header displaying level and XP information
     * 
     * @return HBox containing the header components
     */
    private HBox createGameificationHeader() {
        HBox headerBox = new HBox(15);
        headerBox.setPadding(new Insets(10));
        headerBox.setAlignment(Pos.CENTER_LEFT);

        levelLabel = new Label("Level 1");
        levelLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        xpProgressBar = new ProgressBar(0);
        xpProgressBar.setPrefWidth(200);

        xpLabel = new Label("0 / 100 XP");

        headerBox.getChildren().addAll(levelLabel, xpProgressBar, xpLabel);

        return headerBox;
    }

    /**
     * Creates content for the main Focus tab
     * 
     * @return VBox containing the Focus tab components
     */
    private VBox createFocusTabContent() {
        VBox focusContent = new VBox(10);
        focusContent.setPadding(new Insets(10));

        // Task creation section
        HBox taskInputBox = new HBox(10);
        taskNameField = new TextField();
        taskNameField.setPromptText("Task Name");
        taskDescriptionField = new TextArea();
        taskDescriptionField.setPromptText("Task Description");
        taskDescriptionField.setPrefRowCount(2);
        createTaskButton = new Button("Create Task");
        createTaskButton.setOnAction(e -> createNewTask());
        taskInputBox.getChildren().addAll(taskNameField, taskDescriptionField, createTaskButton);

        // Task list section
        taskListView = new ListView<>();
        taskListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectTask(newVal);
            }
        });

        // Current task display
        currentTaskLabel = new Label("No task selected");
        taskComplete = new CheckBox("Complete");
        taskComplete.setOnAction(e -> {
            if (currentTask != null) {
                if (taskComplete.isSelected()) {
                    currentTask.markAsCompleted();
                    endCurrentSession();

                    // Check for task completion achievements
                    checkTaskCompletionAchievements();
                } else {
                    currentTask.markAsIncomplete();
                }
                updateTaskDisplay();
            }
        });

        // Timer type selection
        timerTypeComboBox = new ComboBox<>();
        timerTypeComboBox.getItems().addAll(TimerType.values());
        timerTypeComboBox.setValue(TimerType.WORK);
        timerTypeComboBox.setOnAction(e -> {
            if (currentTask != null) {
                timer = new PomodoroTimer(timerTypeComboBox.getValue());
                setupTimerListeners();
            }
        });

        // Timer section
        timerLabel = new Label("25:00");
        timerLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        sessionStatusLabel = new Label("No active session");
        setupTimerListeners();

        // Timer controls
        startButton = new Button("Start");
        pauseButton = new Button("Pause");
        resetButton = new Button("Reset");

        startButton.setOnAction(e -> startNewSession());
        pauseButton.setOnAction(e -> pauseCurrentSession());
        resetButton.setOnAction(e -> resetTimer());

        HBox timerControls = new HBox(10);
        timerControls.setAlignment(Pos.CENTER);
        timerControls.getChildren().addAll(startButton, pauseButton, resetButton);

        // Session history
        sessionHistoryArea = new TextArea();
        sessionHistoryArea.setEditable(false);
        sessionHistoryArea.setPrefRowCount(5);

        // Add components to root
        focusContent.getChildren().addAll(
                new Label("Create New Task:"),
                taskInputBox,
                new Separator(),
                new Label("Task List:"),
                taskListView,
                new Separator(),
                new Label("Current Task:"),
                currentTaskLabel,
                taskComplete,
                new Separator(),
                new Label("Timer Type:"),
                timerTypeComboBox,
                new Separator(),
                timerLabel,
                sessionStatusLabel,
                timerControls,
                new Separator(),
                new Label("Session History:"),
                sessionHistoryArea);

        return focusContent;
    }

    /**
     * Creates content for the Quests tab
     * 
     * @return VBox containing the Quests tab components
     */
    private VBox createQuestsTabContent() {
        VBox questsContent = new VBox(10);
        questsContent.setPadding(new Insets(10));

        // Quest creation section
        HBox questInputBox = new HBox(10);
        questNameField = new TextField();
        questNameField.setPromptText("Quest Name");
        questDescriptionField = new TextArea();
        questDescriptionField.setPromptText("Quest Description");
        questDescriptionField.setPrefRowCount(2);
        createQuestButton = new Button("Create Quest");
        createQuestButton.setOnAction(e -> createNewQuest());
        questInputBox.getChildren().addAll(questNameField, questDescriptionField, createQuestButton);

        // Quest list
        questsListView = new ListView<>();
        questsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectQuest(newVal);
            }
        });

        // Current quest display
        currentQuestLabel = new Label("No quest selected");
        currentQuestLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Quest progress bar
        questProgressBar = new ProgressBar(0);
        questProgressBar.setPrefWidth(200);

        // Button to add current task to selected quest
        addTaskToQuestButton = new Button("Add Current Task to Quest");
        addTaskToQuestButton.setOnAction(e -> addCurrentTaskToSelectedQuest());
        addTaskToQuestButton.setDisable(true);

        // Add components to quests tab
        questsContent.getChildren().addAll(
                new Label("Create New Quest:"),
                questInputBox,
                new Separator(),
                new Label("Active Quests:"),
                questsListView,
                new Separator(),
                new Label("Selected Quest:"),
                currentQuestLabel,
                new Label("Progress:"),
                questProgressBar,
                addTaskToQuestButton);

        return questsContent;
    }

    /**
     * Creates content for the Achievements tab
     * 
     * @return VBox containing the Achievements tab components
     */
    private VBox createAchievementsTabContent() {
        VBox achievementsContent = new VBox(10);
        achievementsContent.setPadding(new Insets(10));

        // Create two panes for locked and unlocked achievements
        Label unlockedHeader = new Label("Unlocked Achievements");
        unlockedHeader.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Create achievements list
        achievementsListView = new ListView<>();
        achievementsListView.setPrefHeight(300);

        // Add components to achievements tab
        achievementsContent.getChildren().addAll(
                unlockedHeader,
                achievementsListView,
                new Separator(),
                new Label("Tip: Complete tasks and quests to unlock achievements!"));

        return achievementsContent;
    }

    /**
     * Creates content for the Stats tab
     * 
     * @return VBox containing the Stats tab components
     */
    private VBox createStatsTabContent() {
        VBox statsContent = new VBox(10);
        statsContent.setPadding(new Insets(10));

        // Create labels for displaying stats
        Label tasksCompletedLabel = new Label("Tasks Completed: 0");
        Label questsCompletedLabel = new Label("Quests Completed: 0");
        Label achievementsUnlockedLabel = new Label("Achievements Unlocked: 0");
        Label totalXpEarnedLabel = new Label("Total XP Earned: 0");
        Label sessionsCompletedLabel = new Label("Focus Sessions Completed: 0");
        Label totalFocusTimeLabel = new Label("Total Focus Time: 0 minutes");

        // Add components to stats tab
        statsContent.getChildren().addAll(
                new Label("Your FocusFlow Statistics:"),
                new Separator(),
                tasksCompletedLabel,
                questsCompletedLabel,
                achievementsUnlockedLabel,
                totalXpEarnedLabel,
                sessionsCompletedLabel,
                totalFocusTimeLabel);

        return statsContent;
    }

    private void setupTimerListeners() {
        timer.addListener(new TimerEventListener() {
            @Override
            public void onTimerTick(Timer timer, int remainingSeconds) {
                Platform.runLater(() -> {
                    int minutes = remainingSeconds / 60;
                    int seconds = remainingSeconds % 60;
                    timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
                });
            }

            @Override
            public void onTimerCompleted(Timer timer) {
                Platform.runLater(() -> {
                    timerLabel.setText("Done!");
                    endCurrentSession();
                    updateSessionHistory();

                    // Award XP for completing a session
                    int xpAwarded = 10; // Base XP for completing a session
                    boolean leveledUp = xpManager.addXp(xpAwarded);

                    // Check for streak achievements
                    checkSessionStreakAchievements();

                    if (leveledUp) {
                        // Display level up notification
                        showLevelUpNotification();
                    }

                    updateGameificationDisplay();
                });
            }

            @Override
            public void onTimerStarted(Timer timer) {
                Platform.runLater(() -> {
                    sessionStatusLabel.setText("Session in progress");
                    updateUIState();
                });
            }

            @Override
            public void onTimerPaused(Timer timer) {
                Platform.runLater(() -> {
                    sessionStatusLabel.setText("Session paused");
                    updateUIState();
                });
            }

            @Override
            public void onTimerResumed(Timer timer) {
                Platform.runLater(() -> {
                    sessionStatusLabel.setText("Session in progress");
                    updateUIState();
                });
            }

            @Override
            public void onTimerStopped(Timer timer) {
                Platform.runLater(() -> {
                    sessionStatusLabel.setText("Session stopped");
                    updateUIState();
                });
            }

            @Override
            public void onTimerReset(Timer timer) {
                Platform.runLater(() -> {
                    timerLabel.setText(String.format("%02d:00", timer.getType().getDefaultDuration() / 60));
                    sessionStatusLabel.setText("No active session");
                    updateUIState();
                });
            }
        });
    }

    private void createNewTask() {
        String name = taskNameField.getText().trim();
        String description = taskDescriptionField.getText().trim();

        if (!name.isEmpty()) {
            Task newTask = new Task(name, description);
            taskListView.getItems().add(newTask);
            taskNameField.clear();
            taskDescriptionField.clear();

            // Check for first task creation achievement
            if (taskListView.getItems().size() == 1) {
                achievementManager.unlockAchievement(Achievement.COMPLETED_FIRST_TASK);
                updateGameificationDisplay();
            }
        }
    }

    private void createNewQuest() {
        String name = questNameField.getText().trim();
        String description = questDescriptionField.getText().trim();

        if (!name.isEmpty()) {
            // Create a new quest with base XP of 50
            Quest newQuest = new Quest(name, description, null, 50);

            // Add to the quest manager
            questManager.addQuest(newQuest);

            // Add to the list view
            questsListView.getItems().add(newQuest);

            // Clear input fields
            questNameField.clear();
            questDescriptionField.clear();

            // Check for first quest creation achievement
            if (questsListView.getItems().size() == 1) {
                achievementManager.unlockAchievement(Achievement.COMPLETED_FIRST_QUEST);
                updateGameificationDisplay();
            }
        }
    }

    private void selectQuest(Quest quest) {
        if (quest != null) {
            // Update UI
            currentQuestLabel.setText(quest.getTitle());
            questProgressBar.setProgress(quest.getProgressPercentage() / 100.0);

            // Enable the add task button if a task is selected
            addTaskToQuestButton.setDisable(currentTask == null);
        }
    }

    private void addCurrentTaskToSelectedQuest() {
        Quest selectedQuest = questsListView.getSelectionModel().getSelectedItem();

        if (selectedQuest != null && currentTask != null) {
            // Add task to quest
            selectedQuest.addTask(currentTask);

            // Update quest progress display
            questProgressBar.setProgress(selectedQuest.getProgressPercentage() / 100.0);

            // Check if adding this task completes the quest
            if (currentTask.isComplete()) {
                selectedQuest.updateCompletionStatus();

                if (selectedQuest.isCompleted()) {
                    handleQuestCompletion(selectedQuest);
                }
            }
        }
    }

    private void selectTask(Task task) {
        if (task != null) {
            currentTask = task;
            updateTaskDisplay();
            updateUIState();
            // Set the task ID for the timer
            timer.setCurrentTaskId(task.getId().toString());

            // Enable the add task to quest button if a quest is selected
            Quest selectedQuest = questsListView.getSelectionModel().getSelectedItem();
            addTaskToQuestButton.setDisable(selectedQuest == null);
        }
    }

    private void updateTaskDisplay() {
        if (currentTask != null) {
            currentTaskLabel.setText(currentTask.getName());
            taskComplete.setSelected(currentTask.isComplete());
        } else {
            currentTaskLabel.setText("No task selected");
            taskComplete.setSelected(false);
        }
    }

    private void updateUIState() {
        boolean hasTask = currentTask != null;
        boolean isSessionActive = timer.getState() == TimerState.RUNNING;
        boolean isSessionPaused = timer.getState() == TimerState.PAUSED;

        createTaskButton.setDisable(false);
        startButton.setDisable(!hasTask || isSessionActive);
        pauseButton.setDisable(!hasTask || !isSessionActive || isSessionPaused);
        resetButton.setDisable(!hasTask || isSessionActive);
        taskComplete.setDisable(!hasTask);
        timerTypeComboBox.setDisable(!hasTask || isSessionActive);
    }

    private void updateGameificationDisplay() {
        // Update level and XP display
        int currentLevel = xpManager.getCurrentLevel();
        int currentXp = xpManager.getCurrentXp();
        int xpForNextLevel = xpManager.getXpForNextLevel();
        int xpForCurrentLevel = xpManager.getXpForCurrentLevel();
        int xpInCurrentLevel = currentXp - xpForCurrentLevel;
        int xpRequiredForLevel = xpForNextLevel - xpForCurrentLevel;

        levelLabel.setText("Level " + currentLevel);
        xpLabel.setText(xpInCurrentLevel + " / " + xpRequiredForLevel + " XP");
        xpProgressBar.setProgress((double) xpInCurrentLevel / xpRequiredForLevel);

        // Update achievements list
        achievementsListView.getItems().clear();
        HashSet<Achievement> unlockedAchievements = achievementManager.getUnlockedAchievements();
        achievementsListView.getItems().addAll(unlockedAchievements);
    }

    private void startNewSession() {
        if (currentTask != null) {
            try {
                sessionManager.startSession(currentTask.getId().toString());
                timer.start();
                updateUIState();
            } catch (IllegalArgumentException e) {
                // Show error to user
                sessionStatusLabel.setText("Error: " + e.getMessage());
            }
        } else {
            sessionStatusLabel.setText("Please select a task first");
        }
    }

    private void pauseCurrentSession() {
        if (timer.getState() == TimerState.RUNNING) {
            timer.pause();
            sessionManager.pauseCurrentSession();
        } else if (timer.getState() == TimerState.PAUSED) {
            timer.resume();
            sessionManager.resumeCurrentSession();
        }
        updateUIState();
    }

    private void resetTimer() {
        timer.reset();
        sessionManager.endCurrentSession();
        updateSessionHistory();
        updateUIState();
    }

    private void endCurrentSession() {
        timer.stop();
        sessionManager.endCurrentSession();
        updateSessionHistory();
        updateUIState();

        // Check for "Return from break" achievement if this was a break session
        if (timer.getType() == TimerType.SHORT_BREAK || timer.getType() == TimerType.LONG_BREAK) {
            achievementManager.unlockAchievement(Achievement.RETURN_FROM_BREAK);
            updateGameificationDisplay();
        }
    }

    private void updateSessionHistory() {
        StringBuilder history = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Object session : sessionManager.getSessionHistory()) {
            history.append(String.format(
                    "Session: %s - Duration: %d seconds\n",
                    ((FocusSession) session).getStartTime().format(formatter),
                    ((FocusSession) session).getDurationSeconds()));
        }
        sessionHistoryArea.setText(history.toString());
    }

    /**
     * Checks for time-based achievements based on the current time of day
     */
    private void checkTimeBasedAchievements() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime time = now.toLocalTime();

        // Check for early morning achievements
        if (time.isBefore(LocalTime.of(6, 0))) {
            achievementManager.unlockAchievement(Achievement.MORNING_HERO);
            achievementManager.unlockAchievement(Achievement.EARLY_BIRD);
        } else if (time.isBefore(LocalTime.of(8, 0))) {
            achievementManager.unlockAchievement(Achievement.EARLY_BIRD);
        }

        // Check for night owl achievement
        if (time.isAfter(LocalTime.of(22, 0))) {
            achievementManager.unlockAchievement(Achievement.NIGHT_OWL);
        }

        // Check for weekend warrior achievement
        int dayOfWeek = now.getDayOfWeek().getValue();
        if (dayOfWeek == 6 || dayOfWeek == 7) { // Saturday or Sunday
            achievementManager.unlockAchievement(Achievement.WEEKEND_WARRIOR);
        }

        updateGameificationDisplay();
    }

    /**
     * Checks for task completion related achievements
     */
    private void checkTaskCompletionAchievements() {
        // Count completed tasks
        int completedTasks = 0;
        int completedSubtasks = 0;

        for (Task task : taskListView.getItems()) {
            if (task.isComplete()) {
                completedTasks++;

                // Count subtasks
                for (Task subtask : task.getSubtasks()) {
                    if (subtask.isComplete()) {
                        completedSubtasks++;
                    }
                }
            }
        }

        // Check for task completion milestones
        if (completedTasks >= 1) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_FIRST_TASK);
        }
        if (completedTasks >= 10) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_10_TASKS);
        }
        if (completedTasks >= 50) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_50_TASKS);
        }
        if (completedTasks >= 100) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_100_TASKS);
        }
        if (completedTasks >= 500) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_500_TASKS);
        }
        if (completedTasks >= 1000) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_1000_TASKS);
        }
        if (completedTasks >= 5000) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_5000_TASKS);
        }
        if (completedTasks >= 9001) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_9001_TASKS);
        }

        // Check for subtask completion milestones
        if (completedSubtasks >= 10) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_10_SUBTASKS);
        }
        if (completedSubtasks >= 50) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_50_SUBTASKS);
        }
        if (completedSubtasks >= 100) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_100_SUBTASKS);
        }
        if (completedSubtasks >= 500) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_500_SUBTASKS);
        }
        if (completedSubtasks >= 1000) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_1000_SUBTASKS);
        }
        if (completedSubtasks >= 5000) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_5000_SUBTASKS);
        }
        if (completedSubtasks >= 9001) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_9001_SUBTASKS);
        }
        if (completedSubtasks >= 10000) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_10000_SUBTASKS);
        }

        // Check if all subtasks of this task are complete
        if (currentTask != null && !currentTask.getSubtasks().isEmpty()) {
            boolean allSubtasksComplete = true;
            for (Task subtask : currentTask.getSubtasks()) {
                if (!subtask.isComplete()) {
                    allSubtasksComplete = false;
                    break;
                }
            }

            if (allSubtasksComplete) {
                achievementManager.unlockAchievement(Achievement.CONQOR_AND_DIVIDE);
            }
        }

        updateGameificationDisplay();
    }

    /**
     * Checks for session streak achievements
     */
    private void checkSessionStreakAchievements() {
        // Get the number of consecutive sessions
        int consecutiveSessions = sessionManager.getConsecutiveSessionsCount();

        // Check for streak achievements
        if (consecutiveSessions >= 2) {
            achievementManager.unlockAchievement(Achievement.TWO_SESSIONS_IN_A_ROW);
        }
        if (consecutiveSessions >= 3) {
            achievementManager.unlockAchievement(Achievement.THREE_SESSIONS_IN_A_ROW);
        }
        if (consecutiveSessions >= 5) {
            achievementManager.unlockAchievement(Achievement.FIVE_SESSIONS_IN_A_ROW);
        }
        if (consecutiveSessions >= 12) {
            achievementManager.unlockAchievement(Achievement.TWELVE_SESSIONS_IN_A_ROW);
        }
        if (consecutiveSessions >= 24) {
            achievementManager.unlockAchievement(Achievement.TWENTY_FOUR_SESSIONS_IN_A_ROW);
        }
        if (consecutiveSessions >= 32) {
            achievementManager.unlockAchievement(Achievement.THIRTY_TWO_SESSIONS_IN_A_ROW);
        }

        updateGameificationDisplay();
    }

    /**
     * Shows a level up notification
     */
    private void showLevelUpNotification() {
        // In a real app, you would create a nice popup notification
        // For now, we'll just update the session status label
        sessionStatusLabel.setText("Level Up! You are now level " + xpManager.getCurrentLevel());
    }

    /**
     * Handles the completion of a quest, awarding achievements and XP
     * 
     * @param quest The completed quest
     */
    private void handleQuestCompletion(Quest quest) {
        // Award XP for completing the quest
        int xpReward = quest.calculateXpReward();
        boolean leveledUp = xpManager.addXp(xpReward);

        if (leveledUp) {
            showLevelUpNotification();
        }

        // Update the quests display
        questProgressBar.setProgress(1.0);

        // Count completed quests for achievements
        int completedQuests = 0;
        for (int i = 0; i < questsListView.getItems().size(); i++) {
            Quest q = questsListView.getItems().get(i);
            if (q.isCompleted()) {
                completedQuests++;
            }
        }

        // Check for quest completion milestones
        if (completedQuests >= 1) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_FIRST_QUEST);
        }
        if (completedQuests >= 10) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_10_QUESTS);
        }
        if (completedQuests >= 50) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_50_QUESTS);
        }
        if (completedQuests >= 100) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_100_QUESTS);
        }
        if (completedQuests >= 500) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_500_QUESTS);
        }
        if (completedQuests >= 1000) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_1000_QUESTS);
        }
        if (completedQuests >= 5000) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_5000_QUESTS);
        }
        if (completedQuests >= 9001) {
            achievementManager.unlockAchievement(Achievement.COMPLETED_9001_QUESTS);
        }

        updateGameificationDisplay();
    }

    public static void main(String[] args) {
        launch(args);
    }
}