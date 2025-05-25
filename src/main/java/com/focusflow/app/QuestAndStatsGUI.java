package com.focusflow.app;

import java.util.ArrayList;
import java.util.List;

import com.focusflow.core.analytics.Analytics;
import com.focusflow.core.gameify.Achievement;
import com.focusflow.core.gameify.AchievementManager;
import com.focusflow.core.gameify.Quest;
import com.focusflow.core.gameify.QuestManager;
import com.focusflow.core.gameify.XpManager;
import com.focusflow.core.session.FocusSession;
import com.focusflow.core.task.Task;
import com.focusflow.core.task.TaskCategory;
import com.focusflow.core.task.TaskPriority;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * GUI components for Quest Creation and Statistics
 * This class provides methods to integrate with your existing App class
 */
public class QuestAndStatsGUI {
    
    private QuestManager questManager;
    private AchievementManager achievementManager;
    private XpManager xpManager;
    private Analytics analytics;
    private Font pixelFont;
    
    // Constructor
    public QuestAndStatsGUI() {
        // Initialize gamification components
        this.xpManager = new XpManager();
        this.achievementManager = new AchievementManager();
        this.questManager = new QuestManager(achievementManager, xpManager);
        this.analytics = new Analytics();
        
        // Initialize achievements
        setupAchievements();
        
        // Load pixel font (you'll need to adapt this to your font loading)
        this.pixelFont = Font.font("Courier New", FontWeight.BOLD, 16);
    }
    
    private void setupAchievements() {
        for (Achievement achievement : Achievement.values()) {
            achievementManager.addAchievement(achievement);
        }
    }
    
