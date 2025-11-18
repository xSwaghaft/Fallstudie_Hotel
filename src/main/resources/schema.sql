-- Vollständiges Schema – löscht bestehende DB und baut sie neu auf (Achtung: Datenverlust)
DROP DATABASE IF EXISTS hotelbooking;
CREATE DATABASE hotelbooking CHARACTER SET = 'utf8mb4' COLLATE = 'utf8mb4_unicode_ci';
USE hotelbooking;

-- Users
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  email VARCHAR(150),
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  birthdate DATE,
  role VARCHAR(32) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Guests (verknüpft mit users optional)
CREATE TABLE guests (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNIQUE,
  email VARCHAR(150) NOT NULL,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  address VARCHAR(255),
  phone_number VARCHAR(50),
  birthdate DATE,
  CONSTRAINT fk_guests_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Room categories
CREATE TABLE room_category (
  category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  description TEXT,
  price_per_night DECIMAL(10,2) NOT NULL,
  max_occupancy INT NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Rooms
CREATE TABLE rooms (
  room_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  room_number VARCHAR(64),
  category_id BIGINT NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  capacity INT DEFAULT 1,
  available BOOLEAN NOT NULL DEFAULT TRUE,
  description TEXT,
  CONSTRAINT fk_rooms_category FOREIGN KEY (category_id) REFERENCES room_category(category_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Password reset tokens (for forgot-password flow)
CREATE TABLE password_reset_tokens (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  token VARCHAR(64) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL,
  expires_at TIMESTAMP NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bookings
CREATE TABLE bookings (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_number VARCHAR(64) NOT NULL UNIQUE,
  amount INT,
  check_in_date DATE NOT NULL,
  check_out_date DATE NOT NULL,
  status VARCHAR(32) NOT NULL,
  total_price DECIMAL(12,2),
  guest_id BIGINT NOT NULL,
  room_id BIGINT,
  invoice_id BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_bookings_guest FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE RESTRICT,
  CONSTRAINT fk_bookings_room FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_booking_dates ON bookings(check_in_date, check_out_date);

-- Invoices (verweist auf Booking)
CREATE TABLE invoices (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  invoice_number VARCHAR(64) NOT NULL UNIQUE,
  amount DECIMAL(12,2) NOT NULL,
  issued_at DATETIME NOT NULL,
  due_date DATE,
  paid BOOLEAN NOT NULL DEFAULT FALSE,
  paid_at DATETIME,
  payment_method VARCHAR(64),
  status VARCHAR(32),
  booking_id BIGINT NOT NULL,
  CONSTRAINT fk_invoices_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Falls gewünscht: bookings.invoice_id -> invoices.id (optional, bidirektional)
ALTER TABLE bookings
  ADD CONSTRAINT fk_bookings_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE SET NULL;

-- Room extras (BookingExtra)
CREATE TABLE room_extras (
  BookingExtra_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  price DECIMAL(10,2) NOT NULL,
  extra_type VARCHAR(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Join table bookings <-> extras
CREATE TABLE booking_extra (
  booking_id BIGINT NOT NULL,
  extra_id BIGINT NOT NULL,
  PRIMARY KEY (booking_id, extra_id),
  CONSTRAINT fk_bookingextra_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
  CONSTRAINT fk_bookingextra_extra FOREIGN KEY (extra_id) REFERENCES room_extras(BookingExtra_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Optional: Many-to-Many Room <-> Booking (falls in Entities vorhanden)
CREATE TABLE room_bookings (
  room_id BIGINT NOT NULL,
  booking_id BIGINT NOT NULL,
  PRIMARY KEY (room_id, booking_id),
  CONSTRAINT fk_roombookings_room FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE CASCADE,
  CONSTRAINT fk_roombookings_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Payments
CREATE TABLE payments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  method VARCHAR(64),
  status VARCHAR(32),
  transaction_ref VARCHAR(100),
  paid_at DATETIME,
  CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Feedback
CREATE TABLE feedback (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT,
  guest_id BIGINT,
  rating INT,
  comment TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_feedback_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
  CONSTRAINT fk_feedback_guest FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Booking cancellations
CREATE TABLE booking_cancellation (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT NOT NULL,
  cancelled_at DATETIME NOT NULL,
  reason TEXT,
  refunded_amount DECIMAL(12,2) DEFAULT 0.00,
  handled_by BIGINT,
  CONSTRAINT fk_cancellation_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
  CONSTRAINT fk_cancellation_handled_by FOREIGN KEY (handled_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Booking modifications
CREATE TABLE booking_modification (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT NOT NULL,
  modified_at DATETIME NOT NULL,
  field_changed VARCHAR(255),
  old_value TEXT,
  new_value TEXT,
  reason TEXT,
  handled_by BIGINT,
  CONSTRAINT fk_modification_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
  CONSTRAINT fk_modification_handled_by FOREIGN KEY (handled_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Reports
CREATE TABLE reports (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  description TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by_user_id BIGINT NOT NULL,
  CONSTRAINT fk_reports_user FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Index-Empfehlungen
CREATE INDEX idx_bookings_guest ON bookings(guest_id);
CREATE INDEX idx_bookings_room ON bookings(room_id);
CREATE INDEX idx_payments_booking ON payments(booking_id);
CREATE INDEX idx_feedback_booking ON feedback(booking_id);

-- Ende des Schemas