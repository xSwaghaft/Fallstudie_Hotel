# Fallstudie_Hotel
Software Buchung und Verwaltung von Hotelzimmern

## HotelBookingApp

A Java-based Hotel Booking Application built with Spring Boot, Vaadin, and MariaDB.

### Technologies Used
- **Spring Boot 3.5.7** - Application framework
- **Vaadin 24.9.5** - UI framework
- **Spring Data JPA** - Data persistence
- **MariaDB 10.11** - Database
- **Maven** - Build tool
- **Java 21** - Runtime

---

## 🚀 Quick Start mit Docker Compose (Empfohlen)

Die einfachste Methode, die Anwendung zu starten:

### Voraussetzungen
- Docker und Docker Compose installiert

### Starten der Anwendung

```bash
# Alle Services starten (DB, Mailpit, App)
docker-compose up --build

# Im Hintergrund starten
docker-compose up -d --build
```

Die Anwendung ist dann verfügbar unter:
- **Anwendung:** http://localhost:8080
- **Mailpit Web UI:** http://localhost:8025 (zum Ansehen von E-Mails)

### Services
- **app** - Spring Boot Anwendung (Port 8080)
- **db** - MariaDB Datenbank (Port 3306)
- **mailpit** - E-Mail Test-Server (SMTP: 1025, Web UI: 8025)

### Stoppen

```bash
# Services stoppen
docker-compose down

# Services stoppen und Volumes löschen (⚠️ löscht Daten!)
docker-compose down -v
```

### Wichtige Hinweise für Docker

- **Erster Start:** Beim ersten Start mit leerer Datenbank sollte `SPRING_JPA_HIBERNATE_DDL_AUTO=update` verwendet werden, um Tabellen zu erstellen. Danach auf `validate` wechseln (siehe `docker-compose.yml`).
- **Datenpersistenz:** Datenbank-Daten werden im Volume `db_data` gespeichert und bleiben erhalten, auch wenn Container gestoppt werden.
- **Bilder:** Hochgeladene Bilder werden im Volume `images_data` gespeichert.

---

## 💻 Lokale Entwicklung (ohne Docker)

### Voraussetzungen
- Java 21 oder höher
- Maven 3.6+
- MariaDB Server auf localhost:3306

### Setup

1. **Datenbank erstellen**
   ```sql
   CREATE DATABASE hotelbooking;
   CREATE USER 'hotel'@'localhost' IDENTIFIED BY 'hotelpass';
   GRANT ALL PRIVILEGES ON hotelbooking.* TO 'hotel'@'localhost';
   FLUSH PRIVILEGES;
   ```

2. **Konfiguration anpassen** (optional)
   Die Standard-Einstellungen in `src/main/resources/application.properties` sollten für lokale Entwicklung funktionieren.

3. **Projekt bauen**
   ```bash
   mvn clean install
   ```

4. **Anwendung starten**
   ```bash
   mvn spring-boot:run
   ```

5. **Anwendung öffnen**
   ```
   http://localhost:8080
   ```

### Development Mode
- Vaadin läuft im Development Mode (Hot Reload aktiviert)
- Hibernate verwendet `ddl-auto=create-drop` (⚠️ Daten werden bei jedem Neustart gelöscht!)
- SQL-Queries werden in der Konsole angezeigt (`show-sql=true`)

---

## 🏗️ Production Build

### Mit Maven

```bash
# Production Build mit optimiertem Frontend
mvn clean package -Pproduction

# JAR-Datei wird erstellt in: target/HotelBookingApp-1.0.0.jar
```

### Mit Docker

```bash
# Image bauen
docker build -t hotelbookingapp:latest .

# Container starten (benötigt externe DB und Mail-Server)
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mariadb://db-host:3306/hotelbooking \
  -e SPRING_DATASOURCE_USERNAME=hotel \
  -e SPRING_DATASOURCE_PASSWORD=hotelpass \
  -e SPRING_JPA_HIBERNATE_DDL_AUTO=validate \
  -e VAADIN_PRODUCTION_MODE=true \
  hotelbookingapp:latest
```

---

## ⚙️ Konfiguration

### Environment Variablen

Die Anwendung unterstützt folgende Environment Variablen (überschreiben `application.properties`):

