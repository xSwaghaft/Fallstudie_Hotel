package com.hotel.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.RoomImage;
 

/**
 * Repository für RoomImage-Entitäten.
 * 
 * @author Viktor Götting
 */
public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {

    /**
     * Findet alle Bilder einer Kategorie, sortiert nach displayOrder
     */
    List<RoomImage> findByCategoryOrderByDisplayOrderAsc(RoomCategory category);


}

