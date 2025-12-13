-- ...existing code...
USE hotelbooking;

-- Hinweis: Ersetze bei Bedarf die Passwort-Hashes (bcrypt). Für admin kannst du z.B. deinen generierten Hash für "admin123" einsetzen.

-- Clean up: Alle Daten löschen (für sauberen Neustart)
SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE feedback;
TRUNCATE TABLE booking_cancellation;
TRUNCATE TABLE booking_modification;
TRUNCATE TABLE payments;
TRUNCATE TABLE booking_extra;
TRUNCATE TABLE room_bookings;
TRUNCATE TABLE bookings;
TRUNCATE TABLE invoices;
TRUNCATE TABLE guests;
TRUNCATE TABLE rooms;
TRUNCATE TABLE room_images;
TRUNCATE TABLE room_category;
TRUNCATE TABLE room_extras;
TRUNCATE TABLE users;
TRUNCATE TABLE reports;
SET FOREIGN_KEY_CHECKS=1;

-- ---------- Users (mind. 5 Einträge) ----------
INSERT IGNORE INTO users 
(id, username, password, email, first_name, last_name,
 street, house_number, postal_code, city, country,
 birthdate, role, active, created_at) 
VALUES
(1,'admin','Test123!','admin@example.com','Admin','Istrator',
 'Hauptstraße','1','33602','Bielefeld','Germany',
 '1980-01-01','MANAGER','1','2025-01-01 08:00:00'),

(2,'reception1','Test123!','desk1@example.com','Anna','Desk',
 'Marktweg','12','33604','Bielefeld','Germany',
 '1990-05-12','RECEPTIONIST','1','2025-02-01 09:00:00'),

(3,'reception2','Test123!','desk2@example.com','John','Front',
 'Bahnhofstraße','88','33609','Bielefeld','Germany',
 '1992-03-30','RECEPTIONIST','1','2025-03-01 09:30:00'),

(4,'clerk','Test123!','clerk@example.com','Luca','Bianchi',
 'Alte Allee','4A','33615','Bielefeld','Germany',
 '1985-07-07','GUEST','0','2025-04-01 10:00:00'),

(5,'guestuser','Test123!','guest@example.com','Sofia','Garcia',
 'Wiesenweg','7','33619','Bielefeld','Germany',
 '1995-11-11','GUEST','1','2025-05-01 11:00:00');
-- ---------- RoomCategory (mind. 5 Einträge) ----------
INSERT IGNORE INTO room_category (category_id, name, description, price_per_night, max_occupancy, active) VALUES
(1,'Standard','Kleines Doppelzimmer',79.90,2,TRUE),
(2,'Deluxe','Größeres Zimmer mit Blick',129.90,3,TRUE),
(3,'Suite','Suite mit Wohnraum',249.00,4,TRUE),
(4,'Economy','Günstiges Einzelzimmer',49.90,1,TRUE),
(5,'Family','Familienzimmer mit 2 Betten',159.90,4,TRUE);

-- ---------- Room amenities ----------
INSERT IGNORE INTO room_category_amenities (category_id, amenity) VALUES
(1,'SHOWER'),
(1,'BATHTUB'),
(2,'SHOWER'),
(2,'BATHTUB'),
(2,'BALCONY'),
(3,'SHOWER'),
(3,'BATHTUB'),
(3,'BALCONY'),
(3,'AIRCONDITIONING'),
(3,'MINIBAR'),
(4,'SHOWER'),
(5,'SHOWER'),
(5,'BATHTUB'),
(5,'AIRCONDITIONING');

-- ---------- Rooms (mind. 6 Einträge) ----------
INSERT IGNORE INTO rooms (room_id, room_number, floor, category_id, status, active, information) VALUES
(1, '101', 1, 1, 'AVAILABLE', TRUE, 'Zimmer 101 Standard'),
(2, '102', 1, 1, 'CLEANING', TRUE, 'Zimmer 102 Standard - Renovierung'),
(3, '201', 2, 2, 'AVAILABLE', TRUE, 'Zimmer 201 Deluxe mit Balkon'),
(4, '301', 3, 3, 'OCCUPIED', TRUE, 'Suite 301 mit Wohnzimmer'),
(5, '401', 4, 5, 'AVAILABLE', TRUE, 'Familienzimmer 401'),
(6, '501', 5, 4, 'AVAILABLE', TRUE, 'Einzelzimmer 501 Economy');

