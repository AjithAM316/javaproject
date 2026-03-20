package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a book borrowing transaction.
 * Encapsulates business logic: fine calculation based on overdue days.
 */
public class BookIssue {

    public enum Status { ISSUED, RETURNED, OVERDUE }

    private static final int    LOAN_PERIOD_DAYS = 14;     // 2-week loan period
    private static final double FINE_PER_DAY     = 2.00;   // ₹2 per overdue day

    private int        issueId;
    private int        bookId;
    private int        userId;
    private String     bookTitle;   // joined display field
    private String     userName;    // joined display field
    private LocalDate  issueDate;
    private LocalDate  dueDate;
    private LocalDate  returnDate;
    private BigDecimal fineAmount;
    private boolean    finePaid;
    private Status     status;

    // ── Constructors ──────────────────────────────────────────────────────────

    public BookIssue() {}

    public BookIssue(int bookId, int userId) {
        this.bookId    = bookId;
        this.userId    = userId;
        this.issueDate = LocalDate.now();
        this.dueDate   = issueDate.plusDays(LOAN_PERIOD_DAYS);
        this.fineAmount = BigDecimal.ZERO;
        this.finePaid  = false;
        this.status    = Status.ISSUED;
    }

    // ── Fine Calculation ──────────────────────────────────────────────────────

    /**
     * Calculates overdue fine based on today's date (or return date if returned).
     * Fine = overdue days × FINE_PER_DAY
     */
    public BigDecimal calculateFine() {
        LocalDate checkDate = (returnDate != null) ? returnDate : LocalDate.now();
        if (checkDate.isAfter(dueDate)) {
            long overdueDays = ChronoUnit.DAYS.between(dueDate, checkDate);
            return BigDecimal.valueOf(overdueDays * FINE_PER_DAY);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Returns the number of overdue days (0 if not overdue).
     */
    public long getOverdueDays() {
        LocalDate checkDate = (returnDate != null) ? returnDate : LocalDate.now();
        long days = ChronoUnit.DAYS.between(dueDate, checkDate);
        return Math.max(0, days);
    }

    public boolean isOverdue() {
        return LocalDate.now().isAfter(dueDate) && status == Status.ISSUED;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int        getIssueId()    { return issueId;    }
    public int        getBookId()     { return bookId;     }
    public int        getUserId()     { return userId;     }
    public String     getBookTitle()  { return bookTitle;  }
    public String     getUserName()   { return userName;   }
    public LocalDate  getIssueDate()  { return issueDate;  }
    public LocalDate  getDueDate()    { return dueDate;    }
    public LocalDate  getReturnDate() { return returnDate; }
    public BigDecimal getFineAmount() { return fineAmount; }
    public boolean    isFinePaid()    { return finePaid;   }
    public Status     getStatus()     { return status;     }

    public void setIssueId(int issueId)         { this.issueId    = issueId;    }
    public void setBookId(int bookId)           { this.bookId     = bookId;     }
    public void setUserId(int userId)           { this.userId     = userId;     }
    public void setBookTitle(String bookTitle)  { this.bookTitle  = bookTitle;  }
    public void setUserName(String userName)    { this.userName   = userName;   }
    public void setIssueDate(LocalDate d)       { this.issueDate  = d;          }
    public void setDueDate(LocalDate d)         { this.dueDate    = d;          }
    public void setReturnDate(LocalDate d)      { this.returnDate = d;          }
    public void setFineAmount(BigDecimal f)     { this.fineAmount = f;          }
    public void setFinePaid(boolean finePaid)   { this.finePaid   = finePaid;   }
    public void setStatus(Status status)        { this.status     = status;     }

    public static int    getLoanPeriodDays() { return LOAN_PERIOD_DAYS; }
    public static double getFinePerDay()     { return FINE_PER_DAY;     }

    @Override
    public String toString() {
        return String.format("BookIssue{id=%d, bookId=%d, userId=%d, due=%s, status=%s, fine=₹%.2f}",
                issueId, bookId, userId, dueDate, status, fineAmount);
    }
}
