package com.library.ui;

import com.library.model.Book;
import com.library.model.BookIssue;
import com.library.model.User;
import com.library.service.LibraryService;
import com.library.util.ConsoleHelper;

import java.sql.SQLException;
import java.util.List;

/**
 * Console UI for the Student role.
 * Demonstrates OOP: Single Responsibility (only handles student UI).
 */
public class StudentMenu {

    private final LibraryService service;
    private final User currentUser;

    public StudentMenu(LibraryService service, User user) {
        this.service     = service;
        this.currentUser = user;
    }

    public void show() {
        boolean running = true;
        while (running) {
            ConsoleHelper.clearScreen();
            ConsoleHelper.printHeader("SMART LIBRARY — Student Portal");
            System.out.printf("  Welcome, %s%s%s!%n%n",
                    ConsoleHelper.BOLD, currentUser.getFullName(), ConsoleHelper.RESET);

            System.out.println("  1. Search Books");
            System.out.println("  2. View All Available Books");
            System.out.println("  3. Issue a Book");
            System.out.println("  4. Return a Book");
            System.out.println("  5. My Borrowing History");
            System.out.println("  6. My Active Issues");
            System.out.println("  0. Logout");
            ConsoleHelper.printDivider();

            int choice = ConsoleHelper.readIntRange("  Enter choice: ", 0, 6);

            try {
                switch (choice) {
                    case 1 -> searchBooks();
                    case 2 -> viewAvailableBooks();
                    case 3 -> issueBook();
                    case 4 -> returnBook();
                    case 5 -> viewBorrowingHistory();
                    case 6 -> viewActiveIssues();
                    case 0 -> { ConsoleHelper.printInfo("Logged out. Goodbye!"); running = false; }
                }
            } catch (SQLException e) {
                ConsoleHelper.printError("Database error: " + e.getMessage());
                ConsoleHelper.pause();
            }
        }
    }

    // ── Menu Actions ──────────────────────────────────────────────────────────

    private void searchBooks() throws SQLException {
        ConsoleHelper.printSubHeader("Search Books");
        String keyword = ConsoleHelper.readLine("  Enter title / author / ISBN: ");
        List<Book> books = service.searchBooks(keyword);
        printBookList(books);
        ConsoleHelper.pause();
    }

    private void viewAvailableBooks() throws SQLException {
        ConsoleHelper.printSubHeader("Available Books");
        List<Book> books = service.getAvailableBooks();
        printBookList(books);
        ConsoleHelper.pause();
    }

    private void issueBook() throws SQLException {
        ConsoleHelper.printSubHeader("Issue a Book");
        List<Book> books = service.getAvailableBooks();
        if (books.isEmpty()) {
            ConsoleHelper.printWarning("No books are currently available.");
            ConsoleHelper.pause();
            return;
        }
        printBookList(books);
        int bookId = ConsoleHelper.readInt("  Enter Book ID to issue (0 to cancel): ");
        if (bookId == 0) return;

        String result = service.issueBook(bookId, currentUser.getUserId());
        ConsoleHelper.printSuccess(result);
        ConsoleHelper.pause();
    }

    private void returnBook() throws SQLException {
        ConsoleHelper.printSubHeader("Return a Book");
        List<BookIssue> active = service.getActiveIssues(currentUser.getUserId());
        if (active.isEmpty()) {
            ConsoleHelper.printInfo("You have no books currently issued.");
            ConsoleHelper.pause();
            return;
        }
        printIssueList(active);
        int bookId = ConsoleHelper.readInt("  Enter Book ID to return (0 to cancel): ");
        if (bookId == 0) return;

        String result = service.returnBook(bookId, currentUser.getUserId());
        ConsoleHelper.printSuccess(result);
        ConsoleHelper.pause();
    }

    private void viewBorrowingHistory() throws SQLException {
        ConsoleHelper.printSubHeader("My Borrowing History");
        List<BookIssue> history = service.getBorrowingHistory(currentUser.getUserId());
        if (history.isEmpty()) {
            ConsoleHelper.printInfo("No borrowing history found.");
        } else {
            ConsoleHelper.printTableHeader("IssueID", "Book Title", "Issue Date", "Due Date", "Status", "Fine");
            for (BookIssue bi : history) {
                ConsoleHelper.printTableRow(
                    String.valueOf(bi.getIssueId()),
                    truncate(bi.getBookTitle(), 18),
                    bi.getIssueDate().toString(),
                    bi.getDueDate().toString(),
                    bi.getStatus().name(),
                    "₹" + bi.getFineAmount()
                );
            }
            ConsoleHelper.printTableFooter(6);
        }
        ConsoleHelper.pause();
    }

    private void viewActiveIssues() throws SQLException {
        ConsoleHelper.printSubHeader("My Active Issues");
        List<BookIssue> active = service.getActiveIssues(currentUser.getUserId());
        if (active.isEmpty()) {
            ConsoleHelper.printInfo("No active issues.");
        } else {
            printIssueList(active);
        }
        ConsoleHelper.pause();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void printBookList(List<Book> books) {
        if (books.isEmpty()) {
            ConsoleHelper.printWarning("No books found.");
            return;
        }
        ConsoleHelper.printTableHeader("ID", "Title", "Author", "Genre", "Year", "Available");
        for (Book b : books) {
            String availability = b.isAvailable()
                ? ConsoleHelper.GREEN + b.getAvailableCopies() + "/" + b.getTotalCopies() + ConsoleHelper.RESET
                : ConsoleHelper.RED + "Not Available" + ConsoleHelper.RESET;
            System.out.printf("│ %-20s│ %-20s│ %-20s│ %-20s│ %-20s│ %-20s│%n",
                b.getBookId(), truncate(b.getTitle(), 18), truncate(b.getAuthor(), 18),
                b.getGenre(), b.getYearPublished(), availability);
        }
        ConsoleHelper.printTableFooter(6);
    }

    private void printIssueList(List<BookIssue> issues) {
        ConsoleHelper.printTableHeader("BookID", "Title", "Issue Date", "Due Date", "Overdue Days");
        for (BookIssue bi : issues) {
            String overdue = bi.isOverdue()
                ? ConsoleHelper.RED + bi.getOverdueDays() + " days" + ConsoleHelper.RESET
                : ConsoleHelper.GREEN + "On time" + ConsoleHelper.RESET;
            System.out.printf("│ %-20s│ %-20s│ %-20s│ %-20s│ %-20s│%n",
                bi.getBookId(), truncate(bi.getBookTitle(), 18),
                bi.getIssueDate(), bi.getDueDate(), overdue);
        }
        ConsoleHelper.printTableFooter(5);
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
