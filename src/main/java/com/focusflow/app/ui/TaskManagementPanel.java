package com.focusflow.app.ui;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.focusflow.core.task.Task;
import com.focusflow.core.task.TaskPriority;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Task Management Panel with inline editing, filtering, and drag-drop support.
 * Provides seamless task management without popup interruptions.
 * 
 * @author FocusFlow Team
 * @version 2.0 - Seamless UX Update
 */
public class TaskManagementPanel extends BasePanel {
    private final List<Task> tasks;
    private final Consumer<Runnable> onTasksUpdated;

    private ListView<TaskItem> taskListView;
    private ObservableList<TaskItem> taskItems;
    private TextField quickAddField;
    private String currentFilter = "all";

    // Filter buttons for updating counts
    private ToggleButton allBtn, activeBtn, doneBtn;

    public TaskManagementPanel(OverlayManager overlayManager, Font pixelFont,
            List<Task> tasks, Consumer<Runnable> onTasksUpdated) {
        // Use delayed initialization constructor
        super(overlayManager, pixelFont, "Task Management", true);

        // Now we can safely initialize our fields
        this.tasks = tasks;
        this.onTasksUpdated = onTasksUpdated;
        this.taskItems = FXCollections.observableArrayList();

        // Finish the initialization now that our fields are set
        finishInitialization();
        refreshTaskList();
    }

    @Override
    protected void createContent() {
        // Filter tabs
        HBox filterTabs = createFilterTabs();

        // Task list with drag-drop support
        taskListView = new ListView<>();
        taskListView.setItems(taskItems);
        taskListView.setCellFactory(listView -> new EditableTaskCell());
        taskListView.setPrefHeight(350);
        taskListView.setStyle(
                "-fx-background-color: transparent; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");

        // Enable drag and drop
        enableDragAndDrop();

        // Quick add section
        HBox quickAddSection = createQuickAddSection();

        // Stats section
        HBox statsSection = createStatsSection();

        getChildren().addAll(filterTabs, taskListView, quickAddSection, statsSection);
    }

    private HBox createFilterTabs() {
        HBox filterBox = new HBox(5);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(0, 0, 15, 0));

        ToggleGroup filterGroup = new ToggleGroup();

        // Calculate counts
        int totalCount = tasks.size();
        int activeCount = (int) tasks.stream().filter(t -> !t.isComplete()).count();
        int doneCount = totalCount - activeCount;

        allBtn = new ToggleButton("All (" + totalCount + ")");
        activeBtn = new ToggleButton("Active (" + activeCount + ")");
        doneBtn = new ToggleButton("Done (" + doneCount + ")");

        String tabStyle = "-fx-background-radius: 15; -fx-padding: 8 15; -fx-font-weight: bold;";
        String activeTabStyle = tabStyle + "-fx-background-color: #2196F3; -fx-text-fill: white;";
        String inactiveTabStyle = tabStyle + "-fx-background-color: #E0E0E0; -fx-text-fill: #666;";

        allBtn.setToggleGroup(filterGroup);
        activeBtn.setToggleGroup(filterGroup);
        doneBtn.setToggleGroup(filterGroup);

        // Set initial state
        allBtn.setSelected(true);
        updateTabStyles(allBtn, activeBtn, doneBtn);

        // Filter handlers
        allBtn.setOnAction(e -> {
            if (allBtn.isSelected()) {
                applyFilter("all");
                updateTabStyles(allBtn, activeBtn, doneBtn);
            }
        });

        activeBtn.setOnAction(e -> {
            if (activeBtn.isSelected()) {
                applyFilter("active");
                updateTabStyles(activeBtn, allBtn, doneBtn);
            }
        });

        doneBtn.setOnAction(e -> {
            if (doneBtn.isSelected()) {
                applyFilter("done");
                updateTabStyles(doneBtn, allBtn, activeBtn);
            }
        });

