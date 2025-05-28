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
import com.focusflow.app.ui.TimerPanel;
import com.focusflow.app.ui.WallpaperSelectionDialog;
import com.focusflow.core.gameify.AchievementManager;
import com.focusflow.core.gameify.Quest;
import com.focusflow.core.gameify.QuestManager;
import com.focusflow.core.gameify.XpManager;
import com.focusflow.core.preferences.UserPreferences;
import com.focusflow.core.session.SessionManager;
import com.focusflow.core.task.Task;

import javafx.application.Application;
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
 * Updated to use TimerPanel for responsive design and improved timer
 * functionality.
 *
 * @author Miles Baack, Emilio Lopez, and Brisa Rueda
 * @version 6.0.0 - TimerPanel Integration
 */
public class App extends Application {
    // Core managers
    private SessionManager sessionManager;
    private QuestManager questManager;
    private AchievementManager achievementManager;
    private XpManager xpManager;
    private List<Task> tasks = new ArrayList<>();

    // UI State
    private UserPreferences userPreferences;
    private ImageView backgroundImageView;
    private OverlayManager overlayManager;
    private Font pixelFont;
    private Scene mainScene;
    private Stage primaryStage;

    // UI Components
    private TimerPanel timerPanel;
    private ProgressBar progressBar;
    private Label questProgressLabel;
    private VBox bottomArea;
    private VBox sidebar;
    private ImageView soundIcon;
    private MediaPlayer backgroundMusic;
    private boolean isMuted = false;

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
            stage.setResizable(true);
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
    }

    private AnchorPane createMainLayout() {
        AnchorPane root = new AnchorPane();
        setupBackground(root);

        BorderPane unifiedLayout = new BorderPane();

        // LEFT: Sidebar
        sidebar = createSimplifiedSidebar();

        // CENTER: Main content with timer (NO overlay container here)
        StackPane mainContent = new StackPane();

        // Create TimerPanel with callbacks
        timerPanel = new TimerPanel(
                pixelFont,
                (unused) -> showTaskSelectionOverlay(),
                (unused) -> {
                    /* Timer panel handles its own start/pause logic */ });
        StackPane.setAlignment(timerPanel, Pos.TOP_CENTER);

        mainContent.getChildren().add(timerPanel); // Only timer, no overlay container

        // BOTTOM: Action bar
        bottomArea = createBottomArea();

        unifiedLayout.setLeft(sidebar);
        unifiedLayout.setCenter(mainContent);
        unifiedLayout.setBottom(bottomArea);

        // CREATE OVERLAY CONTAINER AT ROOT LEVEL (covers everything)
        StackPane overlayContainer = new StackPane();
        overlayContainer.setVisible(false); // Hidden by default

        // Initialize overlay manager
        this.overlayManager = new OverlayManager(overlayContainer);

        // Add both layout and overlay container to root
        root.getChildren().addAll(unifiedLayout, overlayContainer);

        // Position unifiedLayout
        AnchorPane.setTopAnchor(unifiedLayout, 0.0);
        AnchorPane.setBottomAnchor(unifiedLayout, 0.0);
        AnchorPane.setLeftAnchor(unifiedLayout, 0.0);
        AnchorPane.setRightAnchor(unifiedLayout, 0.0);

        // Position overlay container to cover EVERYTHING
        AnchorPane.setTopAnchor(overlayContainer, 0.0);
        AnchorPane.setBottomAnchor(overlayContainer, 0.0);
        AnchorPane.setLeftAnchor(overlayContainer, 0.0);
        AnchorPane.setRightAnchor(overlayContainer, 0.0);

        return root;
    }

    private void setupResponsiveDesign() {
        if (mainScene != null) {
            // Sidebar responsive width
            sidebar.prefWidthProperty().bind(
                    Bindings.max(60, mainScene.widthProperty().multiply(0.08)));

            // TimerPanel responsive sizing
            timerPanel.prefWidthProperty().bind(
                    Bindings.min(600, mainScene.widthProperty().multiply(0.7)));
            timerPanel.maxWidthProperty().bind(timerPanel.prefWidthProperty());

            // Bottom area responsive height
            bottomArea.prefHeightProperty().bind(
                    Bindings.max(100, mainScene.heightProperty().multiply(0.15)));
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

    private VBox createBottomArea() {
        VBox bottomArea = new VBox();
        bottomArea.setAlignment(Pos.CENTER);
        bottomArea.setPadding(new Insets(15));
        bottomArea.setSpacing(10);

        // Quick action buttons
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER);

        Button addTaskBtn = new Button("+ Add Task");
        Button viewTasksBtn = new Button("📋 Tasks");
        Button questsBtn = new Button("🏆 Create Quest");
        Button statsBtn = new Button("📊 Stats");

        String buttonStyle = "-fx-background-color: rgba(255,255,255,0.8); " +
                "-fx-background-radius: 10; -fx-padding: 8 15; " +
                "-fx-font-weight: bold; -fx-cursor: hand;";

        addTaskBtn.setStyle(buttonStyle + "-fx-background-color: #2196F3; -fx-text-fill: white;");
        viewTasksBtn.setStyle(buttonStyle);
        questsBtn.setStyle(buttonStyle);
        statsBtn.setStyle(buttonStyle);

        // Click handlers
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

    // Overlay methods
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
                "Keyboard Shortcuts:\n" +
                "• Ctrl+N: New task • Ctrl+T: Task management\n" +
                "• Ctrl+Q: Quest management • Ctrl+S: Statistics\n" +
                "• Space: Start/Pause timer • Escape: Close overlay\n" +
                "• F11: Fullscreen mode\n\n" +
                "The timer will automatically handle work/break cycles.");
        helpText.setWrapText(true);
        helpText.setPrefWidth(400);

        Button gotItBtn = new Button("Got it!");
        gotItBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: bold;");
        gotItBtn.setOnAction(e -> overlayManager.hideCurrentOverlay());

        helpPanel.getChildren().addAll(header, helpText, gotItBtn);
        overlayManager.showOverlay(helpPanel, OverlayManager.AnimationType.FADE_IN);
    }

    // Callback methods
    private void onTaskSelected(Task task) {
        System.out.println("[App] onTaskSelected called with task: " + (task != null ? task.getName() : "null"));

        if (task != null) {
            // Set the task in the timer panel
            timerPanel.setCurrentTask(task);
            System.out.println("[App] Task set in timer panel: " + task.getName());

            // If user selected task during break, switch to work mode
            if (timerPanel.isOnBreak()) {
                System.out.println("[App] Switching from break to work mode");
                timerPanel.forceWorkMode();
            }

            // Don't automatically start the timer - let user click START button

            System.out.println("[App] Task selection complete, user can now start timer");

            updateQuestProgress();
        } else {
            System.out.println("[App] No task was selected");
        }

        // Close the overlay
        overlayManager.hideCurrentOverlay();
    }

    private void onTasksUpdated(Runnable callback) {
        updateQuestProgress();
        if (callback != null) {
            callback.run();
        }
    }

    private void onQuestsUpdated(Runnable callback) {
        updateQuestProgress();
        if (callback != null) {
            callback.run();
        }
    }

    // Background music and settings
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
                primaryStage, userPreferences, pixelFont, this::onWallpaperChanged);
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

    private void updateQuestProgress() {
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
            if (e.getCode() == KeyCode.F11) {
                toggleFullscreen();
                return;
            }

            if (e.isControlDown()) {
                if (e.getCode() == KeyCode.N) {
                    showTaskCreationOverlay();
                } else if (e.getCode() == KeyCode.T) {
                    showTaskManagementOverlay();
                } else if (e.getCode() == KeyCode.Q) {
                    showQuestManagementOverlay();
                } else if (e.getCode() == KeyCode.S) {
                    showStatsOverlay();
                } else if (e.getCode() == KeyCode.B) {
                    showWallpaperSelection();
                } else if (e.getCode() == KeyCode.M) {
                    toggleSound();
                } else if (e.getCode() == KeyCode.H) {
                    showHelpOverlay();
                }
            }

            if (e.getCode() == KeyCode.ESCAPE) {
                overlayManager.hideCurrentOverlay();
            }

            if (e.getCode() == KeyCode.SPACE && !e.isControlDown()) {
                // Let TimerPanel handle its own timer logic
                timerPanel.startTimer();
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}