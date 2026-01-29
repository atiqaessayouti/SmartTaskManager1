package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.service.AIService;
import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class AddTaskController {

    @FXML private TextField tfTitle;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<String> cbPriority;
    @FXML private DatePicker dpDeadline;

    @FXML
    public void handleAISuggestion() {
        String title = tfTitle.getText();
        if (title == null || title.isEmpty()) return;

        // Detection automatique
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
        LocalDate deadline = dpDeadline.getValue(); // Récupérer la date de l'AI

        if (title.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Title is required!");
            return;
        }

        // 4 "?" correspondants à : title, description, priority, deadline
        String sql = "INSERT INTO tasks (title, description, priority, status, deadline) VALUES (?, ?, ?, 'In Progress', ?)";

        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setString(1, title);
            prepare.setString(2, description);
            prepare.setString(3, priority != null ? priority : "Medium");

            // Fix pour le Deadline NULL
            if (deadline != null) {
                prepare.setDate(4, java.sql.Date.valueOf(deadline));
            } else {
                prepare.setNull(4, java.sql.Types.DATE);
            }

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