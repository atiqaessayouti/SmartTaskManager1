package com.smarttask.smarttaskmanager.controller;

// 👇 HADU HUMA LI KANO NAQSIN (Imports)
import com.smarttask.smarttaskmanager.DAO.TaskDAO;
import com.smarttask.smarttaskmanager.model.Task;
import com.smarttask.smarttaskmanager.service.AIService;
import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import com.smarttask.smarttaskmanager.util.UserSession;

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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List; // 👈 Hada darouri bach t-khdem List<Task>

public class DashboardController {

    @FXML private Label lblEnCours;
    @FXML private Label lblTerminees;
    @FXML private Label lblEnRetard;
    @FXML private Label aiSuggestionLabel;
    @FXML private PieChart pieChartPriority;
    @FXML private BarChart<String, Number> productivityChart;
    @FXML private Button btnMyTasks;

    @FXML
    public void initialize() {
        updateDashboardKPIs();
        loadPieChartData();
        loadPerformanceTrends();
        checkNotifications();
        loadAIInsights(); // ✅ Dabba ghadi t-khdem bla error
    }

    // --- 🤖 AI INSIGHTS ---
    private void loadAIInsights() {
        // 1. Thread Jdid bach l-interface mat-bloquach
        new Thread(() -> {
            try {
                // 2. Jbed Tasks
                TaskDAO taskDAO = new TaskDAO();
                List<Task> tasks = taskDAO.getAllTasks();

                // 3. Hder m3a Gemini
                AIService aiService = new AIService();
                String insight = aiService.getProductivityInsights(tasks);

                // 4. Affichi Jawab
                javafx.application.Platform.runLater(() -> {
                    if (aiSuggestionLabel != null) {
                        aiSuggestionLabel.setText("💡 AI Tip: " + insight);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // --- 📊 ANALYTICS & TRENDS ---
    private void loadPerformanceTrends() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tasks Completed");

        String sql = "SELECT deadline, COUNT(*) as total FROM tasks WHERE status = 'Completed' GROUP BY deadline ORDER BY deadline LIMIT 7";

        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            ResultSet result = prepare.executeQuery();
            while (result.next()) {
                series.getData().add(new XYChart.Data<>(result.getString("deadline"), result.getInt("total")));
            }
            if (productivityChart != null) productivityChart.getData().add(series);

        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- 🔔 NOTIFICATIONS ---
    private void checkNotifications() {
        String currentUser = UserSession.getInstance().getEmail();
        String sql = "SELECT COUNT(*) FROM tasks WHERE shared_with = ? AND status != 'Completed'";

        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {
            prepare.setString(1, currentUser);
            ResultSet rs = prepare.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                if (btnMyTasks != null) {
                    btnMyTasks.setText("My Tasks (" + rs.getInt(1) + ")");
                    btnMyTasks.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- 🚀 NAVIGATION ---
    @FXML public void goToDashboard(ActionEvent event) { System.out.println("Déjà sur le Dashboard"); }

    @FXML public void goToCalendar(ActionEvent event) {
        navigate(event, "/com/smarttask/smarttaskmanager/view/calendar_view.fxml", "Calendrier");
    }

    @FXML public void goToTasks(ActionEvent event) {
        navigate(event, "/com/smarttask/smarttaskmanager/view/tasks.fxml", "Mes Tâches");
    }

    @FXML public void goToProfile(ActionEvent event) {
        navigate(event, "/com/smarttask/smarttaskmanager/view/profile.fxml", "Profil");
    }

    @FXML public void handleLogout(ActionEvent event) {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { System.out.println("Erreur Navigation: " + fxmlPath); }
    }

    private void updateDashboardKPIs() {
        Connection connect = DatabaseConnection.getInstance().getConnection();
        try {
            Statement stmt = connect.createStatement();
            ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) FROM tasks WHERE status = 'In Progress'");
            if(rs1.next()) lblEnCours.setText(String.valueOf(rs1.getInt(1)));
            ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) FROM tasks WHERE status = 'Completed'");
            if(rs2.next()) lblTerminees.setText(String.valueOf(rs2.getInt(1)));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadPieChartData() {
        Connection connect = DatabaseConnection.getInstance().getConnection();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        try {
            ResultSet rs = connect.createStatement().executeQuery("SELECT priority, COUNT(*) as count FROM tasks GROUP BY priority");
            while (rs.next()) {
                pieData.add(new PieChart.Data(rs.getString("priority") + " (" + rs.getInt("count") + ")", rs.getInt("count")));
            }
            pieChartPriority.setData(pieData);
        } catch (Exception e) { e.printStackTrace(); }
    }
}