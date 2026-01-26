package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DashboardController {

    // These IDs must match your FXML file exactly
    @FXML private Label lblEnCours;
    @FXML private Label lblTerminees;
    @FXML private Label lblEnRetard;

    // The PieChart defined in FXML
    @FXML private PieChart pieChartPriority;

    @FXML
    public void initialize() {
        // Load data when the page opens
        updateDashboardKPIs();
        loadPieChartData();
    }

    // Method to populate the PieChart with Priority data
    private void loadPieChartData() {
        Connection connect = DatabaseConnection.getInstance().getConnection();

        // Query to count tasks grouped by priority
        String sql = "SELECT priority, COUNT(*) as count FROM tasks GROUP BY priority";

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        try {
            Statement stmt = connect.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String priority = rs.getString("priority");
                int count = rs.getInt("count");

                // Add data to the chart: "PriorityName (Count)"
                pieData.add(new PieChart.Data(priority + " (" + count + ")", count));
            }

            pieChartPriority.setData(pieData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to update the numbers (KPIs) at the top
    private void updateDashboardKPIs() {
        Connection connect = DatabaseConnection.getInstance().getConnection();

        String sqlInProgress = "SELECT COUNT(*) FROM tasks WHERE status = 'In Progress'";
        String sqlCompleted = "SELECT COUNT(*) FROM tasks WHERE status = 'Completed'";
        String sqlOverdue = "SELECT COUNT(*) FROM tasks WHERE status = 'Overdue'";

        try {
            Statement stmt = connect.createStatement();

            // 1. In Progress
            ResultSet rs1 = stmt.executeQuery(sqlInProgress);
            if(rs1.next()) lblEnCours.setText(String.valueOf(rs1.getInt(1)));

            // 2. Completed
            ResultSet rs2 = stmt.executeQuery(sqlCompleted);
            if(rs2.next()) lblTerminees.setText(String.valueOf(rs2.getInt(1)));

            // 3. Overdue
            ResultSet rs3 = stmt.executeQuery(sqlOverdue);
            if(rs3.next()) lblEnRetard.setText(String.valueOf(rs3.getInt(1)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- NAVIGATION METHODS ---

    // 1. Tasks
    @FXML
    public void goToTasks(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smarttask/smarttaskmanager/view/tasks.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading tasks.fxml");
        }
    }

    // 2. Profile
    @FXML
    public void goToProfile(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smarttask/smarttaskmanager/view/profile.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }

    // 3. Logout
    @FXML
    public void handleLogout(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smarttask/smarttaskmanager/view/login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.centerOnScreen();
        stage.show();
    }

    // 4. Calendar (ZEDNAHA HNA ✅)
    @FXML
    public void goToCalendar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smarttask/smarttaskmanager/view/calendar.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 5. Dashboard (ZEDNAHA HNA ✅ - Darouriya bach maycrashich l'bouton)
    @FXML
    public void goToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smarttask/smarttaskmanager/view/dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}