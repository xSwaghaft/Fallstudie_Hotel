package com.hotel.booking.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingStatus;

/**
 * Repository interface for Booking entity operations.
 * 
 * <p>
 * Provides methods to query bookings by various criteria such as booking number,
 * room, guest, dates, and status. Includes specialized queries for availability
 * checks and time period filtering.
 * </p>
 * 
 * @author Viktor Götting
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    // --- Basics ---------------------------------------------------------------

    /**
     * Finds a booking by its unique booking number.
     * Uses EntityGraph to eagerly load guest, room, invoice, and feedback.
     * 
     * @param bookingNumber the booking number
     * @return optional booking
     */
    @EntityGraph(attributePaths = {"guest", "room", "invoice", "feedback"})
    Optional<Booking> findByBookingNumber(String bookingNumber);

    // --- Room-related queries --------------------------------------------------

    /**
     * Finds all bookings for a specific room.
     * 
     * @param roomId the room ID
     * @return list of bookings for the room
     */
    List<Booking> findByRoom_Id(Long roomId);

    // --- RoomCategory-related queries -----------------------------------------
    
    /**
     * Finds all bookings for a specific room category.
     * 
     * @param categoryId the room category ID
     * @return list of bookings for the category
     */
    @Query("SELECT b FROM Booking b WHERE b.roomCategory.category_id = :categoryId")
    List<Booking> findByRoomCategoryId(@Param("categoryId") Long categoryId);

    // Same logic as Exists - useful for "is room available?"
    boolean existsByRoom_IdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
            Long roomId,
            LocalDate endInclusive,
            LocalDate startInclusive);

    // --- Time period queries --------------------------------------------------

    // All bookings created within a date range:
    // (checkIn <= end) AND (checkOut >= start)
    //Matthias Lohr
    List<Booking> findByCreatedAtLessThanEqualAndCreatedAtGreaterThanEqual(LocalDate endInclusive, LocalDate startInclusive);

    // Returns all active (excluding cancelled) bookings in the period.
    List<Booking> findByCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualAndStatusNot(
            LocalDate endInclusive,
            LocalDate startInclusive,
            BookingStatus statusToExclude
    );

    /**
     * Finds all bookings for a guest that have checked out before a specific date
     * and match a specific status. Uses EntityGraph to eagerly load feedback,
     * roomCategory, and room.
     * 
     * @param guestId the guest ID
     * @param beforeDate the date before which check-out occurred
     * @param status the booking status
     * @return list of matching bookings
     */
    @EntityGraph(attributePaths = {"feedback", "roomCategory", "room"})
    @Query("""
            SELECT b FROM Booking b 
            WHERE b.guest.id = :guestId 
              AND b.checkOutDate < :beforeDate 
              AND b.status = :status
            """)
    List<Booking> findByGuest_IdAndCheckOutDateBeforeAndStatus(
            @Param("guestId") Long guestId,
            @Param("beforeDate") LocalDate beforeDate,
            @Param("status") com.hotel.booking.entity.BookingStatus status);
    
    /**
     * Finds all bookings for a specific guest.
     * 
     * @param guestId the guest ID
     * @return list of bookings for the guest
     */
    List<Booking> findByGuest_Id(Long guestId);
}
