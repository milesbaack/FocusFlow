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
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
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
 * Enhanced with responsive design and fixed timer functionality.
 *
 * @author Miles Baack, Emilio Lopez, and Brisa Rueda
 * @version 5.1.0 - Responsive Design & Timer Fixes
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
    private Scene mainScene;
    private Stage primaryStage;

    // UI Components - Timer Area
    private Label timerLabel;
    private Label timerTypeLabel;
    private Label currentTaskLabel;
    private Button startButton;
    private Button startWorkingButton;
    private VBox timerSection;

    // UI Components - Bottom Area
    private ProgressBar progressBar;
    private Label questProgressLabel;
    private VBox bottomArea;

    // UI Components - Sidebar
    private ImageView soundIcon;
    private MediaPlayer backgroundMusic;
    private boolean isMuted = false;
    private VBox sidebar;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        userPreferences = new UserPreferences();

        loadPixelFont();
        initializeManagers();
        setupBackgroundMusic();

        mainScene = new Scene(createMainLayout(), 1070, 610);
        stage.setScene(mainScene);
        stage.setTitle("FocusFlow");

        if (userPreferences.isFullscreenEnabled()) {
            stage.setFullScreen(true);
            stage.setResizable(true);
        } else {
            stage.setResizable(true); // Allow resizing
        }

        setupKeyboardShortcuts(mainScene);
        setupResponsiveDesign();
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

        // Initialize timers with proper setup
        workTimer = new PomodoroTimer(TimerType.WORK, 25 * 60);
        breakTimer = new PomodoroTimer(TimerType.SHORT_BREAK, 5 * 60);
        
        // Add listeners
        workTimer.addListener(this);
        breakTimer.addListener(this);
        
        System.out.println("Timers initialized: Work=" + workTimer.getRemainingTime() + "s, Break=" + breakTimer.getRemainingTime() + "s");
    }

    private AnchorPane createMainLayout() {
        AnchorPane root = new AnchorPane();

        // Background setup
        setupBackground(root);

        // Create unified layout
        BorderPane unifiedLayout = new BorderPane();

        // LEFT: Simplified sidebar
        sidebar = createSimplifiedSidebar();

        // CENTER: Main content area with overlay support
        StackPane mainContent = new StackPane();

        // Create persistent timer area
        timerSection = createPersistentTimerArea();
        StackPane.setAlignment(timerSection, Pos.TOP_CENTER);

        // Create overlay container
        StackPane overlayContainer = new StackPane();

        mainContent.getChildren().addAll(timerSection, overlayContainer);

        // Initialize overlay manager
        this.overlayManager = new OverlayManager(overlayContainer);

        // BOTTOM: Action bar and progress
        bottomArea = createBottomArea();

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

    private void setupResponsiveDesign() {
        // Setup responsive bindings for main components
        if (mainScene != null) {
            // Sidebar responsive width
            sidebar.prefWidthProperty().bind(
                Bindings.max(60, // Minimum width
                    mainScene.widthProperty().multiply(0.08)
                )
            );

            // Timer area responsive sizing
            timerSection.paddingProperty().bind(
                Bindings.createObjectBinding(() -> {
                    double width = mainScene.getWidth();
                    double basePadding = width < 800 ? 20 : 30;
                    return new Insets(basePadding);
                }, mainScene.widthProperty())
            );

            timerSection.spacingProperty().bind(
                Bindings.createDoubleBinding(() -> {
                    return mainScene.getWidth() < 800 ? 12.0 : 15.0;
                }, mainScene.widthProperty())
            );

            // Bottom area responsive height
            bottomArea.prefHeightProperty().bind(
                Bindings.max(100, // Minimum height
                    mainScene.heightProperty().multiply(0.15)
                )
            );

            // Timer label responsive font size
            timerLabel.fontProperty().bind(
                Bindings.createObjectBinding(() -> {
                    double width = mainScene.getWidth();
                    double baseSize = width < 800 ? 36.0 : 48.0;
                    return Font.font(pixelFont.getFamily(), FontWeight.BOLD, baseSize);
                }, mainScene.widthProperty())
            );

            // Timer type label responsive font size  
            timerTypeLabel.fontProperty().bind(
                Bindings.createObjectBinding(() -> {
                    double width = mainScene.getWidth();
                    double baseSize = width < 800 ? 14.0 : 18.0;
                    return Font.font(pixelFont.getFamily(), FontWeight.BOLD, baseSize);
                }, mainScene.widthProperty())
            );

            // Current task label responsive font size
            currentTaskLabel.fontProperty().bind(
                Bindings.createObjectBinding(() -> {
                    double width = mainScene.getWidth();
                    double baseSize = width < 800 ? 12.0 : 14.0;
                    return Font.font(pixelFont.getFamily(), FontWeight.NORMAL, baseSize);
                }, mainScene.widthProperty())
            );
        }
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
        VBox timerSection = new VBox();
        timerSection.setAlignment(Pos.CENTER);
        timerSection.setMaxHeight(Region.USE_PREF_SIZE);
        timerSection.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                "-fx-background-radius: 15;");

        // Current task indicator
        currentTaskLabel = new Label("No task selected");
        currentTaskLabel.setTextFill(Color.DARKSLATEGRAY);

        // Timer display
        timerTypeLabel = new Label("WORK SESSION");
        timerTypeLabel.setTextFill(Color.BLACK);

        timerLabel = new Label("25:00");
        timerLabel.setTextFill(Color.BLACK);

        Label remainingLabel = new Label("REMAINING...");
        remainingLabel.setFont(Font.font(pixelFont.getFamily(), 14));
        remainingLabel.setTextFill(Color.DARKSLATEGRAY);

        // Control buttons
        HBox controls = new HBox();
        controls.setAlignment(Pos.CENTER);
        
        // Set initial spacing and update based on scene width
        controls.setSpacing(15);
        if (mainScene != null) {
            controls.spacingProperty().bind(
                Bindings.createDoubleBinding(() -> {
                    return mainScene.getWidth() < 800 ? 10.0 : 15.0;
                }, mainScene.widthProperty())
            );
        }

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

        // Initial timer display
        updateTimerDisplay(workTimer.getRemainingTime());

        return timerSection;
    }

    private VBox createBottomArea() {
        VBox bottomArea = new VBox();
        bottomArea.paddingProperty().bind(
            Bindings.createObjectBinding(() -> {
                double width = mainScene != null ? mainScene.getWidth() : 1070;
                double basePadding = width < 800 ? 12 : 15;
                return new Insets(basePadding);
            }, mainScene != null ? mainScene.widthProperty() : 
                javafx.beans.binding.Bindings.createDoubleBinding(() -> 15.0))
        );
        bottomArea.spacingProperty().bind(
            Bindings.createDoubleBinding(() -> {
                if (mainScene == null) return 10.0;
                return mainScene.getWidth() < 800 ? 8.0 : 10.0;
            }, mainScene != null ? mainScene.widthProperty() : 
                javafx.beans.binding.Bindings.createDoubleBinding(() -> 10.0))
        );

        // Quick action buttons
        HBox actionButtons = new HBox();
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setSpacing(15);
        
        if (mainScene != null) {
            actionButtons.spacingProperty().bind(
                Bindings.createDoubleBinding(() -> {
                    return mainScene.getWidth() < 800 ? 10.0 : 15.0;
                }, mainScene.widthProperty())
            );
        }

        Button addTaskBtn = new Button("+ Add Task");
        Button viewTasksBtn = new Button("📋 Tasks");
        Button questsBtn = new Button("🏆 Create Quest");
        Button statsBtn = new Button("📊 Stats");

        // Style buttons with responsive sizing
        String buttonStyle = "-fx-background-color: rgba(255,255,255,0.8); " +
                "-fx-background-radius: 10; -fx-padding: 8 15; " +
                "-fx-font-weight: bold; -fx-cursor: hand;";

        addTaskBtn.setStyle(buttonStyle + "-fx-background-color: #2196F3; -fx-text-fill: white;");
        viewTasksBtn.setStyle(buttonStyle);
        questsBtn.setStyle(buttonStyle);
        statsBtn.setStyle(buttonStyle);

        // Responsive font sizes for buttons
        if (mainScene != null) {
            addTaskBtn.fontProperty().bind(
                Bindings.createObjectBinding(() -> {
                    double width = mainScene.getWidth();
                    double baseSize = width < 800 ? 11.0 : 12.0;
                    return Font.font(pixelFont.getFamily(), FontWeight.BOLD, baseSize);
                }, mainScene.widthProperty())
            );
            viewTasksBtn.fontProperty().bind(addTaskBtn.fontProperty());
            questsBtn.fontProperty().bind(addTaskBtn.fontProperty());
            statsBtn.fontProperty().bind(addTaskBtn.fontProperty());
        }

        // Add click handlers
        addTaskBtn.setOnAction(e -> showTaskCreationOverlay());
        viewTasksBtn.setOnAction(e -> showTaskManagementOverlay());
        questsBtn.setOnAction(e -> showQuestManagementOverlay());
        statsBtn.setOnAction(e -> showStatsOverlay());

        actionButtons.getChildren().addAll(addTaskBtn, viewTasksBtn, questsBtn, statsBtn);

        // Progress area
        HBox progressArea = new HBox();
        progressArea.setAlignment(Pos.CENTER);
        progressArea.setSpacing(20);
        
        if (mainScene != null) {
            progressArea.spacingProperty().bind(
                Bindings.createDoubleBinding(() -> {
                    return mainScene.getWidth() < 800 ? 15.0 : 20.0;
                }, mainScene.widthProperty())
            );
        }

        questProgressLabel = new Label("No active quest");
        questProgressLabel.setTextFill(Color.BLACK);
        if (mainScene != null) {
            questProgressLabel.fontProperty().bind(
                Bindings.createObjectBinding(() -> {
                    double width = mainScene.getWidth();
                    double baseSize = width < 800 ? 12.0 : 14.0;
                    return Font.font(pixelFont.getFamily(), FontWeight.NORMAL, baseSize);
                }, mainScene.widthProperty())
            );
        }

        progressBar = new ProgressBar(0.0);
        progressBar.setPrefWidth(300);
        progressBar.setPrefHeight(20);
        progressBar.setStyle("-fx-accent: #FF9800;");
        
        if (mainScene != null) {
            progressBar.prefWidthProperty().bind(
                Bindings.createDoubleBinding(() -> {
                    return mainScene.getWidth() < 800 ? 200.0 : 300.0;
                }, mainScene.widthProperty())
            );
        }

        Label xpLabel = new Label("Level " + xpManager.getCurrentLevel());
        if (mainScene != null) {
            xpLabel.fontProperty().bind(
                Bindings.createObjectBinding(() -> {
                    double width = mainScene.getWidth();
                    double baseSize = width < 800 ? 12.0 : 14.0;
                    return Font.font(pixelFont.getFamily(), FontWeight.BOLD, baseSize);
                }, mainScene.widthProperty())
            );
        }

        progressArea.getChildren().addAll(questProgressLabel, progressBar, xpLabel);

        bottomArea.getChildren().addAll(actionButtons, progressArea);
        return bottomArea;
    }

    private VBox createSimplifiedSidebar() {
        VBox sidebar = new VBox();
        sidebar.setAlignment(Pos.CENTER);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setSpacing(20);
        sidebar.setStyle("-fx-background-color: rgba(0,0,0,0.1);");
        
        if (mainScene != null) {
            sidebar.paddingProperty().bind(
                Bindings.createObjectBinding(() -> {
                    double basePadding = mainScene.getWidth() < 800 ? 15.0 : 20.0;
                    return new Insets(basePadding, 10, basePadding, 10);
                }, mainScene.widthProperty())
            );
            
            sidebar.spacingProperty().bind(
                Bindings.createDoubleBinding(() -> {
                    return mainScene.getWidth() < 800 ? 15.0 : 20.0;
                }, mainScene.widthProperty())
            );
        }

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

        Label helpText = new Label();
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
        helpText.setPrefWidth(400);
        helpText.setMaxHeight(300);
        helpText.setStyle("-fx-padding: 10;");

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
        
        // Set task ID for both timers
        workTimer.setCurrentTaskId(task.getId().toString());
        
        System.out.println("Task selected: " + task.getName() + " (ID: " + task.getId().toString() + ")");

        // Switch to work mode if on break
        if (isOnBreak) {
            switchToWorkMode();
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
        WallpaperSelectionDialog wallpaperDialog = new WallpaperSelectionDialog(
                primaryStage,
                userPreferences,
                pixelFont,
                this::onWallpaperChanged);

        wallpaperDialog.show();
    }

    private void onWallpaperChanged(String newWallpaperFilename) {
        double width = mainScene.getWidth();
        double height = mainScene.getHeight();

        ImageView newBackground = BackgroundManager.createBackgroundImageView(
                newWallpaperFilename, width, height);

        newBackground.fitWidthProperty().bind(mainScene.widthProperty());
        newBackground.fitHeightProperty().bind(mainScene.heightProperty());

        AnchorPane root = (AnchorPane) mainScene.getRoot();
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

        System.out.println("Toggle timer called. Current state: " + currentTimer.getState() + 
                          ", isOnBreak: " + isOnBreak + 
                          ", currentTask: " + (currentTask != null ? currentTask.getName() : "null"));

        if (currentTimer.getState() == TimerState.RUNNING) {
            currentTimer.pause();
            startButton.setText("START");
            System.out.println("Timer paused");
        } else {
            if (!isOnBreak && currentTask == null) {
                showAlert("No Task Selected", "Please select a task first using 'Select Task' button!");
                return;
            }
            
            System.out.println("Starting timer. Remaining seconds: " + currentTimer.getRemainingTime());
            currentTimer.start();
            startButton.setText("PAUSE");
            System.out.println("Timer started");
        }
    }

    private void switchToWorkMode() {
        System.out.println("Switching to work mode");
        isOnBreak = false;
        timerTypeLabel.setText("WORK SESSION");
        
        // Reset work timer
        workTimer.reset();
        updateTimerDisplay(workTimer.getRemainingTime());
        
        // Stop break timer if it's running
        if (breakTimer.getState() == TimerState.RUNNING) {
            breakTimer.stop();
        }
    }

    private void switchToBreakMode() {
        System.out.println("Switching to break mode");
        isOnBreak = true;
        timerTypeLabel.setText("BREAK TIME");
        
        // Reset break timer
        breakTimer.reset();
        updateTimerDisplay(breakTimer.getRemainingTime());
        
        // Stop work timer if it's running
        if (workTimer.getState() == TimerState.RUNNING) {
            workTimer.stop();
        }
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
        boolean isCurrentlyFullscreen = primaryStage.isFullScreen();

        primaryStage.setFullScreen(!isCurrentlyFullscreen);
        userPreferences.setFullscreenEnabled(!isCurrentlyFullscreen);

        backgroundImageView.setPreserveRatio(false);
    }

    private void updateTimerDisplay(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        String timeText = String.format("%02d:%02d", minutes, remainingSeconds);

        // Add color transition based on time remaining
        String baseStyle = "-fx-font-family: '" + pixelFont.getFamily() + "'; -fx-font-weight: bold;";
        
        if (seconds <= 60) { // Last minute - red
            timerLabel.setStyle(baseStyle + "-fx-text-fill: #F44336;");
        } else if (seconds <= 300) { // Last 5 minutes - orange
            timerLabel.setStyle(baseStyle + "-fx-text-fill: #FF9800;");
        } else {
            timerLabel.setStyle(baseStyle + "-fx-text-fill: black;");
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
        Platform.runLater(() -> {
            startButton.setText("PAUSE");
            System.out.println("Timer started event received");
        });
    }

    @Override
    public void onTimerPaused(com.focusflow.core.timer.Timer timer) {
        Platform.runLater(() -> {
            startButton.setText("START");
            System.out.println("Timer paused event received");
        });
    }

    @Override
    public void onTimerResumed(com.focusflow.core.timer.Timer timer) {
        Platform.runLater(() -> {
            startButton.setText("PAUSE");
            System.out.println("Timer resumed event received");
        });
    }

    @Override
    public void onTimerCompleted(com.focusflow.core.timer.Timer timer) {
        Platform.runLater(() -> {
            startButton.setText("START");
            System.out.println("Timer completed event received");

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
        Platform.runLater(() -> {
            startButton.setText("START");
            System.out.println("Timer stopped event received");
        });
    }

    @Override
    public void onTimerTick(com.focusflow.core.timer.Timer timer, int remainingSeconds) {
        Platform.runLater(() -> updateTimerDisplay(remainingSeconds));
    }

    @Override
    public void onTimerReset(com.focusflow.core.timer.Timer timer) {
        Platform.runLater(() -> {
            startButton.setText("START");
            System.out.println("Timer reset event received");
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}