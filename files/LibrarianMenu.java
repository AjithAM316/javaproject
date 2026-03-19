package com.library.ui;

import com.library.model.Book;
import com.library.model.BookIssue;
import com.library.model.User;
import com.library.service.LibraryService;
import com.library.util.ConsoleHelper;

import java.sql.SQLException;
import java.util.List;

/**
 * Console UI for the Librarian/Admin role.
 * Demonstrates OOP: Polymorphism – both StudentMenu and LibrarianMenu
 *   extend distinct panels but share the same LibraryService.
 */
public class LibrarianMenu {

    private final LibraryService service;
    private final User currentUser;

    public LibrarianMenu(LibraryService service, User user) {
        this.service     = service;
        this.currentUser = user;
    }

    public void show() {
        boolean running = true;
        while (running) {
            ConsoleHelper.clearScreen();
            ConsoleHelper.printHeader("SMART LIBRARY — Librarian Dashboard");
            System.out.printf("  Admin: %s%s%s%n%n",
                    ConsoleHelper.BOLD, currentUser.getFullName(), ConsoleHelper.RESET);

            System.out.println("  ── BOOK MANAGEMENT ───────────────────");
            System.out.println("  1. Add New Book");
            System.out.println("  2. Update Book");
            System.out.println("  3. Delete Book");
            System.out.println("  4. View All Books");
            System.out.println("  5. Search Books");
            System.out.println();
            System.out.println("  ── ISSUE & RETURN ────────────────────");
            System.out.println("  6. Issue Book to Student");
            System.out.println("  7. Return Book from Student");
            System.out.println();
            System.out.println("  ── MONITORING & REPORTS ──────────────");
            System.out.println("  8. View All Active Issues");
            System.out.println("  9. View Overdue Books");
            System.out.println(" 10. Manage Students");
            System.out.println(" 11. Mark Fine as Paid");
            System.out.println(" 12. Analytics Report");
            System.out.println();
            System.out.println("  0. Logout");
            ConsoleHelper.printDivider();

            int choice = ConsoleHelper.readIntRange("  Enter choice: ", 0, 12);

            try {
                switch (choice) {
                    case  1 -> addBook();
                    case  2 -> updateBook();
                    case  3 -> deleteBook();
                    case  4 -> viewAllBooks();
                    case  5 -> searchBooks();
                    case  6 -> issueBook();
                    case  7 -> returnBook();
                    case  8 -> viewAllActiveIssues();
                    case  9 -> viewOverdueBooks();
                    case 10 -> viewStudents();
                    case 11 -> markFinePaid();
                    case 12 -> showAnalytics();
                    case  0 -> { ConsoleHelper.printInfo("Logged out. Goodbye!"); running = false; }
                }
            } catch (SQLException e) {
                ConsoleHelper.printError("Database error: " + e.getMessage());
                ConsoleHelper.pause();
            } catch (IllegalArgumentException | IllegalStateException e) {
                ConsoleHelper.printError(e.getMessage());
                ConsoleHelper.pause();
            }
        }
    }

    // ── Book Management ───────────────────────────────────────────────────────

    private void addBook() throws SQLException {
        ConsoleHelper.printSubHeader("Add New Book");
        String isbn      = ConsoleHelper.readLine("  ISBN:           ");
        String title     = ConsoleHelper.readLine("  Title:          ");
        String author    = ConsoleHelper.readLine("  Author:         ");
        String publisher = ConsoleHelper.readLine("  Publisher:      ");
        String genre     = ConsoleHelper.readLine("  Genre:          ");
        int year         = ConsoleHelper.readInt("  Year Published: ");
        int copies       = ConsoleHelper.readInt("  Total Copies:   ");

        Book book = new Book(isbn, title, author, publisher, genre, year, copies);
        service.addBook(book);
        ConsoleHelper.printSuccess("Book '" + title + "' added successfully!");
        ConsoleHelper.pause();
    }

