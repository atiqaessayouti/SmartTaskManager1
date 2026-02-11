package com.smarttask.smarttaskmanager.service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NLPProcessor {

    public static LocalDate extractDate(String text) {
        if (text == null) return null;
        text = text.toLowerCase();
        LocalDate date = LocalDate.now();

        // --------------------------------------------------------
        // 1. MOTS CLÉS SIMPLES (Today, Tomorrow...) 📆
        // --------------------------------------------------------

        // AUJOURD'HUI (Today)
        if (text.matches(".*\\b(today|aujourd'hui|hoy|lyoum|lyoma)\\b.*")) {
            return date;
        }


        if (text.matches(".*\\b(after tomorrow|après-demain|pasado mañana|b3d ghda|ba3d ghda)\\b.*")) {
            return date.plusDays(2);
        }

        // DEMAIN (Tomorrow)
        if (text.matches(".*\\b(tomorrow|demain|mañana|ghda|ghdda|ghada)\\b.*")) {
            return date.plusDays(1);
        }

        // --------------------------------------------------------

        // --------------------------------------------------------

        // FIN DU MOIS
        if (text.matches(".*\\b(end of month|fin du mois|fin de mes|lkher d chher|lakher d ch-her)\\b.*")) {
            return date.with(TemporalAdjusters.lastDayOfMonth());
        }

        // WEEKEND (Fin de semaine)
        if (text.matches(".*\\b(weekend|fin de semaine|fin de semana|wikand|simana jaya)\\b.*")) {
            return date.with(TemporalAdjusters.next(java.time.DayOfWeek.FRIDAY));
        }

        // --------------------------------------------------------
        //
        // --------------------------------------------------------


        // (days|jours|dias|yamat|yam) = Unité
        Pattern pDays = Pattern.compile("(in|dans|en|mn hna|men hna)\\s+(\\d+)\\s+(days|jours|dias|días|yamat|iyyam|yam)");
        Matcher mDays = pDays.matcher(text);
        if (mDays.find()) {
            return date.plusDays(Integer.parseInt(mDays.group(2)));
        }

        // "Dans X semaines"
        Pattern pWeeks = Pattern.compile("(in|dans|en|mn hna)\\s+(\\d+)\\s+(weeks|semaines|semanas|simanat)");
        Matcher mWeeks = pWeeks.matcher(text);
        if (mWeeks.find()) {
            return date.plusWeeks(Integer.parseInt(mWeeks.group(2)));
        }

        // "Dans X mois"
        Pattern pMonths = Pattern.compile("(in|dans|en|mn hna)\\s+(\\d+)\\s+(months|mois|meses|chhour|chhora)");
        Matcher mMonths = pMonths.matcher(text);
        if (mMonths.find()) {
            return date.plusMonths(Integer.parseInt(mMonths.group(2)));
        }

        return null;
    }

    public static String extractPriority(String text) {
        if (text == null) return null;
        text = text.toLowerCase();

        // 🔴 HIGH / URGENT (4 Langues)
        // EN: urgent, asap, important, now
        // FR: urgent, vite, important
        // ES: urgente, rapido, importante, ya, ahora

        if (text.matches(".*\\b(urgent|asap|important|vite|now|fast|critique|urgente|rapido|ya|ahora|darouri|zerba|bzrba|dghya|mohim|mohem)\\b.*")) {
            return "High";
        }

        // 🟢 LOW / COOL (4 Langues)
        // EN: later, low, slow
        // FR: tard, tranquille, pas pressé
        // ES: tarde, lento, tranquilo

        if (text.matches(".*\\b(later|low|slow|relax|tard|cool|tarde|lento|tranquilo|blati|mn b3d|men b3d|machi darouri)\\b.*")) {
            return "Low";
        }

        return null;
    }
}