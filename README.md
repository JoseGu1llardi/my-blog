# Blog REST API

## 🚀 Features
- ✅ Complete CRUD for Posts, Authors, and Categories
- ✅ JWT Authentication & Role-based Authorization
- ✅ Custom Slug Generation with Value Objects
- ✅ Pagination, Sorting, and Filtering
- ✅ Global Exception Handling
- ✅ Clean Architecture & SOLID Principles
- ✅ Comprehensive Test Coverage

## 🛠️ Tech Stack
**Backend:** Java 17, Spring Boot 3.x, Spring Data JPA, Spring Security
**Database:** PostgreSQL
**Build Tool:** Maven
**Testing:** JUnit 5, Mockito

## 📐 Architecture
```
src/
├── main/java/com/seublog/
│   ├── model/        # Entities & Value Objects
│   ├── repository/   # Spring Data JPA Repositories
│   ├── service/      # Business Logic Layer
│   ├── controller/   # REST Controllers
│   ├── dto/          # Request/Response DTOs
│   ├── mapper/       # DTO Mappers
│   ├── exception/    # Custom Exceptions
│   └── config/       # Security & App Configuration
```

## 🎯 Highlights
- Clean separation of concerns with layered architecture
- Custom value objects (Slug) with JPA converters
- DTO pattern for API contract stability
- Custom exception hierarchy with proper HTTP status codes