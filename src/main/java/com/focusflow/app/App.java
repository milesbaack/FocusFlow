package com.focusflow.app;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.focusflow.app.ui.BackgroundManager;
import com.focusflow.app.ui.QuestAndStatsGUI;
import com.focusflow.app.ui.TaskListCell;
import com.focusflow.app.ui.TaskManagementWindow;
import com.focusflow.app.ui.WallpaperSelectionDialog;
import com.focusflow.core.gameify.AchievementManager;
import com.focusflow.core.gameify.Quest;
import com.focusflow.core.gameify.QuestManager;
import com.focusflow.core.gameify.XpManager;
import com.focusflow.core.preferences.UserPreferences;
import com.focusflow.core.session.SessionManager;
import com.focusflow.core.task.Task;
import com.focusflow.core.task.TaskPriority;
import com.focusflow.core.timer.PomodoroTimer;
import com.focusflow.core.timer.TimerEventListener;
import com.focusflow.core.timer.TimerState;
import com.focusflow.core.timer.TimerType;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * The App class serves as the main JavaFX application for the FocusFlow app.
 * It provides the graphical user interface (GUI) and integrates task
 * management,
 * a Pomodoro timer, and user interactions for productivity tracking.
 *
 * @author Miles Baack, Emilio Lopez, and Brisa Rueda
 * @version 4.0.0
 */
public class App extends Application implements TimerEventListener {
    private PomodoroTimer workTimer;
    private PomodoroTimer breakTimer;
    private SessionManager sessionManager;
    private QuestManager questManager;
    private AchievementManager achievementManager;
    private XpManager xpManager;
    private QuestAndStatsGUI questStatsGUI;
    private Task currentTask;
    private List<Task> tasks = new ArrayList<>();
    private boolean isOnBreak = false;

    // UI Components
    private UserPreferences userPreferences; // settings
    private ImageView backgroundImageView; // background image

    private Label timerLabel;
    private Label timerTypeLabel;
    private ProgressBar progressBar;
    private Button startButton;
    private Button startWorkingButton;
    private MediaPlayer backgroundMusic;
    private boolean isMuted = false;
    private TextArea notepadArea;
    private boolean isNotepadOpen = false;
    private AnchorPane notepadPane;
    private ImageView soundIcon;
    private AnchorPane infoPane;
    private boolean isInfoOpen = false;
    private Font pixelFont;
    private Label questProgressLabel;

    @Override
    public void start(Stage stage) {
        userPreferences = new UserPreferences();

        loadPixelFont();
        initializeManagers();
        setupUI(stage);
        setupBackgroundMusic();

        Scene scene = new Scene(createMainLayout(), 1070, 610);
        stage.setScene(scene);
        stage.setTitle("FocusFlow");

        // Apply fullscreen setting from preferences
        if (userPreferences.isFullscreenEnabled()) {
            stage.setFullScreen(true);
            stage.setResizable(true);
        } else {
            stage.setResizable(false);
        }

        setupKeyboardShortcuts(scene);

        stage.show();
    }

