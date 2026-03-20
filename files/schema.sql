-- Smart Library Management System - Database Schema
-- Run this in MySQL before starting the application

CREATE DATABASE IF NOT EXISTS library_db;
USE library_db;

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
    description TEXT,
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

-- Initial Sample Books
INSERT IGNORE INTO books (isbn, title, author, publisher, genre, year_published, description, total_copies, available_copies) VALUES
('978-0134685991', 'Effective Java', 'Joshua Bloch', 'Addison-Wesley', 'Computer Science', 2018, 'A comprehensive guide to best practices for the Java platform.', 3, 3),
('978-0132350884', 'Clean Code', 'Robert C. Martin', 'Prentice Hall', 'Computer Science', 2008, 'Even bad code can function. But if code isn''t clean, it can bring a development organization to its knees.', 2, 2),
('978-0201633610', 'Design Patterns', 'Gang of Four', 'Addison-Wesley', 'Computer Science', 1994, 'Capturing a wealth of experience about the design of object-oriented software.', 2, 2),
('978-0596009205', 'Head First Java', 'Kathy Sierra', 'O Reilly', 'Computer Science', 2005, 'A complete object-oriented programming and Java learning experience.', 4, 4),
('978-0135957059', 'The Pragmatic Programmer', 'Andrew Hunt', 'Addison-Wesley', 'Computer Science', 2019, 'Straight from the programming trenches, it cuts through the increasing specialization and technical complexities of modern development.', 2, 2);

-- Category: History
INSERT IGNORE INTO books (isbn, title, author, publisher, genre, year_published, description, total_copies, available_copies) VALUES
('978-HI01', 'Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', 'Harper', 'History', 2015, 'Explores the ways in which biology and history have defined us and enhanced our understanding of what it means to be human.', 4, 4),
('978-HI02', 'Guns, Germs, and Steel', 'Jared Diamond', 'Norton', 'History', 1997, 'A short history of everybody for the last 13,000 years.', 3, 3),
('978-HI03', 'The Silk Roads', 'Peter Frankopan', 'Vintage', 'History', 2015, 'A new history of the world from the perspective of the East.', 5, 5),
('978-HI04', 'A People''s History of the United States', 'Howard Zinn', 'HarperPerennial', 'History', 1980, 'American history from the bottom up, throwing out the official version of history taught in schools.', 2, 2),
('978-HI05', 'SPQR: A History of Ancient Rome', 'Mary Beard', 'Liveright', 'History', 2015, 'A sweeping revisionist history of the Roman Empire.', 3, 3);

-- Category: Politics
INSERT IGNORE INTO books (isbn, title, author, publisher, genre, year_published, description, total_copies, available_copies) VALUES
('978-PO01', 'The Prince', 'Niccolo Machiavelli', 'Bantam Classics', 'Politics', 1532, 'A 16th-century political treatise on how to acquire and maintain political power.', 4, 4),
('978-PO02', 'The Republic', 'Plato', 'Penguin Classics', 'Politics', -380, 'A Socratic dialogue concerning justice, the order and character of the just city-state, and the just man.', 2, 2),
('978-PO03', '1984', 'George Orwell', 'Signet Classic', 'Politics', 1949, 'A dystopian social science fiction novel and cautionary tale about the dangers of totalitarianism.', 6, 6),
('978-PO04', 'On Liberty', 'John Stuart Mill', 'Oxford', 'Politics', 1859, 'A philosophical essay applying the ethical system of utilitarianism to society and state.', 3, 3),
('978-PO05', 'The Origins of Totalitarianism', 'Hannah Arendt', 'Harvest Book', 'Politics', 1951, 'An influential study of the circumstances that led to the rise of totalitarianism in the 20th century.', 2, 2);

-- Category: Maths
INSERT IGNORE INTO books (isbn, title, author, publisher, genre, year_published, description, total_copies, available_copies) VALUES
('978-MA01', 'Calculus', 'James Stewart', 'Cengage', 'Maths', 2015, 'A rigorous and accessible calculus textbook.', 5, 5),
('978-MA02', 'Linear Algebra and Its Applications', 'Gilbert Strang', 'Cengage', 'Maths', 2005, 'A renowned textbook for linear algebra courses.', 4, 4),
('978-MA03', 'Principles of Mathematical Analysis', 'Walter Rudin', 'McGraw-Hill', 'Maths', 1976, 'Often known as Baby Rudin, a classic undergraduate text on mathematical analysis.', 2, 2),
('978-MA04', 'Discrete Mathematics and Its Applications', 'Kenneth Rosen', 'McGraw-Hill', 'Maths', 2018, 'A comprehensive introduction to discrete mathematics.', 6, 6),
('978-MA05', 'Fermat''s Enigma', 'Simon Singh', 'Anchor', 'Maths', 1997, 'The epic quest to solve the world''s greatest mathematical problem.', 3, 3);

-- Category: Physics
INSERT IGNORE INTO books (isbn, title, author, publisher, genre, year_published, description, total_copies, available_copies) VALUES
('978-PH01', 'The Feynman Lectures on Physics', 'Richard P. Feynman', 'Basic Books', 'Physics', 1964, 'Perhaps the most popular physics book ever written.', 3, 3),
('978-PH02', 'A Brief History of Time', 'Stephen Hawking', 'Bantam', 'Physics', 1988, 'A landmark volume in science writing by one of the great minds of our time.', 5, 5),
('978-PH03', 'University Physics', 'Hugh D. Young', 'Pearson', 'Physics', 2019, 'A standard university-level physics textbook.', 4, 4),
('978-PH04', 'Introduction to Electrodynamics', 'David J. Griffiths', 'Cambridge', 'Physics', 2017, 'The standard undergraduate textbook for electromagnetism.', 2, 2),
('978-PH05', 'The Elegant Universe', 'Brian Greene', 'Vintage', 'Physics', 1999, 'Superstrings, hidden dimensions, and the quest for the ultimate theory.', 3, 3);
