package com.library.model;

import java.time.LocalDateTime;

/**
 * Represents a system user – either a Student or a Librarian.
 * Demonstrates OOP: Encapsulation with private fields and public getters/setters.
 */
public class User {

    public enum Role { STUDENT, LIBRARIAN }

    private int         userId;
    private String      username;
    private String      password;
    private String      fullName;
    private String      email;
    private String      phone;
    private Role        role;
    private boolean     isActive;
    private LocalDateTime createdAt;

    // ── Constructors ──────────────────────────────────────────────────────────

    public User() {}

    public User(String username, String password, String fullName,
                String email, String phone, Role role) {
        this.username  = username;
        this.password  = password;
        this.fullName  = fullName;
        this.email     = email;
        this.phone     = phone;
        this.role      = role;
        this.isActive  = true;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int       getUserId()    { return userId;    }
    public String    getUsername()  { return username;  }
    public String    getPassword()  { return password;  }
    public String    getFullName()  { return fullName;  }
    public String    getEmail()     { return email;     }
    public String    getPhone()     { return phone;     }
    public Role      getRole()      { return role;      }
    public boolean   isActive()     { return isActive;  }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setUserId(int userId)         { this.userId   = userId;   }
    public void setUsername(String username)  { this.username = username; }
    public void setPassword(String password)  { this.password = password; }
    public void setFullName(String fullName)  { this.fullName = fullName; }
    public void setEmail(String email)        { this.email    = email;    }
    public void setPhone(String phone)        { this.phone    = phone;    }
    public void setRole(Role role)            { this.role     = role;     }
    public void setActive(boolean active)     { this.isActive = active;   }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t;       }

    public boolean isLibrarian() { return role == Role.LIBRARIAN; }
    public boolean isStudent()   { return role == Role.STUDENT;   }

    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', name='%s', role=%s}",
                userId, username, fullName, role);
    }
}
