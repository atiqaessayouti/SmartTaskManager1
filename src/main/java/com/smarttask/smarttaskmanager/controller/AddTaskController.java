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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class AddTaskController {

    @FXML private TextField tfTitle;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<String> cbPriority;
    @FXML private DatePicker dpDeadline;

    // Nouveaux champs pour Sous-tâches et Récurrence
    @FXML private ComboBox<String> cbRecurrence;
    @FXML private ComboBox<Task> cbParentTask;

    @FXML
    public void initialize() {
        // Charger la liste des tâches existantes pour pouvoir créer des sous-tâches
        loadParentTasks();
    }

    private void loadParentTasks() {
        ObservableList<Task> tasks = FXCollections.observableArrayList();
        String sql = "SELECT id, title FROM tasks WHERE user_email = ?";
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {
            prepare.setString(1, UserSession.getInstance().getEmail());
            ResultSet rs = prepare.executeQuery();
            while (rs.next()) {
                tasks.add(new Task(rs.getInt("id"), rs.getString("title"), null, null, null, null, null, null));
            }
            cbParentTask.setItems(tasks);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAISuggestion() {
        String title = tfTitle.getText();
        if (title == null || title.isEmpty()) return;

        LocalDate suggestedDate = AIService.parseDate(title);
        if (suggestedDate != null) {
            dpDeadline.setValue(suggestedDate);
        }

        String suggestedPriority = AIService.suggestPriority(title);
        cbPriority.setValue(suggestedPriority);
    }

    @FXML
    public void saveTask(ActionEvent event) {
        String title = tfTitle.getText();
        String description = taDescription.getText();
        String priority = cbPriority.getValue();
        LocalDate deadline = dpDeadline.getValue();

        String recurrence = cbRecurrence.getValue() != null ? cbRecurrence.getValue() : "NONE";
        Task parentTask = cbParentTask.getValue();

        if (title.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Title is required!");
            return;
        }

        // SQL mis à jour avec parent_id et recurrence_type
        String sql = "INSERT INTO tasks (title, description, priority, status, deadline, user_email, parent_id, recurrence_type) VALUES (?, ?, ?, 'In Progress', ?, ?, ?, ?)";

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

            prepare.setString(5, UserSession.getInstance().getEmail());

            // Parent ID pour les sous-tâches
            if (parentTask != null) {
                prepare.setInt(6, parentTask.getId());
            } else {
                prepare.setNull(6, java.sql.Types.INTEGER);
            }

            // Type de récurrence
            prepare.setString(7, recurrence);

            if (prepare.executeUpdate() > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Task added successfully!");
                ((Stage) tfTitle.getScene().getWindow()).close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database error.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}