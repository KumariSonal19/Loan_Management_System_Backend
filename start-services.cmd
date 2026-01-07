@echo off

echo Starting Eureka Server...
start "Eureka Server" cmd /k java -jar eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar

echo Starting Config Server...
start "Config Server" cmd /k java -jar config-server/target/config-server-0.0.1-SNAPSHOT.jar

echo Starting Booking Service...
start "Loan Service" cmd /k java -jar loan-service/target/loan-service-0.0.1-SNAPSHOT.jar

echo Starting Flight Service...
start "EMI Service" cmd /k java -jar emi-service/target/emi-service-0.0.1-SNAPSHOT.jar

echo Starting Auth Service...
start "Auth Service" cmd /k java -jar auth-service/target/auth-service-0.0.1-SNAPSHOT.jar

echo Starting Auth Service...
start "Admin Service" cmd /k java -jar admin-service/target/admin-service-0.0.1-SNAPSHOT.jar

echo Starting Notification Service...
start "Notification Service" cmd /k java -jar notification-service/target/notification-service-0.0.1-SNAPSHOT.jar

echo Starting API Gateway...
start "API Gateway" cmd /k java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar


echo All services started.
pause
