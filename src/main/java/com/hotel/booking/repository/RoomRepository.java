package com.hotel.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hotel.booking.entity.Room;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.RoomStatus;

/**
 * Repository interface for managing {@link Room} entities.
 * <p>
 * Provides CRUD operations and custom query methods for accessing
 * rooms based on their status and category.
 * </p>
 *
 * @author Matthias Lohr
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Retrieves all rooms with the given status.
     *
     * @param status the status of the rooms
     * @return a list of rooms matching the specified status
     */
    List<Room> findByStatus(RoomStatus status);

    /**
     * Retrieves all rooms belonging to the given category.
     *
     * @param category the room category
     * @return a list of rooms matching the specified category
     */
    List<Room> findByCategory(RoomCategory category);

    /**
     * Counts the number of rooms belonging to the given category.
     *
     * @param category the room category
     * @return the number of rooms in the specified category
     */
    long countByCategory(RoomCategory category);
}