    private void loadPixelFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/UI/pixel.ttf");
            if (is != null) {
                pixelFont = Font.loadFont(is, 24);
            } else {
                pixelFont = Font.font("Courier New", FontWeight.BOLD, 24);
            }
        } catch (Exception e) {
            pixelFont = Font.font("Courier New", FontWeight.BOLD, 24);
        }
    }

    private void initializeManagers() {
        sessionManager = new SessionManager();
        achievementManager = new AchievementManager();
        xpManager = new XpManager();
        questManager = new QuestManager(achievementManager, xpManager);

        // Initialize the integrated QuestAndStatsGUI
        questStatsGUI = new QuestAndStatsGUI(questManager, achievementManager, xpManager, pixelFont);

        // Initialize timers
        workTimer = new PomodoroTimer(TimerType.WORK, 25 * 60); // 25 minutes
        breakTimer = new PomodoroTimer(TimerType.SHORT_BREAK, 5 * 60); // 5 minutes
        workTimer.addListener(this);
        breakTimer.addListener(this);
    }

    private AnchorPane createMainLayout() {

        // Load and set background
        AnchorPane root = new AnchorPane();

        // Load background using BackgroundManager
        String selectedWallpaper = userPreferences.getSelectedWallpaper();
        backgroundImageView = BackgroundManager.createBackgroundImageView(
                selectedWallpaper, 1070, 610);

        // Make background responsive to window size changes
        backgroundImageView.fitWidthProperty().bind(root.widthProperty());
        backgroundImageView.fitHeightProperty().bind(root.heightProperty());

        AnchorPane.setTopAnchor(backgroundImageView, 0.0);
        AnchorPane.setLeftAnchor(backgroundImageView, 0.0);
        root.getChildren().add(backgroundImageView);

        // Create sidebar
        createSidebar(root);

        // Create main timer area
        createTimerArea(root);

        // Create control buttons
        createControlButtons(root);

        // Create progress area
        createProgressArea(root);

        // Create top controls
        createTopControls(root);

        // Create overlays
        createOverlays(root);

        return root;
    }

    private void createSidebar(AnchorPane root) {
        VBox sidebarIcons = new VBox(20);
        sidebarIcons.setPadding(new Insets(0, 0, 0, 45));
        sidebarIcons.setAlignment(Pos.CENTER);
        sidebarIcons.setSpacing(25); // Reduced spacing to fit new button
        AnchorPane.setLeftAnchor(sidebarIcons, 0.0);
        AnchorPane.setTopAnchor(sidebarIcons, 0.0);
        AnchorPane.setBottomAnchor(sidebarIcons, 0.0);

        final int ICON_SIZE = 55; // Slightly smaller to fit more buttons

        // Add task button
        ImageView addTaskIcon = new ImageView(new Image(
                getClass().getResource("/UI/AddTask.png").toString(), ICON_SIZE, ICON_SIZE, true, true));
        StackPane addTaskBtn = new StackPane(addTaskIcon);
        addTaskBtn.setStyle("-fx-cursor: hand;");
        addTaskBtn.setOnMouseClicked(e -> showCreateTaskDialog());

        // Task management button
        ImageView taskMgmtIcon = new ImageView(new Image(
                getClass().getResource("/UI/StatsIcon.png").toString(), ICON_SIZE, ICON_SIZE, true, true));
        StackPane taskMgmtBtn = new StackPane(taskMgmtIcon);
        taskMgmtBtn.setStyle("-fx-cursor: hand;");
        taskMgmtBtn.setOnMouseClicked(e -> openStatisticsAndManagement());

        // NEW: Wallpaper selection button
        ImageView wallpaperIcon = new ImageView(new Image(
                getClass().getResource("/UI/BackgroundIcon.png").toString(), ICON_SIZE, ICON_SIZE, true, true));
        StackPane wallpaperBtn = new StackPane(wallpaperIcon);
        wallpaperBtn.setStyle("-fx-cursor: hand;");
        wallpaperBtn.setOnMouseClicked(e -> showWallpaperSelection());

        // Add hover effect (optional)
        wallpaperBtn.setOnMouseEntered(e -> wallpaperIcon.setOpacity(0.7));
        wallpaperBtn.setOnMouseExited(e -> wallpaperIcon.setOpacity(1.0));

        // Calendar icon
        ImageView calendarIcon = new ImageView(new Image(
                getClass().getResource("/UI/CalendarIcon.png").toString(), ICON_SIZE, ICON_SIZE, true, true));
        StackPane calendarBtn = new StackPane(calendarIcon);
        calendarBtn.setStyle("-fx-cursor: hand;");

        // Question icon
        ImageView questionIcon = new ImageView(new Image(
                getClass().getResource("/UI/question.png").toString(), ICON_SIZE, ICON_SIZE, true, true));
        StackPane questionBtn = new StackPane(questionIcon);
        questionBtn.setStyle("-fx-cursor: hand;");
        questionBtn.setOnMouseClicked(e -> toggleInfo());

        sidebarIcons.getChildren().addAll(addTaskBtn, taskMgmtBtn, wallpaperBtn, calendarBtn, questionBtn);
        root.getChildren().add(sidebarIcons);
    }

    // method for wallpaper selection:
    private void showWallpaperSelection() {
        Stage currentStage = (Stage) startButton.getScene().getWindow();

        WallpaperSelectionDialog wallpaperDialog = new WallpaperSelectionDialog(
                currentStage,
                userPreferences,
                pixelFont,
                this::onWallpaperChanged);

        wallpaperDialog.show();
    }

    // method to handle wallpaper changes:
    private void onWallpaperChanged(String newWallpaperFilename) {
        // Get current scene dimensions
        Scene currentScene = backgroundImageView.getScene();
        double width = currentScene.getWidth();
        double height = currentScene.getHeight();

        // Create new background image
        ImageView newBackground = BackgroundManager.createBackgroundImageView(
                newWallpaperFilename, width, height);

        // Bind to scene size for responsiveness
        newBackground.fitWidthProperty().bind(currentScene.widthProperty());
        newBackground.fitHeightProperty().bind(currentScene.heightProperty());

        // Replace the background in the scene
        AnchorPane root = (AnchorPane) currentScene.getRoot();
        root.getChildren().remove(backgroundImageView);
        root.getChildren().add(0, newBackground); // Add at index 0 to keep it behind other elements

        // Update reference
        backgroundImageView = newBackground;

        // Position the new background
        AnchorPane.setTopAnchor(backgroundImageView, 0.0);
        AnchorPane.setLeftAnchor(backgroundImageView, 0.0);
    }

    private void createTimerArea(AnchorPane root) {
        // Timer display
        timerLabel = new Label("25:00");
        timerLabel.setFont(Font.font(pixelFont.getFamily(), 80));
        timerLabel.setTextFill(Color.BLACK);
        AnchorPane.setTopAnchor(timerLabel, 170.0);
        AnchorPane.setRightAnchor(timerLabel, 170.0);

        // Timer type label
        timerTypeLabel = new Label("WORK SESSION");
        timerTypeLabel.setFont(Font.font(pixelFont.getFamily(), 24));
        timerTypeLabel.setTextFill(Color.BLACK);
        AnchorPane.setTopAnchor(timerTypeLabel, 140.0);
        AnchorPane.setRightAnchor(timerTypeLabel, 170.0);

        // "REMAINING..." text under timer
        Label remainingLabel = new Label("REMAINING...");
        remainingLabel.setFont(Font.font(pixelFont.getFamily(), 24));
        remainingLabel.setTextFill(Color.BLACK);
        AnchorPane.setTopAnchor(remainingLabel, 240.0);
        AnchorPane.setRightAnchor(remainingLabel, 170.0);

        root.getChildren().addAll(timerLabel, timerTypeLabel, remainingLabel);
    }

    private void createControlButtons(AnchorPane root) {
        // Start working on task button
        startWorkingButton = new Button("Start Working");
        startWorkingButton.setFont(Font.font(pixelFont.getFamily(), 20));
        startWorkingButton
                .setStyle("-fx-background-color:rgb(102, 204, 102); -fx-text-fill: white; -fx-background-radius: 15;");
        startWorkingButton.setPrefSize(120, 35);
        AnchorPane.setTopAnchor(startWorkingButton, 390.0);
        AnchorPane.setRightAnchor(startWorkingButton, 220.0);
        startWorkingButton.setOnAction(e -> showTaskSelection());

        // Start/Pause button
        startButton = new Button("START");
        startButton.setFont(Font.font(pixelFont.getFamily(), 24));
        startButton
                .setStyle("-fx-background-color:rgb(255, 255, 255); -fx-text-fill: black; -fx-background-radius: 15;");
        startButton.setPrefSize(80, 35);
        AnchorPane.setTopAnchor(startButton, 390.0);
        AnchorPane.setRightAnchor(startButton, 120.0);
        startButton.setOnAction(e -> toggleTimer());

        root.getChildren().addAll(startWorkingButton, startButton);
    }

    private void createProgressArea(AnchorPane root) {
        // Quest progress label
        questProgressLabel = new Label("No active quest");
        questProgressLabel.setFont(Font.font(pixelFont.getFamily(), 18));
        questProgressLabel.setTextFill(Color.BLACK);
        AnchorPane.setBottomAnchor(questProgressLabel, 100.0);
        AnchorPane.setLeftAnchor(questProgressLabel, 20.0);

        // Progress bar
        progressBar = new ProgressBar(0.0);
        progressBar.setPrefWidth(950);
        progressBar.setPrefHeight(48);
        progressBar.setStyle("-fx-accent:rgb(255, 209, 3);");
        AnchorPane.setBottomAnchor(progressBar, 40.0);
        AnchorPane.setLeftAnchor(progressBar, 20.0);

        // Star icon
        ImageView starIcon = new ImageView(new Image(
                getClass().getResource("/UI/star.png").toString(), 60, 60, true, true));
        AnchorPane.setBottomAnchor(starIcon, 50.0);
        AnchorPane.setRightAnchor(starIcon, 10.0);

        root.getChildren().addAll(questProgressLabel, progressBar, starIcon);
    }

    private void createTopControls(AnchorPane root) {
        // Sound icon
        Image soundImg = new Image(getClass().getResource("/UI/sound.png").toString(), 40, 40, true, true);
        soundIcon = new ImageView(soundImg);
        StackPane soundBtn = new StackPane(soundIcon);
        soundBtn.setStyle("-fx-cursor: hand;");
        AnchorPane.setTopAnchor(soundBtn, 20.0);
        AnchorPane.setRightAnchor(soundBtn, 20.0);
        soundBtn.setOnMouseClicked(e -> toggleSound());

        // Notepad icon
        ImageView notepadIcon = new ImageView(new Image(
                getClass().getResource("/UI/Notepad.png").toString(), 40, 40, true, true));
        StackPane notepadBtn = new StackPane(notepadIcon);
        notepadBtn.setStyle("-fx-cursor: hand;");
        AnchorPane.setTopAnchor(notepadBtn, 20.0);
        AnchorPane.setRightAnchor(notepadBtn, 80.0);
        notepadBtn.setOnMouseClicked(e -> toggleNotepad());

        root.getChildren().addAll(soundBtn, notepadBtn);
    }

    private void createOverlays(AnchorPane root) {
        // Notepad panel
        createNotepadPane(root);

        // Info panel
        createInfoPane(root);
    }

    private void createNotepadPane(AnchorPane root) {
        notepadPane = new AnchorPane();
        Rectangle notepadBg = new Rectangle(200, 300);
        notepadBg.setFill(Color.WHITE);
        notepadBg.setStroke(Color.BLACK);

        notepadArea = new TextArea();
        notepadArea.setPrefSize(180, 280);
        notepadArea.setWrapText(true);

        AnchorPane.setTopAnchor(notepadBg, 0.0);
        AnchorPane.setLeftAnchor(notepadBg, 0.0);
        AnchorPane.setTopAnchor(notepadArea, 10.0);
        AnchorPane.setLeftAnchor(notepadArea, 10.0);

        notepadPane.getChildren().addAll(notepadBg, notepadArea);
        notepadPane.setVisible(false);

        AnchorPane.setTopAnchor(notepadPane, 70.0);
        AnchorPane.setRightAnchor(notepadPane, 80.0);

        root.getChildren().add(notepadPane);
    }

    private void createInfoPane(AnchorPane root) {
        infoPane = new AnchorPane();
        Rectangle infoBg = new Rectangle(400, 350); // Made taller for more content
        infoBg.setFill(Color.WHITE);
        infoBg.setStroke(Color.BLACK);

        Label infoTitle = new Label("About FocusFlow");
        infoTitle.setFont(Font.font(pixelFont.getFamily(), 20));

        TextArea infoText = new TextArea();
        infoText.setFont(Font.font(pixelFont.getFamily(), 17));
        infoText.setText("FocusFlow - Productivity with Gamification\n\n" +
                "• Create tasks and organize them into quests\n" +
                "• Use Pomodoro technique: 25min work + 5min break\n" +
                "• Earn XP and unlock achievements\n" +
                "• Track your productivity with detailed analytics\n" +
                "• Use the task management screen to organize work\n" +
                "• Click 'Start Working' to select a task and begin\n" +
                "• 🎨 Click the wallpaper icon to change backgrounds\n" +
                "• Press F11 for fullscreen mode");
        infoText.setWrapText(true);
        infoText.setEditable(false);

        AnchorPane.setTopAnchor(infoBg, 0.0);
        AnchorPane.setLeftAnchor(infoBg, 0.0);
        AnchorPane.setTopAnchor(infoTitle, 10.0);
        AnchorPane.setLeftAnchor(infoTitle, 10.0);
        AnchorPane.setTopAnchor(infoText, 40.0);
        AnchorPane.setLeftAnchor(infoText, 10.0);
        AnchorPane.setRightAnchor(infoText, 10.0);
        AnchorPane.setBottomAnchor(infoText, 10.0);

        infoPane.getChildren().addAll(infoBg, infoTitle, infoText);
        infoPane.setVisible(false);

        AnchorPane.setTopAnchor(infoPane, 150.0);
        AnchorPane.setLeftAnchor(infoPane, 150.0);

        root.getChildren().add(infoPane);
    }

    private void setupUI(Stage stage) {
        // UI setup is now handled in createMainLayout()
    }

    // ADD keyboard shortcut handling (optional enhancement):
    private void setupKeyboardShortcuts(Scene scene) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case F11:
                    toggleFullscreen();
                    break;
                case B:
                    if (e.isControlDown()) {
                        showWallpaperSelection();
                    }
                    break;
                case M:
                    if (e.isControlDown()) {
                        toggleSound();
                    }
                    break;
                default:
                    break;
            }
        });
    }

    private void setupBackgroundMusic() {
        try {
            Media sound = new Media(getClass().getResource("/UI/background_music.wav").toString());
            backgroundMusic = new MediaPlayer(sound);
            backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);

            // Only play if music is enabled in preferences
            if (userPreferences.isMusicEnabled()) {
                backgroundMusic.play();
            }
        } catch (Exception e) {
            System.out.println("Could not load background music: " + e.getMessage());
        }
    }

    private void showCreateTaskDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Create New Task");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        // Task name
        Label nameLabel = new Label("Task Name:");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter task name");

        // Task description
        Label descLabel = new Label("Description:");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Enter task description (optional)");
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);

        // Priority
        Label priorityLabel = new Label("Priority:");
        ComboBox<TaskPriority> priorityCombo = new ComboBox<>();
        priorityCombo.setItems(FXCollections.observableArrayList(TaskPriority.values()));
        priorityCombo.setValue(TaskPriority.MEDIUM);

        // Due date
        Label dueDateLabel = new Label("Due Date:");
        DatePicker dueDatePicker = new DatePicker();

        // Quest assignment
        Label questLabel = new Label("Assign to Quest:");
        ComboBox<String> questCombo = new ComboBox<>();
        updateQuestComboBox(questCombo);

        // Buttons
        Button saveButton = new Button("Save Task");
        Button cancelButton = new Button("Cancel");

        saveButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showAlert("Error", "Task name cannot be empty!");
                return;
            }

            Task newTask = new Task(name, descArea.getText().trim());
            newTask.setPriority(priorityCombo.getValue());

            if (dueDatePicker.getValue() != null) {
                newTask.setDueDateTime(dueDatePicker.getValue().atStartOfDay());
            }

            tasks.add(newTask);

            // Add to quest if selected
            String selectedQuest = questCombo.getValue();
            if (selectedQuest != null && !selectedQuest.equals("No Quest")) {
                // Find quest by title and add task
                questManager.getAllQuests().values().stream()
                        .filter(quest -> quest.getTitle().equals(selectedQuest))
                        .findFirst()
                        .ifPresent(quest -> questManager.addTaskToQuest(quest.getId(), newTask));
            }

            updateQuestProgress();
            dialog.close();
        });

        cancelButton.setOnAction(e -> dialog.close());

        // Layout
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(descLabel, 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(priorityLabel, 0, 2);
        grid.add(priorityCombo, 1, 2);
        grid.add(dueDateLabel, 0, 3);
        grid.add(dueDatePicker, 1, 3);
        grid.add(questLabel, 0, 4);
        grid.add(questCombo, 1, 4);

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        grid.add(buttonBox, 1, 5);

        Scene scene = new Scene(grid, 400, 350);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void updateQuestComboBox(ComboBox<String> questCombo) {
        List<String> questTitles = new ArrayList<>();
        questTitles.add("No Quest");
        questManager.getAllQuests().values().forEach(quest -> questTitles.add(quest.getTitle()));
        questCombo.setItems(FXCollections.observableArrayList(questTitles));
        questCombo.setValue("No Quest");
    }

    private void showTaskSelection() {
        List<Task> incompleteTasks = tasks.stream()
                .filter(task -> !task.isComplete())
                .toList();

        if (incompleteTasks.isEmpty()) {
            showAlert("No Tasks", "No incomplete tasks available. Create a task first!");
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Select Task to Work On");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        Label titleLabel = new Label("Choose a task to start working on:");
        titleLabel.setFont(Font.font(pixelFont.getFamily(), 16));

        ComboBox<Task> taskCombo = new ComboBox<>();
        taskCombo.setItems(FXCollections.observableArrayList(incompleteTasks));
        taskCombo.setCellFactory(listView -> new TaskListCell());
        taskCombo.setButtonCell(new TaskListCell());

        Button startButton = new Button("Start Working");
        Button cancelButton = new Button("Cancel");

        startButton.setOnAction(e -> {
            Task selectedTask = taskCombo.getValue();
            if (selectedTask != null) {
                currentTask = selectedTask;
                workTimer.setCurrentTaskId(selectedTask.getId().toString());

                // Reset timer to work mode if on break
                if (isOnBreak) {
                    switchToWorkMode();
                }

                // Auto-start the timer
                if (workTimer.getState() != TimerState.RUNNING) {
                    workTimer.start();
                }

                dialog.close();
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(startButton, cancelButton);

        layout.getChildren().addAll(titleLabel, taskCombo, buttonBox);

        Scene scene = new Scene(layout, 400, 200);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void openTaskManagement() {
        TaskManagementWindow taskMgmt = new TaskManagementWindow(tasks, questManager, pixelFont);
        taskMgmt.setOnTasksUpdated(() -> updateQuestProgress());
        taskMgmt.show();
    }

    private void openStatisticsAndManagement() {
        // Use the integrated QuestAndStatsGUI which includes both stats and management
        Stage currentStage = (Stage) startButton.getScene().getWindow();
        questStatsGUI.showStatisticsWindow(currentStage, tasks, sessionManager.getSessionHistory());
    }

    private void toggleTimer() {
        PomodoroTimer currentTimer = isOnBreak ? breakTimer : workTimer;

        if (currentTimer.getState() == TimerState.RUNNING) {
            currentTimer.pause();
            startButton.setText("START");
        } else {
            if (!isOnBreak && currentTask == null) {
                showAlert("No Task Selected", "Please select a task first using 'Start Working' button!");
                return;
            }
            currentTimer.start();
            startButton.setText("PAUSE");
        }
    }

    private void switchToWorkMode() {
        isOnBreak = false;
        timerTypeLabel.setText("WORK SESSION");
        updateTimerDisplay(25 * 60);
    }

    private void switchToBreakMode() {
        isOnBreak = true;
        timerTypeLabel.setText("BREAK TIME");
        updateTimerDisplay(5 * 60);
    }

    private void toggleSound() {
        if (isMuted) {
            if (backgroundMusic != null) {
                backgroundMusic.play();
            }
            soundIcon.setOpacity(1.0);
            userPreferences.setMusicEnabled(true);
        } else {
            if (backgroundMusic != null) {
                backgroundMusic.pause();
            }
            soundIcon.setOpacity(0.5);
            userPreferences.setMusicEnabled(false);
        }
        isMuted = !isMuted;
    }

    private void toggleNotepad() {
        notepadPane.setVisible(!isNotepadOpen);
        isNotepadOpen = !isNotepadOpen;
    }

    private void toggleInfo() {
        infoPane.setVisible(!isInfoOpen);
        isInfoOpen = !isInfoOpen;
    }

    private void toggleFullscreen() {
        Stage stage = (Stage) startButton.getScene().getWindow();
        boolean isCurrentlyFullscreen = stage.isFullScreen();

        stage.setFullScreen(!isCurrentlyFullscreen);
        userPreferences.setFullscreenEnabled(!isCurrentlyFullscreen);

        // Update background size when switching modes
        if (!isCurrentlyFullscreen) {
            // Going to fullscreen - background will auto-resize due to bindings
            backgroundImageView.setPreserveRatio(false);
        } else {
            // Going to windowed - might want to preserve ratio
            backgroundImageView.setPreserveRatio(false);
        }
    }

    private void updateTimerDisplay(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, remainingSeconds));
    }

    private void updateQuestProgress() {
        // Find active quest (quest with incomplete tasks)
        Optional<Quest> activeQuest = questManager.getAllQuests().values().stream()
                .filter(quest -> !quest.isCompleted())
                .filter(quest -> quest.getTasks().stream().anyMatch(task -> tasks.contains(task)))
                .findFirst();

        if (activeQuest.isPresent()) {
            Quest quest = activeQuest.get();
            questProgressLabel.setText(String.format("%s: %d%% complete",
                    quest.getTitle(), quest.getProgressPercentage()));
        } else {
            questProgressLabel.setText("No active quest");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Timer Event Listeners
    @Override
    public void onTimerStarted(com.focusflow.core.timer.Timer timer) {
        Platform.runLater(() -> startButton.setText("PAUSE"));
    }

    @Override
    public void onTimerPaused(com.focusflow.core.timer.Timer timer) {
        Platform.runLater(() -> startButton.setText("START"));
    }

    @Override
    public void onTimerResumed(com.focusflow.core.timer.Timer timer) {
        Platform.runLater(() -> startButton.setText("PAUSE"));
    }

    @Override
    public void onTimerCompleted(com.focusflow.core.timer.Timer timer) {
        Platform.runLater(() -> {
            startButton.setText("START");

            if (isOnBreak) {
                // Break completed - prompt for next work session
                switchToWorkMode();
                showAlert("Break Complete!",
                        "Ready for another work session?\nClick 'Start Working' to select a task.");
            } else {
                // Work session completed - start break
                switchToBreakMode();
                breakTimer.start();
                showAlert("Work Session Complete!", "Great job! Starting a 5-minute break.");
            }
        });
    }

    @Override
    public void onTimerStopped(com.focusflow.core.timer.Timer timer) {
        Platform.runLater(() -> startButton.setText("START"));
    }

    @Override
    public void onTimerTick(com.focusflow.core.timer.Timer timer, int remainingSeconds) {
        Platform.runLater(() -> updateTimerDisplay(remainingSeconds));
    }

    @Override
    public void onTimerReset(com.focusflow.core.timer.Timer timer) {
        Platform.runLater(() -> startButton.setText("START"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}