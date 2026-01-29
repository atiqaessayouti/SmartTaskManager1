package com.smarttask.smarttaskmanager.model;

import java.time.LocalDate;

public class Task {
    private int id;
    private String title;
    private String description; // <-- Zedt hada
    private String priority;
    private String status;
    private LocalDate deadline;
    private String category;
    private String sharedWith; // <-- Zedt hada

    // Constructeur Complet
    public Task(int id, String title, String description, String priority, String status, LocalDate deadline, String category, String sharedWith) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.deadline = deadline;
        this.category = category;
        this.sharedWith = sharedWith;
    }

    // Constructeur Sghir (ila bghiti tcreer tache jdida bla id)
    public Task(String title, String description, String priority, LocalDate deadline) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.deadline = deadline;
        this.status = "In Progress"; // Default
    }

    // --- GETTERS (Hado huma li kanu naqssin) ---
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; } // ✅
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public LocalDate getDeadline() { return deadline; }
    public String getCategory() { return category; }
    public String getSharedWith() { return sharedWith; } // ✅

    // --- SETTERS ---
    public void setSharedWith(String sharedWith) { this.sharedWith = sharedWith; }
    public void setStatus(String status) { this.status = status; }
}