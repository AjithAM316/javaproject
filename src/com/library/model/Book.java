package com.library.model;

import java.time.LocalDateTime;

/**
 * Represents a Book in the library catalogue.
 * Demonstrates OOP: Encapsulation.
 */
public class Book {

    private int    bookId;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String genre;
    private int    yearPublished;
    private String description;
    private int    totalCopies;
    private int    availableCopies;
    private LocalDateTime addedAt;

    // ── Constructors ──────────────────────────────────────────────────────────

    public Book() {}

    public Book(String isbn, String title, String author, String publisher,
                String genre, int yearPublished, String description, int totalCopies) {
        this.isbn            = isbn;
        this.title           = title;
        this.author          = author;
        this.publisher       = publisher;
        this.genre           = genre;
        this.yearPublished   = yearPublished;
        this.description     = description;
        this.totalCopies     = totalCopies;
        this.availableCopies = totalCopies;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int    getBookId()          { return bookId;          }
    public String getIsbn()            { return isbn;            }
    public String getTitle()           { return title;           }
    public String getAuthor()          { return author;          }
    public String getPublisher()       { return publisher;       }
    public String getGenre()           { return genre;           }
    public int    getYearPublished()   { return yearPublished;   }
    public String getDescription()     { return description;     }
    public int    getTotalCopies()     { return totalCopies;     }
    public int    getAvailableCopies() { return availableCopies; }
    public LocalDateTime getAddedAt()  { return addedAt;        }

    public void setBookId(int bookId)                   { this.bookId          = bookId;          }
    public void setIsbn(String isbn)                    { this.isbn            = isbn;            }
    public void setTitle(String title)                  { this.title           = title;           }
    public void setAuthor(String author)                { this.author          = author;          }
    public void setPublisher(String publisher)          { this.publisher       = publisher;       }
    public void setGenre(String genre)                  { this.genre           = genre;           }
    public void setYearPublished(int yearPublished)     { this.yearPublished   = yearPublished;   }
    public void setDescription(String description)      { this.description     = description;     }
    public void setTotalCopies(int totalCopies)         { this.totalCopies     = totalCopies;     }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }
    public void setAddedAt(LocalDateTime addedAt)       { this.addedAt         = addedAt;         }

    public boolean isAvailable() { return availableCopies > 0; }

    @Override
    public String toString() {
        return String.format("Book{id=%d, isbn='%s', title='%s', author='%s', available=%d/%d}",
                bookId, isbn, title, author, availableCopies, totalCopies);
    }
}
