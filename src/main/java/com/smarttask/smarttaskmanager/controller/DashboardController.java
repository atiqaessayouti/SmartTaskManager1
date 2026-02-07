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

        // --- ML Training ---
        try {
            mlModel = new MLPredictionService();
            mlModel.trainModel(null); // Mode Data Mining SQL
        } catch (Exception e) {
            System.err.println("⚠️ Warning ML: " + e.getMessage());
        }

        // --- Chargement des Données ---
        updateDashboardKPIs();       // Charge KPIs (En cours, Terminé, En retard)
        loadPieChartData();          // Charge Pie Chart
        loadPerformanceTrends();     // Charge Bar Chart

        // --- Services Background ---
        checkNotifications();
        startNotificationService();
        loadAIInsights();
    }

    // ✅ METHODE CORRIGEE: KPI avec Calcul du RETARD
    private void updateDashboardKPIs() {
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             Statement stmt = connect.createStatement()) {

            // 1. In Progress
            if(lblEnCours != null) {
                ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) FROM tasks WHERE status = 'In Progress'");
                if(rs1.next()) lblEnCours.setText(String.valueOf(rs1.getInt(1)));
            }

            // 2. Completed
            if(lblTerminees != null) {
                ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) FROM tasks WHERE status = 'Completed'");
                if(rs2.next()) lblTerminees.setText(String.valueOf(rs2.getInt(1)));
            }

            // 3. Overdue (Logic ajouté pour le calcul du retard)
            if(lblEnRetard != null) {
                // Compatible SQLite/MySQL: deadline passé et tâche non terminée
                String query = "SELECT COUNT(*) FROM tasks WHERE deadline < DATE('now') AND status != 'Completed'";
                ResultSet rs3 = stmt.executeQuery(query);
                if(rs3.next()) lblEnRetard.setText(String.valueOf(rs3.getInt(1)));
            }

        } catch (Exception e) { e.printStackTrace(); }
    }

    // ✅ METHODE AJOUTEE: Couleurs Custom pour PieChart (Sans CSS)
    private void applyCustomChartColors() {
        // Palette: Mauve, Bleu ciel, Rose, Rouge pale
        String[] colors = {"#a18cd1", "#8fd3f4", "#fbc2eb", "#ff9a9e"};
        int i = 0;
        for (PieChart.Data data : pieChartPriority.getData()) {
            String color = colors[i % colors.length];
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
            i++;
        }
    }

    private void loadPieChartData() {
        if(pieChartPriority == null) return;
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             Statement stmt = connect.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT priority, COUNT(*) as count FROM tasks GROUP BY priority");
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            while (rs.next()) {
                pieData.add(new PieChart.Data(rs.getString("priority"), rs.getInt("count")));
            }
            pieChartPriority.setData(pieData);

            // Appliquer les couleurs après chargement
            applyCustomChartColors();

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadPerformanceTrends() {
        if (productivityChart == null) return;
        productivityChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Productivity");

        // Requête pour les 7 derniers jours
        String sql = "SELECT deadline, COUNT(*) as total FROM tasks WHERE status = 'Completed' GROUP BY deadline ORDER BY deadline LIMIT 7";
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {
            ResultSet result = prepare.executeQuery();
            while (result.next()) {
                series.getData().add(new XYChart.Data<>(result.getString("deadline"), result.getInt("total")));
            }
            productivityChart.getData().add(series);

            // Couleur des barres (Bleu ciel)
            for(XYChart.Data<String, Number> data : series.getData()) {
                data.getNode().setStyle("-fx-bar-fill: #a6c1ee;");
            }

        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- Services Notifications & AI ---
    private void startNotificationService() {
        try {
            notifService = new NotificationService(this);
            notifService.startService();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void addNotificationToQueue(int taskId, String type, String message, boolean isUrgent) {
        Platform.runLater(() -> {
            if (aiSuggestionLabel != null) {
                aiSuggestionLabel.setText("🔔 " + message);
                // Style rouge pour alerte
                aiSuggestionLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: #ff9a9e; -fx-padding: 5px; -fx-background-radius: 5;");
            }
            if (type.equals("INVITE")) {
                showInvitationDialog(taskId, message);
            }
        });
    }

    private void showInvitationDialog(int taskId, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Invitation");
        alert.setHeaderText("Task Shared");
        alert.setContentText(message + "\n\nDo you accept?");

        ButtonType btnAccept = new ButtonType("Accept");
        ButtonType btnDecline = new ButtonType("Decline");
        ButtonType btnLater = new ButtonType("Later", ButtonBar.ButtonData.CANCEL_CLOSE);

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
                updateDashboardKPIs();
                checkNotifications();
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- Navigation & Export ---
    @FXML
    public void handleExportPDF() {
        try {
            TaskDAO taskDAO = new TaskDAO();
            List<Task> tasks = taskDAO.getAllTasks();
            if (tasks.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Warning", "No tasks to export!");
                return;
            }
            String path = System.getProperty("user.home") + "/Desktop/SmartManager_Report.pdf";
            PDFExportService pdfService = new PDFExportService();
            pdfService.exportTasksToPDF(tasks, path);
            showAlert(Alert.AlertType.INFORMATION, "Success", "PDF Exported to Desktop!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Export Failed.");
        }
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
                        aiSuggestionLabel.setText("✨ AI Insight: " + insight);

                        // HNA FIN KAN L-MOCHKIL: Rdditha K7la (#2d3436) blast White
                        aiSuggestionLabel.setStyle("-fx-text-fill: #512da8; -fx-font-weight: bold; -fx-font-size: 14px;");
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
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
                btnMyTasks.setStyle("-fx-background-color: #ff9a9e; -fx-text-fill: white; -fx-font-weight: bold;");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }

    // --- Navigation Handlers ---
    @FXML public void goToDashboard(ActionEvent event) { }
    @FXML public void goToCalendar(ActionEvent event) { navigate(event, "/com/smarttask/smarttaskmanager/view/calendar_view.fxml", "Calendar"); }
    @FXML public void goToTasks(ActionEvent event) { navigate(event, "/com/smarttask/smarttaskmanager/view/tasks.fxml", "My Tasks"); }
    @FXML public void goToProfile(ActionEvent event) { navigate(event, "/com/smarttask/smarttaskmanager/view/profile.fxml", "Profile"); }
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
        } catch (Exception e) { System.out.println("Error Navigating to: " + fxmlPath); e.printStackTrace(); }
    }
}