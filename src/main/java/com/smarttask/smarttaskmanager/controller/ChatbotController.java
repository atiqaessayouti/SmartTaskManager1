package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.DAO.TaskDAO;
import com.smarttask.smarttaskmanager.model.Task;
import com.smarttask.smarttaskmanager.service.AIService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.util.List;

public class ChatbotController {

    @FXML private TextArea chatArea; // منطقة عرض الرسائل (Set editable=false في SceneBuilder)
    @FXML private TextField chatInput; // حقل الكتابة

    private AIService aiService = new AIService();
    private TaskDAO taskDAO = new TaskDAO();

    @FXML
    public void initialize() {
        chatArea.appendText("🤖 AI: Hello! How can I help you manage your tasks today?\n\n");
    }

    @FXML
    private void handleSendMessage() {
        String userMsg = chatInput.getText();
        if (userMsg == null || userMsg.trim().isEmpty()) return;

        // 1. عرض رسالة المستخدم
        chatArea.appendText("👤 You: " + userMsg + "\n");
        chatInput.clear();

        // 2. معالجة الرد في Thread منفصل لضمان عدم تجمد التطبيق
        new Thread(() -> {
            try {
                // جلب المهام الحالية لتزويد الـ AI بالسياق
                List<Task> tasks = taskDAO.getAllTasks();
                String response = aiService.getChatResponse(userMsg, tasks);

                // تحديث الواجهة من Thread الرئيسي
                Platform.runLater(() -> {
                    chatArea.appendText("🤖 AI: " + response + "\n\n");
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}