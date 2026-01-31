package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import com.smarttask.smarttaskmanager.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    protected void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        Connection connectDB = DatabaseConnection.getInstance().getConnection();

        // ✅ CORRECTED QUERY: On récupère 'role' (pas is_admin)
        String query = "SELECT user_id, role FROM users WHERE email = ? AND password_hash = ?";

        try (PreparedStatement statement = connectDB.prepareStatement(query)) {
            statement.setString(1, email);
            statement.setString(2, password);

            ResultSet queryResult = statement.executeQuery();

            if (queryResult.next()) {
                int userId = queryResult.getInt("user_id");

                // ✅ CORRECTED LOGIC:
                // La base de données retourne une String ("admin", "user", etc.)
                String role = queryResult.getString("role");

                // On vérifie si c'est admin
                boolean isAdmin = "admin".equalsIgnoreCase(role);

                // Initialiser la session
                UserSession.getInstance(userId, email);

                // Passer à la méthode de navigation
                goToDashboard(event, isAdmin);
            } else {
                showError("Email ou mot de passe incorrect.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de connexion à la base de données.");
        }
    }

    private void goToDashboard(ActionEvent event, boolean isAdmin) {
        try {
            String path;
            String roleName;

            // Définir le chemin EXACT selon le rôle
            if (isAdmin) {
                path = "/com/smarttask/smarttaskmanager/view/admin_dashboard.fxml";
                roleName = "Admin";
            } else {
                path = "/com/smarttask/smarttaskmanager/view/dashboard.fxml";
                roleName = "User";
            }

            System.out.println("Tentative de chargement du fichier : " + path);

            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();

            if (root == null) {
                errorLabel.setText("Erreur : Fichier FXML introuvable !");
                return;
            }

            // Changer la scène
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Smart Task Manager - " + roleName);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur critique : Vue introuvable !");
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur inconnue : " + e.getMessage());
        }
    }

    @FXML
    public void handleGoToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smarttask/smarttaskmanager/view/register.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Vue Register introuvable.");
        }
    }

    @FXML
    public void handleForgotPassword(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smarttask/smarttaskmanager/view/forgot_password.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.out.println("Erreur chargement forgot password: " + e.getMessage());
            showError("Fonctionnalité non disponible.");
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}