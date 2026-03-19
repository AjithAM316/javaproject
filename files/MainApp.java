package com.library.ui;

import com.library.model.User;
import com.library.service.LibraryService;
import com.library.util.ConsoleHelper;
import com.library.util.DatabaseConnection;

import java.sql.SQLException;

/**
 * ╔══════════════════════════════════════════════════════╗
 * ║         SMART LIBRARY MANAGEMENT SYSTEM              ║
 * ║    Jawaharlal College of Engineering & Technology    ║
 * ║    Group 11 — Java OOP Project                       ║
 * ╚══════════════════════════════════════════════════════╝
 *
 * Main entry point. Demonstrates OOP concepts:
 *   • Encapsulation  – private fields, public methods in all model classes
 *   • Abstraction    – DAO layer hides SQL; Service layer hides business logic
 *   • Inheritance    – User, Book, BookIssue inherit from Object; Role-based menus
 *   • Polymorphism   – service methods work for both Student & Librarian roles
 */
public class MainApp {

    private static final LibraryService service = new LibraryService();

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::closeConnection));

        boolean running = true;
        while (running) {
            printWelcome();
            int choice = ConsoleHelper.readIntRange("  Enter choice: ", 0, 3);

            try {
                switch (choice) {
                    case 1 -> login();
                    case 2 -> registerStudent();
                    case 3 -> showAbout();
                    case 0 -> {
                        ConsoleHelper.printInfo("Thank you for using Smart Library. Goodbye!");
                        running = false;
                    }
                }
            } catch (SQLException e) {
                ConsoleHelper.printError("Database connection error: " + e.getMessage());
                ConsoleHelper.printInfo("Ensure MySQL is running and credentials in DatabaseConnection.java are correct.");
                ConsoleHelper.pause();
            } catch (Exception e) {
                ConsoleHelper.printError("Unexpected error: " + e.getMessage());
                ConsoleHelper.pause();
            }
        }
    }

    // ── Welcome Screen ────────────────────────────────────────────────────────

    private static void printWelcome() {
        ConsoleHelper.clearScreen();
        System.out.println(ConsoleHelper.CYAN);
        System.out.println("  ╔══════════════════════════════════════════════════════╗");
        System.out.println("  ║         SMART LIBRARY MANAGEMENT SYSTEM              ║");
        System.out.println("  ║    Jawaharlal College of Engineering & Technology    ║");
        System.out.println("  ╚══════════════════════════════════════════════════════╝");
        System.out.println(ConsoleHelper.RESET);
        System.out.println("  1. Login");
        System.out.println("  2. Register as Student");
        System.out.println("  3. About");
        System.out.println("  0. Exit");
        ConsoleHelper.printDivider();
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    private static void login() throws SQLException {
        ConsoleHelper.printSubHeader("Login");
        String username = ConsoleHelper.readLine("  Username: ");
        String password = ConsoleHelper.readLine("  Password: ");

        try {
            User user = service.login(username, password);
            ConsoleHelper.printSuccess("Welcome, " + user.getFullName() + "! (" + user.getRole() + ")");
            ConsoleHelper.pause();

            if (user.isLibrarian()) {
                new LibrarianMenu(service, user).show();
            } else {
                new StudentMenu(service, user).show();
            }
        } catch (IllegalArgumentException e) {
            ConsoleHelper.printError(e.getMessage());
            ConsoleHelper.pause();
        }
    }

    // ── Student Registration ──────────────────────────────────────────────────

    private static void registerStudent() throws SQLException {
        ConsoleHelper.printSubHeader("Student Registration");
        String username = ConsoleHelper.readLine("  Username:    ");
        String password = ConsoleHelper.readLine("  Password:    ");
        String fullName = ConsoleHelper.readLine("  Full Name:   ");
        String email    = ConsoleHelper.readLine("  Email:       ");
        String phone    = ConsoleHelper.readLine("  Phone:       ");

        try {
            service.registerStudent(username, password, fullName, email, phone);
            ConsoleHelper.printSuccess("Registration successful! You can now log in.");
        } catch (IllegalArgumentException e) {
            ConsoleHelper.printError(e.getMessage());
        }
        ConsoleHelper.pause();
    }

    // ── About ─────────────────────────────────────────────────────────────────

    private static void showAbout() {
        ConsoleHelper.clearScreen();
        ConsoleHelper.printHeader("About — Smart Library Management System");
        System.out.println();
        System.out.println("  A Java-based OOP project for automating library operations.");
        System.out.println();
        System.out.println("  Key Features:");
        System.out.println("   • Role-based access (Student / Librarian)");
        System.out.println("   • Book catalogue with real-time availability");
        System.out.println("   • Automated fine calculation (₹2/day overdue)");
        System.out.println("   • Borrowing history & analytics reports");
        System.out.println("   • MySQL backend via JDBC");
        System.out.println();
        System.out.println("  OOP Concepts Demonstrated:");
        System.out.println("   • Encapsulation  — private fields, getters/setters");
        System.out.println("   • Abstraction    — DAO / Service layers");
        System.out.println("   • Inheritance    — User.Role enum, model hierarchy");
        System.out.println("   • Polymorphism   — unified service, role-specific UI");
        System.out.println("   • Exception Handling — custom business exceptions");
        System.out.println("   • JDBC Connectivity  — PreparedStatements, ResultSets");
        System.out.println();
        System.out.println("  Group 11 Members:");
        System.out.println("   • Ajith AM");
        System.out.println("   • Abin Jayaram V");
        System.out.println("   • Safad Asharaf");
        System.out.println("   • Sreehari P");
        System.out.println();
        System.out.println("  Jawaharlal College of Engineering & Technology");
        System.out.println("  Lakkidi, Palakkad, Kerala");
        ConsoleHelper.pause();
    }
}
