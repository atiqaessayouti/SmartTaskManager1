package com.smarttask.smarttaskmanager.controller;

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
    public void saveTask(ActionEvent event) {
        String title = tfTitle.getText();
        String description = taDescription.getText();
        String priority = cbPriority.getValue();
        LocalDate deadline = dpDeadline.getValue();

        // Vérification
        if (title.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Title is required!");
            return;
        }

        String sql = "INSERT INTO tasks (title, description, priority, status, deadline) VALUES (?, ?, ?, 'In Progress', ?)";

        try {
            Connection connect = DatabaseConnection.getInstance().getConnection();
            PreparedStatement prepare = connect.prepareStatement(sql);
            prepare.setString(1, title);
            prepare.setString(2, description);
            prepare.setString(3, priority);

            if (deadline != null) {
                prepare.setDate(4, java.sql.Date.valueOf(deadline));
            } else {
                prepare.setDate(4, null);
            }

            int result = prepare.executeUpdate();

            if (result > 0) {
                // ✅ ICI : On affiche le message de succès en anglais
                showAlert(Alert.AlertType.INFORMATION, "Success", "Task added successfully!");

                // Ensuite, on ferme la fenêtre
                Stage stage = (Stage) tfTitle.getScene().getWindow();
                stage.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database error occurred.");
        }
    }

    // ✅ J'ai modifié cette méthode pour accepter le TYPE d'alerte (Information ou Erreur)
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}