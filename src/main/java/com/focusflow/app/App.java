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
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
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

    // Navigation
    private enum ScreenType {
        TIMER,
        TASK_MANAGEMENT,
        STATS
    }

    private StackPane mainContentPane;
    private VBox timerPanel;
    private VBox taskManagementPanel;
    private VBox statsPanel;
    private ScreenType currentScreen = ScreenType.TIMER;

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

        questStatsGUI = new QuestAndStatsGUI(questManager, achievementManager, xpManager, pixelFont);

        workTimer = new PomodoroTimer(TimerType.WORK, 25 * 60);
        breakTimer = new PomodoroTimer(TimerType.SHORT_BREAK, 5 * 60);
        workTimer.addListener(this);
        breakTimer.addListener(this);
    }

    private AnchorPane createMainLayout() {
        AnchorPane root = new AnchorPane();

        // Load and set background
        String selectedWallpaper = userPreferences.getSelectedWallpaper();
        backgroundImageView = BackgroundManager.createBackgroundImageView(
                selectedWallpaper, 1070, 610);

        backgroundImageView.fitWidthProperty().bind(root.widthProperty());
        backgroundImageView.fitHeightProperty().bind(root.heightProperty());

        AnchorPane.setTopAnchor(backgroundImageView, 0.0);
        AnchorPane.setLeftAnchor(backgroundImageView, 0.0);
        root.getChildren().add(backgroundImageView);

        // Sidebar (anchor to left)
        VBox sidebarContainer = createResponsiveSidebar();
        sidebarContainer.prefWidthProperty().bind(root.widthProperty().multiply(0.12));
        sidebarContainer.prefHeightProperty().bind(root.heightProperty());
        AnchorPane.setTopAnchor(sidebarContainer, 0.0);
        AnchorPane.setLeftAnchor(sidebarContainer, 0.0);
        AnchorPane.setBottomAnchor(sidebarContainer, 0.0);
        root.getChildren().add(sidebarContainer);

        // Main content area (StackPane)
        mainContentPane = new StackPane();
        mainContentPane.prefWidthProperty().bind(root.widthProperty().multiply(0.85));
        mainContentPane.prefHeightProperty().bind(root.heightProperty().multiply(0.85));
        mainContentPane.minWidthProperty().set(300);
        mainContentPane.minHeightProperty().set(200);

        mainContentPane.translateXProperty().bind(
                root.widthProperty().subtract(mainContentPane.widthProperty()).divide(2));
        mainContentPane.translateYProperty().bind(
                root.heightProperty().subtract(mainContentPane.heightProperty()).divide(2));

        // Create and add panels
        timerPanel = new VBox(createResponsiveTimerArea());
        timerPanel.setAlignment(Pos.CENTER);

        taskManagementPanel = createTaskManagementPanel();
        taskManagementPanel.setAlignment(Pos.CENTER);

        statsPanel = createStatsPanel();
        statsPanel.setAlignment(Pos.CENTER);

        mainContentPane.getChildren().addAll(timerPanel, taskManagementPanel, statsPanel);

        showScreen(ScreenType.TIMER);

        root.getChildren().add(mainContentPane);

        createProgressArea(root);
        createTopControls(root);
        createOverlays(root);

        return root;
    }

    private VBox createResponsiveSidebar() {
        VBox sidebarContainer = new VBox();
        sidebarContainer.setAlignment(Pos.CENTER);
        sidebarContainer.setStyle("-fx-background-color: transparent;");
        sidebarContainer.setMinHeight(0);

        VBox sidebarIcons = new VBox();
        sidebarIcons.setAlignment(Pos.CENTER);
        sidebarIcons.setSpacing(25);
        sidebarIcons.setMinHeight(0);

        DoubleBinding iconSizeBinding = sidebarContainer.heightProperty().multiply(0.08).add(10);

        // Add task button
        ImageView addTaskIcon = new ImageView(new Image(
                getClass().getResource("/UI/AddTask.png").toString()));
        addTaskIcon.fitWidthProperty().bind(iconSizeBinding);
        addTaskIcon.fitHeightProperty().bind(iconSizeBinding);
        addTaskIcon.setPreserveRatio(true);
        addTaskIcon.setSmooth(true);

        StackPane addTaskBtn = new StackPane(addTaskIcon);
        addTaskBtn.setStyle("-fx-cursor: hand;");
        addTaskBtn.setOnMouseClicked(e -> showCreateTaskDialog());

        // Task management button
        ImageView taskMgmtIcon = new ImageView(new Image(
                getClass().getResource("/UI/StatsIcon.png").toString()));
        taskMgmtIcon.fitWidthProperty().bind(iconSizeBinding);
        taskMgmtIcon.fitHeightProperty().bind(iconSizeBinding);
        taskMgmtIcon.setPreserveRatio(true);
        taskMgmtIcon.setSmooth(true);

        StackPane taskMgmtBtn = new StackPane(taskMgmtIcon);
        taskMgmtBtn.setStyle("-fx-cursor: hand;");
        taskMgmtBtn.setOnMouseClicked(e -> showScreen(ScreenType.TASK_MANAGEMENT));

        // Stats button
        ImageView statsIcon = new ImageView(new Image(
                getClass().getResource("/UI/StatsIcon.png").toString()));
        statsIcon.fitWidthProperty().bind(iconSizeBinding);
        statsIcon.fitHeightProperty().bind(iconSizeBinding);
        statsIcon.setPreserveRatio(true);
        statsIcon.setSmooth(true);

        StackPane statsBtn = new StackPane(statsIcon);
        statsBtn.setStyle("-fx-cursor: hand;");
        statsBtn.setOnMouseClicked(e -> showScreen(ScreenType.STATS));

        // Wallpaper selection button
        ImageView wallpaperIcon = new ImageView(new Image(
                getClass().getResource("/UI/BackgroundIcon.png").toString()));
        wallpaperIcon.fitWidthProperty().bind(iconSizeBinding);
        wallpaperIcon.fitHeightProperty().bind(iconSizeBinding);
        wallpaperIcon.setPreserveRatio(true);
        wallpaperIcon.setSmooth(true);

        StackPane wallpaperBtn = new StackPane(wallpaperIcon);
        wallpaperBtn.setStyle("-fx-cursor: hand;");
        wallpaperBtn.setOnMouseClicked(e -> showWallpaperSelection());

        // Calendar icon
        ImageView calendarIcon = new ImageView(new Image(
                getClass().getResource("/UI/CalendarIcon.png").toString()));
        calendarIcon.fitWidthProperty().bind(iconSizeBinding);
        calendarIcon.fitHeightProperty().bind(iconSizeBinding);
        calendarIcon.setPreserveRatio(true);
        calendarIcon.setSmooth(true);

        StackPane calendarBtn = new StackPane(calendarIcon);
        calendarBtn.setStyle("-fx-cursor: hand;");

        // Question icon
        ImageView questionIcon = new ImageView(new Image(
                getClass().getResource("/UI/question.png").toString()));
        questionIcon.fitWidthProperty().bind(iconSizeBinding);
        questionIcon.fitHeightProperty().bind(iconSizeBinding);
        questionIcon.setPreserveRatio(true);
        questionIcon.setSmooth(true);

        StackPane questionBtn = new StackPane(questionIcon);
        questionBtn.setStyle("-fx-cursor: hand;");
        questionBtn.setOnMouseClicked(e -> toggleInfo());

        sidebarIcons.spacingProperty().bind(sidebarContainer.heightProperty().multiply(0.04));

        sidebarIcons.getChildren().addAll(addTaskBtn, taskMgmtBtn, statsBtn, wallpaperBtn, calendarBtn, questionBtn);
        sidebarContainer.getChildren().add(sidebarIcons);

        return sidebarContainer;
    }

    private VBox createTaskManagementPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(20));
        panel.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Task Management");
        title.setFont(Font.font(pixelFont.getFamily(), 22));

        TaskManagementWindow taskMgmt = new TaskManagementWindow(tasks, questManager, pixelFont);
        taskMgmt.setOnTasksUpdated(this::updateQuestProgress);

        Button homeBtn = new Button("Home");
        homeBtn.setOnAction(e -> showScreen(ScreenType.TIMER));

        panel.getChildren().addAll(title, taskMgmt.getRoot(), homeBtn);
        panel.setVisible(false);
        return panel;
    }

    private VBox createStatsPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(20));
        panel.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Statistics & Management");
        title.setFont(Font.font(pixelFont.getFamily(), 22));

        // Embed the QuestAndStatsGUI tabs directly
        // Defensive: check startButton is initialized before using its scene
        Stage parentStage = null;
        if (startButton != null && startButton.getScene() != null) {
            parentStage = (Stage) startButton.getScene().getWindow();
        }

        // If parentStage is null, just pass null (shouldn't break, but disables some
        // features)
        javafx.scene.control.TabPane statsTabs = questStatsGUI.getStatsAndManagementTabs(
                tasks,
                sessionManager.getSessionHistory(),
                parentStage);

        Button homeBtn = new Button("Home");
        homeBtn.setOnAction(e -> showScreen(ScreenType.TIMER));

        panel.getChildren().addAll(title, statsTabs, homeBtn);
        panel.setVisible(false);
        return panel;
    }

    private void showScreen(ScreenType screen) {
        timerPanel.setVisible(screen == ScreenType.TIMER);
        taskManagementPanel.setVisible(screen == ScreenType.TASK_MANAGEMENT);
        statsPanel.setVisible(screen == ScreenType.STATS);
        currentScreen = screen;
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

    private void setupResponsiveTimerBindings(StackPane container, VBox timerGroup, HBox buttonGroup) {
        // Calculate base font sizes based on container dimensions
        NumberBinding containerMinDimension = Bindings.min(
                container.widthProperty(),
                container.heightProperty());

        // Font size bindings with no max size, only a minimum
        DoubleBinding timerFontSize = Bindings.createDoubleBinding(
                () -> Math.max(containerMinDimension.doubleValue() * 0.15, 40),
                containerMinDimension);

        DoubleBinding typeFontSize = Bindings.createDoubleBinding(
                () -> Math.max(containerMinDimension.doubleValue() * 0.06, 16),
                containerMinDimension);

        DoubleBinding remainingFontSize = Bindings.createDoubleBinding(
                () -> Math.max(containerMinDimension.doubleValue() * 0.05, 14),
                containerMinDimension);

        DoubleBinding buttonFontSize = Bindings.createDoubleBinding(
                () -> Math.max(containerMinDimension.doubleValue() * 0.04, 12),
                containerMinDimension);

        // Apply font bindings
        timerLabel.styleProperty().bind(Bindings.concat(
                "-fx-font-family: '", pixelFont.getFamily(), "'; -fx-font-size: ",
                timerFontSize.asString("%.0f"), "px;"));

        timerTypeLabel.styleProperty().bind(Bindings.concat(
                "-fx-font-family: '", pixelFont.getFamily(), "'; -fx-font-size: ",
                typeFontSize.asString("%.0f"), "px;"));

        Label remainingLabel = (Label) timerGroup.getChildren().get(2);
        remainingLabel.styleProperty().bind(Bindings.concat(
                "-fx-font-family: '", pixelFont.getFamily(), "'; -fx-font-size: ",
                remainingFontSize.asString("%.0f"), "px;"));

        // Button styling and sizing
        DoubleBinding buttonWidth = Bindings.createDoubleBinding(
                () -> containerMinDimension.doubleValue() * 0.2,
                containerMinDimension);

        DoubleBinding buttonHeight = Bindings.createDoubleBinding(
                () -> containerMinDimension.doubleValue() * 0.08,
                containerMinDimension);

        DoubleBinding buttonSpacing = Bindings.createDoubleBinding(
                () -> containerMinDimension.doubleValue() * 0.03,
                containerMinDimension);

        startWorkingButton.prefWidthProperty().bind(buttonWidth);
        startWorkingButton.prefHeightProperty().bind(buttonHeight);
        startWorkingButton.styleProperty().bind(Bindings.concat(
                "-fx-background-color:rgb(102, 204, 102); -fx-text-fill: white; -fx-background-radius: 15; -fx-font-family: '",
                pixelFont.getFamily(), "'; -fx-font-size: ", buttonFontSize.asString("%.0f"), "px;"));

        startButton.prefWidthProperty().bind(Bindings.createDoubleBinding(
                () -> buttonWidth.get() * 0.7, buttonWidth));
        startButton.prefHeightProperty().bind(buttonHeight);
        startButton.styleProperty().bind(Bindings.concat(
                "-fx-background-color:rgb(255, 255, 255); -fx-text-fill: black; -fx-background-radius: 15; -fx-font-family: '",
                pixelFont.getFamily(), "'; -fx-font-size: ", buttonFontSize.asString("%.0f"), "px;"));

        // Spacing bindings to prevent crowding
        buttonGroup.spacingProperty().bind(buttonSpacing);
        timerGroup.spacingProperty().bind(Bindings.createDoubleBinding(
                () -> containerMinDimension.doubleValue() * 0.04,
                containerMinDimension));
    }

    private StackPane createResponsiveTimerArea() {
        StackPane timerContainer = new StackPane();
        timerContainer.setAlignment(Pos.CENTER);

        // Create main timer group
        VBox timerGroup = new VBox();
        timerGroup.setAlignment(Pos.CENTER);

        // Initialize timer labels
        timerTypeLabel = new Label("WORK SESSION");
        timerTypeLabel.setTextFill(Color.BLACK);

        timerLabel = new Label("25:00");
        timerLabel.setTextFill(Color.BLACK);

        Label remainingLabel = new Label("REMAINING...");
        remainingLabel.setTextFill(Color.BLACK);

        // Create button group
        HBox buttonGroup = new HBox();
        buttonGroup.setAlignment(Pos.CENTER);

        startWorkingButton = new Button("Start Working");
        startWorkingButton
                .setStyle("-fx-background-color:rgb(102, 204, 102); -fx-text-fill: white; -fx-background-radius: 15;");
        startWorkingButton.setOnAction(e -> showTaskSelection());

        startButton = new Button("START");
        startButton
                .setStyle("-fx-background-color:rgb(255, 255, 255); -fx-text-fill: black; -fx-background-radius: 15;");
        startButton.setOnAction(e -> toggleTimer());

        buttonGroup.getChildren().addAll(startWorkingButton, startButton);

        // Add all components to timer group
        timerGroup.getChildren().addAll(timerTypeLabel, timerLabel, remainingLabel, buttonGroup);

        // Create responsive bindings
        setupResponsiveTimerBindings(timerContainer, timerGroup, buttonGroup);

        timerContainer.getChildren().add(timerGroup);
        return timerContainer;
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