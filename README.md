# 🚗 Car Sharing Service API

## 🚀 Introduction

This project is a RESTful API for a car sharing service.  
It allows users to register, authenticate, browse available cars, rent vehicles, and process payments online.

The goal was to modernize a paper-based car rental system by implementing a fully digital solution with real-time inventory tracking, online payments via Stripe, and automated Telegram notifications.

---

## 🛠️ Technologies Used

- Java 21
- Spring Boot 3
- Spring Security (JWT authentication)
- Spring Data JPA (Hibernate)
- MySQL
- Liquibase (database migrations)
- MapStruct (DTO mapping)
- Stripe API (payments)
- Telegram Bot API (notifications)
- Swagger (OpenAPI documentation)
- Docker / Docker Compose
- AWS EC2 (deployment)
- Maven

---

## 🔐 Features

### 👤 Authentication
- User registration
- User login with JWT token
- Role-based authorization (CUSTOMER / MANAGER)

### 🚗 Car Management
- Create, update, delete cars (MANAGER)
- Get all cars / get car by ID (PUBLIC)
- Track car inventory (available quantity)

### 📅 Rental Management
- Create rental (decreases car inventory)
- Return rental (increases car inventory)
- View rental history with filters (active/by user)
- Automatic overdue tracking with daily scheduled checks

### 💳 Payments (Stripe)
- Create Stripe payment session for rental
- Process payments and fines for overdue returns
- Handle payment success/cancel callbacks
- Stripe webhook integration

### 📱 Notifications (Telegram)
- Send notification on new rental creation
- Daily overdue rental alerts to Telegram chat
- Payment confirmation notifications

---
🧠 Architecture

![Diagram](src/main/resources/images/dia.png)



## 📊 API Documentation

Swagger UI is available at:

http://ec2-18-208-174-108.compute-1.amazonaws.com/api/swagger-ui.html

---

## How to Run the Project
```
### 1. Clone repository
git clone <your-repo-link>
cd car-sharing-app
```
### 2. Configure environment variables
```
Create `.env` file from sample:
cp .env.sample .env

//Edit `.env` with your values:
Database
MYSQL_DATABASE=carsharing
MYSQL_USER=admin
MYSQL_PASSWORD=your_password
MYSQL_ROOT_PASSWORD=root_password
```

JWT
```
JWT_SECRET=your_super_secret_key_min_256bits
JWT_EXPIRATION=86400000
```

Stripe
```
STRIPE_SECRET_KEY=sk_test_...
STRIPE_PUBLIC_KEY=pk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
```
Telegram
```
TELEGRAM_BOT_TOKEN=1234567890:AAF...
TELEGRAM_CHAT_ID=-123456789
```
### 3. Run with Docker Compose
```
docker-compose up -d
```

### 4. Or run locally
```
Start MySQL:
docker run -d --name mysql-car -p 3306:3306
-e MYSQL_DATABASE=carsharing
-e MYSQL_USER=admin
-e MYSQL_PASSWORD=password
-e MYSQL_ROOT_PASSWORD=rootpassword
mysql:8.1
```
Before running tests, verify that Stripe environment variables are set correctly:
```
$env:STRIPE_SECRET_KEY="STRIPE_SECRET_KEY"
$env:STRIPE_WEBHOOK_SECRET="STRIPE_WEBHOOK_SECRET"
mvn clean test
```

Run the application:
```
mvn clean spring-boot:run
```


---

## Example Requests

### Register a new user
```
**POST** `/auth/register`
{
"email": "customer@gmail.com",
"password": "password123",
"repeatPassword": "password123",
"firstName": "Ivan",
"lastName": "Petrenko"
}
```
**Response:**
```
{
"id": 1,
"email": "customer@gmail.com",
"firstName": "Ivan",
"lastName": "Petrenko",
"role": "CUSTOMER"
}
```

### Login
```
**POST** `/auth/login`
{
"email": "customer@gmail.com",
"password": "password123"
}
```

**Response:**
```
{
"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
"type": "Bearer"
}
```

### Create a rental
```
**POST** `/rentals`
{
"carId": 1,
"rentalDate": "2024-01-15",
"returnDate": "2024-01-20"
}
```

### Create payment session
```
**POST** `/payments`
{
"rentalId": 1,
"type": "PAYMENT"
}
```

**Response:**
```
{
"id": 1,
"status": "PENDING",
"sessionUrl": "https://checkout.stripe.com/...",
"amountToPay": 249.95
}
```
---

## Role-based Access to Endpoints

The application implements role-based access control using Spring Security and JWT authentication. Endpoints are accessible depending on user role: **CUSTOMER** or **MANAGER**.

### Customer Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register a new user |
| POST | `/auth/login` | Authenticate user and receive JWT token |
| GET | `/cars` | Get all cars with pagination |
| GET | `/cars/{id}` | Get car details by ID |
| GET | `/users/me` | Get current user profile |
| PUT | `/users/me` | Update current user profile |
| DELETE | `/users/me` | Delete own account |
| GET | `/rentals` | Get user's rentals (filtered) |
| POST | `/rentals` | Create a new rental |
| GET | `/rentals/{id}` | Get rental details |
| POST | `/rentals/{id}/return` | Return a rental |
| GET | `/payments` | Get user's payments |
| POST | `/payments` | Create Stripe payment session |
| POST | `/payments/cancel` | Cancel pending payment |

### Manager Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/cars` | Create a new car |
| PUT | `/cars/{id}` | Update car |
| DELETE | `/cars/{id}` | Delete car |
| PUT | `/users/{id}/role` | Update user role |
| GET | `/notifications` | Get notification history |
| POST | `/notifications` | Send notification |

### Public Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/cars` | Get all cars |
| GET | `/cars/{id}` | Get car by ID |
| GET | `/payments/success` | Stripe success callback |
| GET | `/payments/cancel` | Stripe cancel callback |
| POST | `/payments/webhook` | Stripe webhook handler |

All endpoints (except authentication and public GET `/cars`) require a valid JWT token in the Authorization header:
Authorization: Bearer <your_token>

text

---

## Test Credit Card for Stripe

| Field | Value |
|-------|-------|
| Card number | 4242 4242 4242 4242 |
| Expiry | 12/28 (any future date) |
| CVC | 123 |

---

## Testing
Before running tests, verify that Stripe environment variables are set correctly:
```
$env:STRIPE_SECRET_KEY="STRIPE_SECRET_KEY"
$env:STRIPE_WEBHOOK_SECRET="STRIPE_WEBHOOK_SECRET"
mvn clean test
```
```
Run tests:
Run all tests with style check
mvn clean verify

Run tests skipping Stripe integration tests
mvn clean verify -Dskip.stripe.tests=true

Run only unit tests
mvn test

Code coverage (60%+ requirement)
mvn jacoco:report
```

---

## Challenges and Solutions

| Challenge | Solution |
|-----------|----------|
| Double inventory counting | Atomic operations with @Transactional and pessimistic locking |
| Stripe webhooks locally | Stripe CLI for local tunneling |
| Telegram duplicate notifications | Publisher-Subscriber pattern with async listeners |
| Dynamic URL building | UriComponentsBuilder for absolute URLs |
| Timezone-aware overdue checks | ZonedDateTime with UTC for scheduled tasks |

---

## Live Demo

The API is deployed on AWS EC2:
```
http://ec2-18-208-174-108.compute-1.amazonaws.com/api
```

---

## License

MIT License — freely use, modify, and distribute.

---

## Acknowledgments

- Stripe for excellent documentation and test cards
- Spring Boot for simplifying enterprise development
- Telegram Bot API for convenient notifications

---

*This project was completed as part of an educational program. All data is test data; no real payments are processed.*