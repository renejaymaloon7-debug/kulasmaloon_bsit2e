package org.example.dao;

import org.example.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private static final String COLUMNS =
            "UserID, FullName, Username, PasswordHash, ContactNumber, Address, Role, AccountStatus";

    // ---------- Registration / Login ----------

    public boolean register(String fullName, String username, String password,
                            String contactNumber, String address, String role) {
        String sql = "INSERT INTO Users (FullName, Username, PasswordHash, ContactNumber, Address, Role, AccountStatus) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.setString(4, contactNumber);
            ps.setString(5, address);
            ps.setString(6, role.toUpperCase());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    // True if a user with this username already exists
    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM Users WHERE Username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    // Returns the user if username/password match, otherwise null
    public User login(String username, String password) {
        String sql = "SELECT " + COLUMNS + " FROM Users WHERE Username = ? AND PasswordHash = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return null;
    }

    // ---------- Dev 5 ----------

    // All accounts regardless of role
    public List<User> getAllUsers() {
        return query("SELECT " + COLUMNS + " FROM Users ORDER BY UserID");
    }

    // All accounts with AccountStatus = 'PENDING', across every role
    public List<User> getPendingUsers() {
        return query("SELECT " + COLUMNS + " FROM Users WHERE AccountStatus = 'PENDING' ORDER BY UserID");
    }

    // PENDING -> APPROVED
    public boolean approveUser(int userId) {
        String sql = "UPDATE Users SET AccountStatus = 'APPROVED' WHERE UserID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    // ---------- Dev 7 ----------

    // Update role and status of a user
    public boolean updateUserRoleAndStatus(int userId, String newRole, String newStatus) {
        String sql = "UPDATE Users SET Role = ?, AccountStatus = ? WHERE UserID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newRole.toUpperCase());
            ps.setString(2, newStatus.toUpperCase());
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    // ---------- Helpers ----------

    private List<User> query(String sql) {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return users;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("UserID"),
                rs.getString("FullName"),
                rs.getString("Username"),
                rs.getString("PasswordHash"),
                rs.getString("ContactNumber"),
                rs.getString("Address"),
                rs.getString("Role"),
                rs.getString("AccountStatus"));
    }
}