package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.DAO.TaskDAO; // ✅
import com.smarttask.smarttaskmanager.model.Task;
import com.smarttask.smarttaskmanager.service.RecurrenceService;
import com.smarttask.smarttaskmanager.util.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TasksController {

    @FXML private FlowPane tasksContainer;
    @FXML private TextField searchField;

    //
    private TaskDAO taskDAO = new TaskDAO();
    private List<Task> allTasks = new ArrayList<>();

    @FXML
    public void initialize() {
        loadTasks();
        searchField.textProperty().addListener((obs, old, newVal) -> displayTasks(newVal));
    }

    private void loadTasks() {
        allTasks.clear();
        //
        allTasks = taskDAO.getAllTasks();
        displayTasks("");
    }

    private void displayTasks(String filter) {
        tasksContainer.getChildren().clear();
        for (Task task : allTasks) {
            if (task.getTitle().toLowerCase().contains(filter.toLowerCase())) {
                tasksContainer.getChildren().add(createTaskCard(task));
            }
        }
    }

    private VBox createTaskCard(Task task) {
        VBox card = new VBox(12);
        card.setPrefSize(310, 260); //
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 6); -fx-padding: 20;");

        //
        if (task.getParentId() != null) {
            card.setStyle(card.getStyle() + "-fx-border-color: #3498db; -fx-border-width: 0 0 0 5;");
        }

        Label title = new Label(task.getTitle());
        title.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: #1a252f;");
        title.setWrapText(true);

        Label category = new Label(task.getCategory());
        category.setStyle("-fx-background-color: #ebedef; -fx-text-fill: #2c3e50; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        //
        HBox infoBox = new HBox(15);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        HBox prioBox = new HBox(6);
        Circle dot = new Circle(5);
        if ("High".equalsIgnoreCase(task.getPriority())) dot.setFill(Color.web("#c0392b"));
        else if ("Medium".equalsIgnoreCase(task.getPriority())) dot.setFill(Color.web("#d35400"));
        else dot.setFill(Color.web("#1e8449"));

        Label pLbl = new Label(task.getPriority());
        prioBox.getChildren().addAll(dot, pLbl);

        Label sLbl = new Label("• " + task.getStatus());
        sLbl.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

        infoBox.getChildren().addAll(prioBox, sLbl);

        //
        HBox timerBox = new HBox(10);
        timerBox.setAlignment(Pos.CENTER_LEFT);

        Label lblTime = new Label(formatTime(task.getTimeSpent()));
        lblTime.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px; -fx-font-family: 'Consolas', monospace;");

        Button btnTimer = new Button();
        if (task.isTimerRunning()) {
            btnTimer.setText("⏸ Stop");
            btnTimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand;");
            lblTime.setText("Running...");
            lblTime.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else {
            btnTimer.setText("▶ Start");
            btnTimer.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand;");
        }

        btnTimer.setOnAction(e -> {
            if (task.isTimerRunning()) {
                taskDAO.stopTimer(task.getId());
            } else {
                taskDAO.startTimer(task.getId());
            }
            loadTasks(); //
        });

        timerBox.getChildren().addAll(btnTimer, lblTime);
        // ----------------------------------------------------

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.BOTTOM_RIGHT);
        VBox.setVgrow(actions, Priority.ALWAYS);

        Button bEdit = new Button("Edit");
        bEdit.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand;");
        bEdit.setOnAction(e -> handleEditTask(task));

        Button bDone = new Button("Done");
        bDone.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand;");
        bDone.setOnAction(e -> updateStatus(task.getId(), "Completed"));

        Button bDel = new Button("Delete");
        bDel.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand;");
        bDel.setOnAction(e -> deleteTask(task.getId()));

        actions.getChildren().addAll(bEdit, bDone, bDel);


        card.getChildren().addAll(title, category, infoBox, timerBox, actions);
        return card;
    }

    //
    private boolean hasIncompleteSubtasks(int parentId) {
        for(Task t : allTasks) {
            if(t.getParentId() != null && t.getParentId() == parentId && !"Completed".equals(t.getStatus())) {
                return true;
            }
        }
        return false;
    }

    private void updateStatus(int id, String status) {
        if ("Completed".equals(status)) {
            if (hasIncompleteSubtasks(id)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Action Bloquée 🚫");
                alert.setHeaderText("Unable to complete !");
                alert.setContentText("\n" +
                        "This task contains unfinished subtasks.\nPlease complete the subtasks first.");
                alert.show();
                return;
            }
        }

        //
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        try (java.sql.Connection conn = com.smarttask.smarttaskmanager.util.DatabaseConnection.getInstance().getConnection();
             java.sql.PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, status);
            pst.setInt(2, id);
            pst.executeUpdate();

            if ("Completed".equals(status)) {
                Task t = allTasks.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
                if(t != null) RecurrenceService.checkAndCreateNextTask(t);
            }
            loadTasks();
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
    }

    private void deleteTask(int id) {
        taskDAO.deleteTask(id);
        loadTasks();
    }

    private void handleEditTask(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smarttask/smarttaskmanager/view/add_task.fxml"));
            Parent root = loader.load();
            AddTaskController controller = loader.getController();
            controller.setTaskData(task);
            Stage stage = new Stage();
            stage.setTitle("✏️ Modifier Tâche");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setOnHiding(e -> loadTasks());
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleAddTask() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smarttask/smarttaskmanager/view/add_task.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("➕ Add Task");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setOnHiding(e -> loadTasks());
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/smarttask/smarttaskmanager/view/dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // د
    private String formatTime(long totalSeconds) {
        if (totalSeconds == 0) return "0m";
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        if (hours > 0) return String.format("%dh %02dm", hours, minutes);
        return String.format("%dm", minutes);
    }
}