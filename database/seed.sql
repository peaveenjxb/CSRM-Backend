-- Sample seed data for CSRM
USE csrm_db;

-- Users (Default passwords: Admin@123, Faculty@123, Student@123)
INSERT INTO users (id, username, password, email, role, status) VALUES
(1, 'admin', '$2a$10$c.2JOMZFu/.Izy8b0.hf6.RgcoVIWogIB9MliqauA/w0kvypnQ576', 'admin@university.edu', 'ADMIN', 'APPROVED'),
(2, 'prof_smith', '$2a$10$gUWa2NuD8/QQ.IbpD/z1v.gbw0F9XtNFhIyjnE8G2B0PbHDCoaqvK', 'smith@university.edu', 'FACULTY', 'APPROVED'),
(3, 'alice_student', '$2a$10$nPXoYqRAY38OoZfEB7C4PuNr.C/yL61vIfHCrJ.pHMGP5iEczuKjC', 'alice@university.edu', 'STUDENT', 'APPROVED')
ON DUPLICATE KEY UPDATE username=VALUES(username);

-- Resources
INSERT INTO resources (id, name, type, location, availability) VALUES
(1, 'Classroom A101', 'CLASSROOM', 'Block A, Floor 1', TRUE),
(2, 'Computer Lab 1', 'LAB', 'Block B, Floor 2', TRUE),
(3, 'Locker L-12', 'LOCKER', 'Library corridor', TRUE),
(4, 'Projector P-3', 'EQUIPMENT', 'AV store room', TRUE),
(5, 'Physics Lab 2', 'LAB', 'Science Block, Floor 3', TRUE),
(6, 'Seminar Hall C', 'CLASSROOM', 'Block C, Ground Floor', TRUE)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Sample Bookings
INSERT INTO bookings (id, user_id, resource_id, start_time, end_time, status, reminder_sent) VALUES
(1, 2, 1, '2026-10-01 09:00:00', '2026-10-01 11:00:00', 'CONFIRMED', FALSE),
(2, 3, 2, '2026-10-01 14:00:00', '2026-10-01 16:00:00', 'CONFIRMED', FALSE)
ON DUPLICATE KEY UPDATE id=VALUES(id);

-- Sample Audit Logs
INSERT INTO audit_logs (id, user_id, username, action, timestamp) VALUES
(1, 1, 'admin', 'SYSTEM_INITIALIZED', NOW()),
(2, 2, 'prof_smith', 'BOOKING_CREATED #1 Classroom A101 2026-10-01 09:00 to 11:00', NOW()),
(3, 3, 'alice_student', 'BOOKING_CREATED #2 Computer Lab 1 2026-10-01 14:00 to 16:00', NOW())
ON DUPLICATE KEY UPDATE id=VALUES(id);
