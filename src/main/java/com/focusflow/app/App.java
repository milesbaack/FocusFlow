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
    private StackPane timerContainer;
    private ProgressBar progressBar;
    private Label questProgressLabel;
    private VBox bottomArea;
    private VBox sidebar;
    private ImageView soundIcon;
    private MediaPlayer backgroundMusic;
    private boolean isMuted = false;
    private HBox topSection;
    private VBox centerContainer;
    private VBox mainLayout;
    private Button selectTaskButton;
    private Button startTimerButton;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        userPreferences = new UserPreferences();

        loadPixelFont();
        initializeManagers();
        setupBackgroundMusic();

        // Create scene with initial layout
        AnchorPane root = createMainLayout();
        mainScene = new Scene(root, 1070, 610);

        // Initialize overlay manager
        setupOverlayManager(root);

        // Setup timer state listener after timerPanel is created
        setupTimerStateListener();

        // Now that mainScene exists, set up responsive design
        setupResponsiveDesign();

        stage.setScene(mainScene);
        stage.setTitle("FocusFlow");

        if (userPreferences.isFullscreenEnabled()) {
            stage.setFullScreen(true);
            stage.setResizable(true);
        } else {
            stage.setResizable(true);
        }

        setupKeyboardShortcuts(mainScene);
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

        // Create main VBox without spacing
        mainLayout = new VBox(0);
        mainLayout.setFillWidth(true);
        mainLayout.setAlignment(Pos.BOTTOM_CENTER);

        // Create bottom area with fixed size
        bottomArea = createBottomArea();
        bottomArea.setMinHeight(80);
        bottomArea.setPrefHeight(80);
        bottomArea.setMaxHeight(80);
        VBox.setVgrow(bottomArea, Priority.NEVER);
        bottomArea.setStyle(
                "-fx-background-color: rgba(255,255,255,0.95); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, -5);");

        // Top section
        topSection = new HBox();
        VBox.setVgrow(topSection, Priority.ALWAYS);
        topSection.setMinHeight(Region.USE_PREF_SIZE);
        topSection.setPrefHeight(Region.USE_COMPUTED_SIZE);
        topSection.setMaxHeight(Double.MAX_VALUE);

        // Create sidebar and center container
        sidebar = createSimplifiedSidebar();
        centerContainer = new VBox();
        centerContainer.setAlignment(Pos.CENTER);
        centerContainer.setFillWidth(true);

        // Initialize timer panel - IMPORTANT: Create this before setupResponsiveDesign
        // is called
        timerPanel = new TimerPanel(
                pixelFont,
                (unused) -> showTaskSelectionOverlay(),
                (unused) -> {
                    /* Timer panel handles its own start/pause logic */});
        centerContainer.getChildren().add(timerPanel);
        VBox.setVgrow(timerPanel, Priority.ALWAYS);

        // Add components to top section
        topSection.getChildren().addAll(sidebar, centerContainer);
        HBox.setHgrow(centerContainer, Priority.ALWAYS);

        // Add sections to main layout
        mainLayout.getChildren().addAll(topSection, bottomArea);

        // Add main layout to root with anchors
        root.getChildren().add(mainLayout);
        AnchorPane.setTopAnchor(mainLayout, 0.0);
        AnchorPane.setBottomAnchor(mainLayout, 0.0);
        AnchorPane.setLeftAnchor(mainLayout, 0.0);
        AnchorPane.setRightAnchor(mainLayout, 0.0);

        return root;
    }

    private void setupResponsiveDesign() {
        if (mainScene != null) {
            // Set minimum window size to prevent elements from overlapping
            primaryStage.setMinWidth(600); // Minimum width that keeps UI readable
            primaryStage.setMinHeight(400); // Minimum height that shows all elements

            // Fix bottom area height
            bottomArea.setMinHeight(80);
            bottomArea.setPrefHeight(80);
            bottomArea.setMaxHeight(80);

            // Main layout takes scene height but respects minimum sizes
            mainLayout.setMinHeight(300); // Minimum height to show timer + bottom area
            mainLayout.prefHeightProperty().bind(mainScene.heightProperty());

            // Top section fills remaining space exactly
            topSection.prefHeightProperty().bind(
                    Bindings.subtract(mainScene.heightProperty(), bottomArea.getHeight()));

            // Sidebar width with minimum
            sidebar.setMinWidth(100); // Minimum width for sidebar
            sidebar.prefWidthProperty().bind(
                    Bindings.max(
                            100,
                            mainScene.widthProperty().multiply(0.2)));

            // Center container with minimum width
            centerContainer.setMinWidth(400); // Minimum width for timer
            centerContainer.prefWidthProperty().bind(
                    Bindings.max(
                            400,
                            Bindings.subtract(mainScene.widthProperty(), sidebar.widthProperty())));

            // Timer panel with minimum size
            timerPanel.setMinWidth(300);
            timerPanel.setMinHeight(200);
            timerPanel.maxHeightProperty().bind(
                    Bindings.subtract(topSection.heightProperty(), 20));
            timerPanel.maxWidthProperty().bind(
                    Bindings.subtract(centerContainer.widthProperty(), 20));

            setupSidebarResponsiveBindings();
            setupBottomAreaResponsiveBindings();

            // Update timer scaling when window changes
            mainScene.widthProperty().addListener((obs, oldWidth, newWidth) -> updateTimerPanelScale());
            mainScene.heightProperty().addListener((obs, oldHeight, newHeight) -> updateTimerPanelScale());
        }
    }

    private void setupOverlayManager(AnchorPane root) {
        StackPane overlayContainer = new StackPane();
        overlayContainer.setVisible(false);
        overlayContainer.setMouseTransparent(false);

        // Add overlay container to root
        root.getChildren().add(overlayContainer);

        // Position overlay container to cover entire scene
        AnchorPane.setTopAnchor(overlayContainer, 0.0);
        AnchorPane.setBottomAnchor(overlayContainer, 0.0);
        AnchorPane.setLeftAnchor(overlayContainer, 0.0);
        AnchorPane.setRightAnchor(overlayContainer, 0.0);

        // Initialize overlay manager
        this.overlayManager = new OverlayManager(overlayContainer);
    }

    private void updateTimerPanelScale() {
        Platform.runLater(() -> {
            // Calculate available space exactly
            double availableWidth = mainScene.getWidth() - sidebar.getWidth();
            double availableHeight = mainScene.getHeight() - bottomArea.getHeight();

            // Scale factor based on available space with minimum scale
            double scaleFactor = Math.max(0.4, Math.min(
                    availableWidth / 1200.0,
                    availableHeight / 800.0));

            timerPanel.setScaleFactor(scaleFactor);
        });
    }

    private void setupSidebarResponsiveBindings() {
        // Responsive padding and spacing for sidebar
        sidebar.paddingProperty().bind(
                Bindings.createObjectBinding(() -> {
                    double padding = sidebar.getWidth() * 0.1;
                    return new Insets(padding);
                }, sidebar.widthProperty()));

        sidebar.spacingProperty().bind(mainScene.heightProperty().multiply(0.05));

        // Bind icon sizes to sidebar width
        ImageView wallpaperIcon = (ImageView) ((Button) sidebar.getChildren().get(0)).getGraphic();
        wallpaperIcon.fitWidthProperty().bind(sidebar.widthProperty().multiply(0.4));
        wallpaperIcon.fitHeightProperty().bind(wallpaperIcon.fitWidthProperty());

        soundIcon.fitWidthProperty().bind(sidebar.widthProperty().multiply(0.4));
        soundIcon.fitHeightProperty().bind(soundIcon.fitWidthProperty());

        ImageView helpIcon = (ImageView) ((Button) sidebar.getChildren().get(2)).getGraphic();
        helpIcon.fitWidthProperty().bind(sidebar.widthProperty().multiply(0.4));
        helpIcon.fitHeightProperty().bind(helpIcon.fitWidthProperty());
    }

    private void setupBottomAreaResponsiveBindings() {
        // Responsive padding - smaller to prevent overflow
        bottomArea.paddingProperty().bind(
                Bindings.createObjectBinding(() -> {
                    double padding = Math.max(5, bottomArea.getHeight() * 0.05);
                    return new Insets(padding);
                }, bottomArea.heightProperty()));

        bottomArea.spacingProperty().bind(
                Bindings.max(5, bottomArea.heightProperty().multiply(0.04)));
        // Get the action buttons container
        HBox actionButtons = (HBox) bottomArea.getChildren().get(0);
        actionButtons.spacingProperty().bind(bottomArea.widthProperty().multiply(0.03));

        // Bind button sizes
        for (javafx.scene.Node node : actionButtons.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                btn.prefWidthProperty().bind(bottomArea.widthProperty().multiply(0.15));
                btn.prefHeightProperty().bind(
                        Bindings.max(25, bottomArea.heightProperty().multiply(0.25)));
                btn.fontProperty().bind(
                        Bindings.createObjectBinding(() -> {
                            double fontSize = Math.max(12, bottomArea.getHeight() * 0.08);
                            return Font.font(pixelFont.getFamily(), FontWeight.BOLD, fontSize);
                        }, bottomArea.heightProperty()));
            }
        }

        // Bind progress area elements
        HBox progressArea = (HBox) bottomArea.getChildren().get(1);
        progressArea.spacingProperty().bind(bottomArea.widthProperty().multiply(0.03));

        questProgressLabel.fontProperty().bind(
                Bindings.createObjectBinding(() -> {
                    double fontSize = Math.max(12, bottomArea.getHeight() * 0.1);
                    return Font.font(pixelFont.getFamily(), fontSize);
                }, bottomArea.heightProperty()));

        progressBar.prefWidthProperty().bind(bottomArea.widthProperty().multiply(0.3));
        progressBar.prefHeightProperty().bind(
                Bindings.max(15, bottomArea.heightProperty().multiply(0.12)));
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
        bottomArea.setStyle("-fx-background-color: rgba(255,255,255,0.95);");
        bottomArea.setPadding(new Insets(15));
        bottomArea.setSpacing(10);

        // Timer controls - NOW CONNECTED TO TIMER PANEL
        HBox timerControls = new HBox(25);
        timerControls.setAlignment(Pos.CENTER);

        Button selectTaskBtn = new Button("Select Task");
        Button startTimerBtn = new Button("Start Timer");

        // Store references to update later
        this.selectTaskButton = selectTaskBtn;
        this.startTimerButton = startTimerBtn;

        // Style the buttons
        String timerButtonStyle = "-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-background-radius: 12; -fx-font-weight: bold; -fx-cursor: hand;";
        selectTaskBtn.setStyle(timerButtonStyle.replace("#4CAF50", "#2196F3"));
        startTimerBtn.setStyle(timerButtonStyle);

        // Set initial sizes (will be bound later)
        selectTaskBtn.setPrefWidth(120);
        startTimerBtn.setPrefWidth(120);
        selectTaskBtn.setPrefHeight(40);
        startTimerBtn.setPrefHeight(40);

        // FIXED: Connect button handlers to TimerPanel
        selectTaskBtn.setOnAction(e -> {
            System.out.println("[App] Select Task button clicked");
            showTaskSelectionOverlay();
        });

        startTimerBtn.setOnAction(e -> {
            System.out.println("[App] Start Timer button clicked");
            if (timerPanel != null) {
                timerPanel.handleStartPauseAction();
                updateTimerControlButtons(); // Update button text after action
            }
        });

        timerControls.getChildren().addAll(selectTaskBtn, startTimerBtn);

        // Quick action buttons (unchanged)
        HBox actionButtons = new HBox(25);
        actionButtons.setAlignment(Pos.CENTER);

        Button addTaskBtn = new Button("+ Add Task");
        Button viewTasksBtn = new Button("📋 Tasks");
        Button questsBtn = new Button("🏆 Create Quest");
        Button statsBtn = new Button("📊 Stats");

        // Style all buttons
        Button[] buttons = { addTaskBtn, viewTasksBtn, questsBtn, statsBtn };
        for (Button btn : buttons) {
            btn.setPrefWidth(120);
            btn.setPrefHeight(40);
            btn.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 14));
            btn.setStyle("-fx-background-color: rgba(255,255,255,0.8); " +
                    "-fx-background-radius: 12; -fx-font-weight: bold; -fx-cursor: hand;");
        }

        addTaskBtn.setStyle(addTaskBtn.getStyle() + "-fx-background-color: #2196F3; -fx-text-fill: white;");

        // Click handlers (unchanged)
        addTaskBtn.setOnAction(e -> showTaskCreationOverlay());
        viewTasksBtn.setOnAction(e -> showTaskManagementOverlay());
        questsBtn.setOnAction(e -> showQuestManagementOverlay());
        statsBtn.setOnAction(e -> showStatsOverlay());

        actionButtons.getChildren().addAll(addTaskBtn, viewTasksBtn, questsBtn, statsBtn);

        // Progress area (unchanged)
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

        // Add all sections to bottom area
        bottomArea.getChildren().addAll(timerControls, actionButtons, progressArea);
        return bottomArea;
    }

    /**
     * Updates the timer control buttons based on current timer state
     */
    private void updateTimerControlButtons() {
        if (timerPanel == null || selectTaskButton == null || startTimerButton == null) {
            return;
        }

        // Update start button text
        startTimerButton.setText(timerPanel.getStartButtonText());

        // Update button enabled states
        selectTaskButton.setDisable(!timerPanel.isTaskSelectionEnabled());
        startTimerButton.setDisable(!timerPanel.isStartButtonEnabled());

        // Update button styles based on state
        if (startTimerButton.isDisabled()) {
            startTimerButton.setStyle("-fx-background-color: #CCCCCC; -fx-text-fill: #666666; " +
                    "-fx-background-radius: 12; -fx-font-weight: bold;");
        } else {
            startTimerButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                    "-fx-background-radius: 12; -fx-font-weight: bold; -fx-cursor: hand;");
        }

        if (selectTaskButton.isDisabled()) {
            selectTaskButton.setStyle("-fx-background-color: #CCCCCC; -fx-text-fill: #666666; " +
                    "-fx-background-radius: 12; -fx-font-weight: bold;");
        } else {
            selectTaskButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                    "-fx-background-radius: 12; -fx-font-weight: bold; -fx-cursor: hand;");
        }
    }

    private VBox createSimplifiedSidebar() {
        VBox sidebar = new VBox();
        sidebar.setAlignment(Pos.CENTER);
        sidebar.setStyle("-fx-background-color: rgba(0,0,0,0.1);");

        // Use fixed initial spacing, will be updated after scene is created
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setSpacing(30);

        String iconStyle = "-fx-cursor: hand; -fx-background-color: transparent;";

        // Settings/wallpaper - responsive icon (40% of sidebar width)
        ImageView wallpaperIcon = new ImageView();
        wallpaperIcon.setImage(new Image(getClass().getResource("/UI/BackgroundIcon.png").toString()));
        wallpaperIcon.setPreserveRatio(true);
        wallpaperIcon.setFitWidth(50); // Initial size, will be bound later
        wallpaperIcon.setFitHeight(50);

        Button wallpaperBtn = new Button();
        wallpaperBtn.setGraphic(wallpaperIcon);
        wallpaperBtn.setStyle(iconStyle);
        wallpaperBtn.setOnAction(e -> showWallpaperSelection());

        // Sound toggle - responsive icon (40% of sidebar width)
        soundIcon = new ImageView();
        soundIcon.setImage(new Image(getClass().getResource("/UI/sound.png").toString()));
        soundIcon.setPreserveRatio(true);
        soundIcon.setFitWidth(50); // Initial size, will be bound later
        soundIcon.setFitHeight(50);

        Button soundBtn = new Button();
        soundBtn.setGraphic(soundIcon);
        soundBtn.setStyle(iconStyle);
        soundBtn.setOnAction(e -> toggleSound());

        // Info/help - responsive icon (40% of sidebar width)
        ImageView helpIcon = new ImageView();
        helpIcon.setImage(new Image(getClass().getResource("/UI/question.png").toString()));
        helpIcon.setPreserveRatio(true);
        helpIcon.setFitWidth(50); // Initial size, will be bound later
        helpIcon.setFitHeight(50);

        Button helpBtn = new Button();
        helpBtn.setGraphic(helpIcon);
        helpBtn.setStyle(iconStyle);
        helpBtn.setOnAction(e -> showHelpOverlay());

        sidebar.getChildren().addAll(wallpaperBtn, soundBtn, helpBtn);

        // Store references for later binding
        this.sidebar = sidebar;

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

            // Update the control buttons
            updateTimerControlButtons();

            System.out.println("[App] Task selection complete, user can now start timer");
            updateQuestProgress();
        } else {
            System.out.println("[App] No task was selected");
        }

        // Close the overlay
        overlayManager.hideCurrentOverlay();
    }

    private void setupTimerStateListener() {
        if (timerPanel != null) {
            // Add listeners to both timers to update buttons when state changes
            timerPanel.getWorkTimer().addListener(new com.focusflow.core.timer.TimerEventListener.Adapter() {
                @Override
                public void onTimerStarted(com.focusflow.core.timer.Timer timer) {
                    Platform.runLater(() -> updateTimerControlButtons());
                }

                @Override
                public void onTimerPaused(com.focusflow.core.timer.Timer timer) {
                    Platform.runLater(() -> updateTimerControlButtons());
                }

                @Override
                public void onTimerResumed(com.focusflow.core.timer.Timer timer) {
                    Platform.runLater(() -> updateTimerControlButtons());
                }

                @Override
                public void onTimerCompleted(com.focusflow.core.timer.Timer timer) {
                    Platform.runLater(() -> updateTimerControlButtons());
                }

                @Override
                public void onTimerStopped(com.focusflow.core.timer.Timer timer) {
                    Platform.runLater(() -> updateTimerControlButtons());
                }
            });

            timerPanel.getBreakTimer().addListener(new com.focusflow.core.timer.TimerEventListener.Adapter() {
                @Override
                public void onTimerStarted(com.focusflow.core.timer.Timer timer) {
                    Platform.runLater(() -> updateTimerControlButtons());
                }

                @Override
                public void onTimerPaused(com.focusflow.core.timer.Timer timer) {
                    Platform.runLater(() -> updateTimerControlButtons());
                }

                @Override
                public void onTimerResumed(com.focusflow.core.timer.Timer timer) {
                    Platform.runLater(() -> updateTimerControlButtons());
                }

                @Override
                public void onTimerCompleted(com.focusflow.core.timer.Timer timer) {
                    Platform.runLater(() -> updateTimerControlButtons());
                }

                @Override
                public void onTimerStopped(com.focusflow.core.timer.Timer timer) {
                    Platform.runLater(() -> updateTimerControlButtons());
                }
            });
        }
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
            if (soundIcon != null)
                soundIcon.setOpacity(1.0);
            userPreferences.setMusicEnabled(true);
        } else {
            if (backgroundMusic != null) {
                backgroundMusic.pause();
            }
            if (soundIcon != null)
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