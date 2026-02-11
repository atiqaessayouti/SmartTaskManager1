package com.smarttask.smarttaskmanager.model;

import java.time.LocalDate;
import java.sql.Timestamp;

public class Task {

    private int id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private LocalDate deadline;
    private String category;
    private String sharedWith;
    private String recurrenceType;
    private Integer parentId;

    // ✅
    private long timeSpent;       //
    private Timestamp timerStart; //

    // ✅ Constructor الجديد (12 باراميتر)
    public Task(int id, String title, String description, String priority, String status,
                LocalDate deadline, String category, String sharedWith,
                String recurrenceType, Integer parentId,
                long timeSpent, Timestamp timerStart) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.deadline = deadline;
        this.category = category;
        this.sharedWith = sharedWith;
        this.recurrenceType = recurrenceType;
        this.parentId = parentId;
        this.timeSpent = timeSpent;
        this.timerStart = timerStart;
    }

    // ✅
    public long getTimeSpent() { return timeSpent; }
    public Timestamp getTimerStart() { return timerStart; }

    //
    public boolean isTimerRunning() { return timerStart != null; }

    // .
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public LocalDate getDeadline() { return deadline; }
    public String getCategory() { return category; }
    public String getSharedWith() { return sharedWith; }
    public String getRecurrenceType() { return recurrenceType; }
    public Integer getParentId() { return parentId; }

    @Override
    public String toString() { return title; }
}