package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.model.Task;
import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import com.smarttask.smarttaskmanager.util.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TaskDetailsController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField shareField;
    @FXML private ListView<String> commentsList;
    @FXML private TextField commentInput;
    @FXML private Label lblSelectedFile; // <-- Label jdid

    private Task currentTask;
    private File selectedFile; // <-- Variable bach nkhbbiw l'fichier

    // Initialisation
    public void setTaskData(Task task) {
        this.currentTask = task;
        if (titleField != null) titleField.setText(task.getTitle());
        if (descriptionArea != null) descriptionArea.setText(task.getDescription());
        if (shareField != null) shareField.setText(task.getSharedWith());

        loadComments(); // Jib les commentaires
    }

    // --- 1. GESTION DES FICHIERS (ATTACHMENTS) ---

    @FXML
    public void handleAttachFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier à joindre");

        // Filtre (Optionnel: Tqdri tkhllih yjib kulchi)
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf")
        );

        // Jib l'fenêtre l7aliya bach t7ll foqha
        Stage stage = (Stage) commentInput.getScene().getWindow();
        this.selectedFile = fileChooser.showOpenDialog(stage);

        if (this.selectedFile != null) {
            lblSelectedFile.setText("Fichier sélectionné: " + this.selectedFile.getName());
        }
    }

    // --- 2. ENVOYER COMMENTAIRE + FICHIER ---

    @FXML
    public void handleAddComment() {
        String content = commentInput.getText();

        // Ila makan la texte la fichier, ma ndiro walo
        if (content.isEmpty() && selectedFile == null) return;

        String currentUser = UserSession.getInstance().getEmail();

        // Requete jdida fiha attachment_path
        String sql = "INSERT INTO comments (task_id, user_email, content, attachment_path) VALUES (?, ?, ?, ?)";

        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setInt(1, currentTask.getId());
            prepare.setString(2, currentUser);
            prepare.setString(3, content);

            // Ila kan fichier, n7tto l'path dyalo, sinon Null
            if (selectedFile != null) {
                prepare.setString(4, selectedFile.getAbsolutePath());
            } else {
                prepare.setString(4, null);
            }

            prepare.executeUpdate();

            // Reset (Nddfo l'blassa)
            commentInput.clear();
            selectedFile = null;
            lblSelectedFile.setText("");

            loadComments(); // Refresh

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- 3. CHARGEMENT DES COMMENTAIRES ---

    private void loadComments() {
        ObservableList<String> comments = FXCollections.observableArrayList();
        // Nzidou attachment_path f Select
        String sql = "SELECT user_email, content, created_at, attachment_path FROM comments WHERE task_id = ? ORDER BY created_at ASC";

        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setInt(1, currentTask.getId());
            ResultSet result = prepare.executeQuery();

            while (result.next()) {
                String user = result.getString("user_email");
                String content = result.getString("content");
                String path = result.getString("attachment_path");

                // Formatage: "User: Message"
                String message = user + ": " + content;

                // Ila kan fichier, nzidou icone 📎
                if (path != null && !path.isEmpty()) {
                    File f = new File(path);
                    message += "  📎 [Fichier: " + f.getName() + "]";
                }

                comments.add(message);
            }
            commentsList.setItems(comments);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- 4. PARTAGE DE TÂCHE ---

    @FXML
    public void handleShare() {
        String emailToShare = shareField.getText();
        if (emailToShare.isEmpty()) return;

        String sql = "UPDATE tasks SET shared_with = ? WHERE id = ?";
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setString(1, emailToShare);
            prepare.setInt(2, currentTask.getId());
            prepare.executeUpdate();

            showAlert("Succès", "Tâche partagée avec " + emailToShare);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }
}