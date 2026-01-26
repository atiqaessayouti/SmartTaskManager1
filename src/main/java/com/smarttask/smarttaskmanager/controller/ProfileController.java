package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import com.smarttask.smarttaskmanager.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.layout.VBox; // Import Mohim
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class ProfileController {

    // --- FXML FIELDS ---
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextArea bioField;
    @FXML private Circle profileImage;

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    // HEADER & STATS
    @FXML private Label lblHeaderName;
    @FXML private Label lblHeaderRole;
    @FXML private Label lblStatDone;
    @FXML private Label lblStatPending;
    @FXML private Label lblStatScore;

    // --- NOUVEAU : POUR LE CHANGEMENT DE VUE (Info vs Security) ---
    @FXML private VBox infoBox;       // La boite Personal Info
    @FXML private VBox securityBox;   // La boite Security

    @FXML private Button btnTabInfo;      // Bouton Info
    @FXML private Button btnTabSecurity;  // Bouton Security

    private String userEmail;

    @FXML
    public void initialize() {
        // Par défaut, nbaynu Info w nkhbiw Security
        showInfoForm();

        UserSession session = UserSession.getInstance();
        if (session != null) {
            this.userEmail = session.getEmail();
            emailField.setText(userEmail);
            loadUserDetails();
            loadUserStats();
        }
    }

    // --- METHODES POUR CHANGER LA VUE (Boutons du haut) ---
    @FXML
    public void switchForm(ActionEvent event) {
        if (event.getSource() == btnTabInfo) {
            showInfoForm();
        } else if (event.getSource() == btnTabSecurity) {
            showSecurityForm();
        }
    }

    private void showInfoForm() {
        infoBox.setVisible(true);
        securityBox.setVisible(false);

        // Style Active (Mfoncé)
        btnTabInfo.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;");
        // Style Inactive (Transparent)
        btnTabSecurity.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    private void showSecurityForm() {
        infoBox.setVisible(false);
        securityBox.setVisible(true);

        // Style Active
        btnTabSecurity.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;");
        // Style Inactive
        btnTabInfo.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    // ... (LES AUTRES METHODES RESTENT LES MEMES : loadUserStats, loadUserDetails, saveInfo, etc.) ...

    private void loadUserDetails() {
        String sql = "SELECT username, bio, image_path FROM users WHERE email = ?";
        Connection connect = DatabaseConnection.getInstance().getConnection();
        try {
            PreparedStatement prepare = connect.prepareStatement(sql);
            prepare.setString(1, userEmail);
            ResultSet rs = prepare.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                String bio = rs.getString("bio");
                String imagePath = rs.getString("image_path");
                usernameField.setText(username);
                bioField.setText(bio);
                lblHeaderName.setText(username);
                lblHeaderRole.setText(bio != null ? bio : "Software Engineer");
                if (imagePath != null && !imagePath.isEmpty()) {
                    try { profileImage.setFill(new ImagePattern(new Image(imagePath))); } catch (Exception e) {}
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadUserStats() {
        Connection connect = DatabaseConnection.getInstance().getConnection();
        String sqlDone = "SELECT COUNT(*) FROM tasks WHERE status = 'Completed'";
        String sqlTotal = "SELECT COUNT(*) FROM tasks";
        try {
            Statement stmt = connect.createStatement();
            ResultSet rsDone = stmt.executeQuery(sqlDone);
            int doneCount = 0;
            if(rsDone.next()) doneCount = rsDone.getInt(1);
            ResultSet rsTotal = stmt.executeQuery(sqlTotal);
            int totalCount = 0;
            if(rsTotal.next()) totalCount = rsTotal.getInt(1);
            int pendingCount = totalCount - doneCount;
            int productivity = (totalCount > 0) ? (doneCount * 100 / totalCount) : 0;
            lblStatDone.setText(String.valueOf(doneCount));
            lblStatPending.setText(String.valueOf(pendingCount));
            lblStatScore.setText(productivity + "%");
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void handleImportImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(((Node)event.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            saveImagePathToDB(selectedFile.toURI().toString());
            profileImage.setFill(new ImagePattern(new Image(selectedFile.toURI().toString())));
        }
    }

    private void saveImagePathToDB(String path) {
        try {
            Connection connect = DatabaseConnection.getInstance().getConnection();
            PreparedStatement prepare = connect.prepareStatement("UPDATE users SET image_path = ? WHERE email = ?");
            prepare.setString(1, path); prepare.setString(2, userEmail); prepare.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void saveInfo(ActionEvent event) {
        try {
            Connection connect = DatabaseConnection.getInstance().getConnection();
            PreparedStatement prepare = connect.prepareStatement("UPDATE users SET username = ?, bio = ? WHERE email = ?");
            prepare.setString(1, usernameField.getText()); prepare.setString(2, bioField.getText()); prepare.setString(3, userEmail);
            if(prepare.executeUpdate() > 0) showAlert(Alert.AlertType.INFORMATION, "Succès", "Profil mis à jour !");
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void updatePassword(ActionEvent event) {
        if(!newPasswordField.getText().equals(confirmPasswordField.getText())) { showAlert(Alert.AlertType.ERROR, "Erreur", "Mots de passe non identiques"); return; }
        try {
            Connection connect = DatabaseConnection.getInstance().getConnection();
            PreparedStatement check = connect.prepareStatement("SELECT password_hash FROM users WHERE email = ?");
            check.setString(1, userEmail); ResultSet rs = check.executeQuery();
            if(rs.next() && rs.getString(1).equals(currentPasswordField.getText())) {
                PreparedStatement update = connect.prepareStatement("UPDATE users SET password_hash = ? WHERE email = ?");
                update.setString(1, newPasswordField.getText()); update.setString(2, userEmail); update.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Mot de passe changé !");
            } else { showAlert(Alert.AlertType.ERROR, "Erreur", "Mot de passe actuel incorrect"); }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void goToDashboard(ActionEvent event) throws IOException { navigate(event, "/com/smarttask/smarttaskmanager/view/dashboard.fxml"); }
    @FXML public void goToTasks(ActionEvent event) throws IOException { navigate(event, "/com/smarttask/smarttaskmanager/view/tasks.fxml"); }
    @FXML public void handleLogout(ActionEvent event) throws IOException { UserSession.getInstance().cleanUserSession(); navigate(event, "/com/smarttask/smarttaskmanager/view/login.fxml"); }

    private void navigate(ActionEvent event, String path) throws IOException { ((Stage)((Node)event.getSource()).getScene().getWindow()).setScene(new Scene(new FXMLLoader(getClass().getResource(path)).load())); }
    private void showAlert(Alert.AlertType type, String title, String content) { Alert alert = new Alert(type); alert.setTitle(title); alert.setContentText(content); alert.showAndWait(); }
}