package com.smarttask.smarttaskmanager.service;

import com.smarttask.smarttaskmanager.controller.DashboardController;
import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import com.smarttask.smarttaskmanager.util.UserSession;
import javafx.application.Platform;
import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotificationService {
    private final DashboardController dashboardController;
    private ScheduledExecutorService scheduler;

    public NotificationService(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public void startService() {
        stopService();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        // Vérifier chaque 10 secondes
        scheduler.scheduleAtFixedRate(this::checkAllNotifications, 0, 10, TimeUnit.SECONDS);
    }

    private void checkAllNotifications() {
        if (UserSession.getInstance() == null) return;
        String email = UserSession.getInstance().getEmail();

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            // 1. Check INVITATIONS (Tâches partagées)
            checkInvitations(conn, email);


            checkDeadlines(conn, email);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void checkInvitations(Connection conn, String email) throws SQLException {
        String sql = "SELECT id, title FROM tasks WHERE shared_with = ? AND share_status = 'PENDING' AND reminder_sent = 0";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int taskId = rs.getInt("id");
                String title = rs.getString("title");

                Platform.runLater(() ->
                        dashboardController.addNotificationToQueue(taskId, "INVITE", "📩 Invitation: " + title, false)
                );

                markAsNotified(conn, taskId); // Bach may-t-3awdch
            }
        }
    }


    private void checkDeadlines(Connection conn, String email) throws SQLException {
        // Kan-qlbou 3la Tasks li dyali (Owner) ola M-partagyin m3aya (Accepted)
        // W li Deadline dyalhom L-YUM ola GHDA
        String sql = "SELECT id, title, deadline FROM tasks " +
                "WHERE (user_email = ? OR (shared_with = ? AND share_status = 'ACCEPTED')) " +
                "AND status != 'Completed' " +
                "AND reminder_sent = 0 " +
                "AND (deadline = CURDATE() OR deadline = DATE_ADD(CURDATE(), INTERVAL 1 DAY))";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, email);
            pst.setString(2, email);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int taskId = rs.getInt("id");
                String title = rs.getString("title");
                Date deadline = rs.getDate("deadline");

                // Message d'urgence
                String msg = "⏳ URGENT: '" + title + "' expire le " + deadline + " !";

                Platform.runLater(() ->
                        dashboardController.addNotificationToQueue(taskId, "DEADLINE", msg, true)
                );

                markAsNotified(conn, taskId); // Bach may-t-3awdch
            }
        }
    }

    // --- Helper pour marquer comme lu ---
    private void markAsNotified(Connection conn, int taskId) throws SQLException {
        String updateSql = "UPDATE tasks SET reminder_sent = 1 WHERE id = ?";
        try (PreparedStatement pstUpdate = conn.prepareStatement(updateSql)) {
            pstUpdate.setInt(1, taskId);
            pstUpdate.executeUpdate();
        }
    }

    public void stopService() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}