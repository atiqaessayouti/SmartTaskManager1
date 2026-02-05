/*package com.smarttask.smarttaskmanager.service;

import com.smarttask.smarttaskmanager.model.Task;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIService {

    public static String getProductivityInsights(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) return "😴 Nothing to do. Relax!";
        long overdue = tasks.stream().filter(t -> t.getDeadline() != null && t.getDeadline().isBefore(LocalDate.now()) && !"Completed".equalsIgnoreCase(t.getStatus())).count();
        long highPriority = tasks.stream().filter(t -> "High".equalsIgnoreCase(t.getPriority()) && !"Completed".equalsIgnoreCase(t.getStatus())).count();

        if (overdue > 0) return "🚨 Attention! " + overdue + " overdue tasks!";
        if (highPriority > 0) return "🔥 Focus: Finish the " + highPriority + " high priority tasks.";
        return "🏆 Great momentum!";
    }

    public static LocalDate parseDate(String input) {
        if (input == null || input.isEmpty()) return null;
        String lowerInput = input.toLowerCase();

        if (lowerInput.contains("aujourd'hui") || lowerInput.contains("lyoum") || lowerInput.contains("today")) return LocalDate.now();
        if (lowerInput.contains("demain") || lowerInput.contains("ghda") || lowerInput.contains("tomorrow")) return LocalDate.now().plusDays(1);
        if (lowerInput.contains("après-demain") || lowerInput.contains("after tomorrow")) return LocalDate.now().plusDays(2);

        Pattern pattern = Pattern.compile("(dans|in) (\\d+) (jours|days)");
        Matcher matcher = pattern.matcher(lowerInput);
        if (matcher.find()) {
            return LocalDate.now().plusDays(Integer.parseInt(matcher.group(2)));
        }
        return null;
    }

    public static String suggestPriority(String input) {
        if (input == null) return "Medium";
        String lower = input.toLowerCase();
        if (lower.contains("urgent") || lower.contains("important") || lower.contains("exam") || lower.contains("darouri")) return "High";
        if (lower.contains("loisir") || lower.contains("film") || lower.contains("café") || lower.contains("fun")) return "Low";
        return "Medium";
    }

    // ✅✅ تصحيح مهم: رديت ليك هادشي بالإنجليزية باش يخدم مع ComboBox
    public static String suggestCategory(String input) {
        if (input == null) return "General";
        String text = input.toLowerCase();

        // --- 💼 WORK / TRAVAIL ---
        String[] workWords = {
                "work", "travail", "boulot", "job", "taff", "khdma", "projet", "project",
                "réunion", "reunion", "meeting", "meet", "client", "boss", "patron", "chef",
                "manager", "rh", "email", "mail", "rapport", "report", "présentation", "presentation",
                "slide", "ppt", "excel", "word", "pdf", "dossier", "bureau", "office",
                "code", "java", "python", "sql", "dev", "bug", "fix", "deploy", "server", "git",
                "agile", "scrum", "task", "tâche", "mission", "objectif", "deadline", "livrable",
                "recrutement", "embauche", "stage", "internship", "société", "company", "entreprise"
        };
        if (containsAny(text, workWords)) return "Work";

        // --- 🎓 EDUCATION / ÉTUDES ---
        String[] eduWords = {
                "education", "éducation", "étude", "etude", "study", "cours", "course", "class", "classe",
                "leçon", "lesson", "réviser", "reviser", "revision", "examen", "exam", "test", "quiz",
                "partiel", "controle", "contrôle", "devoir", "homework", "exercice", "exo", "tp", "td",
                "amphi", "université", "university", "fac", "école", "school", "lycée", "college",
                "formation", "learning", "apprendre", "mooc", "certif", "certification", "thèse",
                "mémoire", "pfe", "soutenance", "recherche", "research", "livre", "book", "chapitre",
                "math", "phy", "info", "science", "histoire", "anglais", "français", "9raya"
        };
        if (containsAny(text, eduWords)) return "Education";

        // --- 🏥 HEALTH / SANTÉ ---
        String[] healthWords = {
                "health", "santé", "sante", "médical", "medical", "médecin", "medecin", "docteur", "doctor",
                "tbib", "dentiste", "dentist", "ophtalmo", "yeux", "lunettes", "hôpital", "hospital",
                "clinique", "urgence", "pharmacie", "pharmacy", "médicament", "medoc", "dwa", "traitement",
                "rendez-vous", "rdv", "consultation", "analyse", "sang", "pcr", "vaccin",
                "sport", "gym", "fitness", "muscu", "workout", "entrainement", "courir", "run", "jogging",
                "marche", "diet", "régime", "rjim", "nutrition", "eau", "water", "sommeil", "sleep", "yoga"
        };
        if (containsAny(text, healthWords)) return "Health";

        // --- 💰 FINANCE / ARGENT ---
        String[] financeWords = {
                "finance", "argent", "money", "flous", "flouss", "banque", "bank", "compte", "account",
                "virement", "transfer", "payer", "pay", "paiement", "payment", "facture", "bill",
                "loyer", "rent", "krah", "électricité", "eau", "wifi", "internet", "abonnement",
                "subscription", "crédit", "credit", "dette", "debt", "kridi", "prêt", "rembourser",
                "salaire", "salary", "prime", "bonus", "impôt", "tax", "assurance", "insurance",
                "budget", "économie", "save", "épargne", "investir", "invest", "bourse", "crypto",
                "achat", "buy", "acheter", "soldes", "promo", "prix", "price", "coût", "cost"
        };
        if (containsAny(text, financeWords)) return "Finance";

        // --- 🏠 PERSONAL / PERSONNEL ---
        String[] personalWords = {
                "personal", "personnel", "famille", "family", "maison", "house", "home", "dar",
                "appartement", "chambre", "ménage", "clean", "nettoyer", "cuisine", "cook", "cuisiner",
                "repas", "meal", "food", "makla", "courses", "groceries", "supermarché", "marjane", "bim",
                "shopping", "vêtement", "habit", "linge", "lessive", "laundry", "voiture", "car", "tomobil",
                "garage", "chat", "chien", "cat", "dog", "maman", "mom", "papa", "dad", "parent",
                "enfant", "kid", "fils", "fille", "anniversaire", "birthday", "fête", "party",
                "ami", "friend", "pote", "sahbi", "sortie", "out", "voyage", "travel", "vol", "flight"
        };
        if (containsAny(text, personalWords)) return "Personal";

        return "General";
    }

    public static String extractCleanTitle(String input) {
        if (input == null || input.isEmpty()) return "New Task";
        String clean = input;
        String[] keywords = {
                // Mots temporels
                "demain", "tomorrow", "ghda", "ghadda",
                "aujourd'hui", "today", "lyoum", "ce soir",
                "après-demain", "after tomorrow",

                // ✅ Recurrence (كنمسحوهم باش ما يبقاوش ف العنوان)
                "chaque jour", "every day", "chaque semaine", "every week",
                "chaque mois", "every month", "daily", "weekly", "monthly",
                "koul nhar", "koul simana", "koul chhar","every morning",

                // Urgence
                "urgent", "important", "darouri", "vite", "asap",

                // Jours de la semaine (Français)
                "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche",

                // Jours de la semaine (English)
                "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday",

                // Patterns Regex
                "dans \\d+ jours", "in \\d+ days"
        };
        for (String word : keywords) {
            clean = clean.replaceAll("(?i)" + word, "");
        }
        return clean.trim().replaceAll(" +", " ");
    }
    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    // =========================================================================
    // 5. ✅ RECURRENCE (NEW) - كيكتشف واش التاش كتعاود
    // =========================================================================
    public static String suggestRecurrence(String input) {
        if (input == null) return "NONE";
        String lower = input.toLowerCase();

        // Daily (يوميا)
        if (containsAny(lower, "chaque jour", "tous les jours", "daily", "quotidien",
                "every day", "everyday", "yawmiyan", "koul nhar", "kula nhar", "kol nhar","every morning")) {
            return "Daily";
        }

        // Weekly (أسبوعيا)
        if (containsAny(lower, "chaque semaine", "toutes les semaines", "weekly", "hebdomadaire",
                "every week", "koul simana", "kol simana", "chaque lundi", "chaque vendredi")) { // وكملي باقي الأيام إلا بغيتي
            return "Weekly";
        }

        // Monthly (شهريا)
        if (containsAny(lower, "chaque mois", "tous les mois", "monthly", "mensuel",
                "every month", "koul chhar", "kol chhar")) {
            return "Monthly";
        }

        // Yearly (سنويا)
        if (containsAny(lower, "chaque année", "annuel", "yearly", "every year", "koul 3am")) {
            return "Yearly";
        }

        return "NONE"; // Defaut
    }
}**/

