package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.DAO.TaskDAO;
import com.smarttask.smarttaskmanager.model.Task;
import com.smarttask.smarttaskmanager.service.AIService;
import com.smarttask.smarttaskmanager.service.MLPredictionService;
import com.smarttask.smarttaskmanager.service.NotificationService;
import com.smarttask.smarttaskmanager.service.PDFExportService;
import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import com.smarttask.smarttaskmanager.util.UserSession;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class DashboardController {

    @FXML private Label lblEnCours;
    @FXML private Label lblTerminees;
    @FXML private Label lblEnRetard;
    @FXML private Label aiSuggestionLabel;
    @FXML private PieChart pieChartPriority;
    @FXML private BarChart<String, Number> productivityChart;
    @FXML private Button btnMyTasks;

    private NotificationService notifService;
    private MLPredictionService mlModel;

    @FXML
    public void initialize() {
        System.out.println("🚀 DÉMARRAGE DU DASHBOARD");

        if (aiSuggestionLabel != null) {
            aiSuggestionLabel.setText("System Ready");
        }

        // -----------------------------------------------------------
        // 🧠 MACHINE LEARNING TRAINING (Version SQL Optimisée)
        // -----------------------------------------------------------
        try {
            System.out.println("🤖 Initialisation du modèle IA (Mode Data Mining)...");
            mlModel = new MLPredictionService();

            // 👇 ICI LE CHANGEMENT : On envoie 'null' car le service lit directement la BDD
            mlModel.trainModel(null);

            // Test Rapide
            System.out.println("🔮 Prédiction actuelle pour 'High' : " + mlModel.predictDaysNeeded("High") + " jours");

        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de l'entraînement IA : " + e.getMessage());
        }
        // -----------------------------------------------------------

        updateDashboardKPIs();
        loadPieChartData();
        loadPerformanceTrends();
        checkNotifications();
        startNotificationService();
        loadAIInsights();
    }

    // --- LE RESTE DU CODE RESTE IDENTIQUE ---

    private void startNotificationService() {
        try {
            notifService = new NotificationService(this);
            notifService.startService();
            System.out.println("✅ Service Notification : DÉMARRÉ");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void addNotificationToQueue(int taskId, String type, String message, boolean isUrgent) {
        Platform.runLater(() -> {
            if (aiSuggestionLabel != null) {
                aiSuggestionLabel.setText("🔔 " + message);
                aiSuggestionLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: #e74c3c; -fx-padding: 5px;");
            }
            if (type.equals("INVITE")) {
                showInvitationDialog(taskId, message);
            }
        });
    }

    private void showInvitationDialog(int taskId, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("📩 Invitation Reçue");
        alert.setHeaderText("Nouvelle Tâche Partagée");
        alert.setContentText(message + "\n\nVoulez-vous accepter cette tâche ?");

        ButtonType btnAccept = new ButtonType("Accepter");
        ButtonType btnDecline = new ButtonType("Refuser");
        ButtonType btnLater = new ButtonType("Plus tard", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnAccept, btnDecline, btnLater);

        alert.showAndWait().ifPresent(response -> {
            if (response == btnAccept) updateShareStatus(taskId, "ACCEPTED");
            else if (response == btnDecline) updateShareStatus(taskId, "DECLINED");
        });
    }

    private void updateShareStatus(int taskId, String status) {
        String sql = "UPDATE tasks SET share_status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, status);
            pst.setInt(2, taskId);
            pst.executeUpdate();
            Platform.runLater(() -> {
                if (aiSuggestionLabel != null) {
                    aiSuggestionLabel.setText("✅ Tâche " + status + " !");
                    aiSuggestionLabel.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 5px;");
                }
                updateDashboardKPIs();
                checkNotifications();
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void handleExportPDF() {
        try {
            TaskDAO taskDAO = new TaskDAO();
            List<Task> tasks = taskDAO.getAllTasks();
            if (tasks.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Aucune tâche à exporter !");
                return;
            }
            String path = System.getProperty("user.home") + "/Desktop/MesTaches_SmartManager.pdf";
            PDFExportService pdfService = new PDFExportService();
            pdfService.exportTasksToPDF(tasks, path);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "PDF exporté sur le Bureau !\n" + path);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Problème lors de l'export PDF.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }

    private void loadAIInsights() {
        new Thread(() -> {
            try {
                TaskDAO taskDAO = new TaskDAO();
                List<Task> tasks = taskDAO.getAllTasks();
                AIService aiService = new AIService();
                String insight = aiService.getProductivityInsights(tasks);
                Platform.runLater(() -> {
                    if (aiSuggestionLabel != null && !aiSuggestionLabel.getText().startsWith("🔔")) {
                        aiSuggestionLabel.setText("💡 AI Tip: " + insight);
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void loadPerformanceTrends() {
        if (productivityChart == null) return;
        productivityChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tasks Completed");
        String sql = "SELECT deadline, COUNT(*) as total FROM tasks WHERE status = 'Completed' GROUP BY deadline ORDER BY deadline LIMIT 7";
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {
            ResultSet result = prepare.executeQuery();
            while (result.next()) {
                series.getData().add(new XYChart.Data<>(result.getString("deadline"), result.getInt("total")));
            }
            productivityChart.getData().add(series);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void checkNotifications() {
        if(UserSession.getInstance() == null) return;
        String currentUser = UserSession.getInstance().getEmail();
        String sql = "SELECT COUNT(*) FROM tasks WHERE shared_with = ? AND status != 'Completed'";
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {
            prepare.setString(1, currentUser);
            ResultSet rs = prepare.executeQuery();
            if (rs.next() && rs.getInt(1) > 0 && btnMyTasks != null) {
                btnMyTasks.setText("My Tasks (" + rs.getInt(1) + ")");
                btnMyTasks.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void goToDashboard(ActionEvent event) { }
    @FXML public void goToCalendar(ActionEvent event) { navigate(event, "/com/smarttask/smarttaskmanager/view/calendar_view.fxml", "Calendrier"); }
    @FXML public void goToTasks(ActionEvent event) { navigate(event, "/com/smarttask/smarttaskmanager/view/tasks.fxml", "Mes Tâches"); }
    @FXML public void goToProfile(ActionEvent event) { navigate(event, "/com/smarttask/smarttaskmanager/view/profile.fxml", "Profil"); }
    @FXML public void handleLogout(ActionEvent event) {
        if (notifService != null) notifService.stopService();
        UserSession.getInstance().cleanUserSession();
        navigate(event, "/com/smarttask/smarttaskmanager/view/login.fxml", "Login");
    }
    @FXML public void handleNewTask(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smarttask/smarttaskmanager/view/add_task.fxml"));
            Stage s = new Stage(); s.setScene(new Scene(loader.load())); s.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
    private void navigate(ActionEvent event, String fxmlPath, String title) {
        try {
            if (notifService != null) notifService.stopService();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { System.out.println("Erreur Navigation: " + fxmlPath); }
    }
    private void updateDashboardKPIs() {
        try (Connection connect = DatabaseConnection.getInstance().getConnection(); Statement stmt = connect.createStatement()) {
            if(lblEnCours != null) {
                ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) FROM tasks WHERE status = 'In Progress'");
                if(rs1.next()) lblEnCours.setText(String.valueOf(rs1.getInt(1)));
            }
            if(lblTerminees != null) {
                ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) FROM tasks WHERE status = 'Completed'");
                if(rs2.next()) lblTerminees.setText(String.valueOf(rs2.getInt(1)));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    private void loadPieChartData() {
        if(pieChartPriority == null) return;
        try (Connection connect = DatabaseConnection.getInstance().getConnection(); Statement stmt = connect.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT priority, COUNT(*) as count FROM tasks GROUP BY priority");
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            while (rs.next()) pieData.add(new PieChart.Data(rs.getString("priority") + " (" + rs.getInt("count") + ")", rs.getInt("count")));
            pieChartPriority.setData(pieData);
        } catch (Exception e) { e.printStackTrace(); }
    }
}