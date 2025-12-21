package com.hotel.booking.service;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.Room;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.RoomStatus;
import com.hotel.booking.repository.BookingRepository;
import com.hotel.booking.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public RoomService(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    // ==================== CRUD-Operationen ====================

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
    public Room saveRoom(Room room) {
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
        Optional<Room> roomOpt = roomRepository.findById(id);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            
            // If the status is not Inactive, set it to Inactive
            if (room.getActive() != null && room.getActive()) {
                room.setActive(false);
                room.setStatus(RoomStatus.INACTIVE);
                roomRepository.save(room);
                return;
            }
            
            // Check for bookings that reference this Room
            List<Booking> relatedBookings = bookingRepository.findByRoom_Id(id);
            if (relatedBookings != null && !relatedBookings.isEmpty()) {
                throw new IllegalStateException(
                    "Cannot delete Room \"" + room.getRoomNumber() + "\": " +
                    relatedBookings.size() + " bookings reference this Room");
            }
            
            // Delete the Room
            roomRepository.deleteById(id);
        }
    }

    /**
     * Determines the appropriate deletion action for a room and returns action details.
     * <p>
     * Returns information about whether the room can be deactivated, permanently deleted,
     * or if deletion is blocked due to related bookings.
     * </p>
     *
     * @param roomId the ID of the room to check
     * @return a RoomDeleteAction with the appropriate action type and details
     * @throws IllegalArgumentException if the room with the specified ID is not found
     */
    public RoomDeleteAction getDeletionAction(Long roomId) {
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            
            // If active, it can be set to inactive
            if (room.getActive() != null && room.getActive()) {
                return new RoomDeleteAction(RoomDeleteActionType.SET_INACTIVE, 
                    null, "Deactivate Room", "Set to INACTIVE",
                    "Set Room {roomNumber} to INACTIVE? It will no longer be available for bookings.",
                    "Room set to INACTIVE!");
            }
            
            // Check for bookings that reference this Room
            List<Booking> relatedBookings = bookingRepository.findByRoom_Id(roomId);
            if (relatedBookings != null && !relatedBookings.isEmpty()) {
                String errorMsg = "Cannot delete Room \"" + room.getRoomNumber() + "\": " +
                    relatedBookings.size() + " bookings reference this Room";
                return new RoomDeleteAction(RoomDeleteActionType.BLOCKED_BY_BOOKINGS, 
                    errorMsg, "Cannot Delete Room", null, null, null);
            }
            
            // If Inactive and no Bookings -> actual deletion possible
            return new RoomDeleteAction(RoomDeleteActionType.PERMANENT_DELETE, 
                null, "Delete Room Permanently", "Delete Permanently",
                "This room is currently INACTIVE. Delete it permanently? This cannot be undone!",
                "Room deleted permanently!");
        }
        
        throw new IllegalArgumentException("Room mit ID " + roomId + " nicht gefunden");
    }

    // ==================== Query-Methoden ====================

    /**
     * Finds all rooms by status.
     *
     * @param status the room status to filter by
     * @return a list of rooms with the specified status
     */
    public List<Room> findByStatus(RoomStatus status) {
        return roomRepository.findByStatus(status);
    }

    /**
     * Finds all rooms by category.
     *
     * @param category the room category to filter by
     * @return a list of rooms in the specified category
     */
    public List<Room> findByCategory(RoomCategory category) {
        return roomRepository.findByCategory(category);
    }

    /**
     * Retrieves all available rooms.
     *
     * @return a list of rooms with AVAILABLE status
     */
    public List<Room> getAvailableRooms() {
        return roomRepository.findByStatus(RoomStatus.AVAILABLE);
    }

    // ==================== Inner Classes ====================

    public enum RoomDeleteActionType {
        SET_INACTIVE,
        PERMANENT_DELETE,
        BLOCKED_BY_BOOKINGS
    }

    public static class RoomDeleteAction {
        private final RoomDeleteActionType type;
        private final String errorMessage;
        private final String dialogTitle;
        private final String buttonLabel;
        private final String messageTemplate;
        private final String successMessage;

        public RoomDeleteAction(RoomDeleteActionType type, String errorMessage, 
                                String dialogTitle, String buttonLabel, 
                                String messageTemplate, String successMessage) {
            this.type = type;
            this.errorMessage = errorMessage;
            this.dialogTitle = dialogTitle;
            this.buttonLabel = buttonLabel;
            this.messageTemplate = messageTemplate;
            this.successMessage = successMessage;
        }

        public RoomDeleteActionType getType() {
            return type;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getDialogTitle() {
            return dialogTitle;
        }

        public String getButtonLabel() {
            return buttonLabel;
        }

        public String getMessageTemplate() {
            return messageTemplate;
        }

        public String getSuccessMessage() {
            return successMessage;
        }

        public boolean isBlocked() {
            return type == RoomDeleteActionType.BLOCKED_BY_BOOKINGS;
        }
    }

    /**
     * Retrieves statistics about all rooms.
     * <p>
     * Returns information about the total number of rooms and their statuses.
     * This method is called by the view to display room statistics.
     * </p>
     *
     * @return a RoomStatistics object containing aggregated room data
     */
    public RoomStatistics getStatistics() {
        List<Room> allRooms = roomRepository.findAll();
        
        long totalRooms = allRooms.size();
        
        long availableRooms = allRooms.stream()
            .filter(room -> RoomStatus.AVAILABLE.equals(room.getStatus()))
            .count();
        
        long occupiedRooms = allRooms.stream()
            .filter(room -> RoomStatus.OCCUPIED.equals(room.getStatus()))
            .count();
        
        long cleaningRooms = allRooms.stream()
            .filter(room -> RoomStatus.CLEANING.equals(room.getStatus()))
            .count();
        
        return new RoomStatistics(totalRooms, availableRooms, occupiedRooms, cleaningRooms);
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
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            room.setStatus(status);
            return roomRepository.save(room);
        }
        throw new IllegalArgumentException("Room with ID " + roomId + " not found");
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
        if (room.getCategory() == null) {
            throw new IllegalArgumentException("Category is required");
        }
        
        if (room.getRoomNumber() == null || room.getRoomNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Room number is required");
        }
        
        if (room.getCategory() != null && room.getCategory().getPricePerNight() == null) {
            throw new IllegalArgumentException("Category price must be set");
        }
        
        if (room.getStatus() == null) {
            throw new IllegalArgumentException("Status is required");
        }
    }

    /**
     * Counts the total number of rooms.
     *
     * @return the total count of Room entities
     */
    public long count() {
        return roomRepository.count();
    }

    /**
     * Checks if a room with the specified ID exists.
     *
     * @param id the ID to check
     * @return {@code true} if a room with the specified ID exists, {@code false} otherwise
     */
    public boolean existsById(Long id) {
        return roomRepository.existsById(id);
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

    // ==================== Inner Class: Statistics DTO ====================

    /**
     * Data Transfer Object for room statistics.
     * <p>
     * This class is used by the view to display room statistics including
     * total rooms, available rooms, occupied rooms, and rooms in cleaning status.
     * </p>
     */
    public static class RoomStatistics {
        private final long totalRooms;
        private final long availableRooms;
        private final long occupiedRooms;
        private final long cleaningRooms;

        public RoomStatistics(long totalRooms, long availableRooms, long occupiedRooms, long cleaningRooms) {
            this.totalRooms = totalRooms;
            this.availableRooms = availableRooms;
            this.occupiedRooms = occupiedRooms;
            this.cleaningRooms = cleaningRooms;
        }

        public long getTotalRooms() {
            return totalRooms;
        }

        public long getAvailableRooms() {
            return availableRooms;
        }

        public long getOccupiedRooms() {
            return occupiedRooms;
        }

        public long getCleaningRooms() {
            return cleaningRooms;
        }

        public double getOccupancyRate() {
            return totalRooms > 0 ? (double) occupiedRooms / totalRooms * 100 : 0;
        }

        @Override
        public String toString() {
            return "RoomStatistics{" +
                    "totalRooms=" + totalRooms +
                    ", availableRooms=" + availableRooms +
                    ", occupiedRooms=" + occupiedRooms +
                    ", cleaningRooms=" + cleaningRooms +
                    ", occupancyRate=" + String.format("%.2f%%", getOccupancyRate()) +
                    '}';
        }
    }
}