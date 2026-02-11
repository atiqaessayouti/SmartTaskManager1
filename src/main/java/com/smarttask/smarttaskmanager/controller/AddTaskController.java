package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.model.Task;
import com.smarttask.smarttaskmanager.service.AIService;
import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import com.smarttask.smarttaskmanager.util.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDate;

public class AddTaskController {
    @FXML private TextField tfTitle;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<String> cbPriority;
    @FXML private DatePicker dpDeadline;
    @FXML private ComboBox<String> cbRecurrence;
    @FXML private ComboBox<Task> cbParentTask;
    @FXML private Button btnSave;
    @FXML private ComboBox<String> cbCategory;

    private boolean isEditMode = false;
    private int taskIdToEdit = -1;

    @FXML
    public void initialize() {
        loadParentTasks();

        // Populate Priority
        if (cbPriority.getItems().isEmpty()) {
            cbPriority.getItems().addAll("High", "Medium", "Low");
        }

        // Populate Category
        if (cbCategory != null && cbCategory.getItems().isEmpty()) {
            cbCategory.getItems().addAll("Work", "Education", "Health", "Finance", "Personal", "General");
        }

        // Populate Recurrence
        if (cbRecurrence != null && cbRecurrence.getItems().isEmpty()) {
            cbRecurrence.getItems().addAll("NONE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY");
        }

        // Listener AI Description (Auto-fill when focus is lost)
        taDescription.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !isEditMode) {
                fillTaskInfoFromDescription();
            }
        });
    }

    @FXML
    public void saveTask(ActionEvent event) {
        if (tfTitle.getText() == null || tfTitle.getText().trim().isEmpty()) {
            fillTaskInfoFromDescription();
        }

        String title = tfTitle.getText();
        String description = taDescription.getText();
        String priority = cbPriority.getValue();
        LocalDate deadline = dpDeadline.getValue();
        String recurrence = cbRecurrence.getValue() != null ? cbRecurrence.getValue() : "NONE";
        String category = (cbCategory != null && cbCategory.getValue() != null) ? cbCategory.getValue() : "General";

        // Parent Task ID
        Integer parentId = (cbParentTask.getValue() != null) ? cbParentTask.getValue().getId() : null;

        if (title == null || title.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Title is required!");
            alert.show();
            return;
        }

        String sql;
        if (isEditMode) {
            sql = "UPDATE tasks SET title=?, description=?, priority=?, deadline=?, recurrence_type=?, category=?, parent_id=? WHERE id=?";
        } else {
            sql = "INSERT INTO tasks (title, description, priority, status, deadline, recurrence_type, category, parent_id, user_email) VALUES (?, ?, ?, 'In Progress', ?, ?, ?, ?, ?)";
        }

        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setString(1, title);
            prepare.setString(2, description);
            prepare.setString(3, priority != null ? priority : "Medium");

            if (deadline != null) {
                prepare.setDate(4, java.sql.Date.valueOf(deadline));
            } else {
                prepare.setNull(4, java.sql.Types.DATE);
            }

            prepare.setString(5, recurrence);
            prepare.setString(6, category);

            // Parent ID (Handling Null)
            if (parentId != null) {
                prepare.setInt(7, parentId);
            } else {
                prepare.setNull(7, java.sql.Types.INTEGER);
            }

            if (isEditMode) {
                prepare.setInt(8, taskIdToEdit);
            } else {
                prepare.setString(8, UserSession.getInstance().getEmail());
            }

            prepare.executeUpdate();
            ((Stage) tfTitle.getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadParentTasks() {
        ObservableList<Task> tasks = FXCollections.observableArrayList();
        String sql = "SELECT * FROM tasks WHERE user_email = ? AND parent_id IS NULL"; // Only tasks that aren't subtasks

        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setString(1, UserSession.getInstance().getEmail());
            ResultSet rs = prepare.executeQuery();

            while (rs.next()) {
                // ✅ التصحيح: استدعاء الـ Constructor بـ 12 باراميتر لتجنب الـ Error
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
                        rs.getLong("time_spent"),      // الباراميتر 11
                        rs.getTimestamp("timer_start") // الباراميتر 12
                ));
            }
            cbParentTask.setItems(tasks);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillTaskInfoFromDescription() {
        String desc = taDescription.getText();
        if (desc == null || desc.isEmpty()) return;

        // AI Recurrence
        String rec = AIService.suggestRecurrence(desc);
        if (cbRecurrence != null && !"NONE".equals(rec)) cbRecurrence.setValue(rec);

        // AI Priority
        String prio = AIService.suggestPriority(desc);
        if (cbPriority != null) cbPriority.setValue(prio);

        // AI Category
        String cat = AIService.suggestCategory(desc);
        if (cbCategory != null) cbCategory.setValue(cat);

        // AI Date
        LocalDate date = AIService.parseDate(desc);
        if (date != null) dpDeadline.setValue(date);

        // AI Title (Clean)
        if (tfTitle.getText().isEmpty()) {
            tfTitle.setText(AIService.extractCleanTitle(desc));
        }
    }

    public void setTaskData(Task task) {
        this.isEditMode = true;
        this.taskIdToEdit = task.getId();
        tfTitle.setText(task.getTitle());
        taDescription.setText(task.getDescription());
        cbPriority.setValue(task.getPriority());
        dpDeadline.setValue(task.getDeadline());
        if (cbCategory != null) cbCategory.setValue(task.getCategory());
        if (cbRecurrence != null) cbRecurrence.setValue(task.getRecurrenceType());
        if (btnSave != null) btnSave.setText("Update");
    }

    @FXML
    public void handleAISuggestion() {
        fillTaskInfoFromDescription();
    }
}