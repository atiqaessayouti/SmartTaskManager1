package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import com.smarttask.smarttaskmanager.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ProfileController {

    // --- FXML FIELDS ---
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextArea bioField;
    @FXML private Circle profileImage;

    // Security Fields
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    // Header Labels
    @FXML private Label lblHeaderName;
    @FXML private Label lblHeaderRole;

    // ✅ Stats Labels (Dynamiques)
    @FXML private Label lblStatDone;
    @FXML private Label lblStatPending;
    @FXML private Label lblStatScore;

    // Layout Switching
    @FXML private VBox infoBox;       // Boite Info
    @FXML private VBox securityBox;   // Boite Security

    @FXML private Button btnTabInfo;
    @FXML private Button btnTabSecurity;

    private String userEmail;

    @FXML
    public void initialize() {
        // 1. Get User Session
        UserSession session = UserSession.getInstance();
        if (session != null) {
            this.userEmail = session.getEmail();
            emailField.setText(userEmail);

            // 2. Load Data
            loadUserDetails(); // Name, Bio, Image
            loadUserStats();   // ✅ Dynamic Numbers (Calculated from DB)
        }

        // 3. Show Default View
        showInfoForm();
    }

    // --- 📊 1. LOAD DYNAMIC STATS (Le plus important) ---
    private void loadUserStats() {
        Connection connect = DatabaseConnection.getInstance().getConnection();

        // Requêtes SQL (Filtrées par user_email)
        String sqlDone = "SELECT COUNT(*) FROM tasks WHERE status = 'Completed' AND user_email = ?";
        String sqlTotal = "SELECT COUNT(*) FROM tasks WHERE user_email = ?";

        try {
            // A. Calculer les tâches terminées
            PreparedStatement pstDone = connect.prepareStatement(sqlDone);
            pstDone.setString(1, userEmail);
            ResultSet rsDone = pstDone.executeQuery();
            int doneCount = 0;
            if (rsDone.next()) doneCount = rsDone.getInt(1);

            // B. Calculer le total
            PreparedStatement pstTotal = connect.prepareStatement(sqlTotal);
            pstTotal.setString(1, userEmail);
            ResultSet rsTotal = pstTotal.executeQuery();
            int totalCount = 0;
            if (rsTotal.next()) totalCount = rsTotal.getInt(1);

            // C. Calculer les autres valeurs
            int pendingCount = totalCount - doneCount;
            int productivityScore = (totalCount > 0) ? (doneCount * 100 / totalCount) : 0;

            // D. Afficher dans l'interface
            lblStatDone.setText(String.valueOf(doneCount));
            lblStatPending.setText(String.valueOf(pendingCount));
            lblStatScore.setText(productivityScore + "%");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur Stats: " + e.getMessage());
        }
    }

    // --- 👤 2. LOAD USER INFO ---
    private void loadUserDetails() {
        String sql = "SELECT username, bio, image_path FROM users WHERE email = ?";
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setString(1, userEmail);
            ResultSet rs = prepare.executeQuery();

            if (rs.next()) {
                String username = rs.getString("username");
                String bio = rs.getString("bio");
                String imagePath = rs.getString("image_path");

                usernameField.setText(username);
                bioField.setText(bio);
                lblHeaderName.setText(username);
                lblHeaderRole.setText((bio != null && !bio.isEmpty()) ? bio : "Software Engineer");

                if (imagePath != null && !imagePath.isEmpty()) {
                    try {
                        // Fixer le chemin si nécessaire (file:///)
                        String path = imagePath.startsWith("file:") ? imagePath : new File(imagePath).toURI().toString();
                        profileImage.setFill(new ImagePattern(new Image(path)));
                    } catch (Exception e) { System.err.println("Erreur Image: " + e.getMessage()); }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- 🔄 3. SWITCH VIEW (TABS) ---
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
        // Style Active
        btnTabInfo.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;");
        btnTabSecurity.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    private void showSecurityForm() {
        infoBox.setVisible(false);
        securityBox.setVisible(true);
        // Style Active
        btnTabSecurity.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;");
        btnTabInfo.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    // --- 💾 4. UPDATE ACTIONS ---

    @FXML
    public void saveInfo(ActionEvent event) {
        String sql = "UPDATE users SET username = ?, bio = ? WHERE email = ?";
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setString(1, usernameField.getText());
            prepare.setString(2, bioField.getText());
            prepare.setString(3, userEmail);

            if(prepare.executeUpdate() > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Profil mis à jour !");
                loadUserDetails(); // Rafraichir le header
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void updatePassword(ActionEvent event) {
        if(!newPasswordField.getText().equals(confirmPasswordField.getText())) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Les mots de passe ne correspondent pas !");
            return;
        }

        try (Connection connect = DatabaseConnection.getInstance().getConnection()) {
            // Vérifier l'ancien mot de passe
            PreparedStatement check = connect.prepareStatement("SELECT password_hash FROM users WHERE email = ?");
            check.setString(1, userEmail);
            ResultSet rs = check.executeQuery();

            if(rs.next() && rs.getString(1).equals(currentPasswordField.getText())) {
                // Update
                PreparedStatement update = connect.prepareStatement("UPDATE users SET password_hash = ? WHERE email = ?");
                update.setString(1, newPasswordField.getText());
                update.setString(2, userEmail);
                update.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Mot de passe changé avec succès !");
                currentPasswordField.clear(); newPasswordField.clear(); confirmPasswordField.clear();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Mot de passe actuel incorrect !");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void handleImportImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(((Node)event.getSource()).getScene().getWindow());

        if (selectedFile != null) {
            String path = selectedFile.toURI().toString();
            saveImagePathToDB(path);
            profileImage.setFill(new ImagePattern(new Image(path)));
        }
    }

    private void saveImagePathToDB(String path) {
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement("UPDATE users SET image_path = ? WHERE email = ?")) {
            prepare.setString(1, path);
            prepare.setString(2, userEmail);
            prepare.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- 🧭 5. NAVIGATION ---
    @FXML public void goToDashboard(ActionEvent event) { navigate(event, "/com/smarttask/smarttaskmanager/view/dashboard.fxml"); }
    @FXML public void goToTasks(ActionEvent event) { navigate(event, "/com/smarttask/smarttaskmanager/view/tasks.fxml"); }
    @FXML public void goToCalendar(ActionEvent event) { navigate(event, "/com/smarttask/smarttaskmanager/view/calendar_view.fxml"); }

    @FXML
    public void handleLogout(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        navigate(event, "/com/smarttask/smarttaskmanager/view/login.fxml");
    }

    private void navigate(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}