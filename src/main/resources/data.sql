-- =========================
-- USERS
-- =========================
INSERT IGNORE INTO users
(id, username, password, email, first_name, last_name,
 street, house_number, postal_code, city, country,
 birthdate, role, active, created_at)
VALUES
(1,'admin','Test123!','admin@example.com','Admin','Istrator','Hauptstraße','1','33602','Bielefeld','Germany','1980-01-01','MANAGER','1','2025-01-01 08:00:00'),
(2,'reception1','Test123!','desk1@example.com','Anna','Desk','Marktweg','12','33604','Bielefeld','Germany','1990-05-12','RECEPTIONIST','1','2025-02-01 09:00:00'),
(3,'reception2','Test123!','desk2@example.com','John','Front','Bahnhofstraße','88','33609','Bielefeld','Germany','1992-03-30','RECEPTIONIST','1','2025-03-01 09:30:00'),
(4,'clerk','Test123!','clerk@example.com','Luca','Bianchi','Alte Allee','4A','33615','Bielefeld','Germany','1985-07-07','GUEST','1','2025-04-01 10:00:00'),
(5,'guestuser','Test123!','guest@example.com','Sofia','Garcia','Wiesenweg','7','33619','Bielefeld','Germany','1995-11-11','GUEST','1','2025-05-01 11:00:00');

-- =========================
-- ROOM CATEGORIES
-- =========================
INSERT IGNORE INTO room_category
(category_id, name, description, price_per_night, max_occupancy, active)
VALUES
(1,'Standard','Small double room',79.90,2,TRUE),
(2,'Deluxe','Spacious room with a view',129.90,3,TRUE),
(3,'Suite','Suite with living area',249.00,4,TRUE),
(4,'Economy','Budget single room',49.90,1,TRUE),
(5,'Family','Family room with two beds',159.90,4,TRUE);

INSERT IGNORE INTO room_category_amenities (category_id, amenity) VALUES
(1,'SHOWER'),(1,'BATHTUB'),
(2,'SHOWER'),(2,'BATHTUB'),(2,'BALCONY'),
(3,'SHOWER'),(3,'BATHTUB'),(3,'BALCONY'),(3,'AIRCONDITIONING'),(3,'MINIBAR'),
(4,'SHOWER'),
(5,'SHOWER'),(5,'BATHTUB'),(5,'AIRCONDITIONING');

-- =========================
-- ROOMS
-- =========================
INSERT IGNORE INTO rooms
(room_id, room_number, floor, category_id, status, active, information)
VALUES
(1,'101',1,1,'AVAILABLE',TRUE,'Room 101 Standard'),
(2,'102',1,1,'CLEANING',TRUE,'Room 102 Standard - under renovation'),
(3,'201',2,2,'AVAILABLE',TRUE,'Room 201 Deluxe with balcony'),
(4,'301',3,3,'OCCUPIED',TRUE,'Suite 301 with living room'),
(5,'401',4,5,'AVAILABLE',TRUE,'Family room 401'),
(6,'501',5,4,'AVAILABLE',TRUE,'Single room 501 Economy');

-- =========================
-- EXTRAS
-- =========================
INSERT IGNORE INTO room_extras (name, description, price, per_person) VALUES
('Breakfast','Breakfast buffet included',12.50,1),
('Spa access','Day pass for spa',20.00,1),
('Parking','Day parking',8.00,0),
('Extra bed','Rollaway bed per night',25.00,0),
('Premium Wi-Fi','High-speed internet',5.00,0);

-- =========================
-- BOOKINGS
-- guest_id fixed to real guests (4 or 5)
-- CONFIRMED in seed means paid -> invoices/payments are PAID
-- =========================
INSERT IGNORE INTO bookings
(id, booking_number, amount, check_in_date, check_out_date, status,
 total_price, guest_id, room_id, room_category_id, created_at)
VALUES
(1,'20251120-A1B2C3D4',2,'2025-11-20','2025-11-22','COMPLETED',159.80,4,1,1,'2025-11-01'),
(2,'20251110-E5F6G7H8',1,'2025-11-10','2025-11-12','CANCELLED',129.90,5,3,2,'2025-11-02'),
(3,'20251201-I9J0K1L2',3,'2025-12-01','2025-12-04','COMPLETED',479.70,5,5,5,'2025-11-21'),
(4,'20251115-M3N4O5P6',1,'2025-11-15','2025-11-16','COMPLETED',249.00,4,4,3,'2025-11-14'),
(5,'20251220-Q7R8S9T0',2,'2025-12-20','2025-12-25','CANCELLED',319.80,5,3,2,'2025-11-10'),
(6,'20251205-U1V2W3X4',1,'2025-12-05','2025-12-06','COMPLETED',49.90,4,6,4,'2025-11-25'),
(7,'20241010-G1H2I3J4',2,'2024-10-10','2024-10-15','COMPLETED',649.50,5,3,2,'2024-10-01'),
(8,'20240915-K5L6M7N8',1,'2024-09-15','2024-09-20','COMPLETED',249.00,5,4,3,'2024-09-10'),

