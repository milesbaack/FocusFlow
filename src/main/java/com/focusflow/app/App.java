package com.focusflow.app;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.focusflow.app.ui.BackgroundManager;
import com.focusflow.app.ui.OverlayManager;
import com.focusflow.app.ui.QuestCreationPanel;
import com.focusflow.app.ui.StatsCreationPanel;
import com.focusflow.app.ui.TaskCreationPanel;
import com.focusflow.app.ui.TaskManagementPanel;
import com.focusflow.app.ui.TaskSelectionPanel;
import com.focusflow.app.ui.WallpaperSelectionDialog;
import com.focusflow.core.gameify.AchievementManager;
import com.focusflow.core.gameify.Quest;
import com.focusflow.core.gameify.QuestManager;
import com.focusflow.core.gameify.XpManager;
import com.focusflow.core.preferences.UserPreferences;
import com.focusflow.core.session.SessionManager;
import com.focusflow.core.task.Task;
import com.focusflow.core.timer.PomodoroTimer;
import com.focusflow.core.timer.TimerEventListener;
import com.focusflow.core.timer.TimerState;
import com.focusflow.core.timer.TimerType;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * The App class serves as the main JavaFX application for the FocusFlow app.
 * Updated to use seamless single-screen navigation with overlay panels.
 *
 * @author Miles Baack, Emilio Lopez, and Brisa Rueda
 * @version 5.0.0 - Seamless UX Update
 */
public class App extends Application implements TimerEventListener {
    // Core managers
    private PomodoroTimer workTimer;
    private PomodoroTimer breakTimer;
    private SessionManager sessionManager;
    private QuestManager questManager;
    private AchievementManager achievementManager;
    private XpManager xpManager;
    private Task currentTask;
    private List<Task> tasks = new ArrayList<>();
    private boolean isOnBreak = false;

    // UI State
    private UserPreferences userPreferences;
    private ImageView backgroundImageView;
    private OverlayManager overlayManager;
    private Font pixelFont;

    // UI Components - Timer Area
    private Label timerLabel;
    private Label timerTypeLabel;
    private Label currentTaskLabel;
    private Button startButton;
    private Button startWorkingButton;

    // UI Components - Bottom Area
    private ProgressBar progressBar;
    private Label questProgressLabel;

    // UI Components - Sidebar
    private ImageView soundIcon;
    private MediaPlayer backgroundMusic;
    private boolean isMuted = false;

    @Override
    public void start(Stage stage) {
        userPreferences = new UserPreferences();

        loadPixelFont();
        initializeManagers();
        setupBackgroundMusic();

        Scene scene = new Scene(createMainLayout(), 1070, 610);
        stage.setScene(scene);
        stage.setTitle("FocusFlow");

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

        workTimer = new PomodoroTimer(TimerType.WORK, 25 * 60);
        breakTimer = new PomodoroTimer(TimerType.SHORT_BREAK, 5 * 60);
        workTimer.addListener(this);
        breakTimer.addListener(this);
    }

    private AnchorPane createMainLayout() {
        AnchorPane root = new AnchorPane();

        // Background setup
        setupBackground(root);

        // Create unified layout
        BorderPane unifiedLayout = new BorderPane();

        // LEFT: Simplified sidebar
        VBox sidebar = createSimplifiedSidebar();
        sidebar.prefWidthProperty().bind(root.widthProperty().multiply(0.08));

        // CENTER: Main content area with overlay support
        StackPane mainContent = new StackPane();

        // Create persistent timer area
        VBox timerArea = createPersistentTimerArea();
        StackPane.setAlignment(timerArea, Pos.TOP_CENTER);

        // Create overlay container
        StackPane overlayContainer = new StackPane();

        mainContent.getChildren().addAll(timerArea, overlayContainer);

        // Initialize overlay manager
        this.overlayManager = new OverlayManager(overlayContainer);

        // BOTTOM: Action bar and progress
        VBox bottomArea = createBottomArea();
        bottomArea.prefHeightProperty().bind(root.heightProperty().multiply(0.15));

        unifiedLayout.setLeft(sidebar);
        unifiedLayout.setCenter(mainContent);
        unifiedLayout.setBottom(bottomArea);

        root.getChildren().add(unifiedLayout);
        AnchorPane.setTopAnchor(unifiedLayout, 0.0);
        AnchorPane.setBottomAnchor(unifiedLayout, 0.0);
        AnchorPane.setLeftAnchor(unifiedLayout, 0.0);
        AnchorPane.setRightAnchor(unifiedLayout, 0.0);

        return root;
    }

