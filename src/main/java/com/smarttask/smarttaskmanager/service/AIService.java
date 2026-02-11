package com.smarttask.smarttaskmanager.service;

import com.smarttask.smarttaskmanager.model.Task;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIService {

    // ✅ CATEGORY DETECTION (English Keywords)
    public static String suggestCategory(String input) {
        if (input == null) return "General";
        String text = input.toLowerCase();

        // 💼 WORK
        if (containsAny(text, "work", "job", "project", "meeting", "client", "boss", "email", "code", "bug", "java", "sql", "hr", "management", "report")) return "Work";

        // 🎓 EDUCATION
        if (containsAny(text, "education", "study", "course", "exam", "test", "assignment", "homework", "project", "research", "book", "read", "school", "university")) return "Education";

        // 🏥 HEALTH
        if (containsAny(text, "health", "doctor", "appointment", "hospital", "gym", "workout", "sport", "medication", "pill", "therapy", "dentist")) return "Health";

        // 💰 FINANCE
        if (containsAny(text, "finance", "money", "bank", "pay", "bill", "invoice", "rent", "salary", "cost", "price", "internet", "subscription", "wifi")) return "Finance";

        // 🏠 PERSONAL
        if (containsAny(text, "personal", "family", "home", "house", "grocery", "shopping", "travel", "trip", "friend", "birthday", "party", "clean", "laundry")) return "Personal";

        return "General";
    }

    // ✅ TITLE CLEANING (Removes time/urgency words from the title)
    public static String extractCleanTitle(String input) {
        if (input == null || input.isEmpty()) return "New Task";
        String clean = input;
        String[] keywords = {
                "tomorrow", "tmrw", "today", "tonight",
                "every day", "daily", "every week", "weekly",
                "every month", "monthly",
                "every year", "yearly",
                "urgent", "important", "asap", "fast", "critical"
        };
        for (String word : keywords) {
            clean = clean.replaceAll("(?i)\\b" + word + "\\b", "");
        }
        return clean.trim().replaceAll(" +", " ");
    }

    // ✅ RECURRENCE DETECTION
    public static String suggestRecurrence(String input) {
        if (input == null) return "NONE";
        String text = input.toLowerCase();
        if (containsAny(text, "every day", "daily", "each day")) return "DAILY";
        if (containsAny(text, "every week", "weekly", "each week")) return "WEEKLY";
        if (containsAny(text, "every month", "monthly", "each month")) return "MONTHLY";
        if (containsAny(text, "every year", "yearly", "annually")) return "YEARLY";
        return "NONE";
    }

    // ✅ PRIORITY DETECTION
    public static String suggestPriority(String input) {
        if (input == null) return "Medium";
        String text = input.toLowerCase();
        if (containsAny(text, "urgent", "important", "critical", "asap", "deadline", "fast")) return "High";
        if (containsAny(text, "leisure", "movie", "coffee", "fun", "game", "maybe")) return "Low";
        return "Medium";
    }

    // ✅ DATE PARSING (NLP Logic)
    public static LocalDate parseDate(String input) {
        if (input == null || input.isEmpty()) return null;
        String text = input.toLowerCase();

        if (containsAny(text, "today", "tonight")) return LocalDate.now();
        if (containsAny(text, "tomorrow", "tmrw")) return LocalDate.now().plusDays(1);
        if (containsAny(text, "after tomorrow", "day after tomorrow")) return LocalDate.now().plusDays(2);
        if (containsAny(text, "next week")) return LocalDate.now().plusWeeks(1);

        // Regex for "in X days"
        Pattern pattern = Pattern.compile("(in) (\\d+) (days|day)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) return LocalDate.now().plusDays(Integer.parseInt(matcher.group(2)));

        return null;
    }

    // 🛠 Helper Method
    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }

    // 🔥🔥 SMART DASHBOARD INSIGHTS (English Messages)
    public static String getProductivityInsights(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) return "🚀 Start by adding a new task!";

        // 1. Count Overdue Tasks
        long overdueCount = tasks.stream()
                .filter(t -> t.getDeadline() != null && t.getDeadline().isBefore(LocalDate.now()) && !"Completed".equals(t.getStatus()))
                .count();

        // 2. Count High Priority Tasks
        long highPriorityCount = tasks.stream()
                .filter(t -> "High".equalsIgnoreCase(t.getPriority()) && !"Completed".equals(t.getStatus()))
                .count();

        // 3. Analyze and return insight
        if (overdueCount > 0) {
            return "⚠️ Warning! You have " + overdueCount + " overdue task(s). Catch up now!";
        }

        if (highPriorityCount > 2) {
            return "🔥 Focus Mode: You have " + highPriorityCount + " high-priority tasks pending.";
        }

        long completedToday = tasks.stream()
                .filter(t -> "Completed".equals(t.getStatus()))
                .count();

        if (completedToday > 0) {
            return "✅ Excellent work! You are making good progress today.";
        }

        return "💡 Tip: Start with the hardest task first ('Eat the Frog').";
    }
    // زيدي هاد الدالة داخل AIService.java
    public String getChatResponse(String message, List<Task> currentTasks) {
        if (message == null || message.trim().isEmpty()) return "I'm listening... How can I help you?";
        String input = message.toLowerCase();

        // 1. طلب نصيحة أو حالة الإنتاجية (Insights)
        if (containsAny(input, "advice", "status", "how am i doing", "report", "productivity")) {
            return getProductivityInsights(currentTasks); //
        }

        // 2. سؤال عن تصنيف مهمة معينة
        if (containsAny(input, "category", "where", "group")) {
            return "I suggest putting this in the '" + suggestCategory(input) + "' category."; //
        }

        // 3. سؤال عن الأولوية
        if (containsAny(input, "priority", "urgent", "important")) {
            return "Based on your description, this looks like a " + suggestPriority(input) + " priority task."; //
        }

        // 4. تحليل التاريخ (NLP)
        if (containsAny(input, "tomorrow", "today", "next week", "in")) {
            java.time.LocalDate date = parseDate(input); //
            return (date != null) ? "I detected the date: " + date : "I couldn't quite catch the date.";
        }

        // 5. رد افتراضي ذكي
        return "I am your Smart Manager AI. You can ask me about your productivity, suggest categories for tasks, or check task priorities!";
    }

}