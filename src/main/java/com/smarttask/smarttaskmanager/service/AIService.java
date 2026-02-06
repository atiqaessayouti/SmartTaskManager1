package com.smarttask.smarttaskmanager.service;

import com.smarttask.smarttaskmanager.model.Task;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIService {

    // ✅ CATEGORY DETECTION (بسيطة وفعالة جداً)
    public static String suggestCategory(String input) {
        if (input == null) return "General";
        String text = input.toLowerCase(); // كنردو كلشي صغير باش نسهلو البحث

        // 💼 WORK
        if (containsAny(text, "work", "travail", "boulot", "projet", "réunion", "meeting", "client", "boss", "email", "code", "bug", "java", "sql", "rh", "management")) return "Work";

        // 🎓 EDUCATION
        if (containsAny(text, "education", "étude", "cours", "exam", "test", "devoir", "projet", "pfe", "soutenance", "biblio", "livre", "revise", "school", "école")) return "Education";

        // 🏥 HEALTH
        if (containsAny(text, "health", "santé", "médecin", "docteur", "tbib", "rdv", "hopital", "sbitar", "sport", "gym", "traitement", "medicament", "dwa")) return "Health";

        // 💰 FINANCE (هنا فين كاين internet و facture)
        if (containsAny(text, "finance", "argent", "flous", "banque", "payer", "pay", "facture", "bill", "loyer", "salaire", "prix", "cost", "internet", "wifi", "abonnement")) return "Finance";

        // 🏠 PERSONAL
        if (containsAny(text, "personal", "famille", "maison", "dar", "courses", "shopping", "voyage", "ami", "anniversaire", "fête", "clean", "ménage")) return "Personal";

        return "General";
    }

    // ✅ TITLE CLEANING (مسح الكلمات الزايدة)
    public static String extractCleanTitle(String input) {
        if (input == null || input.isEmpty()) return "New Task";
        String clean = input;
        String[] keywords = {
                "demain", "tomorrow", "ghda", "aujourd'hui", "today", "lyoum",
                "chaque jour", "every day", "chaque semaine", "weekly",
                "chaque mois", "monthly", "mensuel", // ✅ كتمسح
                "chaque année", "yearly",
                "urgent", "important", "darouri", "vite", "asap"
        };
        for (String word : keywords) {
            clean = clean.replaceAll("(?i)\\b" + word + "\\b", "");
        }
        return clean.trim().replaceAll(" +", " ");
    }

    // ✅ RECURRENCE (MAJUSCULE)
    public static String suggestRecurrence(String input) {
        if (input == null) return "NONE";
        String text = input.toLowerCase();
        if (containsAny(text, "chaque jour", "daily", "quotidien", "every day")) return "DAILY";
        if (containsAny(text, "chaque semaine", "weekly", "hebdomadaire", "every week")) return "WEEKLY";
        if (containsAny(text, "chaque mois", "monthly", "mensuel", "every month")) return "MONTHLY"; // ✅ هنا كاين "chaque mois"
        if (containsAny(text, "chaque année", "yearly", "annuel", "every year")) return "YEARLY";
        return "NONE";
    }

    // ✅ PRIORITY
    public static String suggestPriority(String input) {
        if (input == null) return "Medium";
        String text = input.toLowerCase();
        if (containsAny(text, "urgent", "important", "darouri", "exam", "dead", "vite")) return "High";
        if (containsAny(text, "loisir", "film", "café", "fun", "game")) return "Low";
        return "Medium";
    }

    // ✅ DATE PARSING
    public static LocalDate parseDate(String input) {
        if (input == null || input.isEmpty()) return null;
        String text = input.toLowerCase();
        if (containsAny(text, "aujourd'hui", "lyoum", "today")) return LocalDate.now();
        if (containsAny(text, "demain", "ghda", "tomorrow")) return LocalDate.now().plusDays(1);
        if (containsAny(text, "après-demain", "after tomorrow")) return LocalDate.now().plusDays(2);

        Pattern pattern = Pattern.compile("(dans|in) (\\d+) (jours|days)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) return LocalDate.now().plusDays(Integer.parseInt(matcher.group(2)));

        return null;
    }

    // 🛠 Helper Method (السر ديال النجاح)
    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true; // ✅ contains كتقرا كلشي، واخا يكونو سطور
        }
        return false;
    }

    // Helper for insights (Optional)
    public static String getProductivityInsights(List<Task> tasks) { return "Keep going!"; }
}