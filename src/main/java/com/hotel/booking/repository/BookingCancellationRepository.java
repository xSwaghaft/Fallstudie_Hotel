//Ruslan Krause

package com.hotel.booking.repository;

import com.hotel.booking.entity.BookingCancellation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingCancellationRepository extends JpaRepository<BookingCancellation, Long> {

    List<BookingCancellation> findByBookingId(Long bookingId);

    List<BookingCancellation> findByHandledById(Long handledById);

    Optional<BookingCancellation> findTopByBookingIdOrderByCancelledAtDesc(Long bookingId);
}