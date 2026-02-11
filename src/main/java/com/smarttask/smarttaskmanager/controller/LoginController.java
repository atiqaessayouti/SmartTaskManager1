package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import com.smarttask.smarttaskmanager.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert; // ✅ ضروري للـ Alert
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern; // ✅ ضروري للتحقق من الإيميل

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    protected void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        // 1. التحقق من أن الخانات ليست فارغة
        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs vides", "Veuillez remplir tous les champs !");
            return;
        }

        // 2. التحقق من صيغة الإيميل (Email Syntax Validation)
        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.WARNING, "Format Email Invalide", "L'adresse email n'est pas valide.\nExemple: user@gmail.com");
            return;
        }

        Connection connectDB = DatabaseConnection.getInstance().getConnection();
        String query = "SELECT user_id, role FROM users WHERE email = ? AND password_hash = ?";

        try (PreparedStatement statement = connectDB.prepareStatement(query)) {
            statement.setString(1, email);
            statement.setString(2, password);

            ResultSet queryResult = statement.executeQuery();

            if (queryResult.next()) {
                // ✅ تسجيل الدخول ناجح
                int userId = queryResult.getInt("user_id");
                String role = queryResult.getString("role");
                boolean isAdmin = "admin".equalsIgnoreCase(role);

                UserSession.getInstance(userId, email);
                goToDashboard(event, isAdmin);

            } else {
                // ❌ كلمة المرور أو الإيميل خاطئ (Alert)
                showAlert(Alert.AlertType.ERROR, "\n" +
                        "Connection failed", "Incorrect email or password !");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur Base de Données", "Impossible de se connecter au serveur.");
        }
    }

    // ✅ دالة مساعدة للتحقق من صيغة الإيميل (Regex)
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pat = Pattern.compile(emailRegex);
        return pat.matcher(email).matches();
    }

    // ✅ دالة مساعدة لإظهار النوافذ المنبثقة (Alerts)
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void goToDashboard(ActionEvent event, boolean isAdmin) {
        try {
            String path = isAdmin ? "/com/smarttask/smarttaskmanager/view/admin_dashboard.fxml"
                    : "/com/smarttask/smarttaskmanager/view/dashboard.fxml";
            String roleName = isAdmin ? "Admin" : "User";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Smart Task Manager - " + roleName);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur Critique", "Impossible de charger le Dashboard !");
        }
    }

    @FXML
    public void handleGoToRegister(ActionEvent event) {
        navigate(event, "/com/smarttask/smarttaskmanager/view/register.fxml");
    }

    @FXML
    public void handleForgotPassword(ActionEvent event) {
        navigate(event, "/com/smarttask/smarttaskmanager/view/forgot_password.fxml");
    }

    // دالة مساعدة للتنقل لتفادي تكرار الكود
    private void navigate(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Vue introuvable : " + fxmlPath);
        }
    }
}