# Loan Management System – Backend (Microservices)

This repository contains the backend implementation of a Loan Management System built using Spring Boot and Microservices architecture. 

---

## Overview

The Loan Management System backend is composed of multiple independent microservices. Each service handles a specific business responsibility and communicates with other services using REST APIs or asynchronous messaging through RabbitMQ.

The system supports user authentication, loan application and approval workflows, EMI calculation and repayment tracking, and notification delivery via email.

---

## Microservices Architecture

The backend consists of the following microservices:

- Auth Service: Handles user registration, login, JWT generation, and role-based authentication.
- Admin Service: Manages loan type configurations such as interest rates, tenure limits, and activation status.
- Loan Service: Handles loan applications, approvals, rejections, and lifecycle state management.
- EMI Service: Responsible for EMI schedule generation, repayment tracking, and overdue detection.
- Notification Service: Sends notifications using asynchronous messaging.
- API Gateway: Acts as the single entry point and enforces security and routing rules.
- Eureka Server: Provides service discovery for dynamic microservice registration.

---

## Technology Stack

### Backend
- Java 17
- Spring Boot
- Spring Security
- Spring Cloud (Eureka, API Gateway, OpenFeign)
- Spring Data JPA with Hibernate
- MySQL
- RabbitMQ
- JWT (JSON Web Token)

### Build Tool
- Maven

---

## Security Design

- Stateless JWT-based authentication
- Role-based authorization (ADMIN, LOAN_OFFICER, CUSTOMER)
- Centralized security enforcement at API Gateway
- BCrypt password hashing
- Global exception handling

---

## Loan Lifecycle

Loan applications follow a strict state machine:

APPLIED → UNDER_REVIEW → APPROVED / REJECTED → CLOSED

All state transitions are validated in the service layer to prevent invalid operations.

---

## EMI Calculation

The EMI calculation follows the standard financial formula:

EMI = (P × R × (1 + R)^N) / ((1 + R)^N − 1)

Where:
- P is the principal amount
- R is the monthly interest rate
- N is the loan tenure in months

---

## API Endpoints

### Auth Service
POST /api/auth/register  
POST /api/auth/login  
GET /api/auth/profile/{id}  
PUT /api/auth/profile/{id}

### Admin Service
POST /api/admin/loan-types  
GET /api/admin/loan-types  
GET /api/admin/loan-types/{id}  
PUT /api/admin/loan-types/{id}/activate  
PUT /api/admin/loan-types/{id}/deactivate  
DELETE /api/admin/loan-types/{id}

### Loan Service
POST /api/loans/apply  
PUT /api/loans/review  
GET /api/loans/{id}  
GET /api/loans/customer/list/{customerId}  
GET /api/loans/status/{status}  
GET /api/loans/count/{status}

### EMI Service
POST /api/emis/generate  
POST /api/emis/repay  
GET /api/emis/schedule/{loanId}  
GET /api/emis/outstanding/{loanId}  
GET /api/emis/overdue

### Notification Service
GET /api/notifications/user/{userId}  
GET /api/notifications/user/{userId}/unread  
PUT /api/notifications/{id}/read

---

## Database Design

The system uses MySQL as the database. Each microservice has its own logical database to maintain data isolation and ownership.

### Databases and Tables

- **auth_db**
  - users

- **admin_db**
  - loan_types

- **loan_db**
  - loan_applications

- **emi_db**
  - emi_schedules
  - repayments

- **notification_db**
  - notifications



---

## Configuration and Setup

### Prerequisites
- Java 17 or higher
- MySQL
- RabbitMQ
- Maven

### Steps to Run
1. Start Eureka Server
2. Start RabbitMQ
3. Start the following services in order:
   - Auth Service
   - Admin Service
   - Loan Service
   - EMI Service
   - Notification Service
   - API Gateway
4. Access APIs through the API Gateway at http://localhost:8087

---

## Demo Flow

1. Login as Admin and create loan types
2. Login as Customer and apply for a loan
3. Login as Loan Officer and approve the loan
4. EMI schedule is generated automatically
5. Customer makes EMI payments
6. Notifications are sent asynchronously

---

## Conclusion

This project demonstrates a real-world, enterprise-style backend system that follows modern microservices principles and industry best practices.
