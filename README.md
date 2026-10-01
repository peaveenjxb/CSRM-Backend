# CSRM Backend (Spring Boot + MySQL)

## Requirements
Java 17+, Maven 3.9+ (or open the folder in IntelliJ / Eclipse / VS Code), MySQL 8+.

## Run
1. Start MySQL and set `spring.datasource.username/password` in `src/main/resources/application.properties`.
   The `csrm_db` database and all tables are created automatically on first start
   (`database/schema.sql` is there for manual setup and holds the 3 required SQL queries).
2. `mvn spring-boot:run`  ->  http://localhost:8080
3. First-run admin login: `admin` / `Admin@123` (change in application.properties). Sample resources are seeded.

## Architecture
Controller -> Service -> Repository, JWT auth (stateless), BCrypt passwords, `UserView` DTO so passwords are never returned,
`GlobalExceptionHandler` returns `{timestamp,status,error,message}` JSON for every error.

## Endpoints
| Method | Path | Access |
|---|---|---|
| POST | /api/auth/register, /api/auth/login | public |
| GET/PUT | /api/admin/users, /api/admin/users/{id} | ADMIN |
| GET | /api/admin/stats, /api/admin/reports/utilization?date= | ADMIN |
| GET | /api/resources | any user |
| POST/PUT/DELETE | /api/resources[/{id}] | ADMIN |
| GET | /api/bookings?resourceId=&date= | any user (no filters: ADMIN) |
| GET | /api/bookings/my | any user |
| POST/PUT/DELETE | /api/bookings[/{id}] | owner or ADMIN |
| GET | /api/audit?userId=&date= | ADMIN |
| POST | /api/services | any user |

## Notifications
Every alert is printed in the console. For real email, fill in the `spring.mail.*` lines in application.properties
and give users an email at registration. Reminders go out 30 minutes before a booking starts.
SMS: add your provider call in `NotificationService`.
