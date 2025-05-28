package com.focusflow.app.ui;

import java.util.function.Consumer;

import com.focusflow.core.task.Task;
import com.focusflow.core.timer.PomodoroTimer;
import com.focusflow.core.timer.TimerState;
import com.focusflow.core.timer.TimerType;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Responsive Timer Panel that handles all timer GUI logic and layout.
 * Automatically scales based on available screen space while maintaining
 * consistent positioning and proportions.
 * 
 * Features:
 * - Responsive sizing that adapts to screen dimensions
 * - Maintains relative positioning of timer elements
 * - Handles timer state changes and visual feedback
 * - Integrates with task selection and timer controls
 * - Provides visual cues for different timer states
 * - Thread-safe UI updates via Platform.runLater
 * 
 * @author FocusFlow Team
 * @version 1.0 - Responsive Timer Implementation
 */
public class TimerPanel extends VBox {

    // Dependencies
    private final Font pixelFont;
    private final Consumer<Void> onSelectTask;
    private final Consumer<Void> onToggleTimer;

    // Timer components
    private PomodoroTimer workTimer;
    private PomodoroTimer breakTimer;
    private Task currentTask;
    private boolean isOnBreak = false;

    // UI Components
    private Label currentTaskLabel;
    private Label timerTypeLabel;
    private Label timerLabel;
    private Label remainingLabel;
    private Button selectTaskButton;
    private Button startButton;
    private HBox controlsContainer;
    private VBox timerDisplay;

    // Responsive sizing properties
    private double baseWidth = 400;
    private double baseHeight = 200;
    private double scaleFactor = 1.0;

