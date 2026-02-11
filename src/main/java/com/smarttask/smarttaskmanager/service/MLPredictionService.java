package com.smarttask.smarttaskmanager.service;

import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class MLPredictionService {

    private Map<String, Double> modelWeights = new HashMap<>();
    private boolean isTrained = false;

    // 🧠 ENTRAÎNEMENT INTELLIGENT (VIA SQL)
    // Hada kay-mchi l MySQL w kay-goul lih: "3tini l-mou3ddal d l-weqt bin creation w deadline"
    public void trainModel(Object ignored) { // Parameter ignored hit ghan-jibu mn DB direct
        System.out.println("🤖 Lancement du Data Mining (Training)...");


        String sql = "SELECT priority, AVG(DATEDIFF(deadline, created_at)) as avg_days " +
                "FROM tasks " +
                "WHERE deadline IS NOT NULL " +
                "AND DATEDIFF(deadline, created_at) > 0 " + // Ghir l-tâches logiques
                "GROUP BY priority";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            boolean dataFound = false;
            while (rs.next()) {
                String priority = rs.getString("priority");
                double avgDays = rs.getDouble("avg_days");

                modelWeights.put(priority, avgDays);
                System.out.println("✅ Appris de la BDD : " + priority + " ≈ " + Math.round(avgDays) + " jours.");
                dataFound = true;
            }

            if (!dataFound) {
                System.out.println("⚠️ Aucune donnée historique valide. Utilisation des valeurs par défaut.");
            }

            isTrained = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔮 PRÉDICTION
    public int predictDaysNeeded(String priority) {

        double defaultDays = switch (priority) {
            case "High" -> 2.0;
            case "Medium" -> 5.0;
            case "Low" -> 7.0;
            default -> 3.0;
        };


        double prediction = modelWeights.getOrDefault(priority, defaultDays);


        return (int) Math.round(prediction);
    }
}