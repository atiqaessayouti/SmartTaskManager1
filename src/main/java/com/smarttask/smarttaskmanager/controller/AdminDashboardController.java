package com.smarttask.smarttaskmanager.controller;

import com.smarttask.smarttaskmanager.util.DatabaseConnection;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class AdminDashboardController {

    @FXML private BorderPane mainBorderPane;

    // --- PRODUCTIVITY METRICS LABELS ---
    @FXML private Label lblTotalTasks;
    @FXML private Label lblCompletedTasks;
    @FXML private Label lblPendingTasks;

    // --- ANALYTICS COMPONENTS ---
    @FXML private LineChart<String, Number> performanceLineChart;
    @FXML private PieChart timeTrackingPieChart;

    // --- CRUD COMPONENTS ---
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, Void> colAction;

    @FXML private TextField emailInput;
    @FXML private TextField passwordInput;
    @FXML private CheckBox isAdminInput; // Case à cocher pour Admin
    @FXML private TextField searchField;

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private boolean isDarkMode = false;
    private int selectedUserId = -1;

    @FXML
    public void initialize() {
        setupMetrics();
        setupAnalytics();
        setupUserTable();
        addButtonToTable();
        loadData();
        setupSearch();
    }

    // --- 1. PRODUCTIVITY METRICS ---
    private void setupMetrics() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement st = conn.createStatement()) {

            ResultSet rsTotal = st.executeQuery("SELECT COUNT(*) FROM tasks");
            if (rsTotal.next()) lblTotalTasks.setText(String.valueOf(rsTotal.getInt(1)));

            Statement st2 = conn.createStatement();
            ResultSet rsDone = st2.executeQuery("SELECT COUNT(*) FROM tasks WHERE status = 'Completed' OR status = 'Done'");
            if (rsDone.next()) lblCompletedTasks.setText(String.valueOf(rsDone.getInt(1)));

            Statement st3 = conn.createStatement();
            ResultSet rsPending = st3.executeQuery("SELECT COUNT(*) FROM tasks WHERE status != 'Completed' AND status != 'Done'");
            if (rsPending.next()) lblPendingTasks.setText(String.valueOf(rsPending.getInt(1)));

        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- 2. ANALYTICS ---
    private void setupAnalytics() {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT category, COUNT(*) as count FROM tasks GROUP BY category")) {
            while (rs.next()) {
                String cat = rs.getString("category");
                if (cat == null || cat.isEmpty()) cat = "N/A";
                pieData.add(new PieChart.Data(cat, rs.getInt("count")));
            }
            timeTrackingPieChart.setData(pieData);
        } catch (SQLException e) { e.printStackTrace(); }

        performanceLineChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Deadlines");
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT deadline, COUNT(*) as count FROM tasks WHERE deadline IS NOT NULL GROUP BY deadline ORDER BY deadline ASC LIMIT 7")) {
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("deadline"), rs.getInt("count")));
            }
            performanceLineChart.getData().add(series);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- 3. CHARGEMENT DONNÉES (CRUD) ---
    private void loadData() {
        userList.clear();
        // ✅ CORRECTION 1: On select 'role' au lieu de 'is_admin'
        String query = "SELECT user_id, email, role FROM users";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("user_id");
                String email = rs.getString("email");
                String role = rs.getString("role"); // On récupère la string "admin" ou "user"

                userList.add(new User(id, email, role));
            }
            userTable.setItems(userList);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur BDD", "Impossible de charger les utilisateurs: " + e.getMessage());
        }
        setupSearch();
    }

    // --- 4. ACTIONS CRUD ---

    @FXML
    private void handleAddUser() {
        if (emailInput.getText().isEmpty() || passwordInput.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez remplir l'email et le mot de passe.");
            return;
        }

        // ✅ CORRECTION 2: On transforme la CheckBox en String "admin" ou "user"
        String role = isAdminInput.isSelected() ? "admin" : "user";

        // ✅ CORRECTION SQL: On utilise la colonne 'role'
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement("INSERT INTO users (email, password_hash, role) VALUES (?, ?, ?)")) {

            pst.setString(1, emailInput.getText());
            pst.setString(2, passwordInput.getText());
            pst.setString(3, role); // On envoie le String, PAS le TableColumn

            pst.executeUpdate();

            loadData();
            handleClear();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur ajouté avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        }
    }

    @FXML
    private void handleUpdateUser() {
        if (selectedUserId == -1) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez cliquer sur 'Edit' dans le tableau d'abord.");
            return;
        }

        String newEmail = emailInput.getText();
        String newPass = passwordInput.getText();
        // ✅ CORRECTION 3: Logic role pour update
        String role = isAdminInput.isSelected() ? "admin" : "user";

        if (newEmail.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "L'email ne peut pas être vide.");
            return;
        }

        // SCÉNARIO 1 : Mot de passe vide -> On met à jour Email + Role
        if (newPass.isEmpty()) {
            // ✅ CORRECTION SQL
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pst = conn.prepareStatement("UPDATE users SET email = ?, role = ? WHERE user_id = ?")) {
                pst.setString(1, newEmail);
                pst.setString(2, role); // setString, pas setInt
                pst.setInt(3, selectedUserId);
                pst.executeUpdate();

                loadData();
                handleClear();
                showAlert(Alert.AlertType.INFORMATION, "Mise à jour", "Email et Rôle mis à jour (Mot de passe inchangé).");
            } catch (SQLException e) { e.printStackTrace(); }
        }
        // SCÉNARIO 2 : Mot de passe rempli -> On met à jour TOUT
        else {
            // ✅ CORRECTION SQL
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pst = conn.prepareStatement("UPDATE users SET email = ?, password_hash = ?, role = ? WHERE user_id = ?")) {
                pst.setString(1, newEmail);
                pst.setString(2, newPass);
                pst.setString(3, role); // setString
                pst.setInt(4, selectedUserId);
                pst.executeUpdate();

                loadData();
                handleClear();
                showAlert(Alert.AlertType.INFORMATION, "Mise à jour", "Tout a été mis à jour avec succès !");
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void handlePromoteUser() {
        if (selectedUserId != -1) {
            // ✅ CORRECTION SQL: Update role = 'admin'
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pst = conn.prepareStatement("UPDATE users SET role = 'admin' WHERE user_id = ?")) {
                pst.setInt(1, selectedUserId);
                pst.executeUpdate();
                loadData();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "L'utilisateur est maintenant Administrateur ! 👮‍♂️");
            } catch (SQLException e) { e.printStackTrace(); }
        } else {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un utilisateur à promouvoir.");
        }
    }

    @FXML
    private void handleClear() {
        emailInput.clear();
        passwordInput.clear();
        if (isAdminInput != null) isAdminInput.setSelected(false);
        selectedUserId = -1;
    }

    // --- 5. TABLEAU & BOUTONS ---

    private void setupUserTable() {
        colId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        colEmail.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        colRole.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole()));
        colId.setStyle("-fx-alignment: CENTER;");
    }

    private void addButtonToTable() {
        colAction.setCellFactory(param -> new TableCell<>() {
            final Button btnEdit = new Button("Edit");
            final Button btnDel = new Button("Delete");
            final HBox pane = new HBox(5, btnEdit, btnDel);

            {
                btnEdit.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand;");
                btnDel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");

                // --- ACTION EDIT ---
                btnEdit.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    emailInput.setText(u.getEmail());
                    passwordInput.setText("");

                    // ✅ CORRECTION: On vérifie si le rôle (String) est "admin"
                    if (isAdminInput != null) {
                        isAdminInput.setSelected("admin".equalsIgnoreCase(u.getRole()));
                    }

                    selectedUserId = u.getId();
                });

                // --- ACTION DELETE ---
                btnDel.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Suppression");
                    alert.setHeaderText("Supprimer l'utilisateur ?");
                    alert.setContentText("Êtes-vous sûr de vouloir supprimer : " + u.getEmail() + " ?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                             PreparedStatement pst = conn.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
                            pst.setInt(1, u.getId());
                            pst.executeUpdate();
                            loadData();
                            showAlert(Alert.AlertType.INFORMATION, "Supprimé", "Utilisateur supprimé.");
                        } catch (SQLException ex) { ex.printStackTrace(); }
                    }
                });
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    // --- UTILITAIRES ---
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void setupSearch() {
        FilteredList<User> filteredData = new FilteredList<>(userList, b -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(user -> newVal == null || newVal.isEmpty() || user.getEmail().toLowerCase().contains(newVal.toLowerCase()));
        });
        SortedList<User> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(userTable.comparatorProperty());
        userTable.setItems(sortedData);
    }

    @FXML private void toggleTheme() {
        isDarkMode = !isDarkMode;
        if(isDarkMode) mainBorderPane.getStyleClass().add("dark-mode");
        else mainBorderPane.getStyleClass().remove("dark-mode");
    }

    @FXML private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smarttask/smarttaskmanager/view/login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene); stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static class User {
        private final int id; private final String email; private final String role;
        public User(int id, String email, String role) { this.id = id; this.email = email; this.role = role; }
        public int getId() { return id; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
    }
}