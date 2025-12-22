package com.hotel.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.google.common.base.Optional;
import com.hotel.booking.entity.RoomCategory;

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

}
