-- Smart Library Management System - Database Schema
-- Run this in MySQL before starting the application

CREATE DATABASE IF NOT EXISTS smart_library;
USE smart_library;

-- Users table (Students & Librarians)
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(15),
    role ENUM('STUDENT', 'LIBRARIAN') NOT NULL DEFAULT 'STUDENT',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Books table
CREATE TABLE IF NOT EXISTS books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    publisher VARCHAR(100),
    genre VARCHAR(50),
    year_published INT,
    total_copies INT DEFAULT 1,
    available_copies INT DEFAULT 1,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Book issues table
CREATE TABLE IF NOT EXISTS book_issues (
    issue_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    user_id INT NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    fine_amount DECIMAL(10,2) DEFAULT 0.00,
    fine_paid BOOLEAN DEFAULT FALSE,
    status ENUM('ISSUED', 'RETURNED', 'OVERDUE') DEFAULT 'ISSUED',
    FOREIGN KEY (book_id) REFERENCES books(book_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Insert default librarian account
INSERT IGNORE INTO users (username, password, full_name, email, role)
VALUES ('admin', 'admin123', 'Head Librarian', 'library@jcet.edu.in', 'LIBRARIAN');

-- Insert sample books
INSERT IGNORE INTO books (isbn, title, author, publisher, genre, year_published, total_copies, available_copies) VALUES
('978-0134685991', 'Effective Java', 'Joshua Bloch', 'Addison-Wesley', 'Programming', 2018, 3, 3),
('978-0132350884', 'Clean Code', 'Robert C. Martin', 'Prentice Hall', 'Programming', 2008, 2, 2),
('978-0201633610', 'Design Patterns', 'Gang of Four', 'Addison-Wesley', 'Programming', 1994, 2, 2),
('978-0596009205', 'Head First Java', 'Kathy Sierra', 'O Reilly', 'Programming', 2005, 4, 4),
('978-0132350884', 'The Pragmatic Programmer', 'Andrew Hunt', 'Addison-Wesley', 'Programming', 2019, 2, 2);
