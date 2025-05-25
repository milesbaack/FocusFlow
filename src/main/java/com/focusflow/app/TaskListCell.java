package com.focusflow.app;

import com.focusflow.core.task.Task;

import javafx.scene.control.ListCell;

/**
 * Custom ListCell for displaying Task objects in ComboBox with formatted
 * information.
 * Shows task name, priority, and due date in a readable format.
 */
public class TaskListCell extends ListCell<Task> {

    @Override
    protected void updateItem(Task task, boolean empty) {
        super.updateItem(task, empty);

        if (empty || task == null) {
            setText(null);
            setGraphic(null);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(task.getName());

            // Add priority
            sb.append(" [").append(task.getPriority().name()).append("]");

            // Add due date if available
            if (task.hasDueDateTime()) {
                sb.append(" - Due: ").append(task.getDueDateTime().toLocalDate().toString());
            }

            setText(sb.toString());

            // Style based on priority
            switch (task.getPriority()) {
                case URGENT:
                    setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    break;
                case HIGH:
                    setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    break;
                case MEDIUM:
                    setStyle("-fx-text-fill: #3498db;");
                    break;
                case LOW:
                    setStyle("-fx-text-fill: #95a5a6;");
                    break;
            }
        }
    }
}