    private void setupBackground(AnchorPane root) {
        String selectedWallpaper = userPreferences.getSelectedWallpaper();
        backgroundImageView = BackgroundManager.createBackgroundImageView(
                selectedWallpaper, 1070, 610);
        backgroundImageView.fitWidthProperty().bind(root.widthProperty());
        backgroundImageView.fitHeightProperty().bind(root.heightProperty());
        AnchorPane.setTopAnchor(backgroundImageView, 0.0);
        AnchorPane.setLeftAnchor(backgroundImageView, 0.0);
        root.getChildren().add(backgroundImageView);
    }

    private VBox createPersistentTimerArea() {
        VBox timerSection = new VBox(15);
        timerSection.setAlignment(Pos.CENTER);
        timerSection.setPadding(new Insets(30));
        timerSection.setMaxHeight(Region.USE_PREF_SIZE);
        timerSection.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                "-fx-background-radius: 15; -fx-padding: 30;");

        // Current task indicator
        currentTaskLabel = new Label("No task selected");
        currentTaskLabel.setFont(Font.font(pixelFont.getFamily(), 14));
        currentTaskLabel.setTextFill(Color.DARKSLATEGRAY);

        // Timer display
        timerTypeLabel = new Label("WORK SESSION");
        timerTypeLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 18));
        timerTypeLabel.setTextFill(Color.BLACK);

        timerLabel = new Label("25:00");
        timerLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 48));
        timerLabel.setTextFill(Color.BLACK);

        Label remainingLabel = new Label("REMAINING...");
        remainingLabel.setFont(Font.font(pixelFont.getFamily(), 14));
        remainingLabel.setTextFill(Color.DARKSLATEGRAY);

        // Control buttons
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER);

        startWorkingButton = new Button("Select Task");
        startWorkingButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-background-radius: 15; -fx-padding: 10 20; -fx-font-weight: bold;");
        startWorkingButton.setOnAction(e -> showTaskSelectionOverlay());

        startButton = new Button("START");
        startButton.setStyle("-fx-background-color: white; -fx-text-fill: black; " +
                "-fx-background-radius: 15; -fx-padding: 10 20; -fx-font-weight: bold;");
        startButton.setOnAction(e -> toggleTimer());

        controls.getChildren().addAll(startWorkingButton, startButton);

        timerSection.getChildren().addAll(
                currentTaskLabel, timerTypeLabel, timerLabel, remainingLabel, controls);

        return timerSection;
    }

    private VBox createBottomArea() {
        VBox bottomArea = new VBox(10);
        bottomArea.setPadding(new Insets(15));

        // Quick action buttons
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER);

        Button addTaskBtn = new Button("+ Add Task");
        Button viewTasksBtn = new Button("📋 Tasks");
        Button questsBtn = new Button("🏆 Create Quest");
        Button statsBtn = new Button("📊 Stats");

        // Style buttons
        String buttonStyle = "-fx-background-color: rgba(255,255,255,0.8); " +
                "-fx-background-radius: 10; -fx-padding: 8 15; " +
                "-fx-font-weight: bold; -fx-cursor: hand;";

        addTaskBtn.setStyle(buttonStyle + "-fx-background-color: #2196F3; -fx-text-fill: white;");
        viewTasksBtn.setStyle(buttonStyle);
        questsBtn.setStyle(buttonStyle);
        statsBtn.setStyle(buttonStyle);

        // Add click handlers
        addTaskBtn.setOnAction(e -> showTaskCreationOverlay());
        viewTasksBtn.setOnAction(e -> showTaskManagementOverlay());
        questsBtn.setOnAction(e -> showQuestManagementOverlay());
        statsBtn.setOnAction(e -> showStatsOverlay());

        actionButtons.getChildren().addAll(addTaskBtn, viewTasksBtn, questsBtn, statsBtn);

        // Progress area
        HBox progressArea = new HBox(20);
        progressArea.setAlignment(Pos.CENTER);

        questProgressLabel = new Label("No active quest");
        questProgressLabel.setFont(Font.font(pixelFont.getFamily(), 14));
        questProgressLabel.setTextFill(Color.BLACK);

        progressBar = new ProgressBar(0.0);
        progressBar.setPrefWidth(300);
        progressBar.setPrefHeight(20);
        progressBar.setStyle("-fx-accent: #FF9800;");

        Label xpLabel = new Label("Level " + xpManager.getCurrentLevel());
        xpLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 14));

        progressArea.getChildren().addAll(questProgressLabel, progressBar, xpLabel);

        bottomArea.getChildren().addAll(actionButtons, progressArea);
        return bottomArea;
    }

    private VBox createSimplifiedSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.setAlignment(Pos.CENTER);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setStyle("-fx-background-color: rgba(0,0,0,0.1);");

        String iconStyle = "-fx-cursor: hand; -fx-background-color: transparent;";

        // Settings/wallpaper
        ImageView wallpaperIcon = new ImageView(new Image(
                getClass().getResource("/UI/BackgroundIcon.png").toString(), 30, 30, true, true));
        Button wallpaperBtn = new Button();
        wallpaperBtn.setGraphic(wallpaperIcon);
        wallpaperBtn.setStyle(iconStyle);
        wallpaperBtn.setOnAction(e -> showWallpaperSelection());

        // Sound toggle
        soundIcon = new ImageView(new Image(
                getClass().getResource("/UI/sound.png").toString(), 30, 30, true, true));
        Button soundBtn = new Button();
        soundBtn.setGraphic(soundIcon);
        soundBtn.setStyle(iconStyle);
        soundBtn.setOnAction(e -> toggleSound());

        // Info/help
        ImageView helpIcon = new ImageView(new Image(
                getClass().getResource("/UI/question.png").toString(), 30, 30, true, true));
        Button helpBtn = new Button();
        helpBtn.setGraphic(helpIcon);
        helpBtn.setStyle(iconStyle);
        helpBtn.setOnAction(e -> showHelpOverlay());

        sidebar.getChildren().addAll(wallpaperBtn, soundBtn, helpBtn);
        return sidebar;
    }

    // Overlay show methods
    private void showTaskCreationOverlay() {
        TaskCreationPanel panel = new TaskCreationPanel(
                overlayManager, pixelFont, tasks, questManager, this::onTasksUpdated);
        overlayManager.showOverlay(panel, OverlayManager.AnimationType.SLIDE_UP);
    }

    private void showTaskManagementOverlay() {
        TaskManagementPanel panel = new TaskManagementPanel(
                overlayManager, pixelFont, tasks, this::onTasksUpdated);
        overlayManager.showOverlay(panel, OverlayManager.AnimationType.SLIDE_UP);
    }

    private void showTaskSelectionOverlay() {
        TaskSelectionPanel panel = new TaskSelectionPanel(
                overlayManager, pixelFont, tasks, this::onTaskSelected);
        overlayManager.showOverlay(panel, OverlayManager.AnimationType.FADE_IN);
    }

    private void showQuestManagementOverlay() {
        QuestCreationPanel panel = new QuestCreationPanel(
                overlayManager, pixelFont, tasks, questManager, this::onQuestsUpdated);
        overlayManager.showOverlay(panel, OverlayManager.AnimationType.SLIDE_UP);
    }

    private void showStatsOverlay() {
        StatsCreationPanel panel = new StatsCreationPanel(
                overlayManager, pixelFont, tasks, sessionManager,
                questManager, achievementManager, xpManager);
        overlayManager.showOverlay(panel, OverlayManager.AnimationType.FADE_IN);
    }

    private void showHelpOverlay() {
        VBox helpPanel = new VBox(15);
        helpPanel.setPadding(new Insets(20));
        helpPanel.setStyle("-fx-background-color: white; -fx-background-radius: 15 15 0 0; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, -5);");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("FocusFlow Help");
        title.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 20));
        title.setTextFill(Color.DARKBLUE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: gray; " +
                "-fx-font-size: 16px; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> overlayManager.hideCurrentOverlay());

        header.getChildren().addAll(title, spacer, closeBtn);

        TextArea helpText = new TextArea();
        helpText.setText("FocusFlow - Productivity with Gamification\n\n" +
                "Quick Start:\n" +
                "1. Click '+ Add Task' to create your first task\n" +
                "2. Click 'Select Task' to choose what to work on\n" +
                "3. Click 'START' to begin your 25-minute focus session\n\n" +
                "Navigation:\n" +
                "• All functions are now accessible without popups\n" +
                "• Use the bottom action bar for quick access\n" +
                "• Timer stays visible while managing tasks\n\n" +
                "Keyboard Shortcuts:\n" +
                "• Ctrl+N: New task\n" +
                "• Ctrl+T: Task management\n" +
                "• Ctrl+Q: Quest management\n" +
                "• Ctrl+S: Statistics\n" +
                "• Space: Start/Pause timer\n" +
                "• Escape: Close overlay\n" +
                "• F11: Fullscreen mode\n\n" +
                "Tips:\n" +
                "• Double-click tasks to edit them inline\n" +
                "• Use the quick-add field for rapid task entry\n" +
                "• Check the progress bar to see quest completion");
        helpText.setWrapText(true);
        helpText.setEditable(false);
        helpText.setPrefHeight(300);

        Button gotItBtn = new Button("Got it!");
        gotItBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: bold;");
        gotItBtn.setOnAction(e -> overlayManager.hideCurrentOverlay());

        helpPanel.getChildren().addAll(header, helpText, gotItBtn);

        overlayManager.showOverlay(helpPanel, OverlayManager.AnimationType.FADE_IN);
    }

    // Helper methods
    private HBox createStatRow(String label, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelNode = new Label(label);
        labelNode.setFont(Font.font(pixelFont.getFamily(), 14));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label valueNode = new Label(value);
        valueNode.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 14));
        valueNode.setStyle("-fx-text-fill: #2196F3;");

        row.getChildren().addAll(labelNode, spacer, valueNode);
        return row;
    }

    private void onTaskSelected(Task task) {
        currentTask = task;
        currentTaskLabel.setText("Working on: " + task.getName());
        workTimer.setCurrentTaskId(task.getId().toString());

        // Switch to work mode if on break
        if (isOnBreak) {
            switchToWorkMode();
        }

        // Auto-start timer if not running
        if (workTimer.getState() != TimerState.RUNNING) {
            workTimer.start();
        }
        updateQuestProgress();

    }

    private void onTasksUpdated(Runnable callback) {
        updateQuestProgress();
        if (callback != null) {
            callback.run();
        }
    }

    // Existing methods (kept from original)
    private void setupBackgroundMusic() {
        try {
            Media sound = new Media(getClass().getResource("/UI/background_music.wav").toString());
            backgroundMusic = new MediaPlayer(sound);
            backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);

            if (userPreferences.isMusicEnabled()) {
                backgroundMusic.play();
            }
        } catch (Exception e) {
            System.out.println("Could not load background music: " + e.getMessage());
        }
    }

    private void showWallpaperSelection() {
        Stage currentStage = (Stage) startButton.getScene().getWindow();

        WallpaperSelectionDialog wallpaperDialog = new WallpaperSelectionDialog(
                currentStage,
                userPreferences,
                pixelFont,
                this::onWallpaperChanged);

        wallpaperDialog.show();
    }

    private void onWallpaperChanged(String newWallpaperFilename) {
        Scene currentScene = backgroundImageView.getScene();
        double width = currentScene.getWidth();
        double height = currentScene.getHeight();

        ImageView newBackground = BackgroundManager.createBackgroundImageView(
                newWallpaperFilename, width, height);

        newBackground.fitWidthProperty().bind(currentScene.widthProperty());
        newBackground.fitHeightProperty().bind(currentScene.heightProperty());

        AnchorPane root = (AnchorPane) currentScene.getRoot();
        root.getChildren().remove(backgroundImageView);
        root.getChildren().add(0, newBackground);

        backgroundImageView = newBackground;

        AnchorPane.setTopAnchor(backgroundImageView, 0.0);
        AnchorPane.setLeftAnchor(backgroundImageView, 0.0);
    }

    private void toggleTimer() {
        // Don't start timer if an overlay is open
        if (overlayManager.isOverlayShowing()) {
            return;
        }

        PomodoroTimer currentTimer = isOnBreak ? breakTimer : workTimer;

        if (currentTimer.getState() == TimerState.RUNNING) {
            currentTimer.pause();
            startButton.setText("START");
        } else {
            if (!isOnBreak && currentTask == null) {
                showAlert("No Task Selected", "Please select a task first using 'Select Task' button!");
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

    private void toggleFullscreen() {
        Stage stage = (Stage) startButton.getScene().getWindow();
        boolean isCurrentlyFullscreen = stage.isFullScreen();

        stage.setFullScreen(!isCurrentlyFullscreen);
        userPreferences.setFullscreenEnabled(!isCurrentlyFullscreen);

        backgroundImageView.setPreserveRatio(false);
    }

    private void updateTimerDisplay(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        String timeText = String.format("%02d:%02d", minutes, remainingSeconds);

        // Add color transition based on time remaining
        if (seconds <= 60) { // Last minute - red
            timerLabel.setStyle("-fx-text-fill: #F44336; -fx-font-family: '" + pixelFont.getFamily()
                    + "'; -fx-font-size: 48px; -fx-font-weight: bold;");
        } else if (seconds <= 300) { // Last 5 minutes - orange
            timerLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-family: '" + pixelFont.getFamily()
                    + "'; -fx-font-size: 48px; -fx-font-weight: bold;");
        } else {
            timerLabel.setStyle("-fx-text-fill: black; -fx-font-family: '" + pixelFont.getFamily()
                    + "'; -fx-font-size: 48px; -fx-font-weight: bold;");
        }

        timerLabel.setText(timeText);
    }

    private void updateQuestProgress() {
        // Find active quest (quest with incomplete tasks)
        Quest activeQuest = questManager.getAllQuests().values().stream()
                .filter(quest -> !quest.isCompleted())
                .filter(quest -> quest.getTasks().stream().anyMatch(task -> tasks.contains(task)))
                .findFirst().orElse(null);

        if (activeQuest != null) {
            questProgressLabel.setText(String.format("%s: %d%% complete",
                    activeQuest.getTitle(), activeQuest.getProgressPercentage()));
            progressBar.setProgress(activeQuest.getProgressPercentage() / 100.0);
        } else {
            questProgressLabel.setText("No active quest");
            progressBar.setProgress(0.0);
        }
    }

    private void setupKeyboardShortcuts(Scene scene) {
        scene.setOnKeyPressed(e -> {
            // Existing shortcuts
            if (e.getCode() == KeyCode.F11) {
                toggleFullscreen();
                return;
            }

            // New shortcuts for seamless navigation
            if (e.isControlDown()) {
                switch (e.getCode()) {
                    case N: // Ctrl+N: New task
                        showTaskCreationOverlay();
                        break;
                    case T: // Ctrl+T: Task management
                        showTaskManagementOverlay();
                        break;
                    case Q: // Ctrl+Q: Quest management
                        showQuestManagementOverlay();
                        break;
                    case S: // Ctrl+S: Stats
                        showStatsOverlay();
                        break;
                    case B: // Ctrl+B: Background
                        showWallpaperSelection();
                        break;
                    case M: // Ctrl+M: Mute
                        toggleSound();
                        break;
                    case H: // Ctrl+H: Help
                        showHelpOverlay();
                        break;
                }
            }

            // ESC to close overlay
            if (e.getCode() == KeyCode.ESCAPE) {
                overlayManager.hideCurrentOverlay();
            }

            // Space to start/pause timer
            if (e.getCode() == KeyCode.SPACE && !e.isControlDown()) {
                toggleTimer();
            }
        });
    }

    private void onQuestsUpdated(Runnable callback) {
        updateQuestProgress();
        if (callback != null) {
            callback.run();
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
                        "Ready for another work session?\nClick 'Select Task' to choose a task.");
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