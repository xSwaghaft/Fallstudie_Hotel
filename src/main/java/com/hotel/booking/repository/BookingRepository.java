package com.hotel.booking.repository;

import com.hotel.booking.entity.Booking;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    // --- Basics ---------------------------------------------------------------

    @EntityGraph(attributePaths = {"guest", "room", "invoice", "feedback"})
    Optional<Booking> findByBookingNumber(String bookingNumber);

    boolean existsByBookingNumber(String bookingNumber);

    void deleteByBookingNumber(String bookingNumber);

        // --- Room-bezogene Abfragen ----------------------------------------------

        List<Booking> findByRoom_Id(Long roomId);

        // Gleiche Logik als Exists – nützlich für “ist Zimmer frei?”
        boolean existsByRoom_IdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
                Long roomId,
                LocalDate endInclusive,
                LocalDate startInclusive);

    // Gleiche Prüfung, aber eine bestehende Buchung beim Update ignorieren
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

    // --- Zeitraum-Abfragen ----------------------------------------------------

        // Alle heute(in einem Zeitraum) aktiven Buchungen:
        // (checkIn <= end) AND (checkOut >= start)
        //Matthias Lohr
        List<Booking> findByCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
                LocalDate today1,
                LocalDate today2);

        List<Booking> findByCheckInDateBetween(LocalDate from, LocalDate to);

    @Query("""
            SELECT COALESCE(SUM(b.totalPrice), 0)
            FROM Booking b
            WHERE b.checkInDate >= :from
              AND b.checkOutDate <= :to
            """)
    BigDecimal revenueBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // --- Sperre für "Jetzt-buchen"-Flow (verhindert Rennbedingungen) ---------
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> lockById(@Param("id") Long id);

    // --- Gast-bezogene Abfragen ----------------------------------------------
    
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
    
    @EntityGraph(attributePaths = {"feedback", "roomCategory", "room", "extras", "invoice"})
    List<Booking> findByGuest_Id(Long guestId);
}
