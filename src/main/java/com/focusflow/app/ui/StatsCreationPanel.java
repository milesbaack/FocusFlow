package com.focusflow.app.ui;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.focusflow.core.analytics.Analytics;
import com.focusflow.core.gameify.Achievement;
import com.focusflow.core.gameify.AchievementManager;
import com.focusflow.core.gameify.Quest;
import com.focusflow.core.gameify.QuestManager;
import com.focusflow.core.gameify.XpManager;
import com.focusflow.core.session.FocusSession;
import com.focusflow.core.session.SessionManager;
import com.focusflow.core.task.Task;
import com.focusflow.core.task.TaskPriority;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Comprehensive Statistics Panel for FocusFlow analytics and insights.
 * Provides detailed views of productivity metrics, task completion rates,
 * focus session analytics, quest progress, and achievement tracking.
 * 
 * Features:
 * - Multiple analytics views (Overview, Tasks, Sessions, Quests, Achievements)
 * - Interactive charts and visualizations
 * - Time period filtering (Today, Week, Month, All Time)
 * - Productivity insights and recommendations
 * - Progress tracking and trend analysis
 * - Export capabilities for data analysis
 * 
 * @author FocusFlow Team
 * @version 2.0 - Comprehensive Analytics Suite
 */
public class StatsCreationPanel extends BasePanel {

    // Dependencies
    private final List<Task> tasks;
    private final SessionManager sessionManager;
    private final QuestManager questManager;
    private final AchievementManager achievementManager;
    private final XpManager xpManager;
    private final Analytics analytics;

    // UI Components
    private TabPane tabPane;
    private ComboBox<TimePeriod> timePeriodCombo;
    private Label lastUpdatedLabel;

    // Current filter state
    private TimePeriod currentPeriod = TimePeriod.ALL_TIME;

    /**
     * Creates a new StatsCreationPanel.
     */
    public StatsCreationPanel(OverlayManager overlayManager, Font pixelFont,
            List<Task> tasks, SessionManager sessionManager,
            QuestManager questManager, AchievementManager achievementManager,
            XpManager xpManager) {
        super(overlayManager, pixelFont, "Productivity Analytics", true);

        this.tasks = tasks;
        this.sessionManager = sessionManager;
        this.questManager = questManager;
        this.achievementManager = achievementManager;
        this.xpManager = xpManager;
        this.analytics = new Analytics();

        // Initialize analytics with current data
        initializeAnalytics();
        finishInitialization();
    }

    @Override
    protected void createContent() {
        // Header with time period filter
        HBox headerSection = createHeaderSection();

        // Main tabs for different analytics views
        tabPane = createAnalyticsTabs();

        // Footer with export options
        HBox footerSection = createFooterSection();

        addContent(headerSection, tabPane, footerSection);

        // Initial data load
        refreshAllData();
    }

    /**
     * Initializes analytics with existing session and task data.
     */
    private void initializeAnalytics() {
        // Track all completed sessions
        sessionManager.getSessionHistory().forEach(session -> {
            if (session.isCompleted()) {
                analytics.trackSession(session);
            }
        });

        // Track all completed tasks
        tasks.stream()
                .filter(Task::isComplete)
                .forEach(analytics::trackTaskCompletion);
    }

