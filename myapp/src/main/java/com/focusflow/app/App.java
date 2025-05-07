package com.focusflow.app;

import java.time.format.DateTimeFormatter;

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
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Main application class for FocusFlow.
 * 
 * This class implements the main user interface for the FocusFlow application,
 * integrating the timer, task management, and session tracking features.
 * 
 * @author Emilio Lopez
 * @version 1.0.0
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

    @Override
    public void start(Stage stage) {
        // Initialize components
        sessionManager = new SessionManager();
        timer = new PomodoroTimer(TimerType.WORK);
        currentTask = null;
        
        // Create UI components
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

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
        timerControls.getChildren().addAll(startButton, pauseButton, resetButton);

        // Session history
        sessionHistoryArea = new TextArea();
        sessionHistoryArea.setEditable(false);
        sessionHistoryArea.setPrefRowCount(5);

        // Add components to root
        root.getChildren().addAll(
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
            sessionHistoryArea
        );

        // Create scene
        Scene scene = new Scene(root, 400, 600);
        stage.setTitle("FocusFlow");
        stage.setScene(scene);
        stage.show();

        // Initialize UI state
        updateUIState();
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
        }
    }

    private void selectTask(Task task) {
        if (task != null) {
            currentTask = task;
            updateTaskDisplay();
            updateUIState();
            // Set the task ID for the timer
            timer.setCurrentTaskId(task.getId().toString());
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
    }

    private void updateSessionHistory() {
        StringBuilder history = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (FocusSession session : sessionManager.getSessionHistory()) {
            history.append(String.format(
                "Session: %s - Duration: %d seconds\n",
                session.getStartTime().format(formatter),
                session.getDurationSeconds()
            ));
        }
        sessionHistoryArea.setText(history.toString());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
