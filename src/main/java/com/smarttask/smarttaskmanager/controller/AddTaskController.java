package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.model.Task;
import com.smarttask.smarttaskmanager.service.AIService;
import com.smarttask.smarttaskmanager.service.CategoryClassifier;
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
    @FXML private ComboBox<String> cbCategory;

    private boolean isEditMode = false;
    private int taskIdToEdit = -1;

    @FXML
    public void initialize() {

        loadParentTasks();

        if (cbPriority.getItems().isEmpty())
            cbPriority.getItems().addAll("High", "Medium", "Low");

        if (cbCategory != null && cbCategory.getItems().isEmpty())
            cbCategory.getItems().addAll("Work", "Education", "Health", "Finance", "Personal", "General");

        if (cbRecurrence != null && cbRecurrence.getItems().isEmpty())
            cbRecurrence.getItems().addAll("NONE", "Daily", "Weekly", "Monthly", "Yearly");

        setupAIPrediction();

        taDescription.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !isEditMode) {
                fillTaskInfoFromDescription();
            }
        });
    }

    private void setupNLPListener() {
        tfTitle.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !isEditMode) {
                applySmartAnalysis();
            }
        });
    }

    private void applySmartAnalysis() {
        String text = tfTitle.getText();
        if (text == null || text.isEmpty()) return;

        LocalDate detectedDate = NLPProcessor.extractDate(text);
        if (detectedDate != null && dpDeadline.getValue() == null)
            dpDeadline.setValue(detectedDate);

        String detectedPriority = NLPProcessor.extractPriority(text);
        if (detectedPriority != null && cbPriority.getValue() == null)
            cbPriority.setValue(detectedPriority);

        if (cbCategory != null && cbCategory.getValue() == null) {
            String suggestedCategory = CategoryClassifier.suggestCategory(text);
            cbCategory.setValue(suggestedCategory);
        }
    }

    private void setupAIPrediction() {
        cbPriority.setOnAction(event -> {
            String selectedPriority = cbPriority.getValue();

            if (selectedPriority != null && !isEditMode && dpDeadline.getValue() == null) {
                try {
                    MLPredictionService mlModel = new MLPredictionService();
                    mlModel.trainModel(null);
                    int daysNeeded = mlModel.predictDaysNeeded(selectedPriority);
                    dpDeadline.setValue(LocalDate.now().plusDays(daysNeeded));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    public void saveTask(ActionEvent event) {

        String title = tfTitle.getText();
        String description = taDescription.getText();
        String priority = cbPriority.getValue();
        LocalDate deadline = dpDeadline.getValue();
        String recurrence = cbRecurrence.getValue() != null ? cbRecurrence.getValue() : "NONE";
        String category = (cbCategory != null && cbCategory.getValue() != null) ? cbCategory.getValue() : "General";

        if (title == null || title.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Title is required!");
            return;
        }

        String sql = isEditMode
                ? "UPDATE tasks SET title=?, description=?, priority=?, deadline=?, recurrence_type=?, category=? WHERE id=?"
                : "INSERT INTO tasks (title, description, priority, status, deadline, recurrence_type, category, user_email) VALUES (?, ?, ?, 'In Progress', ?, ?, ?, ?)";

        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setString(1, title);
            prepare.setString(2, description);
            prepare.setString(3, priority != null ? priority : "Medium");

            if (deadline != null)
                prepare.setDate(4, java.sql.Date.valueOf(deadline));
            else
                prepare.setNull(4, java.sql.Types.DATE);

            prepare.setString(5, recurrence);
            prepare.setString(6, category);

            if (isEditMode)
                prepare.setInt(7, taskIdToEdit);
            else
                prepare.setString(7, UserSession.getInstance().getEmail());

            if (prepare.executeUpdate() > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        isEditMode ? "Task updated!" : "Task added!");
                ((Stage) tfTitle.getScene().getWindow()).close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database error: " + e.getMessage());
        }
    }

    public void setTaskData(Task task) {
        this.isEditMode = true;
        this.taskIdToEdit = task.getId();
        tfTitle.setText(task.getTitle());
        taDescription.setText(task.getDescription());
        cbPriority.setValue(task.getPriority());
        dpDeadline.setValue(task.getDeadline());

        if (cbCategory != null && task.getCategory() != null)
            cbCategory.setValue(task.getCategory());

        if (btnSave != null)
            btnSave.setText("Mettre à jour");
    }

    private void loadParentTasks() {
        ObservableList<Task> tasks = FXCollections.observableArrayList();

        String sql = "SELECT id, title FROM tasks WHERE user_email = ?";

        try (Connection connect = DatabaseConnection.getInstance().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setString(1, UserSession.getInstance().getEmail());
            ResultSet rs = prepare.executeQuery();

            while (rs.next())
                tasks.add(new Task(rs.getInt("id"), rs.getString("title"),
                        null, null, null, null, null, null));

            cbParentTask.setItems(tasks);

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    // 🔥 AI brain (Corrected Logic for Recurrence + Date)
    private void fillTaskInfoFromDescription() {

        String descText = taDescription.getText();
        if (descText == null || descText.trim().isEmpty()) return;

        // متغير باش نعرفو واش لقينا تكرار ولا لا
        boolean isRecurringFound = false;

        // 1. Recurrence (التكرار)
        if (cbRecurrence != null) {
            String detectedRecurrence = AIService.suggestRecurrence(descText);

            // ✅ التصحيح: مابقاش كيهمنا واش كانت NONE ولا خاوية، إلا لقا Daily يحطها
            if (!"NONE".equals(detectedRecurrence)) {
                cbRecurrence.setValue(detectedRecurrence);
                isRecurringFound = true; // ✅ قيدنا بلي راها كتعاود
            }
        }

        // 2. Priority (الأولوية)
        if (cbPriority != null) {
            String detectedPriority = AIService.suggestPriority(descText);
            // ✅ التصحيح: نفس الشيء، إلا لقا High يحطها
            if (!"Medium".equals(detectedPriority)) {
                cbPriority.setValue(detectedPriority);
            }
        }

        // 3. Category (التصنيف)
        if (cbCategory != null) {
            String detectedCategory = AIService.suggestCategory(descText);
            if (!"General".equals(detectedCategory)) {
                cbCategory.setValue(detectedCategory);
            }
        }

        // 4. Deadline (التاريخ)

        // 🚨 التصحيح: حيدنا الشرط if (dpDeadline.getValue() == null)
        // السبب: حيت فاش كتبدّل Priority، الموديل ML كيعمّر التاريخ، وهادشي كان كيبلوكي الـ NLP
        // دابا: الـ NLP (النص) عندو الكلمة الأخيرة ديما.

        LocalDate detectedDate = AIService.parseDate(descText);

        if (detectedDate != null) {
            // إلا لقا تاريخ صريح (demain, next week...) يحطو
            dpDeadline.setValue(detectedDate);
        }
        else if (isRecurringFound) {
            // ✅ إلا كانت task كتعاود (every day)، خاصها تبدا اليوم بزز
            dpDeadline.setValue(LocalDate.now());
        }

        // 5. Title (العنوان)
        if (tfTitle.getText() == null || tfTitle.getText().trim().isEmpty()) {
            String cleanTitle = AIService.extractCleanTitle(descText);
            tfTitle.setText(cleanTitle);
        }
    }
}
