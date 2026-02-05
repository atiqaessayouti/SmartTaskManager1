package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.model.Task;
import com.smarttask.smarttaskmanager.service.AIService;
import com.smarttask.smarttaskmanager.service.CategoryClassifier; // ✅ IMPORT NEW
import com.smarttask.smarttaskmanager.service.MLPredictionService;
import com.smarttask.smarttaskmanager.service.NLPProcessor;
import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import com.smarttask.smarttaskmanager.util.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class AddTaskController {

    @FXML private TextField tfTitle;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<String> cbPriority;
    @FXML private DatePicker dpDeadline;
    @FXML private ComboBox<String> cbRecurrence;
    @FXML private ComboBox<Task> cbParentTask;
    @FXML private Button btnSave;

    // ✅ AJOUT: ComboBox pour la catégorie (Assure-toi de l'avoir ajouté dans SceneBuilder avec fx:id="cbCategory")
    @FXML private ComboBox<String> cbCategory;

    private boolean isEditMode = false;
    private int taskIdToEdit = -1;

    @FXML
    public void initialize() {
        loadParentTasks();

        // 1. Initialiser Priority
        if(cbPriority.getItems().isEmpty()) {
            cbPriority.getItems().addAll("High", "Medium", "Low");
        }

        // 2. ✅ Initialiser Categories (NOUVEAU)
        if(cbCategory != null && cbCategory.getItems().isEmpty()) {
            cbCategory.getItems().addAll("Work", "Education", "Health", "Finance", "Personal", "General");
        }

        // 3. Déclencheur Machine Learning (SQL)
        setupAIPrediction();

        // 4. Déclencheur NLP & Category (Langage Naturel)
        setupNLPListener();
    }

    // 🧠 NLP + CATEGORY: Analyse complète
    private void setupNLPListener() {
        tfTitle.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Focus Lost (Quand on quitte le champ)
                applySmartAnalysis();
            }
        });
    }

    private void applySmartAnalysis() {
        String text = tfTitle.getText();
        if (text == null || text.isEmpty()) return;

        // A. Détecter la date
        LocalDate detectedDate = NLPProcessor.extractDate(text);
        if (detectedDate != null) {
            dpDeadline.setValue(detectedDate);
            System.out.println("🤖 NLP: Date -> " + detectedDate);
        }

        // B. Détecter la priorité
        String detectedPriority = NLPProcessor.extractPriority(text);
        if (detectedPriority != null) {
            cbPriority.setValue(detectedPriority);
            System.out.println("🤖 NLP: Priority -> " + detectedPriority);
        }

        // C. ✅ Détecter la catégorie (NOUVEAU)
        if (cbCategory != null) {
            String suggestedCategory = CategoryClassifier.suggestCategory(text);
            cbCategory.setValue(suggestedCategory);
            System.out.println("🤖 AI Category -> " + suggestedCategory);
        }
    }

    // 🧠 ML: Prediction Automatique basée sur l'historique SQL
    private void setupAIPrediction() {
        cbPriority.setOnAction(event -> {
            String selectedPriority = cbPriority.getValue();

            if (selectedPriority != null && !isEditMode) {
                try {
                    MLPredictionService mlModel = new MLPredictionService();
                    mlModel.trainModel(null); // Mode SQL direct
                    int daysNeeded = mlModel.predictDaysNeeded(selectedPriority);

                    // Si le NLP n'a pas mis de date, on utilise le ML
                    if (dpDeadline.getValue() == null) {
                        dpDeadline.setValue(LocalDate.now().plusDays(daysNeeded));
                        System.out.println("🤖 ML (SQL) a suggéré : + " + daysNeeded + " jours");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 👇 SAUVEGARDE EN BASE DE DONNÉES (MISE À JOUR)
    @FXML
    public void saveTask(ActionEvent event) {
        String title = tfTitle.getText();
        String description = taDescription.getText();
        String priority = cbPriority.getValue();
        LocalDate deadline = dpDeadline.getValue();
        String recurrence = cbRecurrence.getValue() != null ? cbRecurrence.getValue() : "NONE";

        // ✅ Récupérer la catégorie
        String category = (cbCategory != null && cbCategory.getValue() != null) ? cbCategory.getValue() : "General";

        if (title.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Title is required!");
            return;
        }

        String sql;
        // ⚠️ NOTE: J'ai ajouté 'category' dans la requête SQL. Assure-toi que la colonne existe dans MySQL !
        if (isEditMode) {
            sql = "UPDATE tasks SET title=?, description=?, priority=?, deadline=?, recurrence_type=?, category=? WHERE id=?";
        } else {
            sql = "INSERT INTO tasks (title, description, priority, status, deadline, recurrence_type, category, user_email) VALUES (?, ?, ?, 'In Progress', ?, ?, ?, ?)";
        }

        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setString(1, title);
            prepare.setString(2, description);
            prepare.setString(3, priority != null ? priority : "Medium");

            if (deadline != null) prepare.setDate(4, java.sql.Date.valueOf(deadline));
            else prepare.setNull(4, java.sql.Types.DATE);

            prepare.setString(5, recurrence);

            // ✅ Ajout du paramètre Category
            prepare.setString(6, category);

            if (isEditMode) {
                prepare.setInt(7, taskIdToEdit);
            } else {
                prepare.setString(7, UserSession.getInstance().getEmail());
            }

            if (prepare.executeUpdate() > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", isEditMode ? "Task updated!" : "Task added!");
                ((Stage) tfTitle.getScene().getWindow()).close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database error: " + e.getMessage());
        }
    }

    // 👇 RESTE DU CODE STANDARD
    public void setTaskData(Task task) {
        this.isEditMode = true;
        this.taskIdToEdit = task.getId();

        tfTitle.setText(task.getTitle());
        taDescription.setText(task.getDescription());
        cbPriority.setValue(task.getPriority());
        dpDeadline.setValue(task.getDeadline());

        // ✅ Remplir la catégorie en mode Edit
        if (cbCategory != null && task.getCategory() != null) {
            cbCategory.setValue(task.getCategory());
        }

        if(btnSave != null) btnSave.setText("Mettre à jour");
    }

    private void loadParentTasks() {
        ObservableList<Task> tasks = FXCollections.observableArrayList();
        String sql = "SELECT id, title FROM tasks WHERE user_email = ?";
        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {
            prepare.setString(1, UserSession.getInstance().getEmail());
            ResultSet rs = prepare.executeQuery();
            while (rs.next()) {
                tasks.add(new Task(rs.getInt("id"), rs.getString("title"), null, null, null, null, null, null));
            }
            cbParentTask.setItems(tasks);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void handleAISuggestion() {
        applySmartAnalysis();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}