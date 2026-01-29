package com.smarttask.smarttaskmanager.service;

import java.time.LocalDate;

public class AIService {

    public static LocalDate parseDate(String input) {
        if (input == null) return null;
        String lowerInput = input.toLowerCase();

        // Natural Language Parsing
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

        // Smart Task Categorization
        if (lowerInput.contains("urgent") || lowerInput.contains("examen") || lowerInput.contains("important")) {
            return "High";
        } else if (lowerInput.contains("maybe") || lowerInput.contains("plus tard")) {
            return "Low";
        }
        return "Medium";
    }
}