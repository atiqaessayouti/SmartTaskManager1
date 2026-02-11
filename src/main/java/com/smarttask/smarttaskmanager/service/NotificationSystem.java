package com.smarttask.smarttaskmanager.service;

import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import com.smarttask.smarttaskmanager.util.UserSession;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.util.Duration;
import java.sql.*;
import java.util.Optional;

public class NotificationSystem {
    private String userEmail;

    public void startChecking() {
        this.userEmail = UserSession.getInstance().getEmail();

        // ⏱️ (Real-time polling)
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
            checkForNewInvitations();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void checkForNewInvitations() {

        String sql = "SELECT id, title, user_email FROM tasks WHERE shared_with = ? AND share_status = 'PENDING'";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int taskId = rs.getInt("id");
                String taskTitle = rs.getString("title");
                String sender = rs.getString("user_email");


                Platform.runLater(() -> showInviteAlert(taskId, taskTitle, sender));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showInviteAlert(int taskId, String title, String sender) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("New Task Invitation 📩");
        alert.setHeaderText("New Task Shared With You");
        alert.setContentText(sender + " wants to share the task: \"" + title + "\"\n\nDo you accept?");

        ButtonType btnAccept = new ButtonType("Accept");
        ButtonType btnDecline = new ButtonType("Decline");

        alert.getButtonTypes().setAll(btnAccept, btnDecline);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == btnAccept) {
                updateStatus(taskId, "ACCEPTED", "In Progress");
            } else {
                updateStatus(taskId, "DECLINED", "Cancelled");
            }
        }
    }

    private void updateStatus(int taskId, String shareStatus, String taskStatus) {
        String sql = "UPDATE tasks SET share_status = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, shareStatus);
            pstmt.setString(2, taskStatus);
            pstmt.setInt(3, taskId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}