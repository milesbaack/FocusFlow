package com.focusflow.app;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Main application class for FocusFlow.
 * 
 * This class implements the main user interface for the FocusFlow application,
 * integrating the timer, task management, and session tracking features
 * 
 * @author Emilio Lopez & Miles Baack
 * @version 2.0.0
 */
public class App extends Application implements TimerEventListener {
    private PomodoroTimer timer;
    private SessionManager sessionManager;
    private Task currentTask;
    private List<Task> tasks = new ArrayList<>();
    private List<HBox> taskRows = new ArrayList<>();
    private int selectedTaskIndex = -1;

    // UI Components
    private Label timerLabel;
    private ProgressBar progressBar;
    private Button startButton;
    private MediaPlayer backgroundMusic;
    private boolean isMuted = false;
    private TextArea notepadArea;
    private boolean isNotepadOpen = false;
    private AnchorPane notepadPane;
    private ImageView soundIcon;
    private AnchorPane infoPane;
    private boolean isInfoOpen = false;
    private VBox taskBox;
    private Font pixelFont;

    @Override
    public void start(Stage stage) {
        // Try to load pixel font
        try {
            InputStream is = getClass().getResourceAsStream("/UI/pixel.ttf");
            if (is != null) {
                pixelFont = Font.loadFont(is, 24);
            } else {
                // Fallback to a system font that looks pixelated
                pixelFont = Font.font("Courier New", FontWeight.BOLD, 24);
            }
        } catch (Exception e) {
            // Fallback to a system font that looks pixelated
            pixelFont = Font.font("Courier New", FontWeight.BOLD, 24);
        }

        // Initialize components
        sessionManager = new SessionManager();
        timer = new PomodoroTimer(TimerType.WORK, 35 * 60); // 35 minutes
        timer.addListener(this);

        // Load main background
        Image backgroundImage = new Image(getClass().getResource("/UI/MainBG.png").toString());
        ImageView background = new ImageView(backgroundImage);

        // Main layout
        AnchorPane root = new AnchorPane();

        // Set background to fill entire scene
        background.setFitWidth(1070);
        background.setFitHeight(620);
        background.setPreserveRatio(false); // This allows stretching
        AnchorPane.setTopAnchor(background, 0.0);
        AnchorPane.setLeftAnchor(background, 0.0);
        AnchorPane.setRightAnchor(background, 0.0);
        AnchorPane.setBottomAnchor(background, 0.0);
        root.getChildren().add(background);

        // Create sidebar (white panel on the left)
        Rectangle sidebar = new Rectangle(70, 600);
        sidebar.setFill(Color.WHITE);
        AnchorPane.setLeftAnchor(sidebar, 0.0);
        AnchorPane.setTopAnchor(sidebar, 0.0);

        // Sidebar icons
        VBox sidebarIcons = new VBox(20);
        sidebarIcons.setPadding(new Insets(0, 0, 0, 45)); // Add padding from the left
        sidebarIcons.setAlignment(Pos.CENTER); // Vertically center the VBox
        sidebarIcons.setSpacing(30); // Increase spacing between icons
        AnchorPane.setLeftAnchor(sidebarIcons, 0.0);
        AnchorPane.setTopAnchor(sidebarIcons, 0.0);
        AnchorPane.setBottomAnchor(sidebarIcons, 0.0); // Ensure it stretches vertically

        // Clock icon
        // Image clockImg = new
        // Image(getClass().getResource("/UI/clock.png").toString(), 40, 40, true,
        // true);
        // ImageView clockIcon = new ImageView(clockImg);
        // StackPane clockBtn = new StackPane(clockIcon);
        // clockBtn.setStyle("-fx-cursor: hand;");
        final int ICON_SIZE = 50;
        // Add task button
        Image addTaskImg = new Image(getClass().getResource("/UI/AddTask.png").toString(), ICON_SIZE, ICON_SIZE, true,
                true);
        ImageView addTaskIcon = new ImageView(addTaskImg);
        StackPane addTaskBtn = new StackPane(addTaskIcon);

        addTaskBtn.setStyle("-fx-cursor: hand;");

        // Stats icon
        Image statsImg = new Image(getClass().getResource("/UI/StatsIcon.png").toString(), ICON_SIZE, ICON_SIZE, true,
                true);
        ImageView statsIcon = new ImageView(statsImg);
        StackPane statsBtn = new StackPane(statsIcon);
        statsBtn.setStyle("-fx-cursor: hand;");

        // Calendar icon
        Image calendarImg = new Image(getClass().getResource("/UI/CalendarIcon.png").toString(), ICON_SIZE, ICON_SIZE,
                true, true);
        ImageView calendarIcon = new ImageView(calendarImg);
        StackPane calendarBtn = new StackPane(calendarIcon);
        calendarBtn.setStyle("-fx-cursor: hand;");

        // Question icon
        Image questionImg = new Image(getClass().getResource("/UI/question.png").toString(), ICON_SIZE, ICON_SIZE, true,
                true);
        ImageView questionIcon = new ImageView(questionImg);
        StackPane questionBtn = new StackPane(questionIcon);
        questionBtn.setStyle("-fx-cursor: hand;");

        // Add all icons to sidebar
        sidebarIcons.getChildren().addAll(addTaskBtn, statsBtn, calendarBtn, questionBtn);

        // Timer display (in the clock)
        timerLabel = new Label("12:56");
        timerLabel.setFont(Font.font(pixelFont.getFamily(), 80)); // Set font size directly
        timerLabel.setTextFill(Color.BLACK);
        // increase font size for better visibility
        AnchorPane.setTopAnchor(timerLabel, 170.0);
        AnchorPane.setRightAnchor(timerLabel, 170.0);

        // "REMAINING..." text under timer
        Label remainingLabel = new Label("REMAINING...");
        remainingLabel.setFont(Font.font(pixelFont.getFamily(), 24)); // Set font size directly
        remainingLabel.setTextFill(Color.BLACK);
        AnchorPane.setTopAnchor(remainingLabel, 240.0);
        AnchorPane.setRightAnchor(remainingLabel, 170.0);

        // Start button
        startButton = new Button("START");
        startButton.setFont(Font.font(pixelFont.getFamily(), 24));
        startButton.setStyle("-fx-background-color: #65B054; -fx-text-fill: white; -fx-background-radius: 15;");
        startButton.setPrefSize(80, 25);
        AnchorPane.setTopAnchor(startButton, 395.0);
        AnchorPane.setRightAnchor(startButton, 190.0);

        // Task list (near the left side)
        taskBox = new VBox(10);
        taskBox.setPadding(new Insets(10));
        AnchorPane.setTopAnchor(taskBox, 160.0);
        AnchorPane.setLeftAnchor(taskBox, 100.0);

        // Let's Go button
        Button letsGoButton = new Button("Let's Go!");
        letsGoButton.setStyle("-fx-background-color: #FFD0B0; -fx-text-fill: black; -fx-background-radius: 10;");
        AnchorPane.setTopAnchor(letsGoButton, 470.0);
        AnchorPane.setRightAnchor(letsGoButton, 10.0);

        // Progress bar
        progressBar = new ProgressBar(0.5); // Initial value showing example
        progressBar.setPrefWidth(950);
        progressBar.setPrefHeight(48);
        progressBar.setStyle("-fx-accent: #FFA500;");
        AnchorPane.setBottomAnchor(progressBar, 40.0);
        AnchorPane.setLeftAnchor(progressBar, 20.0);

        // Sound icon in top right
        Image soundImg = new Image(getClass().getResource("/UI/sound.png").toString(), 40, 40, true, true);
        soundIcon = new ImageView(soundImg);
        StackPane soundBtn = new StackPane(soundIcon);
        soundBtn.setStyle("-fx-cursor: hand;");
        AnchorPane.setTopAnchor(soundBtn, 20.0);
        AnchorPane.setRightAnchor(soundBtn, 20.0);

        // Notepad icon in top right
        Image notepadImg = new Image(getClass().getResource("/UI/Notepad.png").toString(), 40, 40, true, true);
        ImageView notepadIcon = new ImageView(notepadImg);
        StackPane notepadBtn = new StackPane(notepadIcon);
        notepadBtn.setStyle("-fx-cursor: hand;");
        AnchorPane.setTopAnchor(notepadBtn, 20.0);
        AnchorPane.setRightAnchor(notepadBtn, 80.0);

        // Notepad panel (hidden initially)
        notepadPane = new AnchorPane();
        Rectangle notepadBg = new Rectangle(300, 400);
        notepadBg.setFill(Color.WHITE);
        notepadBg.setStroke(Color.BLACK);

        notepadArea = new TextArea();
        notepadArea.setPrefSize(280, 380);
        notepadArea.setWrapText(true);

        AnchorPane.setTopAnchor(notepadBg, 0.0);
        AnchorPane.setLeftAnchor(notepadBg, 0.0);
        AnchorPane.setTopAnchor(notepadArea, 10.0);
        AnchorPane.setLeftAnchor(notepadArea, 10.0);

        notepadPane.getChildren().addAll(notepadBg, notepadArea);
        notepadPane.setVisible(false);

        AnchorPane.setTopAnchor(notepadPane, 70.0);
        AnchorPane.setRightAnchor(notepadPane, 80.0);

        // Info panel (hidden initially)
        infoPane = new AnchorPane();
        Rectangle infoBg = new Rectangle(400, 300);
        infoBg.setFill(Color.WHITE);
        infoBg.setStroke(Color.BLACK);

        Label infoTitle = new Label("FocusFlow - App Information");
        infoTitle.setFont(Font.font("Arial", 16));

        TextArea infoText = new TextArea();
        infoText.setText("FocusFlow is a productivity app that helps you focus on tasks.\n\n" +
                "- Add tasks with the + button\n" +
                "- Select a task to start the timer\n" +
                "- Tasks are automatically marked as completed when the timer ends\n" +
                "- Progress bar fills as you complete tasks\n" +
                "- Use the notepad for quick notes\n" +
                "- Toggle background music with the sound button");
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
        AnchorPane.setLeftAnchor(infoPane, 300.0);

        // Add a star icon at the bottom right of progress bar
        Image starImg = new Image(getClass().getResource("/UI/star.png").toString(),
                60, 60, true, true);
        ImageView starIcon = new ImageView(starImg);
        AnchorPane.setBottomAnchor(starIcon, 50.0);
        AnchorPane.setRightAnchor(starIcon, 10.0);

        // Add components to root
        root.getChildren().addAll(
                // sidebar,
                sidebarIcons,
                timerLabel,
                remainingLabel,
                startButton,
                taskBox,
                progressBar,
                soundBtn,
                notepadBtn,
                notepadPane,
                infoPane,
                letsGoButton, starIcon);

        // Button actions
        startButton.setOnAction(e -> toggleTimer());

        addTaskBtn.setOnMouseClicked(e -> showAddTaskDialog(stage));

        statsBtn.setOnMouseClicked(e -> {
            // Placeholder for stats functionality
            System.out.println("Stats button clicked");
            openStats();
        });

        calendarBtn.setOnMouseClicked(e -> {
            // Placeholder for calendar functionality
            System.out.println("Calendar button clicked");
        });

        soundBtn.setOnMouseClicked(e -> toggleSound());

        notepadBtn.setOnMouseClicked(e -> toggleNotepad());

        questionBtn.setOnMouseClicked(e -> toggleInfo());



        // Setup background music
        try {
            Media sound = new Media(getClass().getResource("/UI/background_music.wav").toString());
            backgroundMusic = new MediaPlayer(sound);
            backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusic.play();
        } catch (Exception e) {
            System.out.println("Could not load background music: " + e.getMessage());
        }

        // Set up the main scene
        Scene scene = new Scene(root, 1070, 610);
        stage.setScene(scene);
        stage.setTitle("FocusFlow");

        stage.setResizable(false);
        stage.show();
    }

    private void addTaskToUI(Task task, int index) {
        HBox taskRow = new HBox(10);

        // Create the checkbox
        CheckBox checkbox = new CheckBox();
        checkbox.setSelected(task.isComplete());

        // Create the task label
        Label taskLabel = new Label(task.getName());
        taskLabel.setPrefWidth(150);
        taskLabel.setFont(Font.font("Arial", 14));

        // Add style for selection
        if (index == selectedTaskIndex) {
            taskLabel.setStyle("-fx-text-fill: #65B054; -fx-font-weight: bold;");
        } else {
            taskLabel.setStyle("-fx-text-fill: black;");
        }

        // Add checkbox and label to row
        taskRow.getChildren().addAll(checkbox, taskLabel);

        // Add click handler for label (selecting task)
        taskLabel.setOnMouseClicked(e -> {
            selectTask(index);
        });

        // Add click handler for checkbox (marking task complete)
        checkbox.setOnAction(e -> {
            if (checkbox.isSelected()) {
                tasks.get(index).markAsCompleted();
                updateProgressBar();
            } else {
                tasks.get(index).markAsIncomplete();
                updateProgressBar();
            }
        });

        // Add to task rows list and to the UI
        taskRows.add(taskRow);
        taskBox.getChildren().add(taskRow);
    }

    private void selectTask(int index) {
        // Deselect previous task
        if (selectedTaskIndex == index) {
            return; // Already selected
        }
        if (selectedTaskIndex >= 0 && selectedTaskIndex < taskRows.size()) {
            Label previousLabel = (Label) taskRows.get(selectedTaskIndex).getChildren().get(1);
            previousLabel.setStyle("-fx-text-fill: black;");
        }

        // Select new task
        selectedTaskIndex = index;
        if (index >= 0 && index < taskRows.size()) {
            Label newLabel = (Label) taskRows.get(index).getChildren().get(1);
            newLabel.setStyle("-fx-text-fill: #65B054; -fx-font-weight: bold;");

            // Set as current task
            currentTask = tasks.get(index);
            System.out.println("Selected task: " + currentTask.getId() + " - " + currentTask.getName() + " duration: "
                    + currentTask.getDuration());
            // Get task duration from its description
            int minutes = currentTask.getDuration();

            // Parse the duration
            // Reset and reconfigure timer for this task
            timer.reset();
            timer = new PomodoroTimer(TimerType.WORK, minutes * 60);
            timer.addListener(this);

            // Update timer display
            updateTimerDisplay(minutes * 60);
        }
    }

    private void showAddTaskDialog(Stage parentStage) {
        Stage dialog = new Stage();
        dialog.initOwner(parentStage);
        dialog.setTitle("Add Task");

        VBox dialogVbox = new VBox(10);
        dialogVbox.setPadding(new Insets(20));

        TextField taskNameField = new TextField();
        taskNameField.setPromptText("Task Name");

        TextField durationField = new TextField();
        durationField.setPromptText("Duration in minutes");

        Button addButton = new Button("Add Task");
        addButton.setOnAction(e -> {
            String taskName = taskNameField.getText();
            if (!taskName.isEmpty() && !durationField.getText().isEmpty()) {
                Task newTask = new Task(taskName, "");
                System.out.println("Adding task: " + newTask.getId() + " - " + taskName);
                // Store the selected duration in the task description (as a hacky way to save
                // it)
                newTask.setDuration(Integer.parseInt(durationField.getText()));
                tasks.add(newTask);

                int newIndex = tasks.size() - 1;
                addTaskToUI(newTask, newIndex);

                // Automatically select the new task
                selectTask(newIndex);

                //Update progress bar
                updateProgressBar();

                dialog.close();
            }
        });

        dialogVbox.getChildren().addAll(
                new Label("Task Name:"),
                taskNameField,
                new Label("Duration:"),
                durationField,
                addButton);

        Scene dialogScene = new Scene(dialogVbox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void toggleTimer() {
        if (timer.getState() == TimerState.RUNNING) {
            timer.pause();
            startButton.setText("START");
        } else {
            if (selectedTaskIndex >= 0) {
                System.out.println("Starting timer for task: " + selectedTaskIndex);
                currentTask = tasks.get(selectedTaskIndex);
                System.out.println("Current task ID: " + (currentTask != null ? currentTask.getId() : "null"));
                if (currentTask != null && currentTask.getId() != null && !currentTask.getId().toString().isEmpty()) {
                    timer.setCurrentTaskId(currentTask.getId().toString());
                    timer.start();
                    startButton.setText("PAUSE");
                } else {
                    System.out.println("Error: Task ID is null or empty. Cannot start the timer.");
                }
            } else {
                System.out.println("Error: No task selected. Cannot start the timer.");
            }
        }
    }

    private void toggleSound() {
        if (isMuted) {
            if (backgroundMusic != null) {
                backgroundMusic.play();
            }
            soundIcon.setOpacity(1.0);
        } else {
            if (backgroundMusic != null) {
                backgroundMusic.pause();
            }
            soundIcon.setOpacity(0.5);
        }
        isMuted = !isMuted;
    }

    private void toggleNotepad() {
        notepadPane.setVisible(!isNotepadOpen);
        isNotepadOpen = !isNotepadOpen;
    }

    private void toggleInfo() {
        System.out.println("Toggling info pane visibility");
        infoPane.setVisible(!isInfoOpen);
        isInfoOpen = !isInfoOpen;
    }

    private void openStats() {
        // Blank screen
        Stage statsStage = new Stage();
        statsStage.setTitle("Statistics");
        VBox statsLayout = new VBox(20);
        statsLayout.setPadding(new Insets(20));
        Label statsLabel = new Label("Statistics will be displayed here.");
        statsLabel.setFont(Font.font(pixelFont.getFamily(), 24));
        statsLabel.setTextFill(Color.BLACK);
        statsLayout.getChildren().add(statsLabel);
        Scene statsScene = new Scene(statsLayout, 400, 300);
        statsStage.setScene(statsScene);
        statsStage.show();

    }

    private void updateProgressBar() {
        int completedTasks = 0;
        for (Task task : tasks) {
            if (task.isComplete()) {
                completedTasks++;
            }
        }

        double progress = tasks.isEmpty() ? 0 : (double) completedTasks / tasks.size();
        progressBar.setProgress(progress);
    }

    private void updateTimerDisplay(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, remainingSeconds));
        System.out.println("Updating timer display: " + String.format("%02d:%02d", minutes, remainingSeconds));
    }

    // TimerEventListener methods
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

            // Mark current task as completed
            if (currentTask != null && selectedTaskIndex >= 0) {
                currentTask.markAsCompleted();

                // Update checkbox
                if (selectedTaskIndex < taskRows.size()) {
                    CheckBox checkbox = (CheckBox) taskRows.get(selectedTaskIndex).getChildren().get(0);
                    checkbox.setSelected(true);
                }

                updateProgressBar();
            }
        });
    }

    @Override
    public void onTimerStopped(com.focusflow.core.timer.Timer timer) {
        Platform.runLater(() -> startButton.setText("START"));
    }

    @Override
    public void onTimerTick(com.focusflow.core.timer.Timer timer, int remainingSeconds) {
        System.out.println("Timer tick: " + remainingSeconds + " seconds remaining");
        Platform.runLater(() -> updateTimerDisplay(remainingSeconds));
    }

    @Override
    public void onTimerReset(com.focusflow.core.timer.Timer timer) {
        Platform.runLater(() -> {
            startButton.setText("START");
            System.out.println("Timer reset");
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}