package com.library.dao;

import com.library.model.BookIssue;
import com.library.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for BookIssue table operations.
 * Handles issuing, returning, and fine management.
 */
public class BookIssueDAO {

    // ── Issue & Return ────────────────────────────────────────────────────────

    public boolean issueBook(BookIssue issue) throws SQLException {
        String sql = "INSERT INTO book_issues (book_id, user_id, issue_date, due_date, status) " +
                     "VALUES (?, ?, ?, ?, 'ISSUED')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, issue.getBookId());
            ps.setInt(2, issue.getUserId());
            ps.setDate(3, Date.valueOf(issue.getIssueDate()));
            ps.setDate(4, Date.valueOf(issue.getDueDate()));
            return ps.executeUpdate() > 0;
        }
    }

    public boolean returnBook(int issueId, BigDecimal fine) throws SQLException {
        String sql = "UPDATE book_issues SET return_date=?, fine_amount=?, status='RETURNED' " +
                     "WHERE issue_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ps.setBigDecimal(2, fine);
            ps.setInt(3, issueId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean markFinePaid(int issueId) throws SQLException {
        String sql = "UPDATE book_issues SET fine_paid = TRUE WHERE issue_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, issueId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<BookIssue> getIssuesByUser(int userId) throws SQLException {
        List<BookIssue> issues = new ArrayList<>();
        String sql = "SELECT bi.*, b.title AS book_title, u.full_name AS user_name " +
                     "FROM book_issues bi " +
                     "JOIN books b ON bi.book_id = b.book_id " +
                     "JOIN users u ON bi.user_id = u.user_id " +
                     "WHERE bi.user_id = ? ORDER BY bi.issue_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            // BUG FIX: ResultSet in try-with-resources to prevent resource leak
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) issues.add(mapRow(rs));
            }
        }
        return issues;
    }

    public List<BookIssue> getActiveIssuesByUser(int userId) throws SQLException {
        List<BookIssue> issues = new ArrayList<>();
        String sql = "SELECT bi.*, b.title AS book_title, u.full_name AS user_name " +
                     "FROM book_issues bi " +
                     "JOIN books b ON bi.book_id = b.book_id " +
                     "JOIN users u ON bi.user_id = u.user_id " +
                     "WHERE bi.user_id = ? AND bi.status = 'ISSUED' ORDER BY bi.due_date";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            // BUG FIX: ResultSet in try-with-resources to prevent resource leak
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) issues.add(mapRow(rs));
            }
        }
        return issues;
    }

    public List<BookIssue> getAllActiveIssues() throws SQLException {
        List<BookIssue> issues = new ArrayList<>();
        String sql = "SELECT bi.*, b.title AS book_title, u.full_name AS user_name " +
                     "FROM book_issues bi " +
                     "JOIN books b ON bi.book_id = b.book_id " +
                     "JOIN users u ON bi.user_id = u.user_id " +
                     "WHERE bi.status = 'ISSUED' ORDER BY bi.due_date";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) issues.add(mapRow(rs));
        }
        return issues;
    }

    public List<BookIssue> getOverdueIssues() throws SQLException {
        List<BookIssue> issues = new ArrayList<>();
        String sql = "SELECT bi.*, b.title AS book_title, u.full_name AS user_name " +
                     "FROM book_issues bi " +
                     "JOIN books b ON bi.book_id = b.book_id " +
                     "JOIN users u ON bi.user_id = u.user_id " +
                     "WHERE bi.status = 'ISSUED' AND bi.due_date < CURDATE() ORDER BY bi.due_date";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) issues.add(mapRow(rs));
        }
        return issues;
    }

    public BookIssue getActiveIssue(int bookId, int userId) throws SQLException {
        String sql = "SELECT bi.*, b.title AS book_title, u.full_name AS user_name " +
                     "FROM book_issues bi " +
                     "JOIN books b ON bi.book_id = b.book_id " +
                     "JOIN users u ON bi.user_id = u.user_id " +
                     "WHERE bi.book_id = ? AND bi.user_id = ? AND bi.status = 'ISSUED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            ps.setInt(2, userId);
            // BUG FIX: ResultSet in try-with-resources to prevent resource leak
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    public int getTotalBooksIssued() throws SQLException {
        String sql = "SELECT COUNT(*) FROM book_issues";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public BigDecimal getTotalFinesCollected() throws SQLException {
        String sql = "SELECT COALESCE(SUM(fine_amount), 0) FROM book_issues WHERE fine_paid = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) return rs.getBigDecimal(1);
        }
        return BigDecimal.ZERO;
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private BookIssue mapRow(ResultSet rs) throws SQLException {
        BookIssue bi = new BookIssue();
        bi.setIssueId(rs.getInt("issue_id"));
        bi.setBookId(rs.getInt("book_id"));
        bi.setUserId(rs.getInt("user_id"));
        bi.setBookTitle(rs.getString("book_title"));
        bi.setUserName(rs.getString("user_name"));
        bi.setIssueDate(rs.getDate("issue_date").toLocalDate());
        bi.setDueDate(rs.getDate("due_date").toLocalDate());
        Date retDate = rs.getDate("return_date");
        if (retDate != null) bi.setReturnDate(retDate.toLocalDate());
        bi.setFineAmount(rs.getBigDecimal("fine_amount"));
        bi.setFinePaid(rs.getBoolean("fine_paid"));
        bi.setStatus(BookIssue.Status.valueOf(rs.getString("status")));
        return bi;
    }
}
