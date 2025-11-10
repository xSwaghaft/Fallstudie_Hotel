package com.hotel.booking.repository;

import com.hotel.booking.entity.BookingExtra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
//Matthias Lohr
@Repository
public interface BookingExtraRepository extends JpaRepository<BookingExtra, Long> {
}