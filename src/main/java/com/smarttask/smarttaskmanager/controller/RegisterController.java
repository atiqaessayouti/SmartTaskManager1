package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterController {

    @FXML
    private TextField tfUsername;

    @FXML
    private TextField tfEmail;

    @FXML
    private PasswordField pfPassword;

    // Quand on clique sur le bouton "Register"
    @FXML
    public void handleRegister(ActionEvent event) {
        String username = tfUsername.getText();
        String email = tfEmail.getText();
        String password = pfPassword.getText();

        // 1. Vérifier si c'est vide
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Remplissez tous les champs !");
            return;
        }

        // 2. Insérer dans la base de données
        String insertSql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";

        Connection connect = DatabaseConnection.getInstance().getConnection();

        try {
            PreparedStatement prepare = connect.prepareStatement(insertSql);
            prepare.setString(1, username);
            prepare.setString(2, email);
            prepare.setString(3, password); // Attention: Idéalement, on crypte le mot de passe ici

            int result = prepare.executeUpdate();

            if (result > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Accound was created successufully ! Login now .");
                // Rediriger vers le Login
                goToLogin(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Echec de l'inscription.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Cet email existe peut-être déjà ou erreur base de données.");
        }
    }

    // Quand on clique sur le lien "Already have an account? Login"
    @FXML
    public void handleLoginLink(ActionEvent event) throws IOException {
        goToLogin(event);
    }

    private void goToLogin(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smarttask/smarttaskmanager/view/login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}