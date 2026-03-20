package com.library.service;

import com.library.dao.BookDAO;
import com.library.dao.BookIssueDAO;
import com.library.dao.UserDAO;
import com.library.model.Book;
import com.library.model.BookIssue;
import com.library.model.User;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Service layer containing all business logic for the Smart Library System.
 * Demonstrates OOP: Separation of Concerns (Service vs DAO vs UI).
 */
public class LibraryService {

    private final UserDAO      userDAO      = new UserDAO();
    private final BookDAO      bookDAO      = new BookDAO();
    private final BookIssueDAO issueDAO     = new BookIssueDAO();

    // ── Authentication ────────────────────────────────────────────────────────

    public User login(String username, String password) throws SQLException {
        User user = userDAO.authenticate(username, password);
        if (user == null) throw new IllegalArgumentException("Invalid username or password.");
        return user;
    }

    // ── Student Registration ──────────────────────────────────────────────────

    public void registerStudent(String username, String password, String fullName,
                                String email, String phone) throws SQLException {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username cannot be empty.");
        if (password == null || password.length() < 4)
            throw new IllegalArgumentException("Password must be at least 4 characters.");
        if (fullName == null || fullName.isBlank())
            throw new IllegalArgumentException("Full name cannot be empty.");
        if (userDAO.usernameExists(username))
            throw new IllegalArgumentException("Username '" + username + "' already exists.");

        User student = new User(username, password, fullName, email, phone, User.Role.STUDENT);
        boolean ok = userDAO.addUser(student);
        if (!ok) throw new RuntimeException("Registration failed. Please try again.");
    }

    // ── Book Operations ───────────────────────────────────────────────────────

    public List<Book> searchBooks(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank())
            return bookDAO.getAllBooks();
        return bookDAO.searchBooks(keyword);
    }

    public List<Book> getAllBooks() throws SQLException {
        return bookDAO.getAllBooks();
    }

    public List<Book> getAvailableBooks() throws SQLException {
        return bookDAO.getAvailableBooks();
    }

    // ── Issue a Book (Student or Librarian) ───────────────────────────────────

    public String issueBook(int bookId, int userId) throws SQLException {
        Book book = bookDAO.getBookById(bookId);
        if (book == null)
            throw new IllegalArgumentException("Book not found.");
        // BUG FIX: Removed stale book.isAvailable() check - race condition
        // The atomic decrementAvailability() check below is the single source of truth

        // Check if student already has this book
        BookIssue existing = issueDAO.getActiveIssue(bookId, userId);
        if (existing != null)
            throw new IllegalStateException("This book is already issued to this student.");

        // BUG FIX: Atomic check-and-decrement - returns false if no copies available
        boolean decremented = bookDAO.decrementAvailability(bookId);
        if (!decremented)
            throw new IllegalStateException("No copies of '" + book.getTitle() + "' are currently available.");

        BookIssue issue = new BookIssue(bookId, userId);
        boolean ok = issueDAO.issueBook(issue);
        if (!ok) {
            // BUG FIX: Rollback the decrement if issue record creation failed
            bookDAO.incrementAvailability(bookId);
            throw new RuntimeException("Failed to issue book.");
        }

        return String.format("Book '%s' issued successfully! Due date: %s",
                book.getTitle(), issue.getDueDate());
    }

    // ── Return a Book ─────────────────────────────────────────────────────────

    public String returnBook(int bookId, int userId) throws SQLException {
        BookIssue issue = issueDAO.getActiveIssue(bookId, userId);
        if (issue == null)
            throw new IllegalStateException("No active issue found for this book and student.");

        BigDecimal fine = issue.calculateFine();
        boolean ok = issueDAO.returnBook(issue.getIssueId(), fine);
        if (!ok) throw new RuntimeException("Failed to process return.");

        bookDAO.incrementAvailability(bookId);

        if (fine.compareTo(BigDecimal.ZERO) > 0) {
            return String.format("Book returned. Overdue fine: ₹%.2f (Please pay at the counter).", fine);
        }
        return "Book returned successfully. No fine applied!";
    }

    // ── Borrowing History ─────────────────────────────────────────────────────

    public List<BookIssue> getBorrowingHistory(int userId) throws SQLException {
        return issueDAO.getIssuesByUser(userId);
    }

    public List<BookIssue> getActiveIssues(int userId) throws SQLException {
        return issueDAO.getActiveIssuesByUser(userId);
    }

    // ── Librarian-only Operations ─────────────────────────────────────────────

    public void addBook(Book book) throws SQLException {
        validateBook(book);
        boolean ok = bookDAO.addBook(book);
        if (!ok) throw new RuntimeException("Failed to add book.");
    }

    public void updateBook(Book book) throws SQLException {
        validateBook(book);
        boolean ok = bookDAO.updateBook(book);
        if (!ok) throw new RuntimeException("Failed to update book.");
    }

    public void deleteBook(int bookId) throws SQLException {
        boolean ok = bookDAO.deleteBook(bookId);
        if (!ok) throw new RuntimeException("Failed to delete book.");
    }

    public List<User> getAllStudents() throws SQLException {
        return userDAO.getAllStudents();
    }

    public List<BookIssue> getOverdueBooks() throws SQLException {
        return issueDAO.getOverdueIssues();
    }

    public List<BookIssue> getAllActiveIssues() throws SQLException {
        return issueDAO.getAllActiveIssues();
    }

    public void markFinePaid(int issueId) throws SQLException {
        boolean ok = issueDAO.markFinePaid(issueId);
        if (!ok) throw new RuntimeException("Failed to mark fine as paid.");
    }

    public List<String[]> getMostBorrowedBooks() throws SQLException {
        return bookDAO.getMostBorrowedBooks(10);
    }

    // ── Stats for Dashboard ───────────────────────────────────────────────────

    public int getTotalBooksIssued() throws SQLException {
        return issueDAO.getTotalBooksIssued();
    }

    public BigDecimal getTotalFinesCollected() throws SQLException {
        return issueDAO.getTotalFinesCollected();
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private void validateBook(Book book) {
        if (book.getTitle() == null || book.getTitle().isBlank())
            throw new IllegalArgumentException("Book title cannot be empty.");
        if (book.getAuthor() == null || book.getAuthor().isBlank())
            throw new IllegalArgumentException("Author cannot be empty.");
        if (book.getIsbn() == null || book.getIsbn().isBlank())
            throw new IllegalArgumentException("ISBN cannot be empty.");
        if (book.getTotalCopies() < 1)
            throw new IllegalArgumentException("Total copies must be at least 1.");
    }
}
