package com.smarttask.smarttaskmanager.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class WelcomeController {

    @FXML
    public void handleLogin(ActionEvent event) throws IOException {
        navigate(event, "/com/smarttask/smarttaskmanager/view/login.fxml");
    }

    @FXML
    public void handleCreateAccount(ActionEvent event) throws IOException {
        navigate(event, "/com/smarttask/smarttaskmanager/view/register.fxml");
    }

    private void navigate(ActionEvent event, String path) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(path));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}