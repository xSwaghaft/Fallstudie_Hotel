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

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    // --- Basics ---------------------------------------------------------------

    @EntityGraph(attributePaths = {"guest", "room", "invoice", "feedback"})
    Optional<Booking> findByBookingNumber(String bookingNumber);

    boolean existsByBookingNumber(String bookingNumber);

    void deleteByBookingNumber(String bookingNumber);

    // --- Room-bezogene Abfragen ----------------------------------------------

        List<Booking> findByRoom_Id(Long roomId);

        // --- RoomCategory-bezogene Abfragen ----------------------------------
        
        @Query("SELECT b FROM Booking b WHERE b.roomCategory.category_id = :categoryId")
        List<Booking> findByRoomCategoryId(@Param("categoryId") Long categoryId);

        // Gleiche Logik als Exists – nützlich für "ist Zimmer frei?"
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
}
