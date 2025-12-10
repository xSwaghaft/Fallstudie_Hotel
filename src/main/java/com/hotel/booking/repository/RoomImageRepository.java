package com.hotel.booking.repository;

import com.hotel.booking.entity.RoomImage;
import com.hotel.booking.entity.RoomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository für RoomImage-Entitäten.
 * 
 * @author Viktor Götting
 */
@Repository
public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {

    /**
     * Findet alle Bilder einer Kategorie, sortiert nach displayOrder
     */
    List<RoomImage> findByCategoryOrderByDisplayOrderAsc(RoomCategory category);


}

