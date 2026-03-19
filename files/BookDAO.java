package com.library.dao;

import com.library.model.Book;
import com.library.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Books table operations.
 */
public class BookDAO {

    // ── Search ────────────────────────────────────────────────────────────────

    public List<Book> searchBooks(String keyword) throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ? ORDER BY title";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) books.add(mapRow(rs));
        }
        return books;
    }

    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY title";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) books.add(mapRow(rs));
        }
        return books;
    }

    public Book getBookById(int bookId) throws SQLException {
        String sql = "SELECT * FROM books WHERE book_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public List<Book> getAvailableBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE available_copies > 0 ORDER BY title";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) books.add(mapRow(rs));
        }
        return books;
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public boolean addBook(Book book) throws SQLException {
        String sql = "INSERT INTO books (isbn, title, author, publisher, genre, year_published, " +
                     "total_copies, available_copies) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getPublisher());
            ps.setString(5, book.getGenre());
            ps.setInt(6, book.getYearPublished());
            ps.setInt(7, book.getTotalCopies());
            ps.setInt(8, book.getTotalCopies());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateBook(Book book) throws SQLException {
        // BUG FIX: Atomic update that adjusts available_copies if total_copies is reduced
        // Prevents data inconsistency where available_copies > total_copies
        String sql = "UPDATE books SET title=?, author=?, publisher=?, genre=?, year_published=?, " +
                     "total_copies=?, " +
                     "available_copies = LEAST(available_copies, ?) " +
                     "WHERE book_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int newTotal = book.getTotalCopies();
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getPublisher());
            ps.setString(4, book.getGenre());
            ps.setInt(5, book.getYearPublished());
            ps.setInt(6, newTotal);
            ps.setInt(7, newTotal);  // LEAST(available_copies, newTotal)
            ps.setInt(8, book.getBookId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteBook(int bookId) throws SQLException {
        String sql = "DELETE FROM books WHERE book_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Availability management ───────────────────────────────────────────────

    public boolean decrementAvailability(int bookId) throws SQLException {
        String sql = "UPDATE books SET available_copies = available_copies - 1 " +
                     "WHERE book_id = ? AND available_copies > 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean incrementAvailability(int bookId) throws SQLException {
        String sql = "UPDATE books SET available_copies = available_copies + 1 " +
                     "WHERE book_id = ? AND available_copies < total_copies";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Analytics ─────────────────────────────────────────────────────────────

    public List<String[]> getMostBorrowedBooks(int limit) throws SQLException {
        List<String[]> results = new ArrayList<>();
        String sql = "SELECT b.title, b.author, COUNT(bi.issue_id) AS borrow_count " +
                     "FROM books b JOIN book_issues bi ON b.book_id = bi.book_id " +
                     "GROUP BY b.book_id ORDER BY borrow_count DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            // BUG FIX: ResultSet in try-with-resources to prevent resource leak
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new String[]{
                        rs.getString("title"),
                        rs.getString("author"),
                        String.valueOf(rs.getInt("borrow_count"))
                    });
                }
            }
        }
        return results;
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private Book mapRow(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setBookId(rs.getInt("book_id"));
        b.setIsbn(rs.getString("isbn"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setPublisher(rs.getString("publisher"));
        b.setGenre(rs.getString("genre"));
        b.setYearPublished(rs.getInt("year_published"));
        b.setTotalCopies(rs.getInt("total_copies"));
        b.setAvailableCopies(rs.getInt("available_copies"));
        Timestamp ts = rs.getTimestamp("added_at");
        if (ts != null) b.setAddedAt(ts.toLocalDateTime());
        return b;
    }
}
