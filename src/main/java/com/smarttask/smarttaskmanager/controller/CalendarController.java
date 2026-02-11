package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.model.Task;
import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import com.smarttask.smarttaskmanager.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class CalendarController {

    @FXML private Label yearMonthLabel;
    @FXML private GridPane calendarGrid;
    @FXML private VBox taskListContainer;

    private YearMonth currentYearMonth;
    private Task draggedTask = null;

    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();
        drawCalendar();
        loadSideTasks();
    }

    private void drawCalendar() {
        calendarGrid.getChildren().clear();
        yearMonthLabel.setText(currentYearMonth.getMonth().name() + " " + currentYearMonth.getYear());

        LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();
        int daysInMonth = currentYearMonth.lengthOfMonth();

        int column = dayOfWeek - 1;
        int row = 0;

        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate date = currentYearMonth.atDay(i);

            VBox dayBox = new VBox();
            dayBox.setMinHeight(100);
            dayBox.setPadding(new Insets(5));
            dayBox.setStyle("-fx-border-color: #ecf0f1; -fx-background-color: white;");

            Label dayLabel = new Label(String.valueOf(i));
            dayLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

            if (date.equals(LocalDate.now())) {
                dayLabel.setTextFill(Color.RED);
                dayBox.setStyle("-fx-background-color: #fdf2f0; -fx-border-color: #ecf0f1;");
            }
            dayBox.getChildren().add(dayLabel);

            List<Task> tasks = getTasksForDate(date);
            for (Task task : tasks) {
                dayBox.getChildren().add(createTaskLabel(task));
            }

            setupDropZone(dayBox, date);
            calendarGrid.add(dayBox, column, row);

            column++;
            if (column > 6) {
                column = 0;
                row++;
            }
        }
    }

    private Label createTaskLabel(Task task) {
        Label lbl = new Label(task.getTitle());
        lbl.setMaxWidth(Double.MAX_VALUE);
        String color = "High".equals(task.getPriority()) ? "#e74c3c" : "#3498db";
        lbl.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 3; -fx-background-radius: 3; -fx-cursor: hand; -fx-font-size: 11px; -fx-margin: 2;");

        lbl.setOnDragDetected(event -> {
            draggedTask = task;
            Dragboard db = lbl.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(task.getTitle());
            db.setContent(content);
            event.consume();
        });

        lbl.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) openTaskDetails(task);
        });

        return lbl;
    }

    private void setupDropZone(VBox dayBox, LocalDate targetDate) {
        dayBox.setOnDragOver(event -> {
            if (event.getGestureSource() != dayBox && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                dayBox.setStyle("-fx-background-color: #eafaf1;");
            }
            event.consume();
        });

        dayBox.setOnDragExited(event -> {
            dayBox.setStyle("-fx-background-color: white;");
            event.consume();
        });

        dayBox.setOnDragDropped(event -> {
            boolean success = false;
            if (draggedTask != null) {
                updateTaskDate(draggedTask.getId(), targetDate);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
            drawCalendar();
            loadSideTasks();
        });
    }

    private void loadSideTasks() {
        taskListContainer.getChildren().clear();
        String sql = "SELECT * FROM tasks WHERE (user_email = ? OR shared_with = ?) AND deadline IS NULL";

        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setString(1, UserSession.getInstance().getEmail());
            prepare.setString(2, UserSession.getInstance().getEmail());

            ResultSet rs = prepare.executeQuery();
            while (rs.next()) {
                //
                Task t = new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("priority"),
                        rs.getString("status"),
                        null,
                        rs.getString("category"),
                        rs.getString("shared_with"),
                        rs.getString("recurrence_type"),
                        rs.getObject("parent_id") != null ? rs.getInt("parent_id") : null,
                        rs.getLong("time_spent"),      // 11
                        rs.getTimestamp("timer_start") // 12
                );

                Label l = createTaskLabel(t);
                l.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 5; -fx-background-radius: 3; -fx-cursor: hand; -fx-margin: 2;");
                taskListContainer.getChildren().add(l);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private List<Task> getTasksForDate(LocalDate date) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE deadline = ? AND (user_email = ? OR shared_with = ?)";
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {
            prepare.setDate(1, java.sql.Date.valueOf(date));
            prepare.setString(2, UserSession.getInstance().getEmail());
            prepare.setString(3, UserSession.getInstance().getEmail());
            ResultSet rs = prepare.executeQuery();
            while (rs.next()) {
                // ✅
                tasks.add(new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("priority"),
                        rs.getString("status"),
                        rs.getDate("deadline").toLocalDate(),
                        rs.getString("category"),
                        rs.getString("shared_with"),
                        rs.getString("recurrence_type"),
                        rs.getObject("parent_id") != null ? rs.getInt("parent_id") : null,
                        rs.getLong("time_spent"),      // 11
                        rs.getTimestamp("timer_start") // 12
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return tasks;
    }

    private void updateTaskDate(int id, LocalDate date) {
        String sql = "UPDATE tasks SET deadline = ? WHERE id = ?";
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {
            prepare.setDate(1, java.sql.Date.valueOf(date));
            prepare.setInt(2, id);
            prepare.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void openTaskDetails(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smarttask/smarttaskmanager/view/task_details.fxml"));
            Parent root = loader.load();
            TaskDetailsController c = loader.getController();
            c.setTaskData(task);
            Stage s = new Stage(); s.setTitle("Détails"); s.setScene(new Scene(root)); s.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML public void previousMonth() { currentYearMonth = currentYearMonth.minusMonths(1); drawCalendar(); }
    @FXML public void nextMonth() { currentYearMonth = currentYearMonth.plusMonths(1); drawCalendar(); }

    @FXML public void handleAddTask(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smarttask/smarttaskmanager/view/add_task.fxml"));
            Stage s = new Stage(); s.setScene(new Scene(loader.load())); s.show();
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML public void goToDashboard(ActionEvent e) { navigate(e, "/com/smarttask/smarttaskmanager/view/dashboard.fxml"); }
    @FXML public void goToTasks(ActionEvent e) { navigate(e, "/com/smarttask/smarttaskmanager/view/tasks.fxml"); }
    @FXML public void goToCalendar(ActionEvent e) {}
    @FXML public void goToProfile(ActionEvent e) { navigate(e, "/com/smarttask/smarttaskmanager/view/profile.fxml"); }
    @FXML public void handleLogout(ActionEvent e) { navigate(e, "/com/smarttask/smarttaskmanager/view/login.fxml"); }

    private void navigate(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}