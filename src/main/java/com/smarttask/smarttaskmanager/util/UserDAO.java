public List<User> getAllUsers() {
    List<User> users = new ArrayList<>();
    String sql = "SELECT user_id, email FROM users";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        while (rs.next()) {
            users.add(new User(rs.getInt("user_id"), rs.getString("email")));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return users;
}