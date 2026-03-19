package com.library.dao;

import com.library.model.User;
import com.library.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User table operations.
 * Demonstrates OOP: Abstraction – UI knows nothing about SQL.
 */
public class UserDAO {

    // ── Authentication ────────────────────────────────────────────────────────

    /**
     * Validates credentials and returns the matching User, or null if invalid.
     */
    public User authenticate(String username, String password) throws SQLException {
        // BUG FIX: Added input validation to prevent null/blank credential queries
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return null;
        }
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND is_active = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            // BUG FIX: ResultSet in try-with-resources to prevent resource leak
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public boolean addUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, full_name, email, phone, role) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getRole().name());
            return ps.executeUpdate() > 0;
        }
    }

    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            // BUG FIX: ResultSet in try-with-resources to prevent resource leak
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<User> getAllStudents() throws SQLException {
        List<User> students = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'STUDENT' ORDER BY full_name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) students.add(mapRow(rs));
        }
        return students;
    }

    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET full_name=?, email=?, phone=?, is_active=? WHERE user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setBoolean(4, user.isActive());
            ps.setInt(5, user.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deactivateUser(int userId) throws SQLException {
        String sql = "UPDATE users SET is_active = FALSE WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            // BUG FIX: ResultSet in try-with-resources to prevent resource leak
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setRole(User.Role.valueOf(rs.getString("role")));
        u.setActive(rs.getBoolean("is_active"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());
        return u;
    }
}
