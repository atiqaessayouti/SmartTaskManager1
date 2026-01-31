package com.smarttask.smarttaskmanager.service;

import com.smarttask.smarttaskmanager.model.Task;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

public class AIService {

    // ==========================================
    // PARTIE 1: LOGIC
    // ==========================================
    public static LocalDate parseDate(String input) {
        if (input == null) return null;
        String lowerInput = input.toLowerCase();
        if (lowerInput.contains("demain") || lowerInput.contains("ghda")) {
            return LocalDate.now().plusDays(1);
        } else if (lowerInput.contains("aujourd'hui") || lowerInput.contains("lyoum")) {
            return LocalDate.now();
        }
        return null;
    }

    public static String suggestPriority(String input) {
        if (input == null) return "Medium";
        String lowerInput = input.toLowerCase();
        if (lowerInput.contains("urgent") || lowerInput.contains("examen") || lowerInput.contains("important")) {
            return "High";
        }
        return "Medium";
    }

    // ==========================================
    // PARTIE 2: GEMINI AVEC FALLBACK (PLAN B)
    // ==========================================

    // Dir l-Key dyalk hna (wakha t-kon "Mita", l-code ghadi y-khdem b Plan B)
    private static final String API_KEY = "AIzaSyB4eJH0SBhgWESzhN5_ngc2qd0ba_QJsrk";

    // N-sta3mlo Flash 1.5 hit howa Standard
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    public String getProductivityInsights(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) return "Ajoutez des tâches pour l'analyse.";

        // Mohawala n-taslo b Gemini (Try Real AI)
        try {
            String prompt = "Donne 1 conseil court en Français pour s'organiser.";
            String jsonBody = "{ \"contents\": [{ \"parts\":[{ \"text\": \"" + prompt + "\" }] }] }";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Ila l-Key khddama, n-jbo l-jawab
                String body = response.body();
                int start = body.indexOf("\"text\": \"") + 9;
                if (start > 8) {
                    int end = body.indexOf("\"", start);
                    return "✨ AI: " + body.substring(start, end).replace("\\n", "\n");
                }
            } else {
                // Ila l-Key MITA (429, 404...), n-affichiw error f console bach t-choufiha nti
                System.out.println("⚠️ Gemini Error: " + response.statusCode() + ". Switch to Backup Mode.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // ==========================================
        // PLAN B: ILA L-INTERNET TQILA OLA KEY KHASRA
        // ==========================================
        return getBackupAdvice();
    }

    // Hada "Fake AI" bach l-appli t-ban dima khddama
    private String getBackupAdvice() {
        String[] tips = {
                "💡 Conseil : Commencez par la tâche la plus difficile le matin.",
                "💡 Conseil : Utilisez la technique Pomodoro (25min travail, 5min pause).",
                "💡 Conseil : Éliminez les distractions pour finir plus vite.",
                "💡 Conseil : Une tâche 'High Priority' doit être faite aujourd'hui.",
                "💡 Conseil : Prenez une pause de 10 minutes après 2h de travail."
        };
        return tips[new Random().nextInt(tips.length)];
    }
}