    private void updateBook() throws SQLException {
        ConsoleHelper.printSubHeader("Update Book");
        viewAllBooks();
        int bookId = ConsoleHelper.readInt("  Enter Book ID to update (0 to cancel): ");
        if (bookId == 0) return;

        Book book = service.getAllBooks().stream()
                .filter(b -> b.getBookId() == bookId).findFirst().orElse(null);
        if (book == null) { ConsoleHelper.printError("Book not found."); ConsoleHelper.pause(); return; }

        System.out.println("  (Leave blank to keep existing value)");
        String title  = ConsoleHelper.readLine("  Title [" + book.getTitle() + "]: ");
        String author = ConsoleHelper.readLine("  Author [" + book.getAuthor() + "]: ");
        String genre  = ConsoleHelper.readLine("  Genre [" + book.getGenre() + "]: ");

        if (!title.isBlank())  book.setTitle(title);
        if (!author.isBlank()) book.setAuthor(author);
        if (!genre.isBlank())  book.setGenre(genre);

        service.updateBook(book);
        ConsoleHelper.printSuccess("Book updated successfully!");
        ConsoleHelper.pause();
    }

    private void deleteBook() throws SQLException {
        ConsoleHelper.printSubHeader("Delete Book");
        viewAllBooks();
        int bookId = ConsoleHelper.readInt("  Enter Book ID to delete (0 to cancel): ");
        if (bookId == 0) return;

        if (ConsoleHelper.confirm("  Are you sure you want to delete Book ID " + bookId + "?")) {
            service.deleteBook(bookId);
            ConsoleHelper.printSuccess("Book deleted.");
        }
        ConsoleHelper.pause();
    }

    private void viewAllBooks() throws SQLException {
        ConsoleHelper.printSubHeader("All Books");
        List<Book> books = service.getAllBooks();
        printBookList(books);
    }

    private void searchBooks() throws SQLException {
        ConsoleHelper.printSubHeader("Search Books");
        String keyword = ConsoleHelper.readLine("  Enter keyword: ");
        printBookList(service.searchBooks(keyword));
        ConsoleHelper.pause();
    }

    // ── Issue & Return ────────────────────────────────────────────────────────

    private void issueBook() throws SQLException {
        ConsoleHelper.printSubHeader("Issue Book");
        viewAllBooks();
        int bookId = ConsoleHelper.readInt("  Enter Book ID to issue: ");
        viewStudents();
        int userId = ConsoleHelper.readInt("  Enter Student User ID: ");

        String result = service.issueBook(bookId, userId);
        ConsoleHelper.printSuccess(result);
        ConsoleHelper.pause();
    }

    private void returnBook() throws SQLException {
        ConsoleHelper.printSubHeader("Return Book");
        viewAllActiveIssues();
        int bookId = ConsoleHelper.readInt("  Enter Book ID being returned: ");
        int userId = ConsoleHelper.readInt("  Enter Student User ID: ");

        String result = service.returnBook(bookId, userId);
        ConsoleHelper.printSuccess(result);
        ConsoleHelper.pause();
    }

    // ── Monitoring ────────────────────────────────────────────────────────────

    private void viewAllActiveIssues() throws SQLException {
        ConsoleHelper.printSubHeader("All Active Issues");
        List<BookIssue> issues = service.getAllActiveIssues();
        printIssueList(issues);
        ConsoleHelper.pause();
    }

    private void viewOverdueBooks() throws SQLException {
        ConsoleHelper.printSubHeader("Overdue Books");
        List<BookIssue> overdue = service.getOverdueBooks();
        if (overdue.isEmpty()) {
            ConsoleHelper.printSuccess("No overdue books! Great job.");
        } else {
            ConsoleHelper.printWarning(overdue.size() + " overdue book(s) found:");
            printIssueList(overdue);
        }
        ConsoleHelper.pause();
    }

