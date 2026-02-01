package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.model.Task;
import com.smarttask.smarttaskmanager.util.DatabaseConnection;
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
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TasksController {

    @FXML private FlowPane tasksContainer;
    @FXML private TextField searchField;
    @FXML private BorderPane tasksRoot;

    private List<Task> allTasks = new ArrayList<>();

    @FXML
    public void initialize() {
        loadTasks();
        searchField.textProperty().addListener((obs, old, newVal) -> displayTasks(newVal));
    }

    private void loadTasks() {
        allTasks.clear();
        String sql = "SELECT * FROM tasks WHERE user_email = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, UserSession.getInstance().getEmail());
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                allTasks.add(new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("priority"),
                        rs.getString("status"),
                        (rs.getDate("deadline") != null) ? rs.getDate("deadline").toLocalDate() : null,
                        rs.getString("category"),
                        rs.getString("shared_with")
                ));
            }
            displayTasks("");
        } catch (SQLException e) { e.printStackTrace(); }
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
        card.setPrefSize(310, 240);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 6); -fx-padding: 20;");

        Label title = new Label(task.getTitle());
        title.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: #1a252f;");
        title.setWrapText(true);

        Label category = new Label(task.getCategory());
        category.setStyle("-fx-background-color: #ebedef; -fx-text-fill: #2c3e50; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        HBox infoBox = new HBox(15);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        HBox prioBox = new HBox(6);
        Circle dot = new Circle(5);
        if (task.getPriority().equalsIgnoreCase("High")) dot.setFill(Color.web("#c0392b"));
        else if (task.getPriority().equalsIgnoreCase("Medium")) dot.setFill(Color.web("#d35400"));
        else dot.setFill(Color.web("#1e8449"));
        Label pLbl = new Label(task.getPriority());
        prioBox.getChildren().addAll(dot, pLbl);

        Label sLbl = new Label("• " + task.getStatus());
        sLbl.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

        infoBox.getChildren().addAll(prioBox, sLbl);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.BOTTOM_RIGHT);
        VBox.setVgrow(actions, Priority.ALWAYS);

        // ✅ Bouton Edit (Modifier)
        Button bEdit = new Button("Edit");
        bEdit.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand;");
        bEdit.setOnAction(e -> handleEditTask(task));

        // ✅ Bouton Done (Terminer)
        Button bDone = new Button("Done");
        bDone.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand;");
        bDone.setOnAction(e -> updateStatus(task.getId(), "Completed"));

        // ✅ Bouton Delete (Supprimer)
        Button bDel = new Button("Delete");
        bDel.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand;");
        bDel.setOnAction(e -> deleteTask(task.getId()));

        actions.getChildren().addAll(bEdit, bDone, bDel);
        card.getChildren().addAll(title, category, infoBox, actions);
        return card;
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

    // ✅ MODIFIÉ : Vérifie la récurrence après avoir mis "Completed"
    private void updateStatus(int id, String status) {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, status);
            pst.setInt(2, id);
            pst.executeUpdate();

            // 👇 LA MAGIE : Si c'est terminé, on vérifie si on doit la recréer pour demain
            if ("Completed".equals(status)) {
                handleRecurrence(id);
            }

            loadTasks();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ✅ NOUVEAU : Logique de Récurrence Automatique
    private void handleRecurrence(int taskId) {
        String selectSql = "SELECT * FROM tasks WHERE id = ?";
        String insertSql = "INSERT INTO tasks (title, description, priority, status, deadline, category, user_email, recurrence_type) VALUES (?, ?, ?, 'In Progress', ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstSelect = conn.prepareStatement(selectSql)) {

            pstSelect.setInt(1, taskId);
            ResultSet rs = pstSelect.executeQuery();

            if (rs.next()) {
                String recurrence = rs.getString("recurrence_type");
                java.sql.Date sqlDate = rs.getDate("deadline");

                // Si pas de récurrence ou pas de date, on arrête
                if (recurrence == null || recurrence.equals("NONE") || sqlDate == null) {
                    return;
                }

                java.time.LocalDate oldDeadline = sqlDate.toLocalDate();
                java.time.LocalDate newDeadline = oldDeadline;

                // Calcul de la nouvelle date
                switch (recurrence) {
                    case "DAILY":   newDeadline = oldDeadline.plusDays(1); break;
                    case "WEEKLY":  newDeadline = oldDeadline.plusWeeks(1); break;
                    case "MONTHLY": newDeadline = oldDeadline.plusMonths(1); break;
                }

                // Insertion de la nouvelle tâche (Copie)
                try (PreparedStatement pstInsert = conn.prepareStatement(insertSql)) {
                    pstInsert.setString(1, rs.getString("title"));
                    pstInsert.setString(2, rs.getString("description"));
                    pstInsert.setString(3, rs.getString("priority"));
                    pstInsert.setDate(4, java.sql.Date.valueOf(newDeadline));
                    pstInsert.setString(5, rs.getString("category"));
                    pstInsert.setString(6, rs.getString("user_email"));
                    pstInsert.setString(7, recurrence);

                    pstInsert.executeUpdate();
                    System.out.println("✅ Tâche récurrente créée pour le : " + newDeadline);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void deleteTask(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            loadTasks();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/smarttask/smarttaskmanager/view/dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}