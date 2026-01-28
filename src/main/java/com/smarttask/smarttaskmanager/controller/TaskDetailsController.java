
package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.model.Comment;
import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import com.smarttask.smarttaskmanager.util.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TaskDetailsController {

    @FXML private Label lblTitle;
    @FXML private Label lblDesc;
    @FXML private Label lblStatus;
    @FXML private TextField txtShareEmail;
    @FXML private ListView<String> listComments;
    @FXML private TextArea txtComment;

    private int taskId;

    // Methode bach n3mmru les données mn Calendar/Dashboard
    public void setTaskData(int id, String title, String desc, String status, String sharedWith) {
        this.taskId = id;
        lblTitle.setText(title);
        lblDesc.setText(desc);
        lblStatus.setText("Status: " + status);
        if(sharedWith != null) txtShareEmail.setText(sharedWith);

        loadComments(); // Charger les anciens messages
    }

    // --- 1. CHARGER LES COMMENTAIRES ---
    private void loadComments() {
        ObservableList<String> items = FXCollections.observableArrayList();
        String sql = "SELECT user_email, content FROM comments WHERE task_id = ? ORDER BY created_at ASC";

        try {
            Connection connect = DatabaseConnection.getInstance().getConnection();
            PreparedStatement prep = connect.prepareStatement(sql);
            prep.setInt(1, taskId);
            ResultSet rs = prep.executeQuery();

            while(rs.next()) {
                String user = rs.getString("user_email");
                String msg = rs.getString("content");
                // Format simple: "user: message"
                items.add(user + ": \n" + msg);
            }
            listComments.setItems(items);

        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- 2. AJOUTER UN COMMENTAIRE ---
    @FXML
    public void handleAddComment(ActionEvent event) {
        String content = txtComment.getText();
        if (content.isEmpty()) return;

        String currentUser = UserSession.getInstance().getEmail();
        String sql = "INSERT INTO comments (task_id, user_email, content) VALUES (?, ?, ?)";

        try {
            Connection connect = DatabaseConnection.getInstance().getConnection();
            PreparedStatement prep = connect.prepareStatement(sql);
            prep.setInt(1, taskId);
            prep.setString(2, currentUser);
            prep.setString(3, content);

            prep.executeUpdate();

            txtComment.clear();
            loadComments(); // Refresh Chat

        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- 3. PARTAGER LA TACHE (SHARE) ---
    @FXML
    public void handleShare(ActionEvent event) {
        String emailToShare = txtShareEmail.getText();
        if(emailToShare.isEmpty()) return;

        String sql = "UPDATE tasks SET shared_with = ? WHERE id = ?";
        try {
            Connection connect = DatabaseConnection.getInstance().getConnection();
            PreparedStatement prep = connect.prepareStatement(sql);
            prep.setString(1, emailToShare);
            prep.setInt(2, taskId);

            int result = prep.executeUpdate();
            if(result > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Shared");
                alert.setContentText("Task shared with " + emailToShare);
                alert.showAndWait();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}