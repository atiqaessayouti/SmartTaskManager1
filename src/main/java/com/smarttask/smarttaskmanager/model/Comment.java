package com.smarttask.smarttaskmanager.model;
import java.sql.Timestamp;
public class Comment {
    private String userEmail;
    private String content;
    private Timestamp createdAt;

    public Comment(String userEmail, String content, Timestamp createdAt) {
        this.userEmail = userEmail;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getUserEmail() { return userEmail; }
    public String getContent() { return content; }
    public Timestamp getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return userEmail + ": " + content; // Simple affichage
    }
}