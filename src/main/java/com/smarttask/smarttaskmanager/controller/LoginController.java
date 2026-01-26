package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import com.smarttask.smarttaskmanager.util.UserSession; // <--- IMPORT MOHIM JIDDAN
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

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    protected void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Remplissez tous les champs.");
            return;
        }

        Connection connectDB = DatabaseConnection.getInstance().getConnection();

        // ⚠️ Bddelna Query: Mabqinach baghin ghir n7sbu (count), baghin njibu Data (username, email)
        // Ila kant colonne d mot de passe smitha "password" f database, bddli "password_hash" b "password"
        String query = "SELECT email, username FROM users WHERE email = ? AND password_hash = ?";

        try {
            PreparedStatement statement = connectDB.prepareStatement(query);
            statement.setString(1, email);
            statement.setString(2, password);

            ResultSet queryResult = statement.executeQuery();

            // Ila lqina resultat (ya3ni login s7i7)
            if (queryResult.next()) {

                // 1. Njibu Data mn Base de Données
                String dbEmail = queryResult.getString("email");
                String dbUsername = queryResult.getString("username");

                // 2. N3mmru SESSION (Hna fin kanbda la mémoire)
                UserSession.getInstace(dbEmail, dbUsername);

                // 3. Login Naj7 -> Sir l Dashboard
                goToDashboard(event);

            } else {
                showError("Email ou mot de passe incorrect.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur connexion base de données.");
        }
    }

    // Méthode bach tmchi l Dashboard
    private void goToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smarttask/smarttaskmanager/view/dashboard.fxml"));
            Scene scene = new Scene(loader.load());

            // Nakhdu l'fenêtre (Stage) l'7aliya w nbdluha
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Smart Task Manager - Dashboard");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur: Impossible de charger le Dashboard.");
        }
    }


    @FXML
    public void handleGoToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smarttask/smarttaskmanager/view/register.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Smart Task Manager - Inscription");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.out.println("Fichier register.fxml mal9inahch");
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(true);
    }
}