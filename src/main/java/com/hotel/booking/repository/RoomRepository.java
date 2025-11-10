package com.hotel.booking.repository;

import com.hotel.booking.entity.Room;
import com.hotel.booking.entity.RoomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
//Matthias Lohr
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // Find rooms by availability
    List<Room> findByAvailability(Boolean availability);

    // Find rooms by category
    List<Room> findByCategory(RoomCategory category);

    // Find rooms by price range
    List<Room> findByPriceBetween(Double minPrice, Double maxPrice);
}
