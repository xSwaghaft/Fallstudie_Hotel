package com.hotel.booking.service;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.Room;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.RoomStatus;
import com.hotel.booking.repository.BookingRepository;
import com.hotel.booking.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Room entities.
 * <p>
 * Provides CRUD operations and business logic for rooms including:
 * </p>
 * <ul>
 *   <li>Creating, reading, updating, and deleting rooms</li>
 *   <li>Room activation/deactivation (soft delete)</li>
 *   <li>Room status management</li>
 *   <li>Retrieval of room statistics</li>
 *   <li>Filtering rooms by status and category</li>
 * </ul>
 *
 * @author Artur Derr
 */
@Service
@Transactional
public class RoomService {

    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);

    // Error messages
    private static final String ROOM_NOT_FOUND_MESSAGE = "Room with ID %d not found";
    private static final String BOOKING_REFERENCE_MESSAGE = "Cannot delete Room \"%s\": %d bookings reference this Room";
    private static final String CATEGORY_REQUIRED_MESSAGE = "Category is required";
    private static final String ROOM_NUMBER_REQUIRED_MESSAGE = "Room number is required";
    private static final String CATEGORY_PRICE_REQUIRED_MESSAGE = "Category price must be set";
    private static final String STATUS_REQUIRED_MESSAGE = "Status is required";

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final RoomCategoryService roomCategoryService;

    /**
     * Constructs a new RoomService with required dependencies.
     *
     * @param roomRepository the repository for Room entities
     * @param bookingRepository the repository for Booking entities
     * @param roomCategoryService the service for managing room categories
     */
    public RoomService(RoomRepository roomRepository, BookingRepository bookingRepository, RoomCategoryService roomCategoryService) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.roomCategoryService = roomCategoryService;
    }

    /**
     * Retrieves all rooms from the database.
     *
     * @return a list of all Room entities
     */
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    /**
     * Retrieves a room by its ID.
     *
     * @param id the ID of the room to retrieve
     * @return an Optional containing the Room if found, empty otherwise
     */
    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    /**
     * Saves a room (create or update operation).
     *
     * @param room the Room entity to save
     * @return the saved Room
     */
    public Room save(Room room) {
        return roomRepository.save(room);
    }

    /**
     * Deletes a room by ID (soft delete with booking validation).
     * <p>
     * If the room is active, it will be set to inactive instead of permanent deletion.
     * Permanent deletion only occurs if there are no related bookings.
     * </p>
     *
     * @param id the ID of the room to delete
     * @throws IllegalStateException if the room cannot be deleted due to related bookings
     */
    public void deleteRoom(Long id) {
        Room room = findRoomById(id);

        // If the room is active, deactivate it instead of permanent deletion
        if (isRoomActive(room)) {
            deactivateRoom(room);
            return;
        }

        // Check for bookings that reference this Room
        List<Booking> relatedBookings = getRelatedBookings(id);
        if (!relatedBookings.isEmpty()) {
            String errorMsg = String.format(BOOKING_REFERENCE_MESSAGE, room.getRoomNumber(), relatedBookings.size());
            logger.warn(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        // Perform permanent deletion
        roomRepository.deleteById(id);
        logger.info("Room with ID {} permanently deleted", id);
    }

    /**
     * Determines the appropriate deletion action type for a room.
     * <p>
     * Returns information about whether the room can be deactivated, permanently deleted,
     * or if deletion is blocked due to related bookings.
     * </p>
     *
     * @param roomId the ID of the room to check
     * @return the RoomManagementDeleteActionType for this room
     * @throws IllegalArgumentException if the room with the specified ID is not found
     */
    public RoomManagementDeleteActionType getDeletionActionType(Long roomId) {
        Room room = findRoomById(roomId);

        // If active, it can be set to inactive
        if (isRoomActive(room)) {
            return RoomManagementDeleteActionType.SET_INACTIVE;
        }

        // Check for bookings that reference this Room
        if (hasRelatedBookings(roomId)) {
            return RoomManagementDeleteActionType.BLOCKED_BY_BOOKINGS;
        }

        // If Inactive and no Bookings -> actual deletion possible
        return RoomManagementDeleteActionType.PERMANENT_DELETE;
    }

    /**
     * Checks if a room has related bookings.
     *
     * @param roomId the ID of the room
     * @return a list of related bookings, or empty list if none exist
     */
    public List<Booking> getRelatedBookings(Long roomId) {
        return bookingRepository.findByRoom_Id(roomId);
    }

    // ==================== Private Helper Methods ====================

    /**
     * Finds a room by ID or throws an exception if not found.
     *
     * @param id the ID of the room to find
     * @return the Room entity
     * @throws IllegalArgumentException if the room is not found
     */
    private Room findRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format(ROOM_NOT_FOUND_MESSAGE, id)));
    }

    /**
     * Checks if a room is active.
     *
     * @param room the Room to check
     * @return {@code true} if the room is active, {@code false} otherwise
     */
    private boolean isRoomActive(Room room) {
        return room.getActive() != null && room.getActive();
    }

    /**
     * Checks if a room has any related bookings.
     *
     * @param roomId the ID of the room
     * @return {@code true} if there are related bookings, {@code false} otherwise
     */
    private boolean hasRelatedBookings(Long roomId) {
        return !bookingRepository.findByRoom_Id(roomId).isEmpty();
    }

    /**
     * Changes the status of a room.
     *
     * @param roomId the ID of the room whose status is to be changed
     * @param status the new status for the room
     * @return the updated Room entity
     * @throws IllegalArgumentException if the room with the specified ID is not found
     */
    public Room changeStatus(Long roomId, RoomStatus status) {
        Room room = findRoomById(roomId);
        room.setStatus(status);
        logger.info("Room with ID {} status changed to {}", roomId, status);
        return roomRepository.save(room);
    }

    /**
     * Validates a room before saving.
     * <p>
     * Checks that the room has a category, room number, and status.
     * Note: Additional validation is handled by binder and entity annotations.
     * </p>
     *
     * @param room the Room to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateRoom(Room room) {
        validateCategory(room.getCategory());
        validateRoomNumber(room.getRoomNumber());
        validateStatus(room.getStatus());
    }

    /**
     * Validates that a room's category is set and has a price.
     *
     * @param category the category to validate
     * @throws IllegalArgumentException if category is null or has no price
     */
    private void validateCategory(RoomCategory category) {
        if (category == null || category.getPricePerNight() == null) {
            throw new IllegalArgumentException(category == null ? CATEGORY_REQUIRED_MESSAGE : CATEGORY_PRICE_REQUIRED_MESSAGE);
        }
    }

    /**
     * Validates that a room number is set and not empty.
     *
     * @param roomNumber the room number to validate
     * @throws IllegalArgumentException if room number is null or empty
     */
    private void validateRoomNumber(String roomNumber) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            throw new IllegalArgumentException(ROOM_NUMBER_REQUIRED_MESSAGE);
        }
    }

    /**
     * Validates that a room's status is set.
     *
     * @param status the status to validate
     * @throws IllegalArgumentException if status is null
     */
    private void validateStatus(RoomStatus status) {
        if (status == null) {
            throw new IllegalArgumentException(STATUS_REQUIRED_MESSAGE);
        }
    }

    /**
     * Counts the number of rooms in a specific category.
     *
     * @param category the room category to count rooms for
     * @return the number of rooms in the specified category
     */
    public long countRoomsByCategory(RoomCategory category) {
        return roomRepository.countByCategory(category);
    }

    /**
     * Deactivates a room and sets its status to INACTIVE.
     *
     * @param room the Room to deactivate
     */
    private void deactivateRoom(Room room) {
        room.setActive(false);
        room.setStatus(RoomStatus.INACTIVE);
        roomRepository.save(room);
        logger.info("Room with ID {} deactivated", room.getId());
    }

    /**
     * Calculates comprehensive room management statistics.
     *
     * @return RoomStatistics object containing all relevant metrics
     */
    public RoomStatistics calculateStatistics() {
        long totalRooms = roomRepository.count();
        long availableRooms = roomRepository.countByStatus(RoomStatus.AVAILABLE);
        long occupiedRooms = roomRepository.countByStatus(RoomStatus.OCCUPIED);
        long cleaningRooms = roomRepository.countByStatus(RoomStatus.CLEANING);
        long renovatingRooms = roomRepository.countByStatus(RoomStatus.RENOVATING);
        long outOfServiceRooms = roomRepository.countByStatus(RoomStatus.OUT_OF_SERVICE);
        long inactiveRooms = roomRepository.countByStatus(RoomStatus.INACTIVE);
        
        long[] categoryStats = roomCategoryService.getStatistics();
        long totalCategories = categoryStats[0];

        return new RoomStatistics(totalRooms, availableRooms, occupiedRooms, cleaningRooms, renovatingRooms, outOfServiceRooms, inactiveRooms, totalCategories);
    }

    /**
     * Data class holding comprehensive room management statistics.
     * <p>
     * Contains aggregated metrics for room availability, status distribution, and category counts.
     * Instances are created by the {@link RoomService#calculateStatistics()} method.
     * </p>
     */
    public static class RoomStatistics {
        public final long totalRooms;
        public final long availableRooms;
        public final long occupiedRooms;
        public final long cleaningRooms;
        public final long renovatingRooms;
        public final long outOfServiceRooms;
        public final long inactiveRooms;
        public final long totalCategories;

        /**
         * Constructs a new RoomStatistics instance with all metric values.
         *
         * @param totalRooms the total number of rooms
         * @param availableRooms the number of available rooms
         * @param occupiedRooms the number of occupied rooms
         * @param cleaningRooms the number of rooms being cleaned
         * @param renovatingRooms the number of rooms being renovated
         * @param outOfServiceRooms the number of out-of-service rooms
         * @param inactiveRooms the number of inactive rooms
         * @param totalCategories the total number of room categories
         */
        public RoomStatistics(long totalRooms, long availableRooms, long occupiedRooms, long cleaningRooms, long renovatingRooms, long outOfServiceRooms, long inactiveRooms, long totalCategories) {
            this.totalRooms = totalRooms;
            this.availableRooms = availableRooms;
            this.occupiedRooms = occupiedRooms;
            this.cleaningRooms = cleaningRooms;
            this.renovatingRooms = renovatingRooms;
            this.outOfServiceRooms = outOfServiceRooms;
            this.inactiveRooms = inactiveRooms;
            this.totalCategories = totalCategories;
        }
    }
}