package com.hotel.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.RoomImage;
 

/**
 * Repository interface for RoomImage entity operations.
 * 
 * @author Viktor Götting
 */
public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {

    /**
     * Finds all images for a category, sorted by displayOrder in ascending order.
     * 
     * @param category the room category
     * @return list of room images sorted by display order
     */
    List<RoomImage> findByCategoryOrderByDisplayOrderAsc(RoomCategory category);


}

