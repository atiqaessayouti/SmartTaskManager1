package com.smarttask.smarttaskmanager.util;

public class UserSession {

    private static UserSession instance;

    private int userId;
    private String email;

    private UserSession(int userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public static UserSession getInstance() {
        return instance;
    }

    public static UserSession getInstance(int userId, String email) {
        if (instance == null) {
            instance = new UserSession(userId, email);
        }
        return instance;
    }

    public static void cleanUserSession() {
        instance = null;
    }

    public int getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}