package com.library.ui;

import com.library.model.User;
import com.library.service.LibraryService;
import com.library.util.ConsoleHelper;
import com.library.util.DatabaseConnection;
import com.library.web.WebServer;

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

        System.out.println("  ╔══════════════════════════════════════════════════════╗");
        System.out.println("  ║         SMART LIBRARY MANAGEMENT SYSTEM              ║");
        System.out.println("  ║               Starting Web Server...                 ║");
        System.out.println("  ╚══════════════════════════════════════════════════════╝");
        System.out.println();

        try {
            // Auto-migrate schema snippet
            try (java.sql.Connection conn = DatabaseConnection.getConnection();
                 java.sql.Statement st = conn.createStatement()) {
                try { st.execute("ALTER TABLE books ADD COLUMN description TEXT"); } catch (Exception ignore) {}
                
                // Hydrate legacy entries cleanly
                st.executeUpdate("UPDATE books SET genre = 'Computer Science' WHERE isbn IN ('978-0134685991', '978-0132350884', '978-0201633610', '978-0596009205', '978-0135957059')");
                
                // Invoke massive 100-Book Procedural Generator
                com.library.util.DatabaseSeeder.seedIfNeeded(conn);

            } catch (Exception e) {
                // Silently ignore
            }

            // Start Server
            WebServer webServer = new WebServer(service);
            webServer.start(8080);
            
            // Auto open browser
            if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                try {
                    System.out.println("[Web] Opening http://localhost:8080 in your default browser...");
                    java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:8080"));
                } catch (Exception e) {
                    System.out.println("[Web] Could not auto-open browser. Please go to http://localhost:8080 manually.");
                }
            }
            
            // Keep main thread alive
            Thread.currentThread().join();

        } catch (Exception e) {
            ConsoleHelper.printError("Unexpected error: " + e.getMessage());
        }
    }


}
