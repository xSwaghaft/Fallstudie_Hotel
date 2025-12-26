package com.hotel.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.google.common.base.Optional;
import com.hotel.booking.entity.RoomCategory;
import java.util.List;

//Matthias Lohr
@Repository
public interface RoomCategoryRepository extends JpaRepository<RoomCategory, Long> {

    //Search for a room category by its name 
    Optional<RoomCategory> findByName(String name);
    
    /**
     * Finds all active room categories.
     *
     * @return a list of active RoomCategory entities
     */
    @Query("SELECT rc FROM RoomCategory rc WHERE rc.active = true")
    List<RoomCategory> findAllActive();
    
    /**
     * Finds all inactive room categories.
     *
     * @return a list of inactive RoomCategory entities
     */
    @Query("SELECT rc FROM RoomCategory rc WHERE rc.active = false OR rc.active IS NULL")
    List<RoomCategory> findAllInactive();
    
    /**
     * Counts the number of active room categories.
     * Uses a count query for optimal database performance.
     *
     * @return the count of active RoomCategory entities
     */
    @Query("SELECT COUNT(rc) FROM RoomCategory rc WHERE rc.active = true")
    long countActive();
    
}