package com.smarttask.smarttaskmanager.model;

import java.sql.Date;

public class Task {
    private int id;
    private String title;
    private Date deadline;
    private String priority;
    private String status;

    public Task(int id, String title, Date deadline, String priority, String status) {
        this.id = id;
        this.title = title;
        this.deadline = deadline;
        this.priority = priority;
        this.status = status;
    }

    // Getters (Darouriyin)
    public int getId() { return id; }
    public String getTitle() { return title; }
    public Date getDeadline() { return deadline; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
}