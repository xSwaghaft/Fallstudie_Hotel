// Ruslan Krause

package com.hotel.booking.repository;

import com.hotel.booking.entity.BookingModification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingModificationRepository extends JpaRepository<BookingModification, Long> {

    List<BookingModification> findByBookingId(Long bookingId);

    List<BookingModification> findByHandledById(Long handledById);

    Optional<BookingModification> findTopByBookingIdOrderByModifiedAtDesc(Long bookingId);
}