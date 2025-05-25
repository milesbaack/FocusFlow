module com.focusflow {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.media; // Add this line to fix the media package visibility

    exports com.focusflow.app;
    exports com.focusflow.core.task;
    exports com.focusflow.core.timer;
    exports com.focusflow.core.session;
    exports com.focusflow.core.analytics;
    exports com.focusflow.core.gameify;
    exports com.focusflow.core.notification;
}