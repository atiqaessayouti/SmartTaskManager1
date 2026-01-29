package com.smarttask.smarttaskmanager.model; // <--- Zidi hadi lfo9

public class User {
    private int id;
    private String email;

    public User(int id, String email) {
        this.id = id;
        this.email = email;
    }

    public int getId() { return id; }
    public String getEmail() { return email; }

    @Override
    public String toString() { return email; } // Bach ibane l-email f ComboBox
}