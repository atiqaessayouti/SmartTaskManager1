package com.smarttask.smarttaskmanager.model;

import java.time.LocalDate;

public class Task {
    private int id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private LocalDate deadline;
    private String category;
    private String sharedWith;
    private String recurrenceType; // ✅ المتغير الجديد

    // ✅ Constructor الكامل (9 د الباراميترات - تأكدي أن هادشي مكتوب عندك)
    public Task(int id, String title, String description, String priority, String status, LocalDate deadline, String category, String sharedWith, String recurrenceType) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.deadline = deadline;
        this.category = category;
        this.sharedWith = sharedWith;
        this.recurrenceType = recurrenceType;
    }

    // Constructor صغير (لإضافة تاسك جديدة)
    public Task(String title, String description, String priority, LocalDate deadline, String recurrenceType) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.deadline = deadline;
        this.recurrenceType = recurrenceType;
        this.status = "In Progress";
        this.category = "General";
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public LocalDate getDeadline() { return deadline; }
    public String getCategory() { return category; }
    public String getSharedWith() { return sharedWith; }
    public String getRecurrenceType() { return recurrenceType; }

    // --- Setters ---
    public void setSharedWith(String sharedWith) { this.sharedWith = sharedWith; }
    public void setStatus(String status) { this.status = status; }
    public void setRecurrenceType(String recurrenceType) { this.recurrenceType = recurrenceType; }

    @Override
    public String toString() { return title; }
}