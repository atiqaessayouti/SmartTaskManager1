package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
            showError("Please fill in all fields.");
            return;
        }

        // Utilisation du Singleton DatabaseConnection
        Connection connectDB = DatabaseConnection.getInstance().getConnection();

        // On récupère is_admin pour rediriger vers l'Analytics Dashboard si nécessaire
        String query = "SELECT is_admin FROM users WHERE email = ? AND password_hash = ?";

        try (PreparedStatement statement = connectDB.prepareStatement(query)) {
            statement.setString(1, email);
            statement.setString(2, password);

            ResultSet queryResult = statement.executeQuery();

            if (queryResult.next()) {
                boolean isAdmin = queryResult.getBoolean("is_admin");
                goToDashboard(event, isAdmin);
            } else {
                showError("Invalid email or password.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database connection error.");
        }
    }

    private void goToDashboard(ActionEvent event, boolean isAdmin) {
        try {
            // Chemins basés sur ta structure de ressources
            String fxmlPath = isAdmin
                    ? "/com/smarttask/smarttaskmanager/view/admin_dashboard.fxml"
                    : "/com/smarttask/smarttaskmanager/view/dashboard.fxml";

            String title = isAdmin ? "Smart Task - Admin Analytics" : "Smart Task - User Dashboard";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());

            // Récupération de la fenêtre actuelle
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            // Affiche l'erreur précise dans la console pour le débogage Master
            e.printStackTrace();
            showError("Critical Error: View not found (" + (isAdmin ? "Admin" : "User") + ").");
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
            showError("Register view not found.");
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}