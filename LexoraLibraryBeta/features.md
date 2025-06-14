# Library Management System (LMS) Technical Specification

## System Overview
A Spring Boot-based Library Management System implementing clean architecture principles with a strong focus on maintainability, testability, and Test-Driven Development (TDD).

## Technology Stack
- Java 21
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL
- JUnit 5
- Mockito
- SLF4J with Logback
- Spring Security
- Spring Doc OpenAPI
- Flyway for database migrations

## Database Schema

### Tables Structure

1. **books**
```sql
CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    category VARCHAR(100) NOT NULL,
    publication_year INTEGER NOT NULL,
    total_copies INTEGER NOT NULL DEFAULT 0,
    available_copies INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT publication_year_check CHECK (publication_year BETWEEN 1800 AND 2100),
    CONSTRAINT copies_check CHECK (total_copies >= 0 AND available_copies >= 0)
);

CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_author ON books(author);
CREATE INDEX idx_books_category ON books(category);
```

2. **members**
```sql
CREATE TABLE members (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    membership_id VARCHAR(50) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_members_email ON members(email);
CREATE INDEX idx_members_membership_id ON members(membership_id);
```

3. **loans**
```sql
CREATE TABLE loans (
    id BIGSERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL REFERENCES books(id),
    member_id BIGINT NOT NULL REFERENCES members(id),
    borrow_date TIMESTAMP NOT NULL,
    expected_return_date TIMESTAMP NOT NULL,
    actual_return_date TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT status_check CHECK (status IN ('ACTIVE', 'OVERDUE', 'RETURNED'))
);

CREATE INDEX idx_loans_book_id ON loans(book_id);
CREATE INDEX idx_loans_member_id ON loans(member_id);
CREATE INDEX idx_loans_status ON loans(status);
CREATE INDEX idx_loans_borrow_date ON loans(borrow_date);
```

4. **audit_logs**
```sql
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    performed_by VARCHAR(255) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50),
    changes JSONB,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
```

5. **users**
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT role_check CHECK (role IN ('ADMIN', 'LIBRARIAN', 'MEMBER'))
);

CREATE INDEX idx_users_username ON users(username);
```

6. **late_fees**
```sql
CREATE TABLE late_fees (
    id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL REFERENCES loans(id),
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP,
    CONSTRAINT status_check CHECK (status IN ('PAID', 'UNPAID')),
    CONSTRAINT amount_check CHECK (amount >= 0)
);

CREATE INDEX idx_late_fees_loan_id ON late_fees(loan_id);
CREATE INDEX idx_late_fees_status ON late_fees(status);
```

### Database Relationships

1. **One-to-Many Relationships:**
   - Member → Loans (one member can have multiple loans)
   - Book → Loans (one book can have multiple loan records)
   - Loan → Late Fees (one loan can have multiple late fee records)

2. **Many-to-One Relationships:**
   - Loans → Member (each loan belongs to one member)
   - Loans → Book (each loan involves one book)
   - Late Fees → Loan (each late fee is associated with one loan)

### Database Migrations

Migrations will be managed using Flyway with the following naming convention:
```
V{version}__{description}.sql
```

Example:
```
V1__create_initial_schema.sql
V2__add_indexes.sql
V3__add_late_fees_table.sql
```

### Indexing Strategy
- Primary keys are automatically indexed
- Foreign keys are indexed for faster joins
- Common search fields (email, ISBN, title, etc.) are indexed
- Composite indexes for frequently combined search criteria
- JSONB index for audit log changes

## Core Features Specification

### 1. Book Management

#### 1.1 Add Book Feature

**Purpose:** Allow administrators and librarians to add new books to the library inventory.

**Data Model:**
```java
@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    private String title;
    
    @NotBlank
    private String author;
    
    @NotBlank
    @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$")
    private String isbn;
    
    @NotBlank
    private String category;
    
    @Min(1800) @Max(2100)
    private Integer publicationYear;
    
    @Min(0)
    private Integer totalCopies;
    
    @Min(0)
    private Integer availableCopies;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
}
```

**API Endpoint:**
```
POST /api/v1/books
```

**Request Format:**
```json
{
    "title": "string",
    "author": "string",
    "isbn": "string",
    "category": "string",
    "publicationYear": "integer",
    "totalCopies": "integer"
}
```

**Response Format:**
```json
{
    "id": "long",
    "title": "string",
    "author": "string",
    "isbn": "string",
    "category": "string",
    "publicationYear": "integer",
    "totalCopies": "integer",
    "availableCopies": "integer",
    "createdAt": "datetime",
    "updatedAt": "datetime"
}
```

**Validation Rules:**
- Title must not be blank
- Author must not be blank
- ISBN must be valid format
- Publication year between 1800 and current year + 1
- Total copies must be >= 0
- ISBN must be unique in the system

**Error Handling:**
- 400 Bad Request: Invalid input data
- 409 Conflict: ISBN already exists
- 403 Forbidden: Unauthorized access
- 500 Internal Server Error: System errors

**Testing Strategy:**
1. Unit Tests:
   - BookService validation logic
   - ISBN format validation
   - Business rule validation
2. Integration Tests:
   - Book creation flow
   - Database persistence
   - Unique ISBN constraint
3. Security Tests:
   - Authorization rules
   - Role-based access

**Audit Logging:**
```java
@Slf4j
public class BookAuditLogger {
    public void logBookCreation(Book book, String username) {
        log.info("Book created: {} by user: {}", book.getIsbn(), username);
    }
}
```

### 2. Book Borrowing

#### 2.1 Borrow Book Feature

**Data Model:**
```java
@Entity
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @NotNull
    private Book book;
    
    @ManyToOne
    @NotNull
    private Member member;
    
    @NotNull
    private LocalDateTime borrowDate;
    
    @NotNull
    private LocalDateTime expectedReturnDate;
    
    private LocalDateTime actualReturnDate;
    
    @Enumerated(EnumType.STRING)
    private LoanStatus status;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}

public enum LoanStatus {
    ACTIVE, OVERDUE, RETURNED
}
```

**API Endpoint:**
```
POST /api/v1/loans
```

**Request Format:**
```json
{
    "bookId": "long",
    "memberId": "long",
    "borrowDate": "datetime"
}
```

**Response Format:**
```json
{
    "id": "long",
    "book": {
        "id": "long",
        "title": "string"
    },
    "member": {
        "id": "long",
        "name": "string"
    },
    "borrowDate": "datetime",
    "expectedReturnDate": "datetime",
    "status": "string"
}
```

**Validation Rules:**
- Book must exist and have available copies
- Member must exist and be active
- Member must not have exceeded borrowing limit
- Member must not have overdue books

**Error Handling:**
- 400 Bad Request: Invalid input
- 404 Not Found: Book/Member not found
- 409 Conflict: Book not available
- 403 Forbidden: Member restrictions

**Testing Strategy:**
1. Unit Tests:
   - Loan service business logic
   - Availability checking
   - Due date calculation
2. Integration Tests:
   - Loan creation flow
   - Book availability update
   - Member borrowing limits

### 3. Book Return Feature

**API Endpoint:**
```
PUT /api/v1/loans/{loanId}/return
```

**Response Format:**
```json
{
    "id": "long",
    "book": {
        "id": "long",
        "title": "string"
    },
    "returnDate": "datetime",
    "status": "string",
    "lateFee": "decimal"
}
```

**Business Rules:**
- Update book availability
- Calculate late fees if applicable
- Update loan status
- Prevent future borrowing if fees unpaid

### 4. Book Listing and Search

**API Endpoints:**
```
GET /api/v1/books
GET /api/v1/books/search
```

**Query Parameters:**
```
page: integer
size: integer
sort: string
direction: string
title: string
author: string
category: string
availability: boolean
```

**Response Format:**
```json
{
    "content": [
        {
            "id": "long",
            "title": "string",
            "author": "string",
            "category": "string",
            "availableCopies": "integer"
        }
    ],
    "totalElements": "long",
    "totalPages": "integer",
    "number": "integer",
    "size": "integer"
}
```

### 5. Member Management

**Data Model:**
```java
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    private String name;
    
    @Email
    @NotBlank
    private String email;
    
    @Pattern(regexp = "^\\+?[1-9][0-9]{7,14}$")
    private String phoneNumber;
    
    @Column(unique = true)
    private String membershipId;
    
    private boolean active;
    
    @OneToMany(mappedBy = "member")
    private Set<Loan> loans;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

**API Endpoints:**
```
POST   /api/v1/members
GET    /api/v1/members/{id}
PUT    /api/v1/members/{id}
DELETE /api/v1/members/{id}
GET    /api/v1/members/search
```

### 6. Pagination Implementation

**Base Controller:**
```java
public abstract class BaseController {
    protected Pageable createPageable(int page, int size, String sort, String direction) {
        Sort.Direction dir = Sort.Direction.fromString(direction);
        return PageRequest.of(page, size, Sort.by(dir, sort));
    }
}
```

### 7. Search Implementation

**Search Service:**
```java
public interface SearchService {
    Page<Book> searchBooks(String query, Pageable pageable);
    Page<Member> searchMembers(String query, Pageable pageable);
}
```

### 8. Audit Logging

**AuditLog Entity:**
```java
@Entity
public class AuditLog {
    @Id
    @GeneratedValue
    private Long id;
    
    @NotNull
    private String action;
    
    @NotNull
    private String performedBy;
    
    @NotNull
    private String entityType;
    
    private String entityId;
    
    @Column(columnDefinition = "jsonb")
    private String changes;
    
    @CreatedDate
    private LocalDateTime timestamp;
}
```

## Security Implementation

### Authentication
- JWT-based authentication
- Role-based access control (ADMIN, LIBRARIAN, MEMBER)
- Session management

### Authorization Matrix
- ADMIN: All operations
- LIBRARIAN: Book management, loan operations
- MEMBER: View books, borrow/return books

## API Documentation
- Swagger UI at `/swagger-ui.html`
- OpenAPI specification at `/v3/api-docs`

## Testing Guidelines
- Follow TDD approach
- Unit test coverage > 80%
- Integration tests for critical flows
- Security tests for authentication/authorization
- Performance tests for search and pagination

## Monitoring and Logging
- Actuator endpoints for health monitoring
- Structured logging with SLF4J
- Performance metrics collection
- Error tracking and alerting 