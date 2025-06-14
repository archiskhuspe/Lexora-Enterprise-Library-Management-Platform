# Library Management System

A Spring Boot-based Library Management System that provides REST APIs for managing library resources including books, members, and loans.

## Technologies Used

- Java 21
- Spring Boot 3.x
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL
- Flyway for database migrations
- SpringDoc OpenAPI for API documentation
- JUnit 5 & Mockito for testing

## Prerequisites

- Java 21 or higher
- Maven 3.8+
- PostgreSQL 12+
- Docker (optional)

## Setup Instructions

1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd library-management-system
   ```

2. **Database Setup**
   ```bash
   # Create PostgreSQL database
   createdb library_db
   ```

3. **Configure Application**
   - Update database credentials in `src/main/resources/application.properties` if needed
   - The default configuration uses:
     ```properties
     spring.datasource.url=jdbc:postgresql://localhost:5432/library_db
     spring.datasource.username=postgres
     spring.datasource.password=postgres
     ```

4. **Build and Run**
   ```bash
   # Build the project
   mvn clean install

   # Run the application
   mvn spring-boot:run
   ```

5. **Access the Application**
   - API Base URL: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - API Docs: `http://localhost:8080/v3/api-docs`

## API Security

The API uses JWT-based authentication. To access protected endpoints:

1. Obtain JWT token from `/api/v1/auth/login`
2. Include token in Authorization header: `Bearer <token>`

## Available Roles

- ADMIN: Full access to all endpoints
- LIBRARIAN: Access to book and member management
- MEMBER: Access to book search and loan operations

## API Endpoints

### Authentication
- POST `/api/v1/auth/login` - Login and get JWT token
- POST `/api/v1/auth/register` - Register new user

### Books
- GET `/api/v1/books` - List all books
- GET `/api/v1/books/{id}` - Get book details
- POST `/api/v1/books` - Add new book (ADMIN, LIBRARIAN)
- PUT `/api/v1/books/{id}` - Update book (ADMIN, LIBRARIAN)
- DELETE `/api/v1/books/{id}` - Delete book (ADMIN)

### Members
- GET `/api/v1/members` - List all members (ADMIN, LIBRARIAN)
- POST `/api/v1/members` - Add new member (ADMIN, LIBRARIAN)
- PUT `/api/v1/members/{id}` - Update member (ADMIN, LIBRARIAN)

### Loans
- POST `/api/v1/loans` - Create new loan
- PUT `/api/v1/loans/{id}/return` - Return book
- GET `/api/v1/loans/member/{memberId}` - Get member's loans

## Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=BookControllerTest

# Run with coverage report
mvn verify
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request