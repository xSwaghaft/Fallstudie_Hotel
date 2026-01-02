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
 * Provides methods to query bookings by various criteria.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    // --- Basics ---------------------------------------------------------------

    /**
     * Finds a booking by its unique booking number.
     * Uses EntityGraph to eagerly load guest, room, invoice, and feedback.
     */
    @EntityGraph(attributePaths = {"guest", "room", "invoice", "feedback"})
    Optional<Booking> findByBookingNumber(String bookingNumber);

    // --- Room-related queries --------------------------------------------------

    /**
     * Finds all bookings for a specific room.
     */
    List<Booking> findByRoom_Id(Long roomId);

    // --- RoomCategory-related queries -----------------------------------------
    
    /**
     * Finds all bookings for a specific room category.
     */
    @Query("SELECT b FROM Booking b WHERE b.roomCategory.category_id = :categoryId")
    List<Booking> findByRoomCategoryId(@Param("categoryId") Long categoryId);

    // Same logic as Exists - useful for "is room available?"
    boolean existsByRoom_IdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
            Long roomId,
            LocalDate endInclusive,
            LocalDate startInclusive);

    boolean existsByRoom_IdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualAndStatusNot(
            Long roomId,
            LocalDate endInclusive,
            LocalDate startInclusive,
            BookingStatus statusToExclude);

    // --- Time period queries --------------------------------------------------

     // Alle Buchungen, in einem Zeitraum erstellt wurden:
        // (checkIn <= end) AND (checkOut >= start)
        //Matthias Lohr
    List<Booking> findByCreatedAtLessThanEqualAndCreatedAtGreaterThanEqual(LocalDate endInclusive, LocalDate startInclusive);

   //Liefert alle aktiven (cancelled ausschließen) Buchungen im Zeitraum.
    List<Booking> findByCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualAndStatusNot(
            LocalDate endInclusive,
            LocalDate startInclusive,
            BookingStatus statusToExclude
    );

    /**
     * Finds all bookings for a guest that have checked out before a specific date
     * and match a specific status. Uses EntityGraph to eagerly load feedback,
     * roomCategory, and room.
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
    
    List<Booking> findByGuest_Id(Long guestId);
    
    // Prüfung, aber eine bestehende Buchung beim Update ignorieren
    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
            FROM Booking b
            WHERE b.room.id = :roomId
              AND b.id <> :excludeId
              AND b.checkInDate <= :endInclusive
              AND b.checkOutDate >= :startInclusive
            """)
    boolean overlapsInRoomExcludingBooking(
            @Param("roomId") Long roomId,
            @Param("excludeId") Long excludeBookingId,
            @Param("startInclusive") LocalDate startInclusive,
            @Param("endInclusive") LocalDate endInclusive);
}