-- ---------- Guests (mind. 5 Einträge) ----------
INSERT IGNORE INTO guests (id, user_id, email, first_name, last_name, address, phone_number, birthdate) VALUES
(1,1,'anna.mueller@example.com','Anna','Müller','Münchener Str. 1, 80331 München','+491701234567','1990-06-15'),
(2,3,'john.doe@example.com','John','Doe','Berliner Allee 2, 10115 Berlin','+491601234567','1988-04-10'),
(3,4,'luca.bianchi@example.com','Luca','Bianchi','Via Roma 4, 00100 Roma','+393491234567','1985-07-07'),
(4,2,'maria.schmidt@example.com','Maria','Schmidt','Hamburger Weg 3, 20095 Hamburg','+491521234567','1991-12-20'),
(5,5,'sofia.garcia@example.com','Sofia','Garcia','Calle Mayor 5, 28013 Madrid','+34111234567','1995-11-11');

-- ---------- Bookings (mind. 6 Einträge) ----------
-- ---------- Bookings (mind. 6 Einträge) ----------
INSERT IGNORE INTO bookings
(id, booking_number, amount, check_in_date, check_out_date, status,
 total_price, guest_id, room_id, room_category_id, created_at)
VALUES
(1,'20251120-A1B2C3D4',2,'2025-11-20','2025-11-22','CONFIRMED',159.80,1,1,1,'2025-11-01'),
(2,'20251110-E5F6G7H8',1,'2025-11-10','2025-11-12','CANCELLED',129.90,2,3,2,'2025-11-02'),
(3,'20251201-I9J0K1L2',3,'2025-12-01','2025-12-04','CONFIRMED',479.70,3,5,5,'2025-11-21'),
(4,'20251115-M3N4O5P6',1,'2025-11-15','2025-11-16','CONFIRMED',249.00,4,4,3,'2025-11-14'),
(5,'20251220-Q7R8S9T0',2,'2025-12-20','2025-12-25','CANCELLED',319.80,5,3,2,'2025-11-10'),
(6,'20251205-U1V2W3X4',1,'2025-12-05','2025-12-06','PENDING',49.90,1,6,4,'2025-11-25'),
(7,'20241010-G1H2I3J4',2,'2024-10-10','2024-10-15','CONFIRMED',649.50,5,3,2,'2024-10-01'),
(8,'20240915-K5L6M7N8',1,'2024-09-15','2024-09-20','CONFIRMED',249.00,5,4,3,'2024-09-10');


-- ---------- Invoices (mind. 5 Einträge) ----------
-- ---------- Invoices (mind. 5 Einträge) ----------
INSERT IGNORE INTO invoices
(id, invoice_number, amount, issued_at, payment_method, status, booking_id)
VALUES
(1,'INV-20251101-001',159.80,'2025-11-01 11:05:00','CARD','PAID',1),
(2,'INV-20251102-002',129.90,'2025-11-02 09:10:00','CARD','PENDING',2),
(3,'INV-20251103-003',479.70,'2025-11-21 16:25:00','TRANSFER','PAID',3),
(4,'INV-20251104-004',249.00,'2025-11-14 10:15:00','CASH','PAID',4),
(5,'INV-20251105-005',319.80,'2025-11-10 14:35:00','CARD','REFUNDED',5),
(6,'INV-20241010-006',649.50,'2024-10-10 10:00:00','CARD','PAID',7),
(7,'INV-20240915-007',249.00,'2024-09-15 09:00:00','CARD','PAID',8);



-- ---------- Room extras (BookingExtra) (mind. 5 Einträge) ----------
INSERT INTO room_extras (name, description, price, extra_type) VALUES
('Frühstück','Buffetfrühstück inklusive',12.50,'BREAKFAST'),
('Parkplatz','Tagesparkplatz',8.00,'PARKING'),
('Zusatzbett','Aufstellbett pro Nacht',25.00,'EXTRA_BED'),
('WLAN Premium','Highspeed Internet',5.00,'WIFI'),
('SPA Zugang','Tagespass für SPA',20.00,'SPA');


-- ---------- booking_extra (Zuordnungen Buchung <-> Extras) ----------
INSERT IGNORE INTO booking_extra (booking_id, extra_id) VALUES
(1,1),
(1,2),
(3,3),
(4,1),
(5,2),
(6,4);

-- ---------- room_bookings (ManyToMany Room <-> Booking) ----------
INSERT IGNORE INTO room_bookings (room_id, booking_id) VALUES
(1,1),
(3,2),
(5,3),
(4,4),
(3,5),
(6,6),
(3,7),
(4,8);

