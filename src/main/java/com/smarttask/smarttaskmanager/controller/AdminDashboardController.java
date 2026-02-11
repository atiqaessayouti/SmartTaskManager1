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
import javafx.scene.layout.VBox; // Import important
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class AdminDashboardController {

    @FXML private BorderPane mainBorderPane;

    // --- NAVIGATION PANES (LES BOITES QUI CHANGENT) ---
    @FXML private VBox overviewPane;       // Boite Charts & Metrics
       // Boite Settings

    // --- NAVIGATION BUTTONS (SIDEBAR) ---
    @FXML private Button btnOverview;
    @FXML private Button btnUsers;
    @FXML private Button btnSettings;

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
    @FXML private CheckBox isAdminInput;
    @FXML private TextField searchField;

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private boolean isDarkMode = false;
    private int selectedUserId = -1;

    @FXML
    public void initialize() {
        // Charger les données
        setupMetrics();
        setupAnalytics();
        setupUserTable();
        addButtonToTable();
        loadData();
        setupSearch();

        // ✅ Afficher l'Overview par défaut au démarrage
        handleShowOverview(null);
    }

    // =========================================================
    // 🧭 NAVIGATION LOGIC (LE CODE QUI FAIT MARCHER LA SIDEBAR)
    // =========================================================

    @FXML
    public void handleShowOverview(ActionEvent event) {
        // 1. Visibilité des Panes
        if(overviewPane != null) overviewPane.setVisible(true);


        // 3. Style des Boutons (Active State)
        setActiveStyle(btnOverview);
        setInactiveStyle(btnUsers);
        setInactiveStyle(btnSettings);
    }



    // Helper pour le style CSS des boutons
    private void setActiveStyle(Button btn) {
        if (btn != null) {
            btn.setStyle("-fx-background-color: #5b4bc4; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-weight: bold;");
        }
    }

    private void setInactiveStyle(Button btn) {
        if (btn != null) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-background-radius: 10;");
        }
    }

    // =========================================================
    // 📊 RESTE DU CODE (LOGIQUE EXISTANTE)
    // =========================================================

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

    private void setupAnalytics() {
        if (timeTrackingPieChart == null || performanceLineChart == null) return;

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

    private void loadData() {
        if (userTable == null) return;
        userList.clear();
        String query = "SELECT user_id, email, role FROM users";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                userList.add(new User(rs.getInt("user_id"), rs.getString("email"), rs.getString("role")));
            }
            userTable.setItems(userList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur BDD", "Unable to load users: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddUser() {
        // 1. التأكد أن الخانات ماشي خاويين
        if (emailInput.getText().isEmpty() || passwordInput.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Please fill in email and password.");
            return;
        }

        String email = emailInput.getText();
        String role = isAdminInput.isSelected() ? "admin" : "user";

        // 2. ✅ الحل: تصاوبي username من الإيميل (مثلاً ali@gmail.com كيعطينا ali)
        String generatedUsername = email.split("@")[0];

        // 3. ✅ تحديث الـ SQL باش يشمل عمود الـ username
        String sql = "INSERT INTO users (email, password_hash, role, username) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, email);
            pst.setString(2, passwordInput.getText());
            pst.setString(3, role);
            pst.setString(4, generatedUsername); // صيفطنا اليوزرنيم للداتابيز

            pst.executeUpdate();

            loadData(); // تحديث الجدول
            handleClear(); // مسح الخانات
            showAlert(Alert.AlertType.INFORMATION, "Succès", "User added successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        }
    }

    @FXML
    private void handleUpdateUser() {
        if (selectedUserId == -1) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Select a user first.");
            return;
        }
        String newEmail = emailInput.getText();
        String newPass = passwordInput.getText();
        String role = isAdminInput.isSelected() ? "admin" : "user";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            if (newPass.isEmpty()) {
                PreparedStatement pst = conn.prepareStatement("UPDATE users SET email = ?, role = ? WHERE user_id = ?");
                pst.setString(1, newEmail);
                pst.setString(2, role);
                pst.setInt(3, selectedUserId);
                pst.executeUpdate();
            } else {
                PreparedStatement pst = conn.prepareStatement("UPDATE users SET email = ?, password_hash = ?, role = ? WHERE user_id = ?");
                pst.setString(1, newEmail);
                pst.setString(2, newPass);
                pst.setString(3, role);
                pst.setInt(4, selectedUserId);
                pst.executeUpdate();
            }
            loadData();
            handleClear();
            showAlert(Alert.AlertType.INFORMATION, "Mise à jour", "User updated!");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void handlePromoteUser() {
        if (selectedUserId != -1) {
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pst = conn.prepareStatement("UPDATE users SET role = 'admin' WHERE user_id = ?")) {
                pst.setInt(1, selectedUserId);
                pst.executeUpdate();
                loadData();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "User promoted to Admin!");
            } catch (SQLException e) { e.printStackTrace(); }
        } else {
            showAlert(Alert.AlertType.WARNING, "Attention", "Select a user.");
        }
    }

    @FXML
    private void handleClear() {
        emailInput.clear();
        passwordInput.clear();
        if (isAdminInput != null) isAdminInput.setSelected(false);
        selectedUserId = -1;
    }

    private void setupUserTable() {
        if (userTable == null) return;
        colId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        colEmail.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        colRole.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole()));
        colId.setStyle("-fx-alignment: CENTER;");
    }

    private void addButtonToTable() {
        if (colAction == null) return;
        colAction.setCellFactory(param -> new TableCell<>() {
            final Button btnEdit = new Button("Edit");
            final Button btnDel = new Button("Delete");
            final HBox pane = new HBox(5, btnEdit, btnDel);

            {
                btnEdit.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand;");
                btnDel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                btnEdit.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    emailInput.setText(u.getEmail());
                    passwordInput.setText("");
                    if (isAdminInput != null) isAdminInput.setSelected("admin".equalsIgnoreCase(u.getRole()));
                    selectedUserId = u.getId();
                });
                btnDel.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Delete");
                    alert.setContentText("Delete " + u.getEmail() + "?");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                             PreparedStatement pst = conn.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
                            pst.setInt(1, u.getId());
                            pst.executeUpdate();
                            loadData();
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

    private void setupSearch() {
        if (searchField == null || userTable == null) return;
        FilteredList<User> filteredData = new FilteredList<>(userList, b -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(user -> newVal == null || newVal.isEmpty() || user.getEmail().toLowerCase().contains(newVal.toLowerCase()));
        });
        SortedList<User> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(userTable.comparatorProperty());
        userTable.setItems(sortedData);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setContentText(content); alert.showAndWait();
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