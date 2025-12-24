package com.hotel.booking.repository;

import com.hotel.booking.entity.BookingExtra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link BookingExtra} entities.
 * <p>
 * Provides standard CRUD operations and database access methods
 * for booking extras using Spring Data JPA.
 * </p>
 *
 * @author Matthias Lohr
 */
@Repository
public interface BookingExtraRepository extends JpaRepository<BookingExtra, Long> {
}
