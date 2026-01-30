package com.smarttask.smarttaskmanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

// ⚠️ HADI HIYA LI KANT NAQSSA ⚠️
public class Launcher extends Application {

    // Méthode Main (Bach demarra l'app)
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("view/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // --- PARTIE LOGO (Li zedna 9bila) ---
        try {
            // T2kdi mn l'massar (path)
            Image icon = new Image(getClass().getResourceAsStream("images/logo.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Logo introuvable: " + e.getMessage());
        }
        // ------------------------------------

        stage.setTitle("Smart Task Manager");
        stage.setScene(scene);
        stage.show();
    }
}