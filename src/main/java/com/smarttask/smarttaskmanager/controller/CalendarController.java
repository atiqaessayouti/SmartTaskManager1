package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import com.smarttask.smarttaskmanager.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class CalendarController {

    @FXML private Label yearMonthLabel;
    @FXML private FlowPane calendarLayout;

    // --- SIDE PANEL IDs ---
    @FXML private Label selectedDateLabel;
    @FXML private VBox tasksContainer;

    private YearMonth currentYearMonth;
    private String userEmail;

    @FXML
    public void initialize() {
        // 1. Get User
        UserSession session = UserSession.getInstance();
        if (session != null) {
            this.userEmail = session.getEmail();
        }

        // 2. Init Calendar
        currentYearMonth = YearMonth.now();
        drawCalendar();

        // 3. Init Side Panel (Aujourd'hui)
        selectedDateLabel.setText("Aujourd'hui: " + LocalDate.now());
        showTasksForDate(LocalDate.now());
    }

    // --- PARTIE 1 : DESSINER LE CALENDRIER (TARGET - CIBLE) ---
    private void drawCalendar() {
        calendarLayout.getChildren().clear();
        yearMonthLabel.setText(currentYearMonth.getMonth().toString() + " " + currentYearMonth.getYear());

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        // Padding (Cases vides)
        for (int i = 1; i < dayOfWeek; i++) {
            StackPane empty = new StackPane();
            empty.setPrefSize(80, 80);
            calendarLayout.getChildren().add(empty);
        }

        // Jours du mois
        int daysInMonth = currentYearMonth.lengthOfMonth();
        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate date = currentYearMonth.atDay(i);

            StackPane dayPane = new StackPane();
            dayPane.setPrefSize(80, 80);
            dayPane.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1); -fx-cursor: hand;");

            // Click -> Show Details
            dayPane.setOnMouseClicked((MouseEvent event) -> {
                showTasksForDate(date);
            });

            // 🔥 DRAG OVER (Accept Drop)
            dayPane.setOnDragOver((DragEvent event) -> {
                if (event.getGestureSource() != dayPane && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            // 🔥 DRAG DROPPED (Update DB)
            dayPane.setOnDragDropped((DragEvent event) -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    int taskId = Integer.parseInt(db.getString());
                    updateTaskDate(taskId, date); // Changement de date
                    success = true;
                }
                event.setDropCompleted(success);
                event.consume();
            });

            // Numéro du jour (CORRIGÉ: COULEUR SOMBRE)
            Label dayNum = new Label(String.valueOf(i));
            dayNum.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-font-size: 14;");

            StackPane.setAlignment(dayNum, Pos.TOP_LEFT);
            dayNum.setTranslateX(5); dayNum.setTranslateY(5);

            // Badge (Points colorés)
            int count = getTaskCountForDate(date);
            if (count > 0) {
                Label taskBadge = new Label(String.valueOf(count));
                String color = count > 2 ? "#e74c3c" : "#2ecc71";
                taskBadge.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 10; -fx-min-width: 20; -fx-alignment: center; -fx-font-size: 11px; -fx-font-weight: bold;");

                StackPane.setAlignment(taskBadge, Pos.BOTTOM_RIGHT);
                taskBadge.setTranslateX(-5); taskBadge.setTranslateY(-5);
                dayPane.getChildren().add(taskBadge);
            }

            // Highlight Aujourd'hui
            if (date.equals(LocalDate.now())) {
                dayPane.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #3498db; -fx-border-width: 2; -fx-background-radius: 5;");
            }

            dayPane.getChildren().add(dayNum);
            calendarLayout.getChildren().add(dayPane);
        }
    }

    // --- PARTIE 2 : SIDE PANEL (SOURCE - DRAG START & ACTIONS) ---
    private void showTasksForDate(LocalDate date) {
        selectedDateLabel.setText(date.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        tasksContainer.getChildren().clear();

        String sql = "SELECT id, title, priority, status, description FROM tasks WHERE deadline = ? AND user_email = ?";

        try {
            Connection connect = DatabaseConnection.getInstance().getConnection();
            PreparedStatement prep = connect.prepareStatement(sql);
            prep.setDate(1, java.sql.Date.valueOf(date));
            prep.setString(2, userEmail);
            ResultSet rs = prep.executeQuery();

            boolean hasTasks = false;
            while(rs.next()) {
                hasTasks = true;
                int taskId = rs.getInt("id"); // ID pour Actions & Drag
                String title = rs.getString("title");
                String priority = rs.getString("priority");
                String status = rs.getString("status");
                String description = rs.getString("description");

                // --- CARD DESIGN ---
                VBox card = new VBox(8);
                card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2); -fx-border-color: #ecf0f1; -fx-border-width: 1;");

                // 🔥 DRAG DETECTED (Start Dragging)
                card.setOnDragDetected((MouseEvent event) -> {
                    Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(String.valueOf(taskId));
                    db.setContent(content);
                    event.consume();
                });

                // Title
                Label lblTitle = new Label(title);
                lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
                lblTitle.setWrapText(true);

                // Priority & Status
                HBox metaBox = new HBox(10);
                metaBox.setAlignment(Pos.CENTER_LEFT);

                Label lblPriority = new Label(priority);
                String pColor = "High".equalsIgnoreCase(priority) ? "#e74c3c" : ("Medium".equalsIgnoreCase(priority) ? "#f39c12" : "#2ecc71");
                lblPriority.setStyle("-fx-text-fill: " + pColor + "; -fx-font-weight: bold; -fx-font-size: 10px; -fx-border-color: " + pColor + "; -fx-border-radius: 3; -fx-padding: 2 6;");

                Label lblStatus = new Label("• " + status);
                lblStatus.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");

                metaBox.getChildren().addAll(lblPriority, lblStatus);

                // Description
                Label lblDesc = null;
                if (description != null && !description.isEmpty()) {
                    lblDesc = new Label(description);
                    lblDesc.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px;");
                    lblDesc.setWrapText(true);
                }

                // --- 🔥 ACTIONS (BOUTONS SUPPRIMER & TERMINER) 🔥 ---
                HBox actionsBox = new HBox(10);
                actionsBox.setAlignment(Pos.CENTER_RIGHT);
                actionsBox.setStyle("-fx-padding: 5 0 0 0; -fx-border-color: #ecf0f1; -fx-border-width: 1 0 0 0;");

                // Bouton Terminer (✔)
                Button btnDone = new Button("✔");
                btnDone.setStyle("-fx-background-color: #eafaf1; -fx-text-fill: #2ecc71; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 11px; -fx-font-weight: bold;");
                btnDone.setOnAction(e -> markTaskAsCompleted(taskId, date));

                // Bouton Supprimer (🗑)
                Button btnDelete = new Button("🗑");
                btnDelete.setStyle("-fx-background-color: #fdedec; -fx-text-fill: #e74c3c; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 11px; -fx-font-weight: bold;");
                btnDelete.setOnAction(e -> deleteTask(taskId, date));

                actionsBox.getChildren().addAll(btnDone, btnDelete);

                // Add to Card
                card.getChildren().addAll(lblTitle, metaBox);
                if(lblDesc != null) card.getChildren().add(lblDesc);
                card.getChildren().add(actionsBox);

                tasksContainer.getChildren().add(card);
            }

            if (!hasTasks) {
                Label empty = new Label("Aucune tâche pour ce jour.");
                empty.setStyle("-fx-text-fill: #bdc3c7; -fx-font-style: italic; -fx-padding: 10;");
                tasksContainer.getChildren().add(empty);
            }

        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- HELPER METHODS ---

    // 1. Update Date (Drag & Drop)
    private void updateTaskDate(int taskId, LocalDate newDate) {
        String sql = "UPDATE tasks SET deadline = ? WHERE id = ?";
        try {
            Connection connect = DatabaseConnection.getInstance().getConnection();
            PreparedStatement prep = connect.prepareStatement(sql);
            prep.setDate(1, java.sql.Date.valueOf(newDate));
            prep.setInt(2, taskId);

            int result = prep.executeUpdate();
            if (result > 0) {
                drawCalendar(); // Refresh dots
                showTasksForDate(newDate); // Show tasks in new date
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 2. Delete Task
    private void deleteTask(int taskId, LocalDate currentDate) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer ?");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer cette tâche ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            String sql = "DELETE FROM tasks WHERE id = ?";
            try {
                Connection connect = DatabaseConnection.getInstance().getConnection();
                PreparedStatement prep = connect.prepareStatement(sql);
                prep.setInt(1, taskId);
                prep.executeUpdate();

                // Refresh
                drawCalendar();
                showTasksForDate(currentDate);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // 3. Mark Done
    private void markTaskAsCompleted(int taskId, LocalDate currentDate) {
        String sql = "UPDATE tasks SET status = 'Completed' WHERE id = ?";
        try {
            Connection connect = DatabaseConnection.getInstance().getConnection();
            PreparedStatement prep = connect.prepareStatement(sql);
            prep.setInt(1, taskId);
            prep.executeUpdate();

            // Refresh
            showTasksForDate(currentDate);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 4. Count Tasks (For Dots)
    private int getTaskCountForDate(LocalDate date) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM tasks WHERE deadline = ? AND user_email = ?";
        try {
            Connection connect = DatabaseConnection.getInstance().getConnection();
            PreparedStatement prep = connect.prepareStatement(sql);
            prep.setDate(1, java.sql.Date.valueOf(date));
            prep.setString(2, userEmail);
            ResultSet rs = prep.executeQuery();
            if (rs.next()) count = rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return count;
    }

    // --- NAVIGATION ---
    @FXML public void previousMonth() { currentYearMonth = currentYearMonth.minusMonths(1); drawCalendar(); }
    @FXML public void nextMonth() { currentYearMonth = currentYearMonth.plusMonths(1); drawCalendar(); }

    @FXML public void goToDashboard(ActionEvent event) { navigate(event, "/com/smarttask/smarttaskmanager/view/dashboard.fxml"); }
    @FXML public void goToTasks(ActionEvent event) { navigate(event, "/com/smarttask/smarttaskmanager/view/tasks.fxml"); }
    @FXML public void goToProfile(ActionEvent event) { navigate(event, "/com/smarttask/smarttaskmanager/view/profile.fxml"); }
    @FXML public void goToCalendar(ActionEvent event) { navigate(event, "/com/smarttask/smarttaskmanager/view/calendar.fxml"); }
    @FXML public void handleLogout(ActionEvent event) { UserSession.getInstance().cleanUserSession(); navigate(event, "/com/smarttask/smarttaskmanager/view/login.fxml"); }

    private void navigate(ActionEvent event, String path) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(new FXMLLoader(getClass().getResource(path)).load()));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}