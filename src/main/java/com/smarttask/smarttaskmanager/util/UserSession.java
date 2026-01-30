package com.smarttask.smarttaskmanager.util;

public class UserSession {
    private static UserSession instance;
    private String email;
    private String username;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) { instance = new UserSession(); }
        return instance;
    }

    // 👇 LA METHODE LI KHASSAK (M-validya)
    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }

    public void cleanUserSession() {
        this.email = null;
        this.username = null;
    }
}