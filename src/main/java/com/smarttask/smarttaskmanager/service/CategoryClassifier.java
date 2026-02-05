package com.smarttask.smarttaskmanager.service;

public class CategoryClassifier {

    public static String suggestCategory(String text) {
        if (text == null) return "General";
        text = text.toLowerCase();

        // 💼 1. WORK / TRAVAIL
        if (text.matches(".*\\b(work|travail|boulot|job|projet|meeting|réunion|boss|client|report|rapport|email|presentation|khedma|khdma|choughl|trabajo|oficina)\\b.*")) {
            return "Work";
        }

        // 🎓 2. EDUCATION / ÉTUDES
        if (text.matches(".*\\b(study|étudier|examen|exam|test|cours|lesson|revise|réviser|book|livre|biblio|pfe|soutenance|qraya|mdrassa|estudio|escuela)\\b.*")) {
            return "Education";
        }

        // 🏥 3. HEALTH / SANTÉ
        if (text.matches(".*\\b(health|santé|doctor|médecin|rdv|dentiste|sport|gym|entrainement|workout|diet|regime|tbib|dwaw|dwa|sbitar|salud|ejercicio)\\b.*")) {
            return "Health";
        }

        // 💰 4. FINANCE / ARGENT
        if (text.matches(".*\\b(pay|payer|buy|acheter|achat|bank|banque|facture|bill|loyer|money|argent|flous|flouss|khalass|khels|dinero|pago|comprar)\\b.*")) {
            return "Finance";
        }

        // 🏠 5. PERSONAL / MAISON
        if (text.matches(".*\\b(home|maison|famille|family|shopping|courses|ménage|clean|cook|repas|dar|lwalida|lwalid|casa|familia)\\b.*")) {
            return "Personal";
        }

        // 6. DEFAULT
        return "General";
    }
}