| Variable | Beschreibung | Standard |
|----------|-------------|----------|
| `SPRING_DATASOURCE_URL` | JDBC Connection URL | `jdbc:mariadb://127.0.0.1:3306/hotelbooking` |
| `SPRING_DATASOURCE_USERNAME` | Datenbank Benutzername | `hotel` |
| `SPRING_DATASOURCE_PASSWORD` | Datenbank Passwort | `hotelpass` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Hibernate DDL Modus | `create-drop` (Dev) / `validate` (Prod) |
| `SPRING_JPA_SHOW_SQL` | SQL-Queries in Konsole anzeigen | `true` (Dev) / `false` (Prod) |
| `SPRING_MAIL_HOST` | SMTP Server Host | `localhost` |
| `SPRING_MAIL_PORT` | SMTP Server Port | `1025` |
| `VAADIN_PRODUCTION_MODE` | Vaadin Production Mode | `false` (Dev) / `true` (Prod) |
| `APP_IMAGES_ROOT_DIR` | Verzeichnis für hochgeladene Bilder | `data/images` |

### Datenbank-Schema

**⚠️ WICHTIG für Production:**
- `ddl-auto=create-drop` löscht **ALLE Daten** bei jedem Neustart - nur für Development!
- In Production sollte `validate` oder `none` verwendet werden
- Für erste Initialisierung: `update` verwenden, danach auf `validate` wechseln

---

## 📁 Projektstruktur

```
src/main/java/com/hotel/
├── HotelBookingApplication.java  # Main application class
├── entity/                       # JPA Entities
├── repository/                   # Spring Data Repositories
├── service/                      # Business Logic
└── view/                         # Vaadin UI Views

src/main/resources/
├── application.properties        # Spring Boot Konfiguration
└── data.sql                      # Initialdaten (Demo-User, Räume, etc.)
```

---

## 🔐 Standard-Benutzer (Demo-Daten)

Die Anwendung erstellt beim ersten Start Demo-Benutzer (siehe `data.sql`):

| Username | Passwort | Rolle |
|----------|----------|-------|
| `admin` | `Test123!` | MANAGER |
| `reception1` | `Test123!` | RECEPTIONIST |
| `reception2` | `Test123!` | RECEPTIONIST |
| `clerk` | `Test123!` | GUEST |
| `guestuser` | `Test123!` | GUEST |

**⚠️ WICHTIG:** Diese Passwörter sollten in Production geändert werden!

---

## 📧 E-Mail Konfiguration

### Development (Mailpit)
- SMTP: `localhost:1025`
- Web UI: http://localhost:8025
- Keine Authentifizierung erforderlich

### Production
Für echte E-Mail-Versendung in Production:
1. SMTP-Server konfigurieren (z.B. Gmail, SendGrid, etc.)
2. Environment Variablen setzen:
   ```
   SPRING_MAIL_HOST=smtp.example.com
   SPRING_MAIL_PORT=587
   SPRING_MAIL_SMTP_AUTH=true
   SPRING_MAIL_STARTTLS_ENABLE=true
   ```

---

## 🐛 Troubleshooting

### Datenbank-Verbindungsfehler
- Prüfen, ob MariaDB läuft: `docker-compose ps`
- Prüfen, ob Port 3306 frei ist
- Datenbank-Credentials in `application.properties` oder Environment Variablen prüfen

### Port bereits belegt
- Port 8080 bereits in Verwendung? Anderen Port in `docker-compose.yml` ändern
- Port 3306 bereits in Verwendung? Lokale MariaDB-Instanz stoppen

### Frontend-Build Fehler
- `node_modules` löschen und neu installieren: `rm -rf node_modules && npm install`
- Maven Clean: `mvn clean`

---

## 📝 Notizen

- Die Anwendung verwendet Hibernate für automatische Schema-Erstellung/Updates
- Vaadin Production Mode optimiert das Frontend für bessere Performance
- Erster Start kann länger dauern, da Vaadin Frontend-Ressourcen vorbereitet
- Bilder werden im Dateisystem gespeichert (nicht in der Datenbank)

---

## 📄 Lizenz

Dieses Projekt ist Teil einer Fallstudie.
