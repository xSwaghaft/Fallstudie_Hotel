-- Cleanup to avoid duplicate PK errors when re-running the script
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM booking_extra;
DELETE FROM room_bookings;
DELETE FROM invoices;
DELETE FROM bookings;
DELETE FROM guests;
DELETE FROM rooms;
DELETE FROM room_category;
DELETE FROM users;
SET FOREIGN_KEY_CHECKS = 1;

-- Users
INSERT INTO users (id, username, password, email, first_name, last_name, birthdate, role, created_at) VALUES
  (1, 'john.guest', 'guest123', 'john@hotel.com', 'John', 'Doe', '1990-01-01', 'GUEST', '2025-11-15 10:00:00'),
  (2, 'sarah.reception', 'recep123', 'sarah@hotel.com', 'Sarah', 'Smith', '1988-05-12', 'RECEPTIONIST', '2025-11-15 10:00:00'),
  (3, 'david.manager', 'manager123', 'david@hotel.com', 'David', 'Miller', '1985-09-20', 'MANAGER', '2025-11-15 10:00:00');

-- Room categories
INSERT INTO room_category (category_id, name, description, price_per_night, max_occupancy, active) VALUES
  (1, 'Single', 'Single room with one bed', 50.00, 1, true),
  (2, 'Double', 'Double room with two beds', 80.00, 2, true),
  (3, 'Suite', 'Luxury suite', 250.00, 4, true);

-- Rooms (note: primary key column is room_id)
INSERT INTO rooms (room_id, category_id, price, availability, information) VALUES
  (1, 1, 50.00, true, 'Room 101'),
  (2, 2, 80.00, false, 'Room 102 - occupied'),
  (3, 3, 250.00, true, 'Room 201 - suite');

-- Guests (user_id must reference users.id)
INSERT INTO guests (id, user_id, email, first_name, last_name, address, phone_number, birthdate) VALUES
  (1, 1, 'john@hotel.com', 'John', 'Doe', 'Main St 1', '+491234567890', '1990-01-01'),
  (2, 2, 'sarah@hotel.com', 'Sarah', 'Smith', 'Second St 2', '+49111222333', '1988-05-12'),
  (3, 3, 'david@hotel.com', 'David', 'Miller', 'Third St 3', '+49199887766', '1985-09-20');

-- Bookings
INSERT INTO bookings (id, booking_number, amount, check_in_date, check_out_date, status, total_price, guest_id, room_id) VALUES
  (1, 'BK-1001', 1, '2025-11-20', '2025-11-25', 'CONFIRMED', 500.00, 1, 1),
  (2, 'BK-1002', 1, '2025-11-10', '2025-11-12', 'CANCELLED', 300.00, 2, 2),
  (3, 'BK-1003', 1, '2025-12-01', '2025-12-05', 'CONFIRMED', 1200.00, 3, 3);

-- Invoices (each invoice references an existing booking via booking_id)
INSERT INTO invoices (id, invoice_number, amount, issued_at, paid_at, payment_method, invoice_status, booking_id) VALUES
  (1, 'INV-1001', 500.00, '2025-11-15 10:05:00', NULL, 'CARD', 'PAID', 1),
  (2, 'INV-1002', 300.00, '2025-11-11 09:00:00', NULL, 'CASH', 'PENDING', 2),
  (3, 'INV-1003', 1200.00, '2025-11-20 11:00:00', '2025-11-21 12:00:00', 'ONLINE', 'PAID', 3);
