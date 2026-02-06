package com.smarttask.smarttaskmanager.service;

import com.smarttask.smarttaskmanager.model.Task;
import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import com.smarttask.smarttaskmanager.util.UserSession;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class RecurrenceService {

    public static void checkAndCreateNextTask(Task completedTask) {
        String recurrence = completedTask.getRecurrenceType();
        if (recurrence == null || recurrence.equalsIgnoreCase("NONE") || recurrence.isEmpty()) return;

        LocalDate oldDeadline = completedTask.getDeadline();
        if (oldDeadline == null) oldDeadline = LocalDate.now();

        LocalDate newDeadline = null;
        switch (recurrence.toUpperCase()) {
            case "DAILY": newDeadline = oldDeadline.plusDays(1); break;
            case "WEEKLY": newDeadline = oldDeadline.plusWeeks(1); break;
            case "MONTHLY": newDeadline = oldDeadline.plusMonths(1); break;
            default: return;
        }

        // Insert Copy
        String sql = "INSERT INTO tasks (title, description, priority, status, deadline, recurrence_type, category, user_email) VALUES (?, ?, ?, 'In Progress', ?, ?, ?, ?)";
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {
            prepare.setString(1, completedTask.getTitle());
            prepare.setString(2, completedTask.getDescription());
            prepare.setString(3, completedTask.getPriority());
            prepare.setDate(4, java.sql.Date.valueOf(newDeadline));
            prepare.setString(5, recurrence);
            prepare.setString(6, completedTask.getCategory());
            prepare.setString(7, UserSession.getInstance().getEmail());
            prepare.executeUpdate();
            System.out.println("✅ Recurrence active: New task for " + newDeadline);
        } catch (Exception e) { e.printStackTrace(); }
    }
}