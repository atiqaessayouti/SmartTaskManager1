package com.smarttask.smarttaskmanager.util;

public class UserSession {

    // Instance unique (Singleton)
    private static UserSession instance;

    // Les données li bghina n3qlo 3lihum
    private String email;
    private String username;

    // Constructeur Privé
    private UserSession(String email, String username) {
        this.email = email;
        this.username = username;
    }

    // Méthode bach nbdaw Session (f Login)
    public static UserSession getInstace(String email, String username) {
        if (instance == null) {
            instance = new UserSession(email, username);
        }
        return instance;
    }

    // Méthode bach njibu Session (f Profil/Dashboard)
    public static UserSession getInstance() {
        return instance;
    }

    // Getters
    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    // Méthode l Logout (Nmss7u Session)
    public void cleanUserSession() {
        email = null;
        username = null;
        instance = null;
    }
}