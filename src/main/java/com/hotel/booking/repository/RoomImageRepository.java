package com.hotel.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotel.booking.entity.RoomImage;
 

/**
 * Repository für RoomImage-Entitäten.
 * 
 * @author Viktor Götting
 */
public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {

    /**
        * Findet alle Bilder einer Kategorie, Primary zuerst
     */
    @Query("SELECT ri FROM RoomImage ri WHERE ri.category.category_id = :categoryId ORDER BY ri.isPrimary DESC, ri.id ASC")
    List<RoomImage> findByCategoryIdOrderByPrimaryFirst(@Param("categoryId") Long categoryId);

    @Query("SELECT ri FROM RoomImage ri WHERE ri.category.category_id = :categoryId AND ri.isPrimary = true")
    List<RoomImage> findPrimaryByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Findet alle Bilder mit eager-geladener Kategorie (zur Vermeidung von LazyInitializationException)
     * Lädt ALLE Bilder, auch die ohne zugewiesene Kategorie
     */
    @Query("SELECT DISTINCT ri FROM RoomImage ri LEFT JOIN FETCH ri.category ORDER BY ri.imagePath")
    List<RoomImage> findAllWithCategory();
}