    // Styling constants
    private static final String TIMER_CONTAINER_STYLE = "-fx-background-color: rgba(255,255,255,0.1); " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);";

    private static final String WORK_TIMER_COLOR = "#4CAF50";
    private static final String BREAK_TIMER_COLOR = "#FF9800";
    private static final String URGENT_TIMER_COLOR = "#F44336";
    private static final String WARNING_TIMER_COLOR = "#FF9800";

    /**
     * Creates a new TimerPanel with responsive sizing capabilities.
     * 
     * @param pixelFont     The font to use for consistent typography
     * @param onSelectTask  Callback when select task button is clicked
     * @param onToggleTimer Callback when start/pause button is clicked
     */
    public TimerPanel(Font pixelFont, Consumer<Void> onSelectTask, Consumer<Void> onToggleTimer) {
        this.pixelFont = pixelFont;
        this.onSelectTask = onSelectTask;
        this.onToggleTimer = onToggleTimer;

        initializeTimers();
        createTimerInterface();
        setupResponsiveLayout();
        updateTimerDisplay();
    }

    /**
     * Initializes the timer objects and sets up event handling.
     */
    private void initializeTimers() {
        workTimer = new PomodoroTimer(TimerType.WORK, 25 * 60);
        breakTimer = new PomodoroTimer(TimerType.SHORT_BREAK, 5 * 60);

        // Add listeners for timer events
        workTimer.addListener(new com.focusflow.core.timer.TimerEventListener() {
            @Override
            public void onTimerStarted(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> updateButtonStates());
            }

            @Override
            public void onTimerPaused(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> updateButtonStates());
            }

            @Override
            public void onTimerResumed(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> updateButtonStates());
            }

            @Override
            public void onTimerCompleted(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> {
                    switchToBreakMode();
                    breakTimer.start();
                    updateButtonStates();
                    updateTimerDisplay();
                });
            }

            @Override
            public void onTimerStopped(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> updateButtonStates());
            }

            @Override
            public void onTimerTick(com.focusflow.core.timer.Timer timer, int remainingSeconds) {
                Platform.runLater(() -> updateTimeDisplay(remainingSeconds));
            }

            @Override
            public void onTimerReset(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> updateButtonStates());
            }
        });

        breakTimer.addListener(new com.focusflow.core.timer.TimerEventListener() {
            @Override
            public void onTimerStarted(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> updateButtonStates());
            }

            @Override
            public void onTimerPaused(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> updateButtonStates());
            }

            @Override
            public void onTimerResumed(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> updateButtonStates());
            }

            @Override
            public void onTimerCompleted(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> {
                    switchToWorkMode();
                    updateButtonStates();
                    updateTimerDisplay();
                });
            }

            @Override
            public void onTimerStopped(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> updateButtonStates());
            }

            @Override
            public void onTimerTick(com.focusflow.core.timer.Timer timer, int remainingSeconds) {
                Platform.runLater(() -> updateTimeDisplay(remainingSeconds));
            }

            @Override
            public void onTimerReset(com.focusflow.core.timer.Timer timer) {
                Platform.runLater(() -> updateButtonStates());
            }
        });
    }

    /**
     * Creates the timer interface components.
     */
    private void createTimerInterface() {
        setAlignment(Pos.CENTER);
        setSpacing(15);
        setStyle(TIMER_CONTAINER_STYLE);

        // Current task indicator
        currentTaskLabel = new Label("No task selected");
        currentTaskLabel.setTextFill(Color.DARKSLATEGRAY);
        currentTaskLabel.setWrapText(true);
        currentTaskLabel.setAlignment(Pos.CENTER);

        // Timer display section
        timerDisplay = new VBox(5);
        timerDisplay.setAlignment(Pos.CENTER);

        timerTypeLabel = new Label("WORK SESSION");
        timerTypeLabel.setTextFill(Color.BLACK);
        timerTypeLabel.setAlignment(Pos.CENTER);

        timerLabel = new Label("25:00");
        timerLabel.setTextFill(Color.BLACK);
        timerLabel.setAlignment(Pos.CENTER);

        remainingLabel = new Label("REMAINING...");
        remainingLabel.setTextFill(Color.DARKSLATEGRAY);
        remainingLabel.setAlignment(Pos.CENTER);

        timerDisplay.getChildren().addAll(timerTypeLabel, timerLabel, remainingLabel);

        // Control buttons
        controlsContainer = new HBox(15);
        controlsContainer.setAlignment(Pos.CENTER);

        selectTaskButton = new Button("Select Task");
        selectTaskButton.setOnAction(e -> {
            if (onSelectTask != null) {
                onSelectTask.accept(null);
            }
        });

        startButton = new Button("START");
        startButton.setOnAction(e -> handleStartButtonClick());

        controlsContainer.getChildren().addAll(selectTaskButton, startButton);

        getChildren().addAll(currentTaskLabel, timerDisplay, controlsContainer);

        // Apply initial styling
        updateButtonStyles();
    }

    /**
     * Sets up responsive layout that adapts to container size changes.
     */
    private void setupResponsiveLayout() {
        // Listen for size changes to adjust scaling
        this.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            if (newWidth.doubleValue() > 0) {
                updateScaling(newWidth.doubleValue(), getHeight());
            }
        });

        this.heightProperty().addListener((obs, oldHeight, newHeight) -> {
            if (newHeight.doubleValue() > 0) {
                updateScaling(getWidth(), newHeight.doubleValue());
            }
        });

        // Set initial preferred size
        setPrefSize(baseWidth, baseHeight);
        setMaxHeight(Region.USE_PREF_SIZE);
    }

    /**
     * Updates the scaling factor based on available space.
     */
    private void updateScaling(double availableWidth, double availableHeight) {
        if (availableWidth <= 0 || availableHeight <= 0)
            return;

        // Calculate scale factor based on available space
        double widthScale = Math.max(0.5, Math.min(2.0, availableWidth / baseWidth));
        double heightScale = Math.max(0.5, Math.min(2.0, availableHeight / baseHeight));

        // Use the smaller scale to maintain proportions
        scaleFactor = Math.min(widthScale, heightScale);

        // Apply responsive font sizes
        updateFontSizes();
        updatePadding();
        updateSpacing();
    }

    /**
     * Updates font sizes based on current scale factor.
     */
    private void updateFontSizes() {
        // Task label font (responsive)
        double taskFontSize = Math.max(12, 14 * scaleFactor);
        currentTaskLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, taskFontSize));

        // Timer type label font (responsive)
        double typeFontSize = Math.max(14, 18 * scaleFactor);
        timerTypeLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, typeFontSize));

        // Main timer display font (responsive)
        double timerFontSize = Math.max(24, 48 * scaleFactor);
        timerLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, timerFontSize));

        // Remaining label font (responsive)
        double remainingFontSize = Math.max(10, 14 * scaleFactor);
        remainingLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, remainingFontSize));

        // Button fonts (responsive)
        double buttonFontSize = Math.max(12, 16 * scaleFactor);
        selectTaskButton.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, buttonFontSize));
        startButton.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, buttonFontSize));
    }

    /**
     * Updates padding based on current scale factor.
     */
    private void updatePadding() {
        double paddingValue = Math.max(15, 30 * scaleFactor);
        setPadding(new Insets(paddingValue));

        double displayPadding = Math.max(5, 10 * scaleFactor);
        timerDisplay.setPadding(new Insets(displayPadding));
    }

    /**
     * Updates spacing based on current scale factor.
     */
    private void updateSpacing() {
        double mainSpacing = Math.max(10, 15 * scaleFactor);
        setSpacing(mainSpacing);

        double displaySpacing = Math.max(3, 5 * scaleFactor);
        timerDisplay.setSpacing(displaySpacing);

        double controlSpacing = Math.max(10, 15 * scaleFactor);
        controlsContainer.setSpacing(controlSpacing);
    }

    /**
     * Handles the start button click with proper validation.
     */
    private void handleStartButtonClick() {
        System.out.println(
                "[TimerPanel] handleStartButtonClick: isOnBreak=" + isOnBreak + ", currentTask=" + currentTask);

        PomodoroTimer currentTimer = getCurrentTimer();
        System.out.println("[TimerPanel] currentTimer state: " + currentTimer.getState());

        if (currentTimer.getState() == TimerState.RUNNING) {
            // Timer is running, pause it
            currentTimer.pause();
        } else {
            // Timer is paused or stopped, try to start/resume it
            if (!isOnBreak && currentTask == null) {
                // Can't start work timer without a task
                if (onSelectTask != null) {
                    onSelectTask.accept(null);
                }
                return;
            }

            if (currentTimer.getState() == TimerState.PAUSED) {
                currentTimer.resume();
            } else {
                currentTimer.start();
            }
        }

        // Notify parent component if needed
        if (onToggleTimer != null) {
            onToggleTimer.accept(null);
        }
    }

    /**
     * Gets the currently active timer based on mode.
     */
    private PomodoroTimer getCurrentTimer() {
        return isOnBreak ? breakTimer : workTimer;
    }

    /**
     * Switches to work mode from break mode.
     */
    private void switchToWorkMode() {
        isOnBreak = false;
        timerTypeLabel.setText("WORK SESSION");
        timerTypeLabel.setTextFill(Color.web(WORK_TIMER_COLOR));
        updateTimeDisplay(25 * 60);
    }

    /**
     * Switches to break mode from work mode.
     */
    private void switchToBreakMode() {
        isOnBreak = true;
        timerTypeLabel.setText("BREAK TIME");
        timerTypeLabel.setTextFill(Color.web(BREAK_TIMER_COLOR));
        updateTimeDisplay(5 * 60);
    }

    /**
     * Updates the time display with color coding based on remaining time.
     */
    private void updateTimeDisplay(int remainingSeconds) {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        String timeText = String.format("%02d:%02d", minutes, seconds);

        timerLabel.setText(timeText);

        // Apply color coding based on remaining time
        Color timerColor;
        if (remainingSeconds <= 60) {
            // Last minute - urgent red
            timerColor = Color.web(URGENT_TIMER_COLOR);
        } else if (remainingSeconds <= 300) {
            // Last 5 minutes - warning orange
            timerColor = Color.web(WARNING_TIMER_COLOR);
        } else {
            // Normal time - use mode-appropriate color
            timerColor = Color.web(isOnBreak ? BREAK_TIMER_COLOR : WORK_TIMER_COLOR);
        }

        timerLabel.setTextFill(timerColor);
    }

    /**
     * Updates button states based on current timer state and conditions.
     */
    private void updateButtonStates() {
        PomodoroTimer currentTimer = getCurrentTimer();
        TimerState state = currentTimer.getState();

        // Update start button text and state
        switch (state) {
            case RUNNING:
                startButton.setText("PAUSE");
                startButton.setDisable(false);
                break;
            case PAUSED:
                startButton.setText("RESUME");
                startButton.setDisable(false);
                break;
            default:
                startButton.setText("START");
                // Disable start button if no task is selected for work mode
                startButton.setDisable(!isOnBreak && currentTask == null);
                break;
        }

        // Update select task button state
        selectTaskButton.setDisable(isOnBreak || state == TimerState.RUNNING);

        updateButtonStyles();
    }

    /**
     * Updates button styling with responsive sizing.
     */
    private void updateButtonStyles() {
        double buttonPadding = Math.max(8, 12 * scaleFactor);
        double buttonMinWidth = Math.max(80, 120 * scaleFactor);

        String selectTaskStyle = String.format(
                "-fx-background-color: %s; -fx-text-fill: white; " +
                        "-fx-background-radius: 15; -fx-padding: %.0f 20; " +
                        "-fx-font-weight: bold; -fx-min-width: %.0f; -fx-cursor: hand;",
                WORK_TIMER_COLOR, buttonPadding, buttonMinWidth);

        String startButtonStyle = String.format(
                "-fx-background-color: white; -fx-text-fill: black; " +
                        "-fx-background-radius: 15; -fx-padding: %.0f 20; " +
                        "-fx-font-weight: bold; -fx-min-width: %.0f; " +
                        "-fx-border-color: #DDD; -fx-border-radius: 15; -fx-cursor: hand;",
                buttonPadding, buttonMinWidth);

        selectTaskButton.setStyle(selectTaskStyle);
        startButton.setStyle(startButtonStyle);

        // Add hover effects
        addButtonHoverEffects();
    }

    /**
     * Adds hover effects to buttons for better user feedback.
     */
    private void addButtonHoverEffects() {
        // Select Task button hover
        selectTaskButton.setOnMouseEntered(e -> {
            if (!selectTaskButton.isDisabled()) {
                selectTaskButton.setStyle(selectTaskButton.getStyle() +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");
            }
        });

        selectTaskButton.setOnMouseExited(e -> {
            selectTaskButton.setStyle(selectTaskButton.getStyle().replace(
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);", ""));
        });

        // Start button hover
        startButton.setOnMouseEntered(e -> {
            if (!startButton.isDisabled()) {
                startButton.setStyle(startButton.getStyle() +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");
            }
        });

        startButton.setOnMouseExited(e -> {
            startButton.setStyle(startButton.getStyle().replace(
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);", ""));
        });
    }

    /**
     * Updates the entire timer display including all visual elements.
     */
    private void updateTimerDisplay() {
        PomodoroTimer currentTimer = getCurrentTimer();
        int remainingTime = currentTimer.getRemainingTime();

        updateTimeDisplay(remainingTime);
        updateButtonStates();

        // Update task label
        if (currentTask != null) {
            String taskText = "Working on: " + currentTask.getName();
            if (taskText.length() > 50) {
                taskText = taskText.substring(0, 47) + "...";
            }
            currentTaskLabel.setText(taskText);
            currentTaskLabel.setTextFill(Color.web("#2E7D32"));
        } else {
            currentTaskLabel.setText(isOnBreak ? "Break time! Relax and recharge" : "No task selected");
            currentTaskLabel.setTextFill(isOnBreak ? Color.web(BREAK_TIMER_COLOR) : Color.DARKSLATEGRAY);
        }
    }

    // Public API methods for external control

    /**
     * Sets the current task and updates the display.
     * 
     * @param task The task to set as current, or null to clear
     */
    public void setCurrentTask(Task task) {
        System.out.println("[TimerPanel] setCurrentTask: " + (task != null ? task.getName() : "null"));

        this.currentTask = task;

        // Set task ID for timers
        if (task != null) {
            workTimer.setCurrentTaskId(task.getId().toString());
        }

        updateTimerDisplay();
    }

    /**
     * Gets the current task.
     * 
     * @return The current task, or null if none is set
     */
    public Task getCurrentTask() {
        return currentTask;
    }

    /**
     * Starts the appropriate timer (work or break).
     */
    public void startTimer() {
        PomodoroTimer currentTimer = getCurrentTimer();
        if (currentTimer.getState() != TimerState.RUNNING) {
            if (!isOnBreak && currentTask == null) {
                // Can't start work timer without a task
                return;
            }
            currentTimer.start();
        }
    }

    /**
     * Pauses the current timer.
     */
    public void pauseTimer() {
        PomodoroTimer currentTimer = getCurrentTimer();
        if (currentTimer.getState() == TimerState.RUNNING) {
            currentTimer.pause();
        }
    }

    /**
     * Stops the current timer.
     */
    public void stopTimer() {
        getCurrentTimer().stop();
    }

    /**
     * Resets the current timer.
     */
    public void resetTimer() {
        getCurrentTimer().reset();
        updateTimerDisplay();
    }

    /**
     * Gets the current timer state.
     * 
     * @return The current TimerState
     */
    public TimerState getTimerState() {
        return getCurrentTimer().getState();
    }

    /**
     * Checks if currently in break mode.
     * 
     * @return true if in break mode, false if in work mode
     */
    public boolean isOnBreak() {
        return isOnBreak;
    }

    /**
     * Forces a switch to work mode.
     */
    public void forceWorkMode() {
        if (isOnBreak) {
            breakTimer.stop();
            switchToWorkMode();
            updateTimerDisplay();
        }
    }

    /**
     * Forces a switch to break mode.
     */
    public void forceBreakMode() {
        if (!isOnBreak) {
            workTimer.stop();
            switchToBreakMode();
            updateTimerDisplay();
        }
    }

    /**
     * Gets the work timer instance.
     * 
     * @return The work PomodoroTimer
     */
    public PomodoroTimer getWorkTimer() {
        return workTimer;
    }

    /**
     * Gets the break timer instance.
     * 
     * @return The break PomodoroTimer
     */
    public PomodoroTimer getBreakTimer() {
        return breakTimer;
    }

    /**
     * Updates the scale factor manually if needed.
     * 
     * @param newScaleFactor The new scale factor (0.5 to 2.0)
     */
    public void setScaleFactor(double newScaleFactor) {
        this.scaleFactor = Math.max(0.5, Math.min(2.0, newScaleFactor));
        updateFontSizes();
        updatePadding();
        updateSpacing();
        updateButtonStyles();
    }

    /**
     * Gets the current scale factor.
     * 
     * @return The current scale factor
     */
    public double getScaleFactor() {
        return scaleFactor;
    }
}