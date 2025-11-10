package com.hotel.booking.repository;

import com.hotel.booking.entity.RoomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
//Matthias Lohr
@Repository
public interface RoomCategoryRepository extends JpaRepository<RoomCategory, Long> {
}