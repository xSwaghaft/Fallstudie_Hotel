package com.hotel.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotel.booking.entity.RoomImage;
 

/**
 * Repository for RoomImage entities.
 */
public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {

    /**
     * Finds all images for a category, primary images first.
     */
    @Query("SELECT ri FROM RoomImage ri WHERE ri.category.category_id = :categoryId ORDER BY ri.isPrimary DESC, ri.id ASC")
    List<RoomImage> findByCategoryIdOrderByPrimaryFirst(@Param("categoryId") Long categoryId);

    @Query("SELECT ri FROM RoomImage ri WHERE ri.category.category_id = :categoryId AND ri.isPrimary = true")
    List<RoomImage> findPrimaryByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Finds all images with eagerly loaded category (to avoid LazyInitializationException).
     * Loads ALL images, including those without an assigned category.
     */
    @Query("SELECT DISTINCT ri FROM RoomImage ri LEFT JOIN FETCH ri.category ORDER BY ri.imagePath")
    List<RoomImage> findAllWithCategory();
}

