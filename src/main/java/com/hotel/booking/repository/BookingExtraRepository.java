package com.hotel.booking.repository;

import com.hotel.booking.entity.BookingExtra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
//Matthias Lohr
@Repository
public interface BookingExtraRepository extends JpaRepository<BookingExtra, Long> {

	/**
	 * Removes all booking-to-extra relations for the given extra.
	 * This is required before deleting the extra itself to avoid issues with the join table.
	 *
	 * @param extraId the extra id (room_extras primary key)
	 */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = "DELETE FROM booking_extra WHERE extra_id = :extraId", nativeQuery = true)
	void deleteBookingExtraRelations(@Param("extraId") Long extraId);
}