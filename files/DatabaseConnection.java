package com.library.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for managing MySQL database connections.
 * Implements Singleton pattern to reuse a single connection.
 */
public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/smart_library?useSSL=false&serverTimezone=UTC";
    private static final String USER     = "root";
    private static final String PASSWORD = "your_password"; // Change this

    private static Connection connection = null;

    // Private constructor – no instantiation
    private DatabaseConnection() {}

    /**
     * Returns the active database connection, creating one if necessary.
     * BUG FIX: Added synchronized for thread-safe Singleton pattern
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("[DB] Connected to MySQL database successfully.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found. Add mysql-connector-java to classpath.", e);
            }
        }
        return connection;
    }

    /**
     * Closes the database connection if open.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DB] Database connection closed.");
            } catch (SQLException e) {
                System.err.println("[DB] Error closing connection: " + e.getMessage());
            }
        }
    }
}
