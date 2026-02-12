package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.DAO.TaskDAO;
import com.smarttask.smarttaskmanager.model.Task;
import com.smarttask.smarttaskmanager.service.AIService;
import com.smarttask.smarttaskmanager.util.UserSession; // ✅ Required for Logout and User Filtering
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatbotController {

    @FXML private TextArea chatArea;
    @FXML private TextField chatInput;

    private AIService aiService = new AIService();
    private TaskDAO taskDAO = new TaskDAO();

    @FXML
    public void initialize() {
        chatArea.appendText("🤖 AI: Hello! I am your intelligent assistant. How can I help you manage your tasks today?\n\n");
    }

    // =========================================================
    // 🧠 1. CHATBOT LOGIC (Artificial Intelligence)
    // =========================================================
    @FXML
    private void handleSendMessage() {
        String userMsg = chatInput.getText();
        if (userMsg == null || userMsg.trim().isEmpty()) return;

        // 1. Display user message
        chatArea.appendText("👤 You: " + userMsg + "\n");
        chatInput.clear();

        // 2. Process response in a separate Thread to keep UI responsive
        new Thread(() -> {
            try {
                // ✅ Optimization: Fetch tasks for the current user only (Security)
                String currentUserEmail = UserSession.getInstance().getEmail();
                List<Task> tasks;

                // Make sure your TaskDAO has a getTasksByUser method; otherwise, use getAllTasks temporarily
                // tasks = taskDAO.getTasksByUser(currentUserEmail);
                tasks = taskDAO.getAllTasks();

                String response = aiService.getChatResponse(userMsg, tasks);

                // Update the UI on the main application thread
                Platform.runLater(() -> {
                    chatArea.appendText("🤖 AI: " + response + "\n\n");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> chatArea.appendText("🤖 AI: Sorry, an error occurred while processing your request.\n"));
            }
        }).start();
    }

    // =========================================================
    // 🚀 2. NAVIGATION LOGIC (SIDEBAR)
    // =========================================================

    @FXML
    public void goToDashboard(ActionEvent event) {
        navigate(event, "/com/smarttask/smarttaskmanager/view/dashboard.fxml");
    }

    @FXML
    public void goToTasks(ActionEvent event) {
        navigate(event, "/com/smarttask/smarttaskmanager/view/tasks.fxml");
    }

    @FXML
    public void goToCalendar(ActionEvent event) {
        navigate(event, "/com/smarttask/smarttaskmanager/view/calendar_view.fxml");
    }

    @FXML
    public void goToProfile(ActionEvent event) {
        navigate(event, "/com/smarttask/smarttaskmanager/view/profile.fxml");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        // ✅ Clear session data before exiting
        if (UserSession.getInstance() != null) {
            UserSession.getInstance().cleanUserSession();
        }
        navigate(event, "/com/smarttask/smarttaskmanager/view/login.fxml");
    }

    /**
     * Helper method to handle scene switching (Refactoring)
     */
    private void navigate(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error: Could not load the file " + fxmlPath);
        }
    }
}