package com.hotel.booking.repository;

import com.hotel.booking.entity.Room;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
//Matthias Lohr
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // Find rooms by status
    List<Room> findByStatus(RoomStatus status);

    // Find rooms by category
    List<Room> findByCategory(RoomCategory category);

    // Count rooms by category
    long countByCategory(RoomCategory category);
}
