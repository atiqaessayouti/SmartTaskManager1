package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import com.smarttask.smarttaskmanager.util.EmailUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

public class ForgotPasswordController {

    @FXML private TextField emailField, codeField;
    @FXML private PasswordField newPassField;
    @FXML private VBox step1Box, step2Box, step3Box;
    @FXML private Label messageLabel;

    private int generatedCode;
    private String userEmail;

    // --- 1. ENVOYER CODE ---
    @FXML
    public void handleSendCode() {
        String email = emailField.getText();

        if (email.isEmpty()) {
            messageLabel.setText("Veuillez entrer un email.");
            return;
        }

        // Vérifier wach email kayn f Database
        if (checkEmailExists(email)) {
            // Générer Code (ex: 458921)
            generatedCode = new Random().nextInt(900000) + 100000;
            userEmail = email;

            try {
                // Sift Email Reel
                EmailUtil.sendEmail(email, "Réinitialisation Mot de Passe", "Votre code est: " + generatedCode);

                // Duz l'étape taniya
                step1Box.setVisible(false);
                step1Box.setManaged(false);
                step2Box.setVisible(true);
                step2Box.setManaged(true);
                messageLabel.setText("Code envoyé ! Vérifiez votre email.");
                messageLabel.setStyle("-fx-text-fill: green;");

            } catch (Exception e) {
                e.printStackTrace();
                messageLabel.setText("Erreur d'envoi. Vérifiez votre connexion.");
            }
        } else {
            messageLabel.setText("Cet email n'existe pas.");
        }
    }

    // --- 2. VERIFIER CODE ---
    @FXML
    public void handleVerifyCode() {
        try {
            int codeInput = Integer.parseInt(codeField.getText());
            if (codeInput == generatedCode) {
                // Code s7i7 -> Duz l'étape talta
                step2Box.setVisible(false);
                step2Box.setManaged(false);
                step3Box.setVisible(true);
                step3Box.setManaged(true);
                messageLabel.setText("");
            } else {
                messageLabel.setText("Code incorrect !");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Veuillez entrer des chiffres uniquement.");
        }
    }

    // --- 3. CHANGER PASSWORD ---
    @FXML
    public void handleResetPassword() {
        String newPass = newPassField.getText();
        if (newPass.isEmpty()) return;

        // Update f Database
        String sql = "UPDATE users SET password_hash = ? WHERE email = ?"; // Ola 'password' ila ma kantch hashé
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setString(1, newPass);
            prepare.setString(2, userEmail);
            prepare.executeUpdate();

            messageLabel.setText("Mot de passe changé avec succès !");
            messageLabel.setStyle("-fx-text-fill: green;");

            // Revenir au login après 2 secondes (Optionnel)

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Check Database Helper
    private boolean checkEmailExists(String email) {
        String sql = "SELECT email FROM users WHERE email = ?";
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {
            prepare.setString(1, email);
            ResultSet rs = prepare.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    public void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smarttask/smarttaskmanager/view/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) { e.printStackTrace(); }
    }
}