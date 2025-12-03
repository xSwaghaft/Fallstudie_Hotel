package com.hotel.booking.service;

import com.hotel.booking.entity.Room;
import com.hotel.booking.entity.RoomCategory;
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

    @Autowired
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
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

    /* Löscht einen Room anhand der ID */
    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    // ==================== Query-Methoden ====================

    /* Findet Rooms nach Verfügbarkeit */
    public List<Room> findByAvailability(String availability) {
        return roomRepository.findByAvailability(availability);
    }

    /* Findet Rooms nach Kategorie */
    public List<Room> findByCategory(RoomCategory category) {
        return roomRepository.findByCategory(category);
    }

    /* Findet Rooms nach Preisbereich */
    public List<Room> findByPriceBetween(Double minPrice, Double maxPrice) {
        return roomRepository.findByPriceBetween(minPrice, maxPrice);
    }

    /* Findet alle verfügbaren Rooms */
    public List<Room> getAvailableRooms() {
        return roomRepository.findAll().stream()
            .filter(room -> "Available".equals(room.getAvailability()))
            .toList();
    }

    // ==================== Business-Logik ====================

    /* Gibt Statistiken über alle Rooms zurück
     * Diese Methode wird von der View aufgerufen */
    public RoomStatistics getStatistics() {
        List<Room> allRooms = roomRepository.findAll();
        
        long totalRooms = allRooms.size();
        
        long availableRooms = allRooms.stream()
            .filter(room -> "Available".equals(room.getAvailability()))
            .count();
        
        long occupiedRooms = allRooms.stream()
            .filter(room -> "Occupied".equals(room.getAvailability()))
            .count();
        
        long maintenanceRooms = allRooms.stream()
            .filter(room -> "Maintenance".equals(room.getAvailability()))
            .count();
        
        return new RoomStatistics(totalRooms, availableRooms, occupiedRooms, maintenanceRooms);
    }

    /* Ändert die Verfügbarkeit eines Rooms */
    public Room changeAvailability(Long roomId, String availability) {
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            room.setAvailability(availability);
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
        
        if (room.getPrice() == null || room.getPrice() < 0) {
            throw new IllegalArgumentException("Price must be greater than or equal to 0");
        }
        
        if (room.getAvailability() == null) {
            throw new IllegalArgumentException("Availability status is required");
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
        private final long maintenanceRooms;

        public RoomStatistics(long totalRooms, long availableRooms, long occupiedRooms, long maintenanceRooms) {
            this.totalRooms = totalRooms;
            this.availableRooms = availableRooms;
            this.occupiedRooms = occupiedRooms;
            this.maintenanceRooms = maintenanceRooms;
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

        public long getMaintenanceRooms() {
            return maintenanceRooms;
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
                    ", maintenanceRooms=" + maintenanceRooms +
                    ", occupancyRate=" + String.format("%.2f%%", getOccupancyRate()) +
                    '}';
        }
    }
}