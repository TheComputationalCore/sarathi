-- =============================================
-- CIVILIZATIONAL BUS NETWORK DATASET
-- =============================================

-- =============================================
-- SAMPLE BUSES
-- =============================================

INSERT INTO bus (from_location,to_location,departure_time,arrival_time,price,total_seats,slug,active)
VALUES
('Delhi','Varanasi','2026-04-01 06:00:00','2026-04-01 18:00:00',1200,40,'delhi-varanasi-corridor',true),
('Varanasi','Prayagraj','2026-04-01 07:00:00','2026-04-01 10:00:00',400,40,'varanasi-prayagraj-pilgrimage',true),
('Prayagraj','Ayodhya','2026-04-01 08:00:00','2026-04-01 12:00:00',450,40,'prayagraj-ayodhya-route',true),
('Delhi','Nalanda','2026-04-01 05:30:00','2026-04-01 19:30:00',1500,40,'knowledge-corridor-delhi-nalanda',true),
('Nalanda','Bodh Gaya','2026-04-01 09:00:00','2026-04-01 11:00:00',300,40,'nalanda-bodhgaya-route',true),
('Chennai','Kanchipuram','2026-04-01 07:00:00','2026-04-01 09:30:00',250,40,'chennai-kanchipuram-temple-route',true),
('Kanchipuram','Thanjavur','2026-04-01 08:00:00','2026-04-01 14:00:00',600,40,'kanchipuram-thanjavur-route',true),
('Thanjavur','Madurai','2026-04-01 10:00:00','2026-04-01 15:00:00',550,40,'thanjavur-madurai-route',true),
('Sarnath','Kushinagar','2026-04-01 06:30:00','2026-04-01 13:00:00',700,40,'buddhist-heritage-route',true),
('Kochi','Calicut','2026-04-01 08:30:00','2026-04-01 14:00:00',500,40,'malabar-coast-route',true);

-- =============================================
-- BUS SCHEDULES
-- =============================================

INSERT INTO bus_schedule (bus_id,travel_date,available_seats)
VALUES
(1,'2026-04-01',40),
(1,'2026-04-02',40),
(1,'2026-04-03',40),
(2,'2026-04-01',40),
(2,'2026-04-02',40),
(3,'2026-04-01',40),
(3,'2026-04-02',40),
(4,'2026-04-01',40),
(4,'2026-04-02',40),
(5,'2026-04-01',40),
(6,'2026-04-01',40),
(7,'2026-04-01',40),
(8,'2026-04-01',40),
(9,'2026-04-01',40),
(10,'2026-04-01',40);

-- =============================================
-- SAMPLE ROUTE STOPS
-- =============================================

INSERT INTO route_stops (bus_id,yatra_point_id,sequence_order)
VALUES
(1,1,1),
(1,2,2),
(1,3,3),
(2,2,1),
(2,4,2),
(3,4,1),
(3,5,2),
(4,1,1),
(4,6,2),
(4,7,3),
(5,6,1),
(5,8,2);

-- =============================================
-- END DATASET
-- =============================================