(100,'20260110-G2A',2,'2026-01-10','2026-01-15','CONFIRMED',649.50,4,2,2,'2025-12-05'),
(101,'20260201-G3A',2,'2026-02-01','2026-02-03','PENDING',259.80,5,3,2,'2025-12-10'),
(102,'20260120-G4A',1,'2026-01-20','2026-01-23','CONFIRMED',747.00,4,4,3,'2025-12-12'),
(103,'20260210-G4B',2,'2026-02-10','2026-02-12','PENDING',319.80,4,5,5,'2025-12-12'),

(104,'20260301-XA1',2,'2026-03-01','2026-03-05','CONFIRMED',519.60,5,3,2,'2026-02-10'),
(105,'20260310-XA2',1,'2026-03-10','2026-03-12','CONFIRMED',159.80,4,1,1,'2026-02-15'),
(106,'20260315-XA3',3,'2026-03-15','2026-03-20','PENDING',1245.00,5,4,3,'2026-02-20'),
(107,'20260401-XA4',1,'2026-04-01','2026-04-03','PENDING',99.80,4,6,4,'2026-03-01'),
(108,'20260405-XA5',2,'2026-04-05','2026-04-10','CONFIRMED',799.50,5,5,5,'2026-03-05'),
(109,'20260415-XA6',1,'2026-04-15','2026-04-18','PENDING',149.70,4,6,4,'2026-03-10'),
(110,'20260501-XA7',2,'2026-05-01','2026-05-04','CONFIRMED',389.70,5,3,2,'2026-04-01'),
(111,'20260510-XA8',1,'2026-05-10','2026-05-11','PENDING',79.90,4,1,1,'2026-04-05'),
(112,'20260520-XA9',3,'2026-05-20','2026-05-25','PENDING',1245.00,5,4,3,'2026-04-15'),
(113,'20260601-XB0',2,'2026-06-01','2026-06-06','CONFIRMED',999.00,5,5,5,'2026-05-01');

-- =========================
-- INVOICES
-- completed/confirmed => PAID
-- cancelled => REFUNDED
-- pending bookings => PENDING
-- =========================
INSERT IGNORE INTO invoices
(id, invoice_number, amount, issued_at, payment_method, status, booking_id)
VALUES
(1,'INV-2025-1849832321989',159.80,'2025-11-01 11:05:00','CARD','PAID',1),
(2,'INV-2025-1154154154198',129.90,'2025-11-02 09:10:00','CARD','REFUNDED',2),
(3,'INV-2025-1154574414554',479.70,'2025-11-21 16:25:00','TRANSFER','PAID',3),
(4,'INV-2025-1516541415154',249.00,'2025-11-14 10:15:00','CASH','PAID',4),
(5,'INV-2025-1415516223144',319.80,'2025-11-10 14:35:00','TRANSFER','REFUNDED',5),
(6,'INV-2024-1041482989446',649.50,'2024-10-10 10:00:00','CARD','PAID',7),
(7,'INV-2024-1767085397048',249.00,'2024-09-15 09:00:00','CARD','PAID',8),
(8,'INV-2025-1125000000006',49.90,'2025-12-06 09:30:00','CASH','PAID',6),

(200,'INV-20251205-200',649.50,'2025-12-05 09:00:00','CARD','PAID',100),
(201,'INV-20251210-201',259.80,'2025-12-10 10:00:00','CARD','PENDING',101),
(202,'INV-20251212-202',747.00,'2025-12-12 11:00:00','CARD','PAID',102),
(203,'INV-20251212-203',319.80,'2025-12-12 11:15:00','CARD','PENDING',103),

(204,'INV-20260210-204',519.60,'2026-02-10 10:00:00','CARD','PAID',104),
(205,'INV-20260215-205',159.80,'2026-02-15 11:00:00','CARD','PAID',105),
(206,'INV-20260220-206',1245.00,'2026-02-20 12:00:00','TRANSFER','PENDING',106),
(207,'INV-20260301-207',99.80,'2026-03-01 09:30:00','CARD','PENDING',107),
(208,'INV-20260305-208',799.50,'2026-03-05 10:15:00','CARD','PAID',108),
(209,'INV-20260310-209',149.70,'2026-03-10 14:00:00','CARD','PENDING',109),
(210,'INV-20260401-210',389.70,'2026-04-01 09:00:00','TRANSFER','PAID',110),
(211,'INV-20260405-211',79.90,'2026-04-05 11:00:00','CARD','PENDING',111),
(212,'INV-20260415-212',1245.00,'2026-04-15 12:30:00','TRANSFER','PENDING',112),
(213,'INV-20260501-213',999.00,'2026-05-01 10:00:00','CARD','PAID',113);

-- =========================
-- BOOKING EXTRAS
-- =========================
INSERT IGNORE INTO booking_extra (booking_id, extra_id) VALUES
(1,1),(1,2),
(3,3),
(4,1),
(5,2),
(6,4);

