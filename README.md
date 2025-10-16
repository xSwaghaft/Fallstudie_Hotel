# Fallstudie_Hotel
Software Buchung und Verwaltung von Hotelzimmern

## HotelBookingApp

A Java-based Hotel Booking Application built with Spring Boot, Vaadin, and MariaDB.

### Technologies Used
- **Spring Boot 3.1.5** - Application framework
- **Vaadin 24.2.5** - UI framework
- **Spring Data JPA** - Data persistence
- **MariaDB** - Database
- **Maven** - Build tool

### Project Structure
```
src/main/java/com/hotel/booking/
├── HotelBookingApplication.java  # Main application class
├── entity/
│   └── Room.java                  # Room entity (id, type, price, availability)
├── repository/
│   └── RoomRepository.java        # JPA repository
├── service/
│   └── RoomService.java           # Business logic
└── view/
    └── MainView.java              # Vaadin UI with room list and form
```

### Database Configuration
The application is configured to connect to MariaDB. Default settings in `application.properties`:
- Database: `hotelbooking`
- Host: `localhost:3306`
- Username: `root`
- Password: _(empty)_

### Setup Instructions

1. **Prerequisites**
   - Java 17 or higher
   - Maven 3.6+
   - MariaDB server running on localhost:3306

2. **Database Setup**
   Create the database in MariaDB:
   ```sql
   CREATE DATABASE hotelbooking;
   ```

3. **Configure Database**
   Edit `src/main/resources/application.properties` if needed to match your MariaDB credentials.

4. **Build the Project**
   ```bash
   mvn clean install
   ```

5. **Run the Application**
   ```bash
   mvn spring-boot:run
   ```

6. **Access the Application**
   Open your browser and navigate to:
   ```
   http://localhost:8080
   ```

### Features
- View list of hotel rooms
- Add new rooms with type, price, and availability
- Update existing room information
- Delete rooms from the system
- Real-time data persistence with MariaDB

### Notes
- The application uses Hibernate with `ddl-auto=update` to automatically create/update database schema
- Vaadin is configured in development mode for easier debugging
- First startup may take longer as Vaadin downloads and prepares frontend resources

