package org.example.dao;

import org.example.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM Users WHERE Username = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Always saves with PENDING status. */
    public boolean register(User u) {
        String sql = "INSERT INTO Users (FullName, Username, PasswordHash, ContactNumber, Address, Role, AccountStatus) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'PENDING')";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getPasswordHash());
            ps.setString(4, u.getContactNumber());
            ps.setString(5, u.getAddress());
            ps.setString(6, u.getRole());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            return false;
        }
    }

    public User findByCredentials(String username, String passwordHash) {
        String sql = "SELECT * FROM Users WHERE Username = ? AND PasswordHash = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** All registered accounts, any role, any status. */
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM Users ORDER BY UserID";
        List<User> users = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) users.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    /** Accounts of every role whose status is PENDING. */
    public List<User> getPendingUsers() {
        String sql = "SELECT * FROM Users WHERE AccountStatus = ? ORDER BY UserID";
        List<User> users = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "PENDING");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    /** PENDING -> APPROVED. Returns true only if a pending row was updated. */
    public boolean approveUser(int userId) {
        String sql = "UPDATE Users SET AccountStatus = 'APPROVED' WHERE UserID = ? AND AccountStatus = 'PENDING'";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(rs.getInt("UserID"), rs.getString("FullName"),
                rs.getString("Username"), rs.getString("PasswordHash"),
                rs.getString("ContactNumber"), rs.getString("Address"),
                rs.getString("Role"), rs.getString("AccountStatus"));
    }
}