    private void viewStudents() throws SQLException {
        ConsoleHelper.printSubHeader("Registered Students");
        List<User> students = service.getAllStudents();
        if (students.isEmpty()) {
            ConsoleHelper.printInfo("No students registered.");
            return;
        }
        ConsoleHelper.printTableHeader("UserID", "Username", "Full Name", "Email", "Phone");
        for (User u : students) {
            ConsoleHelper.printTableRow(
                String.valueOf(u.getUserId()), u.getUsername(),
                truncate(u.getFullName(), 18), u.getEmail(), u.getPhone()
            );
        }
        ConsoleHelper.printTableFooter(5);
        ConsoleHelper.pause();
    }

    private void markFinePaid() throws SQLException {
        ConsoleHelper.printSubHeader("Mark Fine as Paid");
        int issueId = ConsoleHelper.readInt("  Enter Issue ID: ");
        service.markFinePaid(issueId);
        ConsoleHelper.printSuccess("Fine marked as paid for Issue ID " + issueId + ".");
        ConsoleHelper.pause();
    }

    private void showAnalytics() throws SQLException {
        ConsoleHelper.clearScreen();
        ConsoleHelper.printHeader("Library Analytics Report");

        int    totalIssued = service.getTotalBooksIssued();
        int    overdue     = service.getOverdueBooks().size();
        var    fines       = service.getTotalFinesCollected();

        System.out.println();
        System.out.printf("  %s%-35s%s %d%n", ConsoleHelper.BOLD, "Total Books Issued:", ConsoleHelper.RESET, totalIssued);
        System.out.printf("  %s%-35s%s %d%n", ConsoleHelper.BOLD, "Currently Overdue:", ConsoleHelper.RESET, overdue);
        System.out.printf("  %s%-35s%s ₹%.2f%n", ConsoleHelper.BOLD, "Total Fines Collected:", ConsoleHelper.RESET, fines);
        System.out.println();

        ConsoleHelper.printSubHeader("Most Borrowed Books");
        List<String[]> top = service.getMostBorrowedBooks();
        if (top.isEmpty()) {
            ConsoleHelper.printInfo("No borrowing data yet.");
        } else {
            ConsoleHelper.printTableHeader("Title", "Author", "Times Borrowed");
            for (String[] row : top) {
                ConsoleHelper.printTableRow(truncate(row[0], 18), truncate(row[1], 18), row[2]);
            }
            ConsoleHelper.printTableFooter(3);
        }
        ConsoleHelper.pause();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void printBookList(List<Book> books) {
        if (books.isEmpty()) { ConsoleHelper.printWarning("No books found."); return; }
        ConsoleHelper.printTableHeader("ID", "Title", "Author", "ISBN", "Genre", "Avail");
        for (Book b : books) {
            System.out.printf("│ %-20s│ %-20s│ %-20s│ %-20s│ %-20s│ %-20s│%n",
                b.getBookId(), truncate(b.getTitle(), 18), truncate(b.getAuthor(), 18),
                b.getIsbn(), b.getGenre(),
                b.getAvailableCopies() + "/" + b.getTotalCopies());
        }
        ConsoleHelper.printTableFooter(6);
    }

    private void printIssueList(List<BookIssue> issues) {
        if (issues.isEmpty()) { ConsoleHelper.printInfo("No records found."); return; }
        ConsoleHelper.printTableHeader("IssueID", "Book", "Student", "Due Date", "Overdue", "Fine");
        for (BookIssue bi : issues) {
            long od = bi.getOverdueDays();
            String overdue = od > 0
                ? ConsoleHelper.RED + od + "d" + ConsoleHelper.RESET
                : ConsoleHelper.GREEN + "OK" + ConsoleHelper.RESET;
            System.out.printf("│ %-20s│ %-20s│ %-20s│ %-20s│ %-20s│ %-20s│%n",
                bi.getIssueId(), truncate(bi.getBookTitle(), 18),
                truncate(bi.getUserName(), 18), bi.getDueDate(),
                overdue, "₹" + bi.calculateFine());
        }
        ConsoleHelper.printTableFooter(6);
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