package com.smarttask.smarttaskmanager.service;

import com.smarttask.smarttaskmanager.model.Task;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIService {

    // ============================================================
    // Productivity insights
    // ============================================================
    public static String getProductivityInsights(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) return "😴 Nothing to do. Relax!";

        long overdue = tasks.stream()
                .filter(t -> t.getDeadline() != null &&
                        t.getDeadline().isBefore(LocalDate.now()) &&
                        !"Completed".equalsIgnoreCase(t.getStatus()))
                .count();

        long highPriority = tasks.stream()
                .filter(t -> "High".equalsIgnoreCase(t.getPriority()) &&
                        !"Completed".equalsIgnoreCase(t.getStatus()))
                .count();

        if (overdue > 0) return "🚨 Attention! " + overdue + " overdue tasks!";
        if (highPriority > 0) return "🔥 Focus: Finish the " + highPriority + " high priority tasks.";

        return "🏆 Great momentum!";
    }

    // ============================================================
    // Date parsing
    // ============================================================
    public static LocalDate parseDate(String input) {
        if (input == null || input.isEmpty()) return null;

        String lowerInput = input.toLowerCase();

        if (lowerInput.contains("aujourd'hui") || lowerInput.contains("lyoum") || lowerInput.contains("today"))
            return LocalDate.now();

        if (lowerInput.contains("demain") || lowerInput.contains("ghda") || lowerInput.contains("tomorrow"))
            return LocalDate.now().plusDays(1);

        if (lowerInput.contains("après-demain") || lowerInput.contains("after tomorrow"))
            return LocalDate.now().plusDays(2);

        Pattern pattern = Pattern.compile("(dans|in) (\\d+) (jours|days)");
        Matcher matcher = pattern.matcher(lowerInput);

        if (matcher.find()) {
            return LocalDate.now().plusDays(Integer.parseInt(matcher.group(2)));
        }

        return null;
    }

    // ============================================================
    // Priority detection
    // ============================================================
    public static String suggestPriority(String input) {
        if (input == null) return "Medium";

        String lower = input.toLowerCase();

        if (lower.contains("urgent") || lower.contains("important") ||
                lower.contains("exam") || lower.contains("darouri"))
            return "High";

        if (lower.contains("loisir") || lower.contains("film") ||
                lower.contains("café") || lower.contains("fun"))
            return "Low";

        return "Medium";
    }

    // ============================================================
    // Category detection (نسختك الأصلية كاملة)
    // ============================================================
    public static String suggestCategory(String input) {
        if (input == null) return "General";
        String text = input.toLowerCase();

        String[] workWords = {
                "work","travail","boulot","job","taff","khdma","projet","project",
                "réunion","reunion","meeting","meet","client","boss","patron","chef",
                "manager","rh","email","mail","rapport","report","présentation","presentation",
                "slide","ppt","excel","word","pdf","dossier","bureau","office",
                "code","java","python","sql","dev","bug","fix","deploy","server","git",
                "agile","scrum","task","tâche","mission","objectif","deadline","livrable",
                "recrutement","embauche","stage","internship","société","company","entreprise"
        };
        if (containsAny(text, workWords)) return "Work";

        String[] eduWords = {
                "education","éducation","étude","etude","study","cours","course","class","classe",
                "leçon","lesson","réviser","reviser","revision","examen","exam","test","quiz",
                "partiel","controle","contrôle","devoir","homework","exercice","exo","tp","td",
                "amphi","université","university","fac","école","school","lycée","college",
                "formation","learning","apprendre","mooc","certif","certification","thèse",
                "mémoire","pfe","soutenance","recherche","research","livre","book","chapitre",
                "math","phy","info","science","histoire","anglais","français","9raya"
        };
        if (containsAny(text, eduWords)) return "Education";

        String[] healthWords = {
                "health","santé","sante","médical","medical","médecin","medecin","docteur","doctor",
                "tbib","dentiste","dentist","ophtalmo","yeux","lunettes","hôpital","hospital",
                "clinique","urgence","pharmacie","pharmacy","médicament","medoc","dwa","traitement",
                "rendez-vous","rdv","consultation","analyse","sang","pcr","vaccin",
                "sport","gym","fitness","muscu","workout","entrainement","courir","run","jogging",
                "marche","diet","régime","rjim","nutrition","eau","water","sommeil","sleep","yoga"
        };
        if (containsAny(text, healthWords)) return "Health";

        String[] financeWords = {
                "finance","argent","money","flous","flouss","banque","bank","compte","account",
                "virement","transfer","payer","pay","paiement","payment","facture","bill",
                "loyer","rent","krah","électricité","eau","wifi","internet","abonnement",
                "subscription","crédit","credit","dette","debt","kridi","prêt","rembourser",
                "salaire","salary","prime","bonus","impôt","tax","assurance","insurance",
                "budget","économie","save","épargne","investir","invest","bourse","crypto",
                "achat","buy","acheter","soldes","promo","prix","price","coût","cost"
        };
        if (containsAny(text, financeWords)) return "Finance";

        String[] personalWords = {
                "personal","personnel","famille","family","maison","house","home","dar",
                "appartement","chambre","ménage","clean","nettoyer","cuisine","cook","cuisiner",
                "repas","meal","food","makla","courses","groceries","supermarché","marjane","bim",
                "shopping","vêtement","habit","linge","lessive","laundry","voiture","car","tomobil",
                "garage","chat","chien","cat","dog","maman","mom","papa","dad","parent",
                "enfant","kid","fils","fille","anniversaire","birthday","fête","party",
                "ami","friend","pote","sahbi","sortie","out","voyage","travel","vol","flight"
        };
        if (containsAny(text, personalWords)) return "Personal";

        return "General";
    }

    // ============================================================
    // Title cleaning (FIX مهم)
    // ============================================================
    public static String extractCleanTitle(String input) {
        if (input == null || input.isEmpty()) return "New Task";

        String clean = input;

        String[] keywords = {
                "demain","tomorrow","ghda","ghadda",
                "aujourd'hui","today","lyoum","ce soir",
                "après-demain","after tomorrow",

                "chaque jour","every day","chaque semaine","every week",
                "chaque mois","every month","daily","weekly","monthly",
                "koul nhar","koul simana","koul chhar","every morning",

                "urgent","important","darouri","vite","asap",

                "lundi","mardi","mercredi","jeudi","vendredi","samedi","dimanche",
                "monday","tuesday","wednesday","thursday","friday","saturday","sunday",

                "dans \\d+ jours","in \\d+ days"
        };

        // ✅ FIX: يمسح غير الكلمات الكاملة
        for (String word : keywords) {
            clean = clean.replaceAll("(?i)\\b" + word + "\\b", "");
        }

        return clean.trim().replaceAll(" +", " ");
    }

    // ============================================================
    // Recurrence detection
    // ============================================================
    public static String suggestRecurrence(String input) {
        if (input == null) return "NONE";

        String lower = input.toLowerCase();

        if (containsAny(lower,
                "chaque jour","tous les jours","daily","quotidien",
                "every day","everyday","yawmiyan","koul nhar","kula nhar","kol nhar","every morning"))
            return "Daily";

        if (containsAny(lower,
                "chaque semaine","toutes les semaines","weekly","hebdomadaire",
                "every week","koul simana","kol simana","chaque lundi","chaque vendredi"))
            return "Weekly";

        if (containsAny(lower,
                "chaque mois","tous les mois","monthly","mensuel",
                "every month","koul chhar","kol chhar"))
            return "Monthly";

        if (containsAny(lower,
                "chaque année","annuel","yearly","every year","koul 3am"))
            return "Yearly";

        return "NONE";
    }

    // ============================================================
    // Helper
    // ============================================================
    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }
}
