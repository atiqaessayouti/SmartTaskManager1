package com.smarttask.smarttaskmanager.DAO;

import com.smarttask.smarttaskmanager.model.Task;
import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import com.smarttask.smarttaskmanager.util.UserSession;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    // ... (methodes l-qdam dyalk b7al addTask, updateStatus...)

    // 👇 ZIDI HAD L-METHODE HNA
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        // Jbed ghir tasks d l-user li m-connecter
        String email = UserSession.getInstance().getEmail();

        // Jbed ghir li mazal ma-tsalawch (Status != Completed)
        String sql = "SELECT * FROM tasks WHERE user_email = ? AND status != 'Completed'";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Sta3mli l-constructor li 3ndek f Task.java
                Task t = new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("priority"),
                        rs.getString("status"),
                        rs.getDate("deadline") != null ? rs.getDate("deadline").toLocalDate() : null,
                        rs.getString("category"),
                        rs.getString("shared_with")
                );
                tasks.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }
}