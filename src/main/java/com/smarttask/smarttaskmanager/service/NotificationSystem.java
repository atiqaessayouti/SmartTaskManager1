package com.smarttask.smarttaskmanager.service;

import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import java.sql.*;

public class NotificationSystem {
    private int lastTaskCount = -1;

    public void startChecking(int userId) {
        // Polling: kat-checki la base de données kol 10 swani
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
            checkNewTasks(userId);
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void checkNewTasks(int userId) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE assigned_to = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int currentCount = rs.getInt(1);
                if (lastTaskCount != -1 && currentCount > lastTaskCount) {
                    showNotification("New Task!", "Someone shared a new task with you.");
                }
                lastTaskCount = currentCount;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