-- =========================
-- ROOM BOOKINGS
-- =========================
INSERT IGNORE INTO room_bookings (room_id, booking_id) VALUES
(1,1),(3,2),(5,3),(4,4),(3,5),(6,6),(3,7),(4,8),
(2,100),(3,101),(4,102),(5,103),
(3,104),(1,105),(4,106),(6,107),(5,108),
(6,109),(3,110),(1,111),(4,112),(5,113);

-- =========================
-- PAYMENTS
-- completed/confirmed => PAID with amount + paid_at
-- pending bookings => PENDING with real amount (NOT 0.00)
-- cancelled => REFUNDED
-- =========================
INSERT IGNORE INTO payments
(id, booking_id, amount, method, status, transaction_ref, paid_at)
VALUES
(1,1,159.80,'CARD','PAID','TXN-1001','2025-11-01 11:00:00'),
(2,2,129.90,'CARD','REFUNDED','TXN-1002','2025-11-03 09:30:00'),
(3,3,479.70,'TRANSFER','PAID','TXN-1003','2025-12-05 12:00:00'),
(4,4,249.00,'CASH','PAID','TXN-1004','2025-11-16 10:10:00'),
(5,5,319.80,'TRANSFER','REFUNDED','TXN-1005','2025-11-10 14:30:00'),
(6,6,49.90,'CASH','PAID','TXN-1006','2025-12-12 09:00:00'),
(7,7,649.50,'CARD','PAID','TXN-1007','2024-10-10 10:00:00'),
(8,8,249.00,'CARD','PAID','TXN-1008','2024-09-15 09:00:00'),

(9,100,649.50,'CARD','PAID','TXN-1009','2025-12-05 09:05:00'),
(10,101,259.80,'CARD','PENDING','TXN-1010',NULL),
(11,102,747.00,'CARD','PAID','TXN-1011','2025-12-12 11:10:00'),
(12,103,319.80,'CARD','PENDING','TXN-1012',NULL),

(13,104,519.60,'CARD','PAID','TXN-2013','2026-02-10 10:05:00'),
(14,105,159.80,'CARD','PAID','TXN-2014','2026-02-15 11:05:00'),
(15,106,1245.00,'TRANSFER','PENDING','TXN-2015',NULL),
(16,107,99.80,'CARD','PENDING','TXN-2016',NULL),
(17,108,799.50,'CARD','PAID','TXN-2017','2026-03-05 10:20:00'),
(18,109,149.70,'CARD','PENDING','TXN-2018',NULL),
(19,110,389.70,'TRANSFER','PAID','TXN-2019','2026-04-01 09:05:00'),
(20,111,79.90,'CARD','PENDING','TXN-2020',NULL),
(21,112,1245.00,'TRANSFER','PENDING','TXN-2021',NULL),
(22,113,999.00,'CARD','PAID','TXN-2022','2026-05-01 10:05:00');

-- =========================
-- CANCELLATIONS
-- =========================
INSERT IGNORE INTO booking_cancellation (id, booking_id, cancelled_at, reason, refunded_amount, handled_by) VALUES
(1,2,'2025-11-03 09:15:00','Guest illness',129.90,2),
(2,5,'2025-11-10 14:30:00','Travel plans changed',319.80,1);

-- =========================
-- MODIFICATIONS
-- =========================
INSERT IGNORE INTO booking_modification (id, booking_id, modified_at, field_changed, old_value, new_value, reason, handled_by) VALUES
(1,1,'2025-11-05 10:00:00','check_out_date','2025-11-21','2025-11-22','Guest extended stay',2),
(2,3,'2025-11-20 12:00:00','room_id','2','5','Upgrade to Family',3),
(3,4,'2025-11-10 08:30:00','amount','1','2','Additional guest added',2);

-- =========================
-- FEEDBACK
-- =========================
INSERT IGNORE INTO feedback (id, booking_id, rating, comment, created_at) VALUES
(1,1,5,'Very clean room and friendly staff.','2025-11-02 12:00:00'),
(2,3,4,'Good stay, breakfast could be better.','2025-11-22 09:30:00'),
(3,4,5,'Perfect suite, great view.','2025-11-16 14:10:00'),
(4,2,2,'Cancellation resulted in refund problems.','2025-11-04 10:00:00'),
(5,6,4,'Nice staff, clean room.','2025-12-07 11:00:00'),
(6,7,5,'Fantastic stay! The Deluxe room was beautiful and the service was excellent.','2024-10-15 12:00:00');

-- =========================
-- ROOM IMAGES
-- =========================
INSERT IGNORE INTO room_images (image_path, alt_text, title, is_primary, category_id) VALUES
('/images/rooms/standard_001.png','Standard room view 1','Standard Room',1,1),
('/images/rooms/standard_002.png','Standard room view 2','Standard Room',0,1),
('/images/rooms/deluxe_001.png','Deluxe room view 1','Deluxe Room',1,2),
('/images/rooms/deluxe_002.png','Deluxe room view 2','Deluxe Room',0,2),
('/images/rooms/Suite_001.png','Suite room view 1','Suite Room',1,3),
('/images/rooms/Economy_001.png','Economy room view 1','Economy Room',1,4),
('/images/rooms/Family_001.png','Family room view 1','Family Room',1,5);
