-- Optional: the app creates these tables itself (ddl-auto=update). Run this only if you prefer manual setup.
CREATE DATABASE IF NOT EXISTS csrm_db;
USE csrm_db;

CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,          -- BCrypt hash
  email VARCHAR(150),
  role ENUM('STUDENT','FACULTY','ADMIN') NOT NULL,
  status ENUM('PENDING','APPROVED','REJECTED') NOT NULL
);
CREATE TABLE resources (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  type VARCHAR(30) NOT NULL,               -- CLASSROOM, LAB, LOCKER, EQUIPMENT
  location VARCHAR(150) NOT NULL,
  availability BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE bookings (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  resource_id BIGINT NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  status ENUM('CONFIRMED','CANCELLED') NOT NULL,
  reminder_sent BOOLEAN NOT NULL DEFAULT FALSE,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (resource_id) REFERENCES resources(id),
  INDEX idx_booking_resource_time (resource_id, start_time, end_time)
);
CREATE TABLE audit_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT,
  username VARCHAR(100),
  action VARCHAR(500) NOT NULL,
  timestamp DATETIME NOT NULL,
  INDEX idx_audit_user_time (user_id, timestamp)
);

-- 1) Detect overlapping bookings (returns any clash for resource 1, 2025-10-01 09:00-11:00)
SELECT * FROM bookings
WHERE resource_id = 1 AND status = 'CONFIRMED'
  AND start_time < '2025-10-01 11:00:00' AND end_time > '2025-10-01 09:00:00';

-- 2) Daily resource utilization report (booked hours out of a 10-hour day, 08:00-18:00)
SELECT r.name,
       ROUND(COALESCE(SUM(TIMESTAMPDIFF(MINUTE, b.start_time, b.end_time)), 0) / 60, 1) AS booked_hours,
       ROUND(COALESCE(SUM(TIMESTAMPDIFF(MINUTE, b.start_time, b.end_time)), 0) / 60 / 10 * 100, 1) AS utilization_pct
FROM resources r
LEFT JOIN bookings b ON b.resource_id = r.id AND b.status = 'CONFIRMED' AND DATE(b.start_time) = '2025-10-01'
GROUP BY r.id, r.name;

-- 3) Audit logs filtered by user and date
SELECT * FROM audit_logs
WHERE user_id = 1 AND DATE(timestamp) = '2025-10-01'
ORDER BY timestamp DESC;
