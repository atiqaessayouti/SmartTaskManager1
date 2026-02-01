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
    @FXML private ComboBox<String> cbRecurrence;
    @FXML private ComboBox<Task> cbParentTask;
    @FXML private Button btnSave; // ✅ Zid fx:id="btnSave" f SceneBuilder

    // 👇 VARIABLES JDAD BACH N-GÉREW L-EDIT
    private boolean isEditMode = false;
    private int taskIdToEdit = -1;

    @FXML
    public void initialize() {
        loadParentTasks();
        // Initialiser Priority s'il est vide
        if(cbPriority.getItems().isEmpty()) {
            cbPriority.getItems().addAll("High", "Medium", "Low");
        }
    }

    // 👇 HADI METHODE MOHIMMA: Hiya li kat-stqbl data mn TasksController
    public void setTaskData(Task task) {
        this.isEditMode = true;
        this.taskIdToEdit = task.getId();

        // Remplir les champs
        tfTitle.setText(task.getTitle());
        taDescription.setText(task.getDescription());
        cbPriority.setValue(task.getPriority());
        dpDeadline.setValue(task.getDeadline());

        // Changement dyal titre l-bouton (Optionnel)
        if(btnSave != null) btnSave.setText("Mettre à jour");
    }

    private void loadParentTasks() {
        // (Khlli l-code dyalk hna kif ma kan)
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
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void handleAISuggestion() {
        // (Khlli l-code dyalk hna kif ma kan)
        String title = tfTitle.getText();
        if (title == null || title.isEmpty()) return;
        LocalDate suggestedDate = AIService.parseDate(title);
        if (suggestedDate != null) dpDeadline.setValue(suggestedDate);
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

        // Validation simple
        if (title.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Title is required!");
            return;
        }

        String sql;

        // 👇 HNA L-FERQ: UPDATE OLA INSERT
        if (isEditMode) {
            sql = "UPDATE tasks SET title=?, description=?, priority=?, deadline=?, recurrence_type=? WHERE id=?";
        } else {
            sql = "INSERT INTO tasks (title, description, priority, status, deadline, recurrence_type, user_email) VALUES (?, ?, ?, 'In Progress', ?, ?, ?)";
        }

        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setString(1, title);
            prepare.setString(2, description);
            prepare.setString(3, priority != null ? priority : "Medium");

            if (deadline != null) prepare.setDate(4, java.sql.Date.valueOf(deadline));
            else prepare.setNull(4, java.sql.Types.DATE);

            prepare.setString(5, recurrence);

            if (isEditMode) {
                // Parametre d l-Update (ID)
                prepare.setInt(6, taskIdToEdit);
            } else {
                // Parametre d l-Insert (Email)
                prepare.setString(6, UserSession.getInstance().getEmail());
            }

            if (prepare.executeUpdate() > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", isEditMode ? "Task updated!" : "Task added!");
                // Fermer la fenêtre
                ((Stage) tfTitle.getScene().getWindow()).close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database error: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}