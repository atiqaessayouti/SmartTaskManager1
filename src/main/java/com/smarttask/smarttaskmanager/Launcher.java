package com.smarttask.smarttaskmanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Launcher extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        // ✅ التغيير هنا: فتح صفحة الـ Welcome أولاً بدلاً من الـ Login
        FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("view/welcome.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // --- PARTIE LOGO ---
        try {
            // تحميل اللوغو الخاص بالتطبيق
            Image icon = new Image(getClass().getResourceAsStream("images/logo.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Logo introuvable: " + e.getMessage());
        }
        // -------------------

        stage.setTitle("Smart Task Manager | Welcome");
        stage.setScene(scene);

        // منع تغيير حجم النافذة للحفاظ على أبعاد التصميم العصري
        stage.setResizable(false);
        stage.show();
    }
}