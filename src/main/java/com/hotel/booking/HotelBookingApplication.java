package com.hotel.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Hotel Booking application.
 * <p>
 * This class bootstraps the Spring Boot application and initializes
 * the application context.
 * </p>
 *
 * @author Matthias Lohr
 */
@SpringBootApplication
public class HotelBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelBookingApplication.class, args);
    }
}