        filterBox.getChildren().addAll(allBtn, activeBtn, doneBtn);
        return filterBox;
    }

    private void updateTabStyles(ToggleButton selected, ToggleButton... others) {
        String activeStyle = "-fx-background-radius: 15; -fx-padding: 8 15; -fx-font-weight: bold; -fx-background-color: #2196F3; -fx-text-fill: white;";
        String inactiveStyle = "-fx-background-radius: 15; -fx-padding: 8 15; -fx-font-weight: bold; -fx-background-color: #E0E0E0; -fx-text-fill: #666;";

        selected.setStyle(activeStyle);
        for (ToggleButton btn : others) {
            btn.setStyle(inactiveStyle);
        }
    }

    private HBox createQuickAddSection() {
        HBox quickAdd = new HBox(10);
        quickAdd.setAlignment(Pos.CENTER_LEFT);
        quickAdd.setPadding(new Insets(15, 15, 10, 15));
        quickAdd.setStyle(
                "-fx-background-color: #F8F9FA; -fx-background-radius: 12; -fx-border-color: #E9ECEF; -fx-border-radius: 12;");

        Label plusLabel = new Label("+");
        plusLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 20));
        plusLabel.setStyle("-fx-text-fill: #4CAF50;");

        quickAddField = new TextField();
        quickAddField.setPromptText("Quick add task... (Press Enter)");
        quickAddField
                .setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-font-size: 14px;");
        HBox.setHgrow(quickAddField, Priority.ALWAYS);

        quickAddField.setOnAction(e -> addQuickTask());
        quickAddField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                quickAddField.clear();
            }
        });

        // Priority selector for quick add
        ComboBox<TaskPriority> quickPriorityCombo = new ComboBox<>();
        quickPriorityCombo.getItems().addAll(TaskPriority.values());
        quickPriorityCombo.setValue(TaskPriority.MEDIUM);
        quickPriorityCombo.setStyle("-fx-background-color: transparent; -fx-font-size: 12px;");
        quickPriorityCombo.setPrefWidth(90);

        Button addBtn = new Button("Add");
        addBtn.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 5 12; -fx-font-weight: bold;");
        addBtn.setOnAction(e -> addQuickTask(quickPriorityCombo.getValue()));

        quickAdd.getChildren().addAll(plusLabel, quickAddField, quickPriorityCombo, addBtn);
        return quickAdd;
    }

    private HBox createStatsSection() {
        HBox stats = new HBox(20);
        stats.setAlignment(Pos.CENTER);
        stats.setPadding(new Insets(10, 0, 5, 0));

        int totalTasks = tasks.size();
        int completedTasks = (int) tasks.stream().filter(Task::isComplete).count();
        int overdueTasks = (int) tasks.stream()
                .filter(task -> task.hasDueDateTime() &&
                        task.getDueDateTime().isBefore(LocalDateTime.now()) &&
                        !task.isComplete())
                .count();

        double completionRate = totalTasks > 0 ? (completedTasks * 100.0) / totalTasks : 0;

        Label completionLabel = new Label(String.format("%.0f%% Complete", completionRate));
        completionLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 12));
        completionLabel.setTextFill(Color.web("#4CAF50"));

        if (overdueTasks > 0) {
            Label overdueLabel = new Label(overdueTasks + " Overdue");
            overdueLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 12));
            overdueLabel.setTextFill(Color.web("#F44336"));
            stats.getChildren().addAll(completionLabel, overdueLabel);
        } else {
            stats.getChildren().add(completionLabel);
        }

        return stats;
    }

    private void addQuickTask() {
        addQuickTask(TaskPriority.MEDIUM);
    }

    private void addQuickTask(TaskPriority priority) {
        String taskName = quickAddField.getText().trim();
        if (!taskName.isEmpty()) {
            Task newTask = new Task(taskName, "");
            newTask.setPriority(priority);
            tasks.add(newTask);
            refreshTaskList();
            updateFilterCounts();
            quickAddField.clear();
            onTasksUpdated.accept(() -> {
            });

            // Auto-scroll to new task if it's visible in current filter
            if (shouldShowTask(newTask)) {
                taskListView.scrollTo(taskItems.size() - 1);
            }
        }
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        taskItems.clear();
        List<Task> filteredTasks;

        switch (filter) {
            case "active":
                filteredTasks = tasks.stream().filter(t -> !t.isComplete()).collect(Collectors.toList());
                break;
            case "done":
                filteredTasks = tasks.stream().filter(Task::isComplete).collect(Collectors.toList());
                break;
            default:
                filteredTasks = tasks;
                break;
        }

        filteredTasks.forEach(task -> taskItems.add(new TaskItem(task)));
    }

    private boolean shouldShowTask(Task task) {
        switch (currentFilter) {
            case "active":
                return !task.isComplete();
            case "done":
                return task.isComplete();
            default:
                return true;
        }
    }

    private void refreshTaskList() {
        applyFilter(currentFilter);
    }

    private void updateFilterCounts() {
        int totalCount = tasks.size();
        int activeCount = (int) tasks.stream().filter(t -> !t.isComplete()).count();
        int doneCount = totalCount - activeCount;

        allBtn.setText("All (" + totalCount + ")");
        activeBtn.setText("Active (" + activeCount + ")");
        doneBtn.setText("Done (" + doneCount + ")");
    }

    private void enableDragAndDrop() {
        taskListView.setCellFactory(listView -> {
            EditableTaskCell cell = new EditableTaskCell();

            cell.setOnDragDetected(event -> {
                if (cell.getItem() != null) {
                    javafx.scene.input.Dragboard dragboard = cell.startDragAndDrop(TransferMode.MOVE);
                    javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                    content.putString(cell.getItem().getTask().getId().toString());
                    dragboard.setContent(content);
                    event.consume();
                }
            });

            cell.setOnDragOver(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            cell.setOnDragDropped(event -> {
                javafx.scene.input.Dragboard dragboard = event.getDragboard();
                boolean success = false;

                if (dragboard.hasString()) {
                    String draggedTaskId = dragboard.getString();
                    TaskItem draggedItem = taskItems.stream()
                            .filter(item -> item.getTask().getId().toString().equals(draggedTaskId))
                            .findFirst().orElse(null);

                    if (draggedItem != null && cell.getItem() != null) {
                        int draggedIndex = taskItems.indexOf(draggedItem);
                        int targetIndex = taskItems.indexOf(cell.getItem());

                        // Reorder in both lists
                        taskItems.remove(draggedIndex);
                        taskItems.add(targetIndex, draggedItem);

                        tasks.remove(draggedItem.getTask());
                        tasks.add(targetIndex, draggedItem.getTask());

                        success = true;
                        onTasksUpdated.accept(() -> {
                        });
                    }
                }

                event.setDropCompleted(success);
                event.consume();
            });

            return cell;
        });
    }

    // Inner class for task items
    public static class TaskItem {
        private final Task task;

        public TaskItem(Task task) {
            this.task = task;
        }

        public Task getTask() {
            return task;
        }

        @Override
        public String toString() {
            return task.getName();
        }
    }

    // Custom cell with inline editing capabilities
    private class EditableTaskCell extends ListCell<TaskItem> {
        private HBox container;
        private CheckBox completeBox;
        private Label nameLabel;
        private Label priorityLabel;
        private Label dueDateLabel;
        private TextField editField;
        private Button editBtn, deleteBtn;
        private boolean isEditing = false;

        @Override
        protected void updateItem(TaskItem item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
                return;
            }

            if (container == null) {
                createCell();
            }

            updateCellContent(item);
            setGraphic(container);
        }

        private void createCell() {
            container = new HBox(12);
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPadding(new Insets(12));
            container.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                    "-fx-border-color: #E9ECEF; -fx-border-radius: 10; -fx-border-width: 1px;");

            // Completion checkbox
            completeBox = new CheckBox();
            completeBox.setStyle("-fx-cursor: hand;");
            completeBox.setOnAction(e -> toggleTaskComplete());

            // Task info container
            VBox infoContainer = new VBox(4);

            nameLabel = new Label();
            nameLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, 14));
            nameLabel.setStyle("-fx-cursor: hand;");
            nameLabel.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    startEdit();
                }
            });

            // Edit field (hidden by default)
            editField = new TextField();
            editField.setVisible(false);
            editField.setManaged(false);
            editField.setStyle("-fx-font-size: 14px;");
            editField.setOnAction(e -> finishEdit(true));
            editField.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                    finishEdit(false);
                }
            });

            // Priority and due date labels
            HBox metaInfo = new HBox(15);
            priorityLabel = new Label();
            priorityLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 11));
            priorityLabel.setStyle("-fx-background-radius: 8; -fx-padding: 2 8;");

            dueDateLabel = new Label();
            dueDateLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, 11));
            dueDateLabel.setStyle("-fx-text-fill: #666;");

            metaInfo.getChildren().addAll(priorityLabel, dueDateLabel);

            infoContainer.getChildren().addAll(nameLabel, editField, metaInfo);
            HBox.setHgrow(infoContainer, Priority.ALWAYS);

            // Action buttons
            HBox actionButtons = new HBox(5);
            actionButtons.setAlignment(Pos.CENTER_RIGHT);

            editBtn = new Button("✎");
            editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #757575; " +
                    "-fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 4;");
            editBtn.setOnAction(e -> startEdit());

            deleteBtn = new Button("🗑");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #F44336; " +
                    "-fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 4;");
            deleteBtn.setOnAction(e -> deleteTask());

            actionButtons.getChildren().addAll(editBtn, deleteBtn);

            container.getChildren().addAll(completeBox, infoContainer, actionButtons);

            // Context menu
            createContextMenu();
        }

        private void createContextMenu() {
            ContextMenu contextMenu = new ContextMenu();

            MenuItem editItem = new MenuItem("Edit");
            editItem.setOnAction(e -> startEdit());

            MenuItem duplicateItem = new MenuItem("Duplicate");
            duplicateItem.setOnAction(e -> duplicateTask());

            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(e -> deleteTask());

            contextMenu.getItems().addAll(editItem, duplicateItem, deleteItem);

            container.setOnContextMenuRequested(e -> {
                contextMenu.show(container, e.getScreenX(), e.getScreenY());
            });
        }

        private void updateCellContent(TaskItem item) {
            Task task = item.getTask();

            completeBox.setSelected(task.isComplete());
            nameLabel.setText(task.getName());

            // Priority styling
            TaskPriority priority = task.getPriority();
            priorityLabel.setText(priority.name());
            String priorityColor = getPriorityColor(priority);
            priorityLabel.setStyle("-fx-background-color: " + priorityColor + "; -fx-text-fill: white; " +
                    "-fx-background-radius: 8; -fx-padding: 2 8; -fx-font-weight: bold; -fx-font-size: 11px;");

            // Due date
            if (task.hasDueDateTime()) {
                LocalDateTime dueDate = task.getDueDateTime();
                String dueDateText = "Due: " + dueDate.toLocalDate().toString();
                dueDateLabel.setText(dueDateText);

                // Color code based on urgency
                if (dueDate.isBefore(LocalDateTime.now()) && !task.isComplete()) {
                    dueDateLabel.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;"); // Overdue
                    container.setStyle(container.getStyle() + "-fx-border-color: #F44336; -fx-border-width: 2px;");
                } else if (dueDate.isBefore(LocalDateTime.now().plusDays(1))) {
                    dueDateLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;"); // Due soon
                } else {
                    dueDateLabel.setStyle("-fx-text-fill: #666;");
                }
            } else {
                dueDateLabel.setText("");
            }

            // Completion styling
            if (task.isComplete()) {
                nameLabel.setStyle("-fx-text-fill: #757575; -fx-strikethrough: true;");
                container.setOpacity(0.7);
            } else {
                nameLabel.setStyle("-fx-text-fill: black;");
                container.setOpacity(1.0);
            }

            // Hover effects
            container.setOnMouseEntered(e -> {
                if (!task.isComplete()) {
                    container.setStyle(
                            container.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
                }
            });

            container.setOnMouseExited(e -> {
                container.setStyle(container.getStyle()
                        .replace("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);", ""));
            });
        }

        private String getPriorityColor(TaskPriority priority) {
            switch (priority) {
                case URGENT:
                    return "#F44336";
                case HIGH:
                    return "#FF9800";
                case MEDIUM:
                    return "#2196F3";
                case LOW:
                    return "#4CAF50";
                default:
                    return "#757575";
            }
        }

        private void toggleTaskComplete() {
            TaskItem item = getItem();
            if (item != null) {
                Task task = item.getTask();
                if (task.isComplete()) {
                    task.markAsIncomplete();
                } else {
                    task.markAsCompleted();
                }
                updateCellContent(item);
                updateFilterCounts();
                onTasksUpdated.accept(() -> {
                });
            }
        }

        public void startEdit() {
            if (!isEditing) {
                isEditing = true;
                nameLabel.setVisible(false);
                nameLabel.setManaged(false);
                editField.setVisible(true);
                editField.setManaged(true);
                editField.setText(nameLabel.getText());
                editField.requestFocus();
                editField.selectAll();
            }
        }

        private void finishEdit(boolean save) {
            if (isEditing) {
                isEditing = false;

                if (save && !editField.getText().trim().isEmpty()) {
                    TaskItem item = getItem();
                    if (item != null) {
                        item.getTask().setName(editField.getText().trim());
                        nameLabel.setText(editField.getText().trim());
                        onTasksUpdated.accept(() -> {
                        });
                    }
                }

                editField.setVisible(false);
                editField.setManaged(false);
                nameLabel.setVisible(true);
                nameLabel.setManaged(true);
            }
        }

        private void duplicateTask() {
            TaskItem item = getItem();
            if (item != null) {
                Task originalTask = item.getTask();
                Task duplicateTask = new Task(
                        originalTask.getName() + " (Copy)",
                        originalTask.getDescription());
                duplicateTask.setPriority(originalTask.getPriority());
                duplicateTask.setCategory(originalTask.getCategory());

                if (originalTask.hasDueDateTime()) {
                    duplicateTask.setDueDateTime(originalTask.getDueDateTime().plusDays(1));
                }

                tasks.add(duplicateTask);
                refreshTaskList();
                updateFilterCounts();
                onTasksUpdated.accept(() -> {
                });
            }
        }

        private void deleteTask() {
            TaskItem item = getItem();
            if (item != null) {
                tasks.remove(item.getTask());
                taskItems.remove(item);
                updateFilterCounts();
                onTasksUpdated.accept(() -> {
                });
            }
        }
    }
}