package com.hotel.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.google.common.base.Optional;
import com.hotel.booking.entity.RoomCategory;
import java.util.List;

/**
 * Repository interface for managing {@link RoomCategory} entities.
 * <p>
 * Provides database access methods for room categories, including
 * custom queries based on category attributes.
 * </p>
 *
 * @author Matthias Lohr
 */
@Repository
public interface RoomCategoryRepository extends JpaRepository<RoomCategory, Long> {

    /**
     * Retrieves a {@link RoomCategory} by its name.
     *
     * @param name the name of the room category
     * @return an {@link Optional} containing the {@link RoomCategory} if found,
     *         or an empty {@link Optional} if no category exists with the given name
     */
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