    /**
     * Creates the header section with filters and controls.
     */
    private HBox createHeaderSection() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 15, 0));

        // Time period filter
        Label periodLabel = new Label("Time Period:");
        periodLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));

        timePeriodCombo = new ComboBox<>();
        timePeriodCombo.getItems().addAll(TimePeriod.values());
        timePeriodCombo.setValue(TimePeriod.ALL_TIME);
        timePeriodCombo.setOnAction(e -> {
            currentPeriod = timePeriodCombo.getValue();
            refreshAllData();
        });

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Last updated info
        lastUpdatedLabel = new Label("Updated: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
        lastUpdatedLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.NORMAL, 12));
        lastUpdatedLabel.setTextFill(Color.web("#6C757D"));

        // Refresh button
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #17A2B8; -fx-text-fill: white; " +
                "-fx-background-radius: 6px; -fx-padding: 6 12;");
        refreshBtn.setOnAction(e -> refreshAllData());

        header.getChildren().addAll(periodLabel, timePeriodCombo, spacer, lastUpdatedLabel, refreshBtn);
        return header;
    }

    /**
     * Creates the main analytics tabs.
     */
    private TabPane createAnalyticsTabs() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setPrefHeight(400);

        // Overview tab
        Tab overviewTab = new Tab("📊 Overview");
        overviewTab.setContent(createOverviewContent());

        // Tasks tab
        Tab tasksTab = new Tab("📋 Tasks");
        tasksTab.setContent(createTasksContent());

        // Sessions tab
        Tab sessionsTab = new Tab("⏱️ Sessions");
        sessionsTab.setContent(createSessionsContent());

        // Quests tab
        Tab questsTab = new Tab("🏆 Quests");
        questsTab.setContent(createQuestsContent());

        // Achievements tab
        Tab achievementsTab = new Tab("🎖️ Achievements");
        achievementsTab.setContent(createAchievementsContent());

        tabs.getTabs().addAll(overviewTab, tasksTab, sessionsTab, questsTab, achievementsTab);
        return tabs;
    }

    /**
     * Creates the overview content with key metrics.
     */
    private ScrollPane createOverviewContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Key metrics cards
        HBox metricsRow1 = createMetricsRow1();
        HBox metricsRow2 = createMetricsRow2();

        // Productivity trend chart
        VBox trendSection = createProductivityTrendSection();

        // Recent achievements and milestones
        VBox milestonesSection = createMilestonesSection();

        content.getChildren().addAll(metricsRow1, metricsRow2, trendSection, milestonesSection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }

    /**
     * Creates the first row of key metrics.
     */
    private HBox createMetricsRow1() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER);

        // Tasks completed
        int completedTasks = getFilteredTasks().stream()
                .mapToInt(task -> task.isComplete() ? 1 : 0)
                .sum();
        VBox tasksCard = createMetricCard("Tasks Completed", String.valueOf(completedTasks),
                "#4CAF50", "📋");

        // Focus time
        Duration totalFocusTime = getFilteredSessions().stream()
                .map(session -> Duration.ofSeconds(session.getDurationSeconds()))
                .reduce(Duration.ZERO, Duration::plus);
        long hours = totalFocusTime.toHours();
        long minutes = totalFocusTime.toMinutesPart();
        VBox focusCard = createMetricCard("Focus Time", hours + "h " + minutes + "m",
                "#2196F3", "⏱️");

        // Current level
        VBox levelCard = createMetricCard("Current Level", String.valueOf(xpManager.getCurrentLevel()),
                "#FF9800", "⭐");

        // Completion rate
        int totalTasks = getFilteredTasks().size();
        double completionRate = totalTasks > 0 ? (completedTasks * 100.0) / totalTasks : 0;
        VBox rateCard = createMetricCard("Completion Rate", String.format("%.1f%%", completionRate),
                "#9C27B0", "📈");

        row.getChildren().addAll(tasksCard, focusCard, levelCard, rateCard);
        return row;
    }

    /**
     * Creates the second row of key metrics.
     */
    private HBox createMetricsRow2() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER);

        // Active quests
        int activeQuests = questManager.getIncompleteQuests().size();
        VBox questsCard = createMetricCard("Active Quests", String.valueOf(activeQuests),
                "#795548", "🏆");

        // Current XP
        VBox xpCard = createMetricCard("Experience Points", String.valueOf(xpManager.getCurrentXp()),
                "#607D8B", "💎");

        // Average session length
        double avgSessionMinutes = getFilteredSessions().stream()
                .mapToLong(FocusSession::getDurationSeconds)
                .average()
                .orElse(0) / 60.0;
        VBox avgCard = createMetricCard("Avg Session", String.format("%.1f min", avgSessionMinutes),
                "#00BCD4", "⏲️");

        // Achievements unlocked
        int unlockedAchievements = achievementManager.getUnlockedAchievements().size();
        VBox achievCard = createMetricCard("Achievements", String.valueOf(unlockedAchievements),
                "#E91E63", "🎖️");

        row.getChildren().addAll(questsCard, xpCard, avgCard, achievCard);
        return row;
    }

    /**
     * Creates a metric card with icon, value, and label.
     */
    private VBox createMetricCard(String title, String value, String color, String icon) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle(String.format("-fx-background-color: white; -fx-border-color: %s; " +
                "-fx-border-width: 2px; -fx-border-radius: 12px; " +
                "-fx-background-radius: 12px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);",
                color));
        card.setPrefWidth(140);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(24));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 20));
        valueLabel.setTextFill(Color.web(color));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.NORMAL, 12));
        titleLabel.setTextFill(Color.web("#666"));

        card.getChildren().addAll(iconLabel, valueLabel, titleLabel);
        return card;
    }

    /**
     * Creates the productivity trend section with line chart.
     */
    private VBox createProductivityTrendSection() {
        VBox section = createSection("Productivity Trend");

        // Create line chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Tasks Completed Over Time");
        chart.setPrefHeight(250);
        chart.setLegendVisible(false);

        // Generate trend data based on current time period
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tasks Completed");

        if (currentPeriod == TimePeriod.LAST_7_DAYS) {
            for (int i = 6; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                long tasksOnDate = tasks.stream()
                        .filter(Task::isComplete)
                        .filter(task -> task.getLastModifiedDateTime().toLocalDate().equals(date))
                        .count();
                series.getData()
                        .add(new XYChart.Data<>(date.format(DateTimeFormatter.ofPattern("MMM dd")), tasksOnDate));
            }
        } else if (currentPeriod == TimePeriod.LAST_30_DAYS) {
            for (int i = 29; i >= 0; i -= 5) {
                LocalDate date = LocalDate.now().minusDays(i);
                long tasksOnDate = tasks.stream()
                        .filter(Task::isComplete)
                        .filter(task -> {
                            LocalDate taskDate = task.getLastModifiedDateTime().toLocalDate();
                            return taskDate.isAfter(date.minusDays(5)) && taskDate.isBefore(date.plusDays(1));
                        })
                        .count();
                series.getData()
                        .add(new XYChart.Data<>(date.format(DateTimeFormatter.ofPattern("MMM dd")), tasksOnDate));
            }
        } else {
            // Default to weekly summary for ALL_TIME
            series.getData().add(new XYChart.Data<>("This Week", getFilteredTasks().stream()
                    .filter(Task::isComplete)
                    .filter(task -> task.getLastModifiedDateTime().isAfter(LocalDateTime.now().minusDays(7)))
                    .count()));
        }

        chart.getData().add(series);
        section.getChildren().add(chart);
        return section;
    }

    /**
     * Creates the milestones and achievements section.
     */
    private VBox createMilestonesSection() {
        VBox section = createSection("Recent Milestones");

        // Recent achievements
        List<Achievement> recentAchievements = achievementManager.getUnlockedAchievements().stream()
                .limit(3)
                .collect(Collectors.toList());

        if (recentAchievements.isEmpty()) {
            Label noAchievements = createHelpText(
                    "No achievements unlocked yet. Keep working to earn your first achievement!");
            section.getChildren().add(noAchievements);
        } else {
            VBox achievementsList = new VBox(10);
            for (Achievement achievement : recentAchievements) {
                HBox achievementRow = createAchievementRow(achievement);
                achievementsList.getChildren().add(achievementRow);
            }
            section.getChildren().add(achievementsList);
        }

        return section;
    }

    /**
     * Creates an achievement row display.
     */
    private HBox createAchievementRow(Achievement achievement) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8px;");

        Label medal = new Label("🏅");
        medal.setFont(Font.font(20));

        VBox info = new VBox(2);
        Label name = new Label(achievement.getName());
        name.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));

        Label description = new Label(achievement.getDescription());
        description.setFont(Font.font(getPixelFont().getFamily(), FontWeight.NORMAL, 12));
        description.setTextFill(Color.web("#666"));

        info.getChildren().addAll(name, description);

        row.getChildren().addAll(medal, info);
        return row;
    }

    /**
     * Creates the tasks analytics content.
     */
    private ScrollPane createTasksContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Task completion summary
        VBox summarySection = createTaskSummarySection();

        // Priority breakdown chart
        VBox prioritySection = createTaskPrioritySection();

        // Category breakdown
        VBox categorySection = createTaskCategorySection();

        content.getChildren().addAll(summarySection, prioritySection, categorySection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }

    /**
     * Creates task summary section.
     */
    private VBox createTaskSummarySection() {
        VBox section = createSection("Task Summary");

        List<Task> filteredTasks = getFilteredTasks();
        int total = filteredTasks.size();
        int completed = (int) filteredTasks.stream().filter(Task::isComplete).count();
        int active = total - completed;
        int overdue = (int) filteredTasks.stream()
                .filter(task -> task.hasDueDateTime() &&
                        task.getDueDateTime().isBefore(LocalDateTime.now()) &&
                        !task.isComplete())
                .count();

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);

        grid.add(createStatRow("Total Tasks:", String.valueOf(total)), 0, 0);
        grid.add(createStatRow("Completed:", String.valueOf(completed)), 0, 1);
        grid.add(createStatRow("Active:", String.valueOf(active)), 0, 2);
        grid.add(createStatRow("Overdue:", String.valueOf(overdue)), 0, 3);

        if (total > 0) {
            ProgressBar completionBar = new ProgressBar((double) completed / total);
            completionBar.setPrefWidth(200);
            completionBar.setStyle("-fx-accent: #4CAF50;");

            Label completionLabel = new Label(String.format("%.1f%% Complete", (completed * 100.0) / total));
            completionLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));

            VBox progressBox = new VBox(5);
            progressBox.getChildren().addAll(completionLabel, completionBar);
            grid.add(progressBox, 1, 1, 1, 2);
        }

        section.getChildren().add(grid);
        return section;
    }

    /**
     * Creates task priority breakdown section.
     */
    private VBox createTaskPrioritySection() {
        VBox section = createSection("Priority Breakdown");

        Map<TaskPriority, Long> priorityCounts = getFilteredTasks().stream()
                .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));

        PieChart priorityChart = new PieChart();
        priorityChart.setTitle("Tasks by Priority");
        priorityChart.setPrefHeight(250);

        priorityCounts.forEach((priority, count) -> {
            PieChart.Data slice = new PieChart.Data(priority.name() + " (" + count + ")", count);
            priorityChart.getData().add(slice);
        });

        section.getChildren().add(priorityChart);
        return section;
    }

    /**
     * Creates task category breakdown section.
     */
    private VBox createTaskCategorySection() {
        VBox section = createSection("Category Breakdown");

        Map<String, Long> categoryCounts = getFilteredTasks().stream()
                .collect(Collectors.groupingBy(
                        task -> task.getCategory() != null ? task.getCategory().getTaskCategory() : "Uncategorized",
                        Collectors.counting()));

        // Create bar chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Tasks by Category");
        chart.setPrefHeight(250);
        chart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        categoryCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));

        chart.getData().add(series);
        section.getChildren().add(chart);
        return section;
    }

    /**
     * Creates the sessions analytics content.
     */
    private ScrollPane createSessionsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Session summary
        VBox summarySection = createSessionSummarySection();

        // Session duration chart
        VBox durationSection = createSessionDurationSection();

        content.getChildren().addAll(summarySection, durationSection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }

    /**
     * Creates session summary section.
     */
    private VBox createSessionSummarySection() {
        VBox section = createSection("Focus Session Summary");

        List<FocusSession> filteredSessions = getFilteredSessions();
        int totalSessions = filteredSessions.size();

        Duration totalTime = filteredSessions.stream()
                .map(session -> Duration.ofSeconds(session.getDurationSeconds()))
                .reduce(Duration.ZERO, Duration::plus);

        double avgDuration = filteredSessions.stream()
                .mapToLong(FocusSession::getDurationSeconds)
                .average()
                .orElse(0) / 60.0;

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);

        grid.add(createStatRow("Total Sessions:", String.valueOf(totalSessions)), 0, 0);
        grid.add(createStatRow("Total Focus Time:", formatDuration(totalTime)), 0, 1);
        grid.add(createStatRow("Average Session:", String.format("%.1f minutes", avgDuration)), 0, 2);

        section.getChildren().add(grid);
        return section;
    }

    /**
     * Creates session duration analysis section.
     */
    private VBox createSessionDurationSection() {
        VBox section = createSection("Session Duration Analysis");

        // Group sessions by duration ranges
        List<FocusSession> sessions = getFilteredSessions();

        Map<String, Long> durationRanges = sessions.stream()
                .collect(Collectors.groupingBy(this::getDurationRange, Collectors.counting()));

        // Create bar chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Session Duration Distribution");
        chart.setPrefHeight(250);
        chart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        // Ensure consistent ordering
        String[] ranges = { "0-10 min", "10-20 min", "20-30 min", "30+ min" };
        for (String range : ranges) {
            long count = durationRanges.getOrDefault(range, 0L);
            series.getData().add(new XYChart.Data<>(range, count));
        }

        chart.getData().add(series);
        section.getChildren().add(chart);
        return section;
    }

    /**
     * Gets duration range category for a session.
     */
    private String getDurationRange(FocusSession session) {
        long minutes = session.getDurationSeconds() / 60;
        if (minutes < 10)
            return "0-10 min";
        if (minutes < 20)
            return "10-20 min";
        if (minutes < 30)
            return "20-30 min";
        return "30+ min";
    }

    /**
     * Creates the quests analytics content.
     */
    private ScrollPane createQuestsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Quest summary
        VBox summarySection = createQuestSummarySection();

        // Quest progress details
        VBox progressSection = createQuestProgressSection();

        content.getChildren().addAll(summarySection, progressSection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }

    /**
     * Creates quest summary section.
     */
    private VBox createQuestSummarySection() {
        VBox section = createSection("Quest Summary");

        int totalQuests = questManager.getAllQuests().size();
        int completedQuests = questManager.getCompletedQuests().size();
        int activeQuests = questManager.getIncompleteQuests().size();

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);

        grid.add(createStatRow("Total Quests:", String.valueOf(totalQuests)), 0, 0);
        grid.add(createStatRow("Completed:", String.valueOf(completedQuests)), 0, 1);
        grid.add(createStatRow("Active:", String.valueOf(activeQuests)), 0, 2);

        if (totalQuests > 0) {
            double completionRate = (completedQuests * 100.0) / totalQuests;
            grid.add(createStatRow("Completion Rate:", String.format("%.1f%%", completionRate)), 0, 3);
        }

        section.getChildren().add(grid);
        return section;
    }

    /**
     * Creates quest progress details section.
     */
    private VBox createQuestProgressSection() {
        VBox section = createSection("Active Quest Progress");

        List<Quest> activeQuests = questManager.getIncompleteQuests();

        if (activeQuests.isEmpty()) {
            Label noQuests = createHelpText("No active quests. Create a quest to start tracking progress!");
            section.getChildren().add(noQuests);
        } else {
            VBox questsList = new VBox(15);

            activeQuests.stream()
                    .sorted(Comparator.comparing(Quest::getProgressPercentage).reversed())
                    .limit(5)
                    .forEach(quest -> {
                        VBox questCard = createQuestProgressCard(quest);
                        questsList.getChildren().add(questCard);
                    });

            section.getChildren().add(questsList);
        }

        return section;
    }

    /**
     * Creates a quest progress card.
     */
    private VBox createQuestProgressCard(Quest quest) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #DEE2E6; " +
                "-fx-border-radius: 8px; -fx-background-radius: 8px;");

        // Quest title and progress percentage
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(quest.getTitle());
        titleLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label progressLabel = new Label(quest.getProgressPercentage() + "%");
        progressLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));
        progressLabel.setTextFill(Color.web("#007BFF"));

        header.getChildren().addAll(titleLabel, spacer, progressLabel);

        // Progress bar
        ProgressBar progressBar = new ProgressBar(quest.getProgressPercentage() / 100.0);
        progressBar.setPrefWidth(300);
        progressBar.setStyle("-fx-accent: #007BFF;");

        // Quest details
        HBox details = new HBox(20);

        Label tasksLabel = new Label("Tasks: " + quest.getTasks().size());
        tasksLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.NORMAL, 12));
        tasksLabel.setTextFill(Color.web("#666"));

        Label xpLabel = new Label("XP Reward: " + quest.calculateXpReward());
        xpLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.NORMAL, 12));
        xpLabel.setTextFill(Color.web("#666"));

        details.getChildren().addAll(tasksLabel, xpLabel);

        card.getChildren().addAll(header, progressBar, details);
        return card;
    }

    /**
     * Creates the achievements analytics content.
     */
    private ScrollPane createAchievementsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Achievement summary
        VBox summarySection = createAchievementSummarySection();

        // Achievement grid
        VBox gridSection = createAchievementGridSection();

        content.getChildren().addAll(summarySection, gridSection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }

    /**
     * Creates achievement summary section.
     */
    private VBox createAchievementSummarySection() {
        VBox section = createSection("Achievement Summary");

        int totalAchievements = Achievement.values().length;
        int unlockedAchievements = achievementManager.getUnlockedAchievements().size();
        int lockedAchievements = totalAchievements - unlockedAchievements;

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);

        grid.add(createStatRow("Total Achievements:", String.valueOf(totalAchievements)), 0, 0);
        grid.add(createStatRow("Unlocked:", String.valueOf(unlockedAchievements)), 0, 1);
        grid.add(createStatRow("Locked:", String.valueOf(lockedAchievements)), 0, 2);

        if (totalAchievements > 0) {
            double completionRate = (unlockedAchievements * 100.0) / totalAchievements;
            grid.add(createStatRow("Completion Rate:", String.format("%.1f%%", completionRate)), 0, 3);

            // Progress bar
            ProgressBar achievementBar = new ProgressBar((double) unlockedAchievements / totalAchievements);
            achievementBar.setPrefWidth(200);
            achievementBar.setStyle("-fx-accent: #E91E63;");

            VBox progressBox = new VBox(5);
            Label progressLabel = new Label("Achievement Progress");
            progressLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 12));
            progressBox.getChildren().addAll(progressLabel, achievementBar);

            grid.add(progressBox, 1, 1, 1, 2);
        }

        section.getChildren().add(grid);
        return section;
    }

    /**
     * Creates achievement grid section.
     */
    private VBox createAchievementGridSection() {
        VBox section = createSection("Achievement Gallery");

        GridPane achievementGrid = new GridPane();
        achievementGrid.setHgap(15);
        achievementGrid.setVgap(15);

        Achievement[] achievements = Achievement.values();
        int columns = 3;

        for (int i = 0; i < achievements.length; i++) {
            Achievement achievement = achievements[i];
            VBox achievementCard = createAchievementCard(achievement);

            int row = i / columns;
            int col = i % columns;
            achievementGrid.add(achievementCard, col, row);
        }

        section.getChildren().add(achievementGrid);
        return section;
    }

    /**
     * Creates an achievement card display.
     */
    private VBox createAchievementCard(Achievement achievement) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(180);
        card.setPrefHeight(120);

        boolean isUnlocked = achievementManager.isAchievementUnlocked(achievement);

        if (isUnlocked) {
            card.setStyle("-fx-background-color: #E8F5E8; -fx-border-color: #4CAF50; " +
                    "-fx-border-width: 2px; -fx-border-radius: 10px; -fx-background-radius: 10px;");
        } else {
            card.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #CCCCCC; " +
                    "-fx-border-width: 1px; -fx-border-radius: 10px; -fx-background-radius: 10px;");
        }

        // Achievement icon
        Label icon = new Label(isUnlocked ? "🏆" : "🔒");
        icon.setFont(Font.font(24));
        icon.setOpacity(isUnlocked ? 1.0 : 0.5);

        // Achievement name
        Label name = new Label(achievement.getName());
        name.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 12));
        name.setTextFill(isUnlocked ? Color.web("#2E7D32") : Color.web("#666"));
        name.setWrapText(true);
        name.setAlignment(Pos.CENTER);

        // Achievement description
        Label description = new Label(achievement.getDescription());
        description.setFont(Font.font(getPixelFont().getFamily(), FontWeight.NORMAL, 10));
        description.setTextFill(Color.web("#666"));
        description.setWrapText(true);
        description.setAlignment(Pos.CENTER);
        description.setMaxWidth(160);

        card.getChildren().addAll(icon, name, description);
        return card;
    }

    /**
     * Creates the footer section with export options.
     */
    private HBox createFooterSection() {
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(15, 0, 0, 0));

        Label exportLabel = new Label("Export Data:");
        exportLabel.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));

        Button exportCsvBtn = new Button("📊 Export CSV");
        exportCsvBtn.setStyle("-fx-background-color: #28A745; -fx-text-fill: white; " +
                "-fx-background-radius: 6px; -fx-padding: 8 15;");
        exportCsvBtn.setOnAction(e -> exportDataAsCsv());

        Button exportReportBtn = new Button("📄 Generate Report");
        exportReportBtn.setStyle("-fx-background-color: #17A2B8; -fx-text-fill: white; " +
                "-fx-background-radius: 6px; -fx-padding: 8 15;");
        exportReportBtn.setOnAction(e -> generateReport());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = createSecondaryButton("Close", () -> getOverlayManager().hideCurrentOverlay());

        footer.getChildren().addAll(exportLabel, exportCsvBtn, exportReportBtn, spacer, closeBtn);
        return footer;
    }

    /**
     * Creates a statistics row with label and value.
     */
    private HBox createStatRow(String label, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelNode = new Label(label);
        labelNode.setFont(Font.font(getPixelFont().getFamily(), FontWeight.NORMAL, 14));
        labelNode.setPrefWidth(150);

        Label valueNode = new Label(value);
        valueNode.setFont(Font.font(getPixelFont().getFamily(), FontWeight.BOLD, 14));
        valueNode.setTextFill(Color.web("#007BFF"));

        row.getChildren().addAll(labelNode, valueNode);
        return row;
    }

    /**
     * Gets filtered tasks based on current time period.
     */
    private List<Task> getFilteredTasks() {
        LocalDateTime cutoffDate = getCutoffDate();

        return tasks.stream()
                .filter(task -> cutoffDate == null ||
                        task.getCreationDateTime().isAfter(cutoffDate) ||
                        (task.isComplete() && task.getLastModifiedDateTime().isAfter(cutoffDate)))
                .collect(Collectors.toList());
    }

    /**
     * Gets filtered sessions based on current time period.
     */
    private List<FocusSession> getFilteredSessions() {
        LocalDateTime cutoffDate = getCutoffDate();

        return sessionManager.getSessionHistory().stream()
                .filter(session -> cutoffDate == null || session.getStartTime().isAfter(cutoffDate))
                .collect(Collectors.toList());
    }

    /**
     * Gets the cutoff date for filtering based on selected time period.
     */
    private LocalDateTime getCutoffDate() {
        switch (currentPeriod) {
            case TODAY:
                return LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
            case LAST_7_DAYS:
                return LocalDateTime.now().minusDays(7);
            case LAST_30_DAYS:
                return LocalDateTime.now().minusDays(30);
            case ALL_TIME:
            default:
                return null;
        }
    }

    /**
     * Refreshes all data and updates displays.
     */
    private void refreshAllData() {
        // Update last updated timestamp
        lastUpdatedLabel.setText("Updated: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));

        // Refresh the currently selected tab
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            switch (selectedTab.getText()) {
                case "📊 Overview":
                    selectedTab.setContent(createOverviewContent());
                    break;
                case "📋 Tasks":
                    selectedTab.setContent(createTasksContent());
                    break;
                case "⏱️ Sessions":
                    selectedTab.setContent(createSessionsContent());
                    break;
                case "🏆 Quests":
                    selectedTab.setContent(createQuestsContent());
                    break;
                case "🎖️ Achievements":
                    selectedTab.setContent(createAchievementsContent());
                    break;
            }
        }
    }

    /**
     * Exports statistics data as CSV.
     */
    private void exportDataAsCsv() {
        // This would implement CSV export functionality
        // For now, just show a placeholder message
        showAlert("Export CSV", "CSV export functionality would be implemented here.\n" +
                "This would generate a comprehensive CSV file with all productivity data.");
    }

    /**
     * Generates a comprehensive productivity report.
     */
    private void generateReport() {
        // This would implement report generation
        // For now, just show a placeholder message
        showAlert("Generate Report", "Report generation functionality would be implemented here.\n" +
                "This would create a detailed PDF report with charts and insights.");
    }

    /**
     * Shows an alert dialog.
     */
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Formats a duration into a readable string.
     */
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + " minutes";
        }
    }

    /**
     * Enum for time period filtering options.
     */
    public enum TimePeriod {
        TODAY("Today"),
        LAST_7_DAYS("Last 7 Days"),
        LAST_30_DAYS("Last 30 Days"),
        ALL_TIME("All Time");

        private final String displayName;

        TimePeriod(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}