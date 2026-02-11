package com.smarttask.smarttaskmanager.DAO;

import com.smarttask.smarttaskmanager.model.Task;
import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import com.smarttask.smarttaskmanager.util.UserSession;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    // ✅ 1. جلب جميع المهام (يقرأ 12 عموداً بما فيها الوقت والعداد)
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        if (UserSession.getInstance() == null) return tasks;

        String email = UserSession.getInstance().getEmail();
        String sql = "SELECT * FROM tasks WHERE user_email = ? OR shared_with = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, email);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tasks.add(new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("priority"),
                        rs.getString("status"),
                        rs.getDate("deadline") != null ? rs.getDate("deadline").toLocalDate() : null,
                        rs.getString("category"),
                        rs.getString("shared_with"),
                        rs.getString("recurrence_type"),
                        rs.getObject("parent_id") != null ? rs.getInt("parent_id") : null,
                        rs.getLong("time_spent"),      // الحقل رقم 11
                        rs.getTimestamp("timer_start") // الحقل رقم 12
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return tasks;
    }

    // ✅ 2. إضافة مهمة جديدة
    public boolean addTask(Task task) {
        String sql = "INSERT INTO tasks (title, description, priority, deadline, category, user_email, recurrence_type, parent_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setString(3, task.getPriority());
            pstmt.setDate(4, task.getDeadline() != null ? Date.valueOf(task.getDeadline()) : null);
            pstmt.setString(5, task.getCategory());
            pstmt.setString(6, UserSession.getInstance().getEmail());
            pstmt.setString(7, task.getRecurrenceType());

            if (task.getParentId() != null) pstmt.setInt(8, task.getParentId());
            else pstmt.setNull(8, Types.INTEGER);

            pstmt.setString(9, "In Progress");
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ✅ 3. تشغيل العداد (Start)
    public void startTimer(int taskId) {
        String sql = "UPDATE tasks SET timer_start = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ✅ 4. إيقاف العداد وحساب الوقت (Stop) - حل مشكلة الـ 0
    public void stopTimer(int taskId) {
        // نستخدم COALESCE لضمان عدم ضياع الوقت القديم إذا كان NULL
        String sql = "UPDATE tasks SET " +
                "time_spent = COALESCE(time_spent, 0) + TIMESTAMPDIFF(SECOND, timer_start, NOW()), " +
                "timer_start = NULL " +
                "WHERE id = ? AND timer_start IS NOT NULL";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ✅ 5. حذف مهمة
    public boolean deleteTask(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}