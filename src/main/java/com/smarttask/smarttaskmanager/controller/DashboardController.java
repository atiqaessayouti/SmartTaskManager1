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
    @FXML private BarChart<String, Number> timeSpentChart;

    @FXML private Button btnMyTasks;
    @FXML private Button btnAIChat; // ✅ أضيفي هذا المعرف للزر في FXML

    private NotificationService notifService;
    private MLPredictionService mlModel;
    private TaskDAO taskDAO = new TaskDAO();

    @FXML
    public void initialize() {
        if (aiSuggestionLabel != null) aiSuggestionLabel.setText("System Ready");

        // 1. ML Training
        try {
            mlModel = new MLPredictionService();
            mlModel.trainModel(null);
        } catch (Exception e) { System.err.println("⚠️ Warning ML: " + e.getMessage()); }

        // 2. Loading Data
        refreshAllData();

        // 3. Animations
        addHoverAnimation(btnMyTasks);
        addHoverAnimation(btnAIChat);
        addHoverAnimation(productivityChart);
        addHoverAnimation(pieChartPriority);
        addHoverAnimation(timeSpentChart);

        // 4. Background Services
        checkNotifications();
        startNotificationService();
        loadAIInsights();
    }

    private void refreshAllData() {
        updateDashboardKPIs();
        loadPieChartData();
        loadPerformanceTrends();
        loadTimeSpentChart();
    }

    // =========================================================
    // 🤖 AI CHATBOT INTEGRATION
    // =========================================================

    // ✅ فتح الشات بوت كصفحة كاملة (Navigation)
    @FXML
    public void goToAIChat(ActionEvent event) {
        navigate(event, "/com/smarttask/smarttaskmanager/view/chatbot.fxml", "AI Chat Assistant");
    }

    // ✅ فتح الشات بوت كنافذة منبثقة (Popup)
    @FXML
    public void handleOpenAIChat(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smarttask/smarttaskmanager/view/chatbot.fxml"));
            Stage stage = new Stage();
            stage.setTitle("AI Assistant");
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // =========================================================
    // 📊 DYNAMIC CHARTS
    // =========================================================

    private void loadPerformanceTrends() {
        if (productivityChart == null) return;
        productivityChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Completed Tasks");

        String sql = "SELECT deadline, COUNT(*) as total FROM tasks WHERE status = 'Completed' AND user_email = ? AND deadline IS NOT NULL GROUP BY deadline ORDER BY deadline LIMIT 7";
        boolean hasData = false;
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {
            prepare.setString(1, UserSession.getInstance().getEmail());
            ResultSet rs = prepare.executeQuery();
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("deadline"), rs.getInt("total")));
                hasData = true;
            }
        } catch (Exception e) { e.printStackTrace(); }

        if (!hasData) {
            series.getData().add(new XYChart.Data<>(java.time.LocalDate.now().minusDays(3).toString(), 2));
            series.getData().add(new XYChart.Data<>(java.time.LocalDate.now().minusDays(2).toString(), 5));
            series.getData().add(new XYChart.Data<>(java.time.LocalDate.now().minusDays(1).toString(), 3));
            series.getData().add(new XYChart.Data<>(java.time.LocalDate.now().toString(), 6));
        }

        productivityChart.getData().add(series);

        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    Tooltip tooltip = new Tooltip(data.getYValue() + " Tasks");
                    Tooltip.install(data.getNode(), tooltip);
                    data.getNode().setOnMouseEntered(e -> data.getNode().setStyle("-fx-bar-fill: #8E2DE2; -fx-cursor: hand;"));
                    data.getNode().setOnMouseExited(e -> data.getNode().setStyle("-fx-bar-fill: #f39c12;"));
                }
            }
        });
    }

    private void loadTimeSpentChart() {
        if (timeSpentChart == null) return;
        timeSpentChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Minutes Spent");

        String sql = "SELECT category, SUM(time_spent) / 60.0 as total_minutes FROM tasks WHERE user_email = ? GROUP BY category";
        boolean hasData = false;
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {
            prepare.setString(1, UserSession.getInstance().getEmail());
            ResultSet rs = prepare.executeQuery();
            while (rs.next()) {
                double mins = rs.getDouble("total_minutes");
                if(mins > 0) {
                    series.getData().add(new XYChart.Data<>(rs.getString("category"), mins));
                    hasData = true;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        if (!hasData) {
            series.getData().add(new XYChart.Data<>("Work", 120));
            series.getData().add(new XYChart.Data<>("Personal", 90));
        }
        timeSpentChart.getData().add(series);
    }

    private void loadPieChartData() {
        if(pieChartPriority == null) return;
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = connect.prepareStatement("SELECT priority, COUNT(*) as count FROM tasks WHERE user_email = ? GROUP BY priority")) {
            pst.setString(1, UserSession.getInstance().getEmail());
            ResultSet rs = pst.executeQuery();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            while (rs.next()) pieData.add(new PieChart.Data(rs.getString("priority"), rs.getInt("count")));

            pieChartPriority.setData(pieData);
            applyCustomChartColors();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void applyCustomChartColors() {
        String[] colors = {"#a18cd1", "#8fd3f4", "#fbc2eb", "#ff9a9e"};
        int i = 0;
        for (PieChart.Data data : pieChartPriority.getData()) {
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-pie-color: " + colors[i % colors.length] + ";");
                i++;
            }
        }
    }

    private void updateDashboardKPIs() {
        if (UserSession.getInstance() == null) return;
        String email = UserSession.getInstance().getEmail();
        try (Connection connect = DatabaseConnection.getInstance().getConnection()) {
            PreparedStatement ps1 = connect.prepareStatement("SELECT COUNT(*) FROM tasks WHERE status = 'In Progress' AND user_email = ?");
            ps1.setString(1, email);
            ResultSet rs1 = ps1.executeQuery();
            if(rs1.next() && lblEnCours != null) lblEnCours.setText(String.valueOf(rs1.getInt(1)));

            PreparedStatement ps2 = connect.prepareStatement("SELECT COUNT(*) FROM tasks WHERE status = 'Completed' AND user_email = ?");
            ps2.setString(1, email);
            ResultSet rs2 = ps2.executeQuery();
            if(rs2.next() && lblTerminees != null) lblTerminees.setText(String.valueOf(rs2.getInt(1)));

            PreparedStatement ps3 = connect.prepareStatement("SELECT COUNT(*) FROM tasks WHERE deadline < CURRENT_DATE AND status != 'Completed' AND user_email = ?");
            ps3.setString(1, email);
            ResultSet rs3 = ps3.executeQuery();
            if(rs3.next() && lblEnRetard != null) lblEnRetard.setText(String.valueOf(rs3.getInt(1)));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // =========================================================
    // 🛠️ SERVICES & NAVIGATION
    // =========================================================

    public void addNotificationToQueue(int taskId, String type, String message, boolean isUrgent) {
        Platform.runLater(() -> {
            if (aiSuggestionLabel != null) {
                aiSuggestionLabel.setText("🔔 " + message);
                String bgColor = isUrgent ? "#e74c3c" : "#3498db";
                aiSuggestionLabel.setStyle("-fx-text-fill: white; -fx-background-color: " + bgColor + "; -fx-padding: 5px; -fx-background-radius: 5;");
            }
            if ("INVITE".equals(type)) showInvitationDialog(taskId, message);
        });
    }

    private void showInvitationDialog(int taskId, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Shared Task Invitation");
        alert.setContentText(message + "\n\nDo you accept?");
        ButtonType btnAccept = new ButtonType("Accept");
        ButtonType btnDecline = new ButtonType("Decline");
        alert.getButtonTypes().setAll(btnAccept, btnDecline);
        alert.showAndWait().ifPresent(response -> {
            if (response == btnAccept) updateShareStatus(taskId, "ACCEPTED");
            else updateShareStatus(taskId, "DECLINED");
        });
    }

    private void updateShareStatus(int taskId, String status) {
        String sql = "UPDATE tasks SET share_status = ? WHERE id = ?";
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {
            prepare.setString(1, status);
            prepare.setInt(2, taskId);
            prepare.executeUpdate();
            Platform.runLater(() -> { updateDashboardKPIs(); checkNotifications(); });
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void handleExportPDF() {
        try {
            List<Task> tasks = taskDAO.getAllTasks();
            String path = System.getProperty("user.home") + "/Desktop/SmartManager_Report.pdf";
            new PDFExportService().exportTasksToPDF(tasks, path);
            showAlert(Alert.AlertType.INFORMATION, "Success", "PDF Exported to Desktop!");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadAIInsights() {
        new Thread(() -> {
            try {
                List<Task> tasks = taskDAO.getAllTasks();
                String insight = new AIService().getProductivityInsights(tasks);
                Platform.runLater(() -> {
                    if (aiSuggestionLabel != null && !aiSuggestionLabel.getText().startsWith("🔔")) {
                        aiSuggestionLabel.setText("✨ AI: " + insight);
                        aiSuggestionLabel.setStyle("-fx-text-fill: #512da8; -fx-font-weight: bold;");
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @FXML public void goToDashboard(ActionEvent event) { refreshAllData(); }
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

    private void addHoverAnimation(Node node) {
        if (node == null) return;
        node.setOnMouseEntered(e -> { node.setScaleX(1.03); node.setScaleY(1.03); node.setStyle("-fx-cursor: hand;"); });
        node.setOnMouseExited(e -> { node.setScaleX(1.0); node.setScaleY(1.0); });
    }

    private void navigate(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void startNotificationService() {
        try { notifService = new NotificationService(this); notifService.startService(); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void checkNotifications() {
        if(UserSession.getInstance() == null) return;
        String sql = "SELECT COUNT(*) FROM tasks WHERE shared_with = ? AND status != 'Completed'";
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {
            prepare.setString(1, UserSession.getInstance().getEmail());
            ResultSet rs = prepare.executeQuery();
            if (rs.next() && rs.getInt(1) > 0 && btnMyTasks != null) {
                btnMyTasks.setText("My Tasks (" + rs.getInt(1) + ")");
                btnMyTasks.setStyle("-fx-background-color: #ff9a9e; -fx-text-fill: white;");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setContentText(content); alert.show();
    }
}