# Exchange api
## Description
This spring boot application provides api for exchange service:
1. Endpoint to add new order to orderbook
2. Endpoint to cancel order
3. Endpoint to find single order by ID
4. Endpoint to find all orders for userId, and filter by status, and sort by date

## Running
To run the application execute:
```bash
mvn package
java -jar target/exchange-service-1.0.jar
```
Application will start on <localhost:8080>

## Swagger

Swagger documentation would be available on <localhost:8080/swagger-ui/index.html>