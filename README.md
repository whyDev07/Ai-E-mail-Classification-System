# 📧 AI Email Classification System

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring
Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-blue)
![JWT](https://img.shields.io/badge/Auth-JWT-yellow)
![Swagger](https://img.shields.io/badge/API-Swagger-success)
![JUnit](https://img.shields.io/badge/Tested-JUnit%20%2B%20Mockito-brightgreen)

## 🚀 Overview

AI Email Classification System is a production-style REST API built with
Java 17 and Spring Boot 3 that classifies emails into **SPAM**,
**IMPORTANT**, **PROMOTIONS**, and **SOCIAL** categories using an LLM
through the OpenRouter API.

## Features
- Classify emails into: SPAM | IMPORTANT | PROMOTIONS | SOCIAL
- Confidence score returned with every classification
- JWT-based authentication (USER and ADMIN roles)
- BCrypt password hashing
- Swagger/OpenAPI Documentation
- Global exception handling with clean error responses
- Retry mechanism for AI API failures
-   JUnit 5 + Mockito Tests
- SLF4J structured logging
- PostgreSQL + Spring Data JPA



## Tech Stack
| Layer       | Technology              |
|-------------|-------------------------|
| Language    | Java 17                 |
| Framework   | Spring Boot 3.x      |
| Database    | PostgreSQL 14+           |
| Security    | Spring Security + JWT   |
| AI API      | OpenRouter API (Free-tier LLM models like Mistral / Qwen) |
| Build       | Maven                   |
| Testing       | Junit 5 + Mockito                   |
| Documentation      | Swagger                   |


## ⚙Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- OpenRouter API Key
## 🎯 Why This Project?

This project demonstrates how modern backend systems integrate AI APIs into real-world workflows, including authentication, persistence, and fault tolerance.


## 🏗️ Architecture

**Client** <br/>
↓↓↓<br/>
[JWT Security Filter]<br/>
↓↓↓<br/>
**Controller** ←→ [DTOs]<br/>
↓↓↓<br/>
**Service** Layer<br/>
↓↓↓ ----------------> [AI API (OpenRouter)]<br/>
**Repository**<br/>
↓↓↓<br/>
**Database** (PostgreSQL)<br/>

Swagger:

``` text
http://localhost:8080/swagger-ui/index.html
```


## Quick Start

### 1. Clone the repository
```bash
git clone https://github.com/whyDev07/Ai-E-mail-Classification-System.git
cd Ai-E-mail-Classification-System
```

### 2. Create PostgreSQL database
```sql
CREATE DATABASE emailclassifier;
```

### 3. Set environment variables
```bash
export DB_URL=jdbc:postgresql://localhost:5432/emailclassifier
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
export OPENROUTER_API_KEY=sk-or-your-key-here
export JWT_SECRET=YourSuperSecretKeyThatIsAtLeast256BitsLong!
```

### 4. Run the application
```bash
mvn spring-boot:run
```

API will start at: http://localhost:8080

## API Endpoints

### Authentication
| Method | Endpoint           | Access | Description    |
|--------|--------------------|--------|----------------|
| POST   | /api/auth/register | Public | Register user  |
| POST   | /api/auth/login    | Public | Get JWT token  |

### Email Classification
| Method | Endpoint              | Access        | Description          |
|--------|-----------------------|---------------|----------------------|
| POST   | /api/classify-email   | Authenticated | Classify an email    |
| GET    | /api/email/{id}       | Authenticated | Get email by ID      |
| GET    | /api/emails           | ADMIN only    | Get all emails       |

## Example Usage

### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"password123","role":"USER"}'
```

### Classify Email
```bash
curl -X POST http://localhost:8080/api/classify-email \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"subject":"Win a free iPhone!","body":"Click here to claim your prize"}'
```

### Response
```json
{
  "id": 1,
  "subject": "Win a free iPhone!",
  "classification": "SPAM",
  "confidenceScore": 0.97,
  "createdAt": "2024-01-15T10:30:00"
}
```

## Project Structure
```
src/main/java/com/emailclassifier/
├── config/          # Security, RestTemplate beans
├── controller/      # REST endpoints
├── dto/             # Request/Response objects
├── entity/          # JPA database entities
├── exception/       # Custom exceptions + global handler
├── filter/          # JWT authentication filter
├── repository/      # Spring Data JPA repositories
├── security/        # JWT utility + UserDetailsService
└── service/         # Business logic + AI integration
src/
└── test/            # Services and Authentication tests
```

## Environment Variables
| Variable            | Required | Description                    |
|---------------------|----------|--------------------------------|
| DB_URL              | Yes      | PostgreSQL JDBC URL            |
| DB_USERNAME         | Yes      | Database username              |
| DB_PASSWORD         | Yes      | Database password              |
| OPENROUTER_API_KEY  | Yes      | Your OpenRouter API key        |
| JWT_SECRET          | Yes      | Min 256-bit secret string      |

## Future Improvements
- Redis caching for repeated email patterns
- Async processing via RabbitMQ
- Batch classification endpoint
- Rate limiting with Bucket4j
- Swagger/OpenAPI documentation

## License
MIT License — see LICENSE file for details.
