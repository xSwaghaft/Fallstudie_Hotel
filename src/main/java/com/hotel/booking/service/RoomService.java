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
            
            // Prüfe ob es Bookings für diesen Room gibt
            List<Booking> bookings = bookingRepository.findByRoom_Id(id);
            
            if (bookings.isEmpty()) {
                // Keine Bookings -> echtes Löschen
                roomRepository.deleteById(id);
            } else {
                // Bookings vorhanden -> setze auf Inactive
                if (!RoomStatus.INACTIVE.equals(room.getStatus())) {
                    room.setStatus(RoomStatus.INACTIVE);
                    room.setActive(false);
                    roomRepository.save(room);
                }
            }
        }
    }

    /* Gibt zurück, ob ein Room gelöscht oder auf Inactive gesetzt werden soll */
    public DeleteAction getDeletionAction(Long roomId) {
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isPresent()) {
            List<Booking> bookings = bookingRepository.findByRoom_Id(roomId);
            
            // Wenn keine Bookings -> echtes Löschen möglich
            if (bookings.isEmpty()) {
                return DeleteAction.PERMANENT_DELETE;
            } else {
                // Bookings vorhanden -> nur auf Inactive setzen
                return DeleteAction.SET_INACTIVE;
            }
        }
        throw new IllegalArgumentException("Room mit ID " + roomId + " nicht gefunden");
    }

    /* Enum für Deletions-Aktion */
    public enum DeleteAction {
        SET_INACTIVE("Deactivate Room", "Set to INACTIVE", 
            "Set Room {roomNumber} to INACTIVE? It will no longer be available for bookings.",
            "Room set to INACTIVE!",
            "This room has existing bookings.\n\n" +
            "Rooms with bookings cannot be deleted permanently to maintain historical records.\n\n" +
            "Please deactivate the room first by setting its status to INACTIVE. " +
            "The room will remain in the system for reference purposes."),
        PERMANENT_DELETE("Delete Room", "Delete", 
            "Delete Room {roomNumber} permanently?",
            "Room deleted successfully!",
            null);

        private final String dialogTitle;
        private final String buttonLabel;
        private final String messageTemplate;
        private final String successMessage;
        private final String explanation;

        DeleteAction(String dialogTitle, String buttonLabel, String messageTemplate, 
                    String successMessage, String explanation) {
            this.dialogTitle = dialogTitle;
            this.buttonLabel = buttonLabel;
            this.messageTemplate = messageTemplate;
            this.successMessage = successMessage;
            this.explanation = explanation;
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

        public String getExplanation() {
            return explanation;
        }
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

    /* Validiert einen Room vor dem Speichern */
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