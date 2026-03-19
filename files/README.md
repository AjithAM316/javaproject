# Smart Library Management System
### Java OOP Project — Group 11
**Jawaharlal College of Engineering & Technology**

---

## Project Structure

```
SmartLibrary/
├── sql/
│   └── schema.sql                  ← Run this in MySQL first
├── src/com/library/
│   ├── model/
│   │   ├── User.java               ← Encapsulation: user entity
│   │   ├── Book.java               ← Encapsulation: book entity
│   │   └── BookIssue.java          ← Fine calculation logic
│   ├── dao/
│   │   ├── UserDAO.java            ← Abstraction: DB operations
│   │   ├── BookDAO.java            ← Abstraction: DB operations
│   │   └── BookIssueDAO.java       ← Abstraction: DB operations
│   ├── service/
│   │   └── LibraryService.java     ← Business logic layer
│   ├── ui/
│   │   ├── MainApp.java            ← Entry point / login screen
│   │   ├── StudentMenu.java        ← Student role UI
│   │   └── LibrarianMenu.java      ← Librarian/Admin role UI
│   └── util/
│       ├── DatabaseConnection.java ← Singleton DB connection
│       └── ConsoleHelper.java      ← Console I/O utilities
└── README.md
```

---

## OOP Concepts Used

| Concept | Where |
|---|---|
| **Encapsulation** | All model classes (User, Book, BookIssue) with private fields |
| **Abstraction** | DAO layer hides SQL; Service layer hides business logic from UI |
| **Inheritance** | User.Role enum; BookIssue.Status enum; all classes extend Object |
| **Polymorphism** | `LibraryService` methods work for both Student and Librarian roles |
| **Exception Handling** | try-catch in all DB calls; IllegalArgumentException for validation |
| **JDBC** | DatabaseConnection (Singleton), PreparedStatements, ResultSets |

---

## Setup Instructions

### 1. Install Requirements
- Java JDK 11 or higher
- MySQL 8.x
- MySQL JDBC Connector: `mysql-connector-java-8.x.x.jar`

### 2. Set Up Database
```sql
-- In MySQL Workbench or terminal:
source sql/schema.sql;
```

### 3. Configure Database Credentials
Edit `src/com/library/util/DatabaseConnection.java`:
```java
private static final String USER     = "root";
private static final String PASSWORD = "your_mysql_password";
```

### 4. Compile
```bash
# From SmartLibrary/ directory:
javac -cp mysql-connector-java-8.x.x.jar \
      -d out \
      src/com/library/**/*.java
```

### 5. Run
```bash
java -cp out:mysql-connector-java-8.x.x.jar \
     com.library.ui.MainApp
```

---

## Default Login

| Role | Username | Password |
|---|---|---|
| Librarian | `admin` | `admin123` |

Students register themselves from the main menu.

---

## Features

### Student Portal
- Search books by title, author, or ISBN
- View available books in real time
- Issue and return books
- View borrowing history with fine details
- View active issues and due dates

### Librarian Dashboard
- Add, update, delete books
- Issue/return books on behalf of students
- Monitor all active issues
- View overdue books list
- Manage student accounts
- Mark fines as paid
- Analytics: most borrowed books, total fines collected

---

## Fine Policy
- Loan period: **14 days**
- Fine rate: **₹2 per overdue day**
- Calculated automatically on return

---

*Group 11: Ajith AM · Abin Jayaram V · Safad Asharaf · Sreehari P*
