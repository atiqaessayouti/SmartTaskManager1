package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.model.Task;
import com.smarttask.smarttaskmanager.service.*;
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

    @FXML public void initialize() {
        loadParentTasks();
        if (cbPriority.getItems().isEmpty()) cbPriority.getItems().addAll("High", "Medium", "Low");

        // ✅ نعمروا الـ Category
        if (cbCategory != null && cbCategory.getItems().isEmpty())
            cbCategory.getItems().addAll("Work", "Education", "Health", "Finance", "Personal", "General");

        // ✅ نعمروا الـ Recurrence بـ MAJUSCULE
        if (cbRecurrence != null && cbRecurrence.getItems().isEmpty())
            cbRecurrence.getItems().addAll("NONE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY");

        // Listener AI Description
        taDescription.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !isEditMode) fillTaskInfoFromDescription();
        });
    }

    @FXML public void saveTask(ActionEvent event) {
        if (tfTitle.getText() == null || tfTitle.getText().trim().isEmpty()) fillTaskInfoFromDescription();

        String title = tfTitle.getText();
        String description = taDescription.getText();
        String priority = cbPriority.getValue();
        LocalDate deadline = dpDeadline.getValue();
        String recurrence = cbRecurrence.getValue() != null ? cbRecurrence.getValue() : "NONE";
        String category = (cbCategory != null && cbCategory.getValue() != null) ? cbCategory.getValue() : "General";

        if (title == null || title.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Title is required!");
            alert.show();
            return;
        }

        String sql = isEditMode ? "UPDATE tasks SET title=?, description=?, priority=?, deadline=?, recurrence_type=?, category=? WHERE id=?"
                : "INSERT INTO tasks (title, description, priority, status, deadline, recurrence_type, category, user_email) VALUES (?, ?, ?, 'In Progress', ?, ?, ?, ?)";
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {
            prepare.setString(1, title);
            prepare.setString(2, description);
            prepare.setString(3, priority != null ? priority : "Medium");
            if (deadline != null) prepare.setDate(4, java.sql.Date.valueOf(deadline)); else prepare.setNull(4, java.sql.Types.DATE);
            prepare.setString(5, recurrence);
            prepare.setString(6, category);
            if (isEditMode) prepare.setInt(7, taskIdToEdit); else prepare.setString(7, UserSession.getInstance().getEmail());

            prepare.executeUpdate();
            ((Stage) tfTitle.getScene().getWindow()).close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadParentTasks() {
        ObservableList<Task> tasks = FXCollections.observableArrayList();
        String sql = "SELECT id, title FROM tasks WHERE user_email = ?";
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {
            prepare.setString(1, UserSession.getInstance().getEmail());
            ResultSet rs = prepare.executeQuery();
            while (rs.next())
                tasks.add(new Task(rs.getInt("id"), rs.getString("title"), null, null, null, null, null, null, null));
            cbParentTask.setItems(tasks);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 🔥 AI Helpers (Auto-fill Logic)
    private void fillTaskInfoFromDescription() {
        String desc = taDescription.getText();
        if (desc == null || desc.isEmpty()) return;

        // 1. Recurrence
        String rec = AIService.suggestRecurrence(desc);
        if (cbRecurrence != null && !"NONE".equals(rec)) {
            cbRecurrence.setValue(rec);
        }

        // 2. Priority
        if (cbPriority != null && !"Medium".equals(AIService.suggestPriority(desc)))
            cbPriority.setValue(AIService.suggestPriority(desc));

        // 3. ✅ CATEGORY (هادي اللي كانت ناقصة!)
        if (cbCategory != null) {
            String cat = AIService.suggestCategory(desc);
            if (!"General".equals(cat)) {
                cbCategory.setValue(cat);
            }
        }

        // 4. Date
        LocalDate date = AIService.parseDate(desc);
        if (date != null) dpDeadline.setValue(date);

        // 5. Title
        if (tfTitle.getText().isEmpty())
            tfTitle.setText(AIService.extractCleanTitle(desc));
    }

    public void setTaskData(Task task) {
        this.isEditMode = true; this.taskIdToEdit = task.getId();
        tfTitle.setText(task.getTitle()); taDescription.setText(task.getDescription());
        cbPriority.setValue(task.getPriority()); dpDeadline.setValue(task.getDeadline());
        if(cbCategory!=null) cbCategory.setValue(task.getCategory());
        if(cbRecurrence!=null && task.getRecurrenceType() != null) cbRecurrence.setValue(task.getRecurrenceType());
        if(btnSave!=null) btnSave.setText("Update");
    }
    @FXML public void handleAISuggestion() {}
}