-- ---------- Payments (mind. 6 Einträge) ----------
INSERT IGNORE INTO payments (id, booking_id, amount, method, status, transaction_ref, paid_at) VALUES
(1,1,159.80,'CARD','PAID','TXN-1001','2025-11-01 11:00:00'),
(2,2,50.00,'CARD','PARTIAL','TXN-1002','2025-11-02 09:00:00'),
(3,3,479.70,'TRANSFER','PAID','TXN-1003','2025-11-21 16:20:00'),
(4,4,249.00,'CASH','PAID','TXN-1004','2025-11-14 10:10:00'),
(5,5,0.00,'CARD','REFUNDED','TXN-1005','2025-11-10 14:30:00'),
(6,6,49.90,'CASH','PENDING','TXN-1006',NULL),
(7,7,649.50,'CARD','PAID','TXN-1007','2024-10-10 10:00:00'),
(8,8,249.00,'CARD','PAID','TXN-1008','2024-09-15 09:00:00');

-- ---------- Booking cancellations (mind. 5 Einträge) ----------
INSERT IGNORE INTO booking_cancellation (id, booking_id, cancelled_at, reason, refunded_amount, handled_by) VALUES
(1,2,'2025-11-03 09:15:00','Krankheit des Gastes',129.90,2),
(2,5,'2025-11-10 14:30:00','Reisepläne geändert',319.80,1),
(3,6,'2025-11-12 08:00:00','Kurzfristig abgesagt',0.00,NULL),
(4,4,'2025-11-13 10:00:00','Persönlicher Grund',0.00,3),
(5,1,'2025-11-14 12:00:00','Doppelbuchung',0.00,2);

-- ---------- Booking modifications (mind. 5 Einträge) ----------
INSERT IGNORE INTO booking_modification (id, booking_id, modified_at, field_changed, old_value, new_value, reason, handled_by) VALUES
(1,1,'2025-11-05 10:00:00','check_out_date','2025-11-21','2025-11-22','Gast verlängert Aufenthalt',2),
(2,3,'2025-11-20 12:00:00','room_id','2','5','Upgrade auf Deluxe',3),
(3,4,'2025-11-10 08:30:00','amount','1','2','Zusatzgast hinzugefügt',2),
(4,6,'2025-11-25 09:00:00','check_in_date','2025-12-06','2025-12-05','Korrektur Datum',1),
(5,1,'2025-11-06 09:00:00','note','alt','neu','Interne Notiz',4);

-- ---------- Feedback (mind. 5 Einträge) ----------
INSERT IGNORE INTO feedback (id, booking_id, guest_id, rating, comment) VALUES
(1,1,1,5,'Sehr sauberes Zimmer und freundliches Personal.'),
(2,3,5,4,'Guter Aufenthalt, Frühstück könnte besser sein.'),
(3,4,5,5,'Perfekte Suite, tolle Aussicht.'),
(4,2,2,2,'Stornierung ergab Probleme mit Rückerstattung.'),
(5,6,1,4,'Nettes Personal, Zimmer sauber.'),
(6,7,5,5,'Fantastischer Aufenthalt! Das Deluxe Zimmer war wunderschön und der Service war ausgezeichnet.');

-- ---------- Reports (mind. 5 Einträge) ----------
INSERT IGNORE INTO reports (id, title, description, created_by_user_id) VALUES
(1,'Auslastung November','Monatliche Auslastung November 2025',1),
(2,'Umsatz Dezember','Umsatzübersicht Dezember 2025',1),
(3,'Kundenfeedback','Zusammenfassung Feedback November',2),
(4,'Wartung','Technische Wartung geplant',2),
(5,'Personaleinsatz','Einsatzplan Rezeption',3);

-- ---------- Room Images (für Standard und Deluxe Kategorien) ----------
INSERT IGNORE INTO room_images (image_path, alt_text, title, display_order, is_primary, category_id) VALUES
('images/rooms/standard_001.png', 'Standard Zimmer Ansicht 1', 'Standard Zimmer', 0, TRUE, 1),
('images/rooms/standard_002.png', 'Standard Zimmer Ansicht 2', 'Standard Zimmer', 1, FALSE, 1),
('images/rooms/deluxe_001.png', 'Deluxe Zimmer Ansicht 1', 'Deluxe Zimmer', 0, TRUE, 2),
('images/rooms/deluxe_002.png', 'Deluxe Zimmer Ansicht 2', 'Deluxe Zimmer', 1, FALSE, 2),
('images/rooms/Suite_001.png', 'Suite Zimmer Ansicht 1', 'Deluxe Zimmer', 1, TRUE, 3),
('images/rooms/Economy_001.png', 'Economy Zimmer Ansicht 1', 'Deluxe Zimmer', 1, TRUE, 4),
('images/rooms/Family_001.png', 'Family Zimmer Ansicht 1', 'Deluxe Zimmer', 1, TRUE, 5);


