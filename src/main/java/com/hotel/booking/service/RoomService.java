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

/* Service-Klasse für Room-Entität.
 * Enthält Business-Logik und CRUD-Operationen für Zimmer.*/
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

    /* Gibt alle Rooms zurück */
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    /* Findet einen Room anhand der ID */
    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    /* Speichert einen Room (Create oder Update) */
    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }

    /* Löscht einen Room oder setzt ihn auf Inactive */
    public void deleteRoom(Long id) {
        Optional<Room> roomOpt = roomRepository.findById(id);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            
            // Wenn Status nicht Inactive ist, setze ihn auf Inactive
            if (room.getActive() != null && room.getActive()) {
                room.setActive(false);
                room.setStatus(RoomStatus.INACTIVE);
                roomRepository.save(room);
                return;
            }
            
            // Prüfe auf Bookings, die diesen Room referenzieren
            List<Booking> relatedBookings = bookingRepository.findByRoom_Id(id);
            if (relatedBookings != null && !relatedBookings.isEmpty()) {
                throw new IllegalStateException(
                    "Cannot delete Room \"" + room.getRoomNumber() + "\": " +
                    relatedBookings.size() + " bookings reference this Room");
            }
            
            // Lösche den Room
            roomRepository.deleteById(id);
        }
    }

    /* Prüft ob ein Room gelöscht werden kann und gibt die Aktion zurück */
    public RoomDeleteAction getDeletionAction(Long roomId) {
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            
            // Wenn aktiv, dann kann auf Inactive gesetzt werden
            if (room.getActive() != null && room.getActive()) {
                return new RoomDeleteAction(RoomDeleteActionType.SET_INACTIVE, 
                    null, "Deactivate Room", "Set to INACTIVE",
                    "Set Room {roomNumber} to INACTIVE? It will no longer be available for bookings.",
                    "Room set to INACTIVE!");
            }
            
            // Prüfe auf Bookings mit diesem Room
            List<Booking> relatedBookings = bookingRepository.findByRoom_Id(roomId);
            if (relatedBookings != null && !relatedBookings.isEmpty()) {
                String errorMsg = "Cannot delete Room \"" + room.getRoomNumber() + "\": " +
                    relatedBookings.size() + " bookings reference this Room";
                return new RoomDeleteAction(RoomDeleteActionType.BLOCKED_BY_BOOKINGS, 
                    errorMsg, "Cannot Delete Room", null, null, null);
            }
            
            // Wenn Inactive und keine Bookings -> echtes Löschen möglich
            return new RoomDeleteAction(RoomDeleteActionType.PERMANENT_DELETE, 
                null, "Delete Room Permanently", "Delete Permanently",
                "This room is currently INACTIVE. Delete it permanently? This cannot be undone!",
                "Room deleted permanently!");
        }
        
        throw new IllegalArgumentException("Room mit ID " + roomId + " nicht gefunden");
    }

    // ==================== Query-Methoden ====================

    /* Findet Rooms nach Status */
    public List<Room> findByStatus(RoomStatus status) {
        return roomRepository.findByStatus(status);
    }

    /* Findet Rooms nach Kategorie */
    public List<Room> findByCategory(RoomCategory category) {
        return roomRepository.findByCategory(category);
    }

    /* Findet alle verfügbaren Rooms */
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

    // ==================== Business-Logik ====================

    /* Gibt Statistiken über alle Rooms zurück
     * Diese Methode wird von der View aufgerufen */
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

    /* Ändert den Status eines Rooms */
    public Room changeStatus(Long roomId, RoomStatus status) {
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            room.setStatus(status);
            return roomRepository.save(room);
        }
        throw new IllegalArgumentException("Room with ID " + roomId + " not found");
    }

    /* Validiert einen Room vor dem Speichern; Wahrscheinlich unnötig durch binder und Entity validation */
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

    /* Zählt die Anzahl aller Rooms */
    public long count() {
        return roomRepository.count();
    }

    /* Prüft ob ein Room mit der ID existiert */
    public boolean existsById(Long id) {
        return roomRepository.existsById(id);
    }

    /* Zählt die Anzahl der Rooms in einer bestimmten Kategorie */
    public long countRoomsByCategory(RoomCategory category) {
        return roomRepository.countByCategory(category);
    }

    // ==================== Inner Class: Statistics DTO ====================

    /* DTO für Room-Statistiken
     * Wird von der View verwendet, um Statistiken anzuzeigen */
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