    /**
     * Shows the Quest Creation Dialog
     */
    public void showQuestCreationDialog(Stage parentStage, List<Task> availableTasks) {
        Stage questDialog = new Stage();
        questDialog.initModality(Modality.WINDOW_MODAL);
        questDialog.initOwner(parentStage);
        questDialog.setTitle("Create New Quest");
        questDialog.setResizable(false);
        
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f0f0f0;");
        
        // Quest basic info
        Text titleText = new Text("Create New Quest");
        titleText.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 24));
        titleText.setFill(Color.DARKBLUE);
        
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(10);
        
        Label questNameLabel = new Label("Quest Name:");
        questNameLabel.setFont(pixelFont);
        TextField questNameField = new TextField();
        questNameField.setPromptText("Enter quest name...");
        questNameField.setPrefWidth(300);
        
        Label questDescLabel = new Label("Description:");
        questDescLabel.setFont(pixelFont);
        TextArea questDescArea = new TextArea();
        questDescArea.setPromptText("Describe your quest...");
        questDescArea.setPrefRowCount(3);
        questDescArea.setPrefWidth(300);
        questDescArea.setWrapText(true);
        
        Label baseXpLabel = new Label("Base XP Reward:");
        baseXpLabel.setFont(pixelFont);
        TextField baseXpField = new TextField("50");
        baseXpField.setPrefWidth(100);
        
        Label achievementLabel = new Label("Achievement Reward:");
        achievementLabel.setFont(pixelFont);
        ComboBox<Achievement> achievementCombo = new ComboBox<>();
        achievementCombo.getItems().addAll(Achievement.values());
        achievementCombo.setValue(Achievement.COMPLETED_FIRST_QUEST);
        achievementCombo.setPrefWidth(250);
        
        infoGrid.add(questNameLabel, 0, 0);
        infoGrid.add(questNameField, 1, 0);
        infoGrid.add(questDescLabel, 0, 1);
        infoGrid.add(questDescArea, 1, 1);
        infoGrid.add(baseXpLabel, 0, 2);
        infoGrid.add(baseXpField, 1, 2);
        infoGrid.add(achievementLabel, 0, 3);
        infoGrid.add(achievementCombo, 1, 3);
        
        // Task selection section
        Text taskSelectionText = new Text("Select Tasks for Quest");
        taskSelectionText.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 18));
        
        // Available tasks list
        Label availableLabel = new Label("Available Tasks:");
        availableLabel.setFont(pixelFont);
        ListView<Task> availableTasksList = new ListView<>();
        availableTasksList.getItems().addAll(availableTasks);
        availableTasksList.setPrefHeight(150);
        availableTasksList.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        
        // Selected tasks list
        Label selectedLabel = new Label("Selected Tasks:");
        selectedLabel.setFont(pixelFont);
        ListView<Task> selectedTasksList = new ListView<>();
        selectedTasksList.setPrefHeight(150);
        
        // Task creation section
        Text newTaskText = new Text("Or Create New Tasks");
        newTaskText.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 16));
        
        HBox newTaskBox = new HBox(10);
        TextField newTaskNameField = new TextField();
        newTaskNameField.setPromptText("Task name...");
        newTaskNameField.setPrefWidth(200);
        
        TextField newTaskDurationField = new TextField();
        newTaskDurationField.setPromptText("Duration (min)");
        newTaskDurationField.setPrefWidth(100);
        
        ComboBox<TaskPriority> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll(TaskPriority.values());
        priorityCombo.setValue(TaskPriority.MEDIUM);
        
        Button addTaskButton = new Button("Add Task");
        addTaskButton.setFont(pixelFont);
        addTaskButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        
        newTaskBox.getChildren().addAll(newTaskNameField, newTaskDurationField, priorityCombo, addTaskButton);
        
        // Buttons for moving tasks
        HBox taskButtonsBox = new HBox(10);
        taskButtonsBox.setAlignment(Pos.CENTER);
        
        Button addSelectedButton = new Button("Add Selected →");
        addSelectedButton.setFont(pixelFont);
        addSelectedButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        
        Button removeSelectedButton = new Button("← Remove Selected");
        removeSelectedButton.setFont(pixelFont);
        removeSelectedButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        
        taskButtonsBox.getChildren().addAll(addSelectedButton, removeSelectedButton);
        
        // Task lists layout
        HBox taskListsBox = new HBox(15);
        VBox availableBox = new VBox(5);
        availableBox.getChildren().addAll(availableLabel, availableTasksList);
        
        VBox selectedBox = new VBox(5);
        selectedBox.getChildren().addAll(selectedLabel, selectedTasksList);
        
        taskListsBox.getChildren().addAll(availableBox, selectedBox);
        
        // Final buttons
        HBox finalButtonsBox = new HBox(15);
        finalButtonsBox.setAlignment(Pos.CENTER);
        
        Button createQuestButton = new Button("Create Quest");
        createQuestButton.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 16));
        createQuestButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 10px 20px;");
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setFont(pixelFont);
        cancelButton.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-padding: 10px 20px;");
        
        finalButtonsBox.getChildren().addAll(createQuestButton, cancelButton);
        
        // Event handlers
        addSelectedButton.setOnAction(e -> {
            List<Task> selected = new ArrayList<>(availableTasksList.getSelectionModel().getSelectedItems());
            selectedTasksList.getItems().addAll(selected);
            availableTasksList.getItems().removeAll(selected);
        });
        
        removeSelectedButton.setOnAction(e -> {
            List<Task> selected = new ArrayList<>(selectedTasksList.getSelectionModel().getSelectedItems());
            availableTasksList.getItems().addAll(selected);
            selectedTasksList.getItems().removeAll(selected);
        });
        
        addTaskButton.setOnAction(e -> {
            String taskName = newTaskNameField.getText().trim();
            String durationText = newTaskDurationField.getText().trim();
            
            if (!taskName.isEmpty() && !durationText.isEmpty()) {
                try {
                    int duration = Integer.parseInt(durationText);
                    Task newTask = new Task(taskName, "Created for quest");
                    newTask.setDuration(duration);
                    newTask.setPriority(priorityCombo.getValue());
                    newTask.setCategory(new TaskCategory("Quest"));
                    
                    selectedTasksList.getItems().add(newTask);
                    newTaskNameField.clear();
                    newTaskDurationField.clear();
                } catch (NumberFormatException ex) {
                    // Handle invalid duration input
                    newTaskDurationField.setStyle("-fx-border-color: red;");
                }
            }
        });
        
        createQuestButton.setOnAction(e -> {
            String questName = questNameField.getText().trim();
            String questDesc = questDescArea.getText().trim();
            String baseXpText = baseXpField.getText().trim();
            
            if (!questName.isEmpty() && !selectedTasksList.getItems().isEmpty()) {
                try {
                    int baseXp = Integer.parseInt(baseXpText);
                    Achievement selectedAchievement = achievementCombo.getValue();
                    
                    Quest newQuest = new Quest(questName, questDesc, selectedAchievement, baseXp);
                    
                    // Add all selected tasks to the quest
                    for (Task task : selectedTasksList.getItems()) {
                        newQuest.addTask(task);
                    }
                    
                    // Add quest to manager
                    questManager.addQuest(newQuest);
                    
                    System.out.println("Quest created: " + questName + " with " + 
                                     selectedTasksList.getItems().size() + " tasks");
                    
                    questDialog.close();
                } catch (NumberFormatException ex) {
                    baseXpField.setStyle("-fx-border-color: red;");
                }
            }
        });
        
        cancelButton.setOnAction(e -> questDialog.close());
        
        // Add all components to main layout
        mainLayout.getChildren().addAll(
            titleText,
            infoGrid,
            taskSelectionText,
            taskListsBox,
            taskButtonsBox,
            newTaskText,
            newTaskBox,
            finalButtonsBox
        );
        
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f0f0f0;");
        
        Scene scene = new Scene(scrollPane, 700, 800);
        questDialog.setScene(scene);
        questDialog.showAndWait();
    }
    
    /**
     * Shows the Statistics Window
     */
    public void showStatisticsWindow(Stage parentStage, List<Task> tasks, List<FocusSession> sessions) {
        Stage statsStage = new Stage();
        statsStage.initOwner(parentStage);
        statsStage.setTitle("FocusFlow Statistics");
        statsStage.setResizable(true);
        
        TabPane tabPane = new TabPane();
        
        // Overview Tab
        Tab overviewTab = new Tab("Overview");
        overviewTab.setClosable(false);
        overviewTab.setContent(createOverviewTab(tasks, sessions));
        
        // Productivity Tab
        Tab productivityTab = new Tab("Productivity");
        productivityTab.setClosable(false);
        productivityTab.setContent(createProductivityTab(tasks, sessions));
        
        // Quests Tab
        Tab questsTab = new Tab("Quests & Achievements");
        questsTab.setClosable(false);
        questsTab.setContent(createQuestsTab());
        
        // Time Tracking Tab
        Tab timeTab = new Tab("Time Tracking");
        timeTab.setClosable(false);
        timeTab.setContent(createTimeTrackingTab(sessions));
        
        tabPane.getTabs().addAll(overviewTab, productivityTab, questsTab, timeTab);
        
        Scene scene = new Scene(tabPane, 900, 700);
        statsStage.setScene(scene);
        statsStage.show();
    }
    
    private VBox createOverviewTab(List<Task> tasks, List<FocusSession> sessions) {
        VBox overviewBox = new VBox(20);
        overviewBox.setPadding(new Insets(20));
        
        // Title
        Text titleText = new Text("Statistics Overview");
        titleText.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 24));
        titleText.setFill(Color.DARKBLUE);
        
        // Key metrics grid
        GridPane metricsGrid = new GridPane();
        metricsGrid.setHgap(30);
        metricsGrid.setVgap(15);
        
        // Calculate statistics
        int totalTasks = tasks.size();
        int completedTasks = (int) tasks.stream().filter(Task::isComplete).count();
        int totalSessions = sessions.size();
        long totalFocusTime = sessions.stream().mapToLong(FocusSession::getDurationSeconds).sum();
        double productivityScore = analytics.getProductivityScore();
        
        // Create metric cards
        addMetricCard(metricsGrid, 0, 0, "Total Tasks", String.valueOf(totalTasks), "#4CAF50");
        addMetricCard(metricsGrid, 1, 0, "Completed Tasks", String.valueOf(completedTasks), "#2196F3");
        addMetricCard(metricsGrid, 2, 0, "Focus Sessions", String.valueOf(totalSessions), "#FF9800");
        addMetricCard(metricsGrid, 0, 1, "Total Focus Time", formatDuration(totalFocusTime), "#9C27B0");
        addMetricCard(metricsGrid, 1, 1, "Productivity Score", String.format("%.1f%%", productivityScore), "#F44336");
        addMetricCard(metricsGrid, 2, 1, "Current Level", String.valueOf(xpManager.getCurrentLevel()), "#795548");
        
        // Task completion chart
        PieChart taskChart = createTaskCompletionChart(completedTasks, totalTasks - completedTasks);
        taskChart.setTitle("Task Completion Status");
        
        overviewBox.getChildren().addAll(titleText, metricsGrid, taskChart);
        
        return overviewBox;
    }
    
    private void addMetricCard(GridPane grid, int col, int row, String title, String value, String color) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: white; -fx-border-color: " + color + 
                     "; -fx-border-width: 2px; -fx-padding: 15px; -fx-border-radius: 5px;");
        card.setPrefWidth(150);
        card.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, 12));
        titleLabel.setTextFill(Color.GRAY);
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 20));
        valueLabel.setTextFill(Color.web(color));
        
        card.getChildren().addAll(titleLabel, valueLabel);
        grid.add(card, col, row);
    }
    
    private VBox createProductivityTab(List<Task> tasks, List<FocusSession> sessions) {
        VBox productivityBox = new VBox(20);
        productivityBox.setPadding(new Insets(20));
        
        Text titleText = new Text("Productivity Analysis");
        titleText.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 24));
        
        // Daily productivity chart
        LineChart<String, Number> dailyChart = createDailyProductivityChart(sessions);
        
        // Task priority distribution
        BarChart<String, Number> priorityChart = createTaskPriorityChart(tasks);
        
        productivityBox.getChildren().addAll(titleText, dailyChart, priorityChart);
        
        return productivityBox;
    }
    
    private VBox createQuestsTab() {
        VBox questsBox = new VBox(20);
        questsBox.setPadding(new Insets(20));
        
        Text titleText = new Text("Quests & Achievements");
        titleText.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 24));
        
        // XP and Level info
        HBox xpBox = new HBox(20);
        xpBox.setAlignment(Pos.CENTER_LEFT);
        
        Label levelLabel = new Label("Level: " + xpManager.getCurrentLevel());
        levelLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 18));
        
        Label xpLabel = new Label("XP: " + xpManager.getCurrentXp() + " / " + xpManager.getXpForNextLevel());
        xpLabel.setFont(pixelFont);
        
        ProgressBar xpProgressBar = new ProgressBar();
        xpProgressBar.setProgress(xpManager.getLevelProgressPercentage() / 100.0);
        xpProgressBar.setPrefWidth(200);
        
        xpBox.getChildren().addAll(levelLabel, xpLabel, xpProgressBar);
        
        // Active Quests
        Text activeQuestsText = new Text("Active Quests");
        activeQuestsText.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 18));
        
        ListView<Quest> activeQuestsList = new ListView<>();
        activeQuestsList.getItems().addAll(questManager.getIncompleteQuests());
        activeQuestsList.setPrefHeight(150);
        
        // Completed Quests
        Text completedQuestsText = new Text("Completed Quests");
        completedQuestsText.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 18));
        
        ListView<Quest> completedQuestsList = new ListView<>();
        completedQuestsList.getItems().addAll(questManager.getCompletedQuests());
        completedQuestsList.setPrefHeight(150);
        
        // Achievements
        Text achievementsText = new Text("Achievements");
        achievementsText.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 18));
        
        GridPane achievementGrid = new GridPane();
        achievementGrid.setHgap(10);
        achievementGrid.setVgap(10);
        
        int col = 0, row = 0;
        for (Achievement achievement : Achievement.values()) {
            VBox achievementBox = new VBox(5);
            achievementBox.setStyle("-fx-background-color: " + 
                                   (achievement.isUnlocked() ? "#E8F5E8" : "#F5F5F5") + 
                                   "; -fx-border-color: " + 
                                   (achievement.isUnlocked() ? "#4CAF50" : "#CCCCCC") + 
                                   "; -fx-padding: 10px; -fx-border-radius: 5px;");
            achievementBox.setPrefWidth(200);
            
            Label nameLabel = new Label(achievement.getName());
            nameLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 12));
            nameLabel.setWrapText(true);
            
            Label descLabel = new Label(achievement.getDescription());
            descLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, 10));
            descLabel.setWrapText(true);
            
            achievementBox.getChildren().addAll(nameLabel, descLabel);
            achievementGrid.add(achievementBox, col, row);
            
            col++;
            if (col > 2) {
                col = 0;
                row++;
            }
        }
        
        ScrollPane achievementScroll = new ScrollPane(achievementGrid);
        achievementScroll.setPrefHeight(200);
        achievementScroll.setFitToWidth(true);
        
        questsBox.getChildren().addAll(
            titleText, xpBox, activeQuestsText, activeQuestsList,
            completedQuestsText, completedQuestsList, achievementsText, achievementScroll
        );
        
        return questsBox;
    }
    
    private VBox createTimeTrackingTab(List<FocusSession> sessions) {
        VBox timeBox = new VBox(20);
        timeBox.setPadding(new Insets(20));
        
        Text titleText = new Text("Time Tracking");
        titleText.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 24));
        
        // Weekly time chart
        BarChart<String, Number> weeklyChart = createWeeklyTimeChart(sessions);
        
        timeBox.getChildren().addAll(titleText, weeklyChart);
        
        return timeBox;
    }
    
    // Helper methods for charts
    private PieChart createTaskCompletionChart(int completed, int remaining) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Completed", completed),
            new PieChart.Data("Remaining", remaining)
        );
        
        PieChart chart = new PieChart(pieChartData);
        chart.setPrefSize(300, 300);
        return chart;
    }
    
    private LineChart<String, Number> createDailyProductivityChart(List<FocusSession> sessions) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Day");
        yAxis.setLabel("Focus Time (minutes)");
        
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Daily Focus Time");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Focus Time");
        
        // Group sessions by day and calculate total time
        // This is a simplified version - you'd want to properly group by date
        for (int i = 0; i < Math.min(7, sessions.size()); i++) {
            FocusSession session = sessions.get(i);
            String day = "Day " + (i + 1);
            double minutes = session.getDurationSeconds() / 60.0;
            series.getData().add(new XYChart.Data<>(day, minutes));
        }
        
        lineChart.getData().add(series);
        lineChart.setPrefHeight(300);
        
        return lineChart;
    }
    
    private BarChart<String, Number> createTaskPriorityChart(List<Task> tasks) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Priority");
        yAxis.setLabel("Number of Tasks");
        
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Tasks by Priority");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tasks");
        
        // Count tasks by priority
        long urgentCount = tasks.stream().filter(t -> t.getPriority() == TaskPriority.URGENT).count();
        long highCount = tasks.stream().filter(t -> t.getPriority() == TaskPriority.HIGH).count();
        long mediumCount = tasks.stream().filter(t -> t.getPriority() == TaskPriority.MEDIUM).count();
        long lowCount = tasks.stream().filter(t -> t.getPriority() == TaskPriority.LOW).count();
        
        series.getData().addAll(
            new XYChart.Data<>("Urgent", urgentCount),
            new XYChart.Data<>("High", highCount),
            new XYChart.Data<>("Medium", mediumCount),
            new XYChart.Data<>("Low", lowCount)
        );
        
        barChart.getData().add(series);
        barChart.setPrefHeight(300);
        
        return barChart;
    }
    
    private BarChart<String, Number> createWeeklyTimeChart(List<FocusSession> sessions) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Day of Week");
        yAxis.setLabel("Focus Time (hours)");
        
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Weekly Focus Time");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Focus Time");
        
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String day : days) {
            // This is simplified - you'd calculate actual time per day
            double hours = Math.random() * 8; // Random data for demo
            series.getData().add(new XYChart.Data<>(day, hours));
        }
        
        barChart.getData().add(series);
        barChart.setPrefHeight(400);
        
        return barChart;
    }
    
    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        return String.format("%dh %dm", hours, minutes);
    }
    
    // Getters for integration with App class
    public QuestManager getQuestManager() {
        return questManager;
    }
    
    public AchievementManager getAchievementManager() {
        return achievementManager;
    }
    
    public XpManager getXpManager() {
        return xpManager;
    }
    
    public Analytics getAnalytics() {
        return analytics;
    }
}