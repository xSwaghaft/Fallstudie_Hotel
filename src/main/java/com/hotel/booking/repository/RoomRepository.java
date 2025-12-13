package com.hotel.booking.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hotel.booking.entity.Room;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.RoomStatus;
//Matthias Lohr
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // Find rooms by status
    List<Room> findByStatus(RoomStatus status);

    // Find rooms by category
    List<Room> findByCategory(RoomCategory category);

    // Count rooms by category
    long countByCategory(RoomCategory category);
    LocalDate findByCategoryIn(RoomCategory category);
}
