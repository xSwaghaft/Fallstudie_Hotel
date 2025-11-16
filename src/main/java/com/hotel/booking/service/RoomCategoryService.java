package com.hotel.booking.service;

import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.repository.RoomCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/* Service-Klasse für RoomCategory-Entität.
 * Enthält Business-Logik und CRUD-Operationen für Zimmerkategorien. */
@Service
@Transactional
public class RoomCategoryService {

    private final RoomCategoryRepository roomCategoryRepository;

    @Autowired
    public RoomCategoryService(RoomCategoryRepository roomCategoryRepository) {
        this.roomCategoryRepository = roomCategoryRepository;
    }

    // ==================== CRUD-Operationen ====================

    /* Gibt alle Room Categories zurück */
    public List<RoomCategory> getAllRoomCategories() {
        return roomCategoryRepository.findAll();
    }

    /* Findet eine Room Category anhand der ID */
    public Optional<RoomCategory> getRoomCategoryById(Long id) {
        return roomCategoryRepository.findById(id);
    }

    /* Speichert eine Room Category (Create oder Update) */
    public RoomCategory saveRoomCategory(RoomCategory roomCategory) {
        return roomCategoryRepository.save(roomCategory);
    }

    /* Löscht eine Room Category anhand der ID */
    public void deleteRoomCategory(Long id) {
        roomCategoryRepository.deleteById(id);
    }

    // ==================== Query-Methoden ====================

    /* Findet alle aktiven Categories */
    public List<RoomCategory> getActiveCategories() {
        return roomCategoryRepository.findAll().stream()
            .filter(cat -> cat.getActive() != null && cat.getActive())
            .toList();
    }

    /* Findet alle inaktiven Categories */
    public List<RoomCategory> getInactiveCategories() {
        return roomCategoryRepository.findAll().stream()
            .filter(cat -> cat.getActive() == null || !cat.getActive())
            .toList();
    }

    // ==================== Business-Logik ====================

    /* Gibt Statistiken über alle Categories zurück
     * Diese Methode wird von der View aufgerufen */
    public CategoryStatistics getStatistics() {
        List<RoomCategory> allCategories = roomCategoryRepository.findAll();
        
        long totalCategories = allCategories.size();
        
        long activeCategories = allCategories.stream()
            .filter(cat -> cat.getActive() != null && cat.getActive())
            .count();
        
        return new CategoryStatistics(totalCategories, activeCategories);
    }

    /* Aktiviert oder deaktiviert eine Category */
    public RoomCategory toggleActive(Long categoryId) {
        Optional<RoomCategory> categoryOpt = roomCategoryRepository.findById(categoryId);
        if (categoryOpt.isPresent()) {
            RoomCategory category = categoryOpt.get();
            category.setActive(!category.getActive());
            return roomCategoryRepository.save(category);
        }
        throw new IllegalArgumentException("Category with ID " + categoryId + " not found");
    }

    /* Validiert eine Category vor dem Speichern */
    public void validateCategory(RoomCategory category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }
        
        if (category.getPricePerNight() == null || category.getPricePerNight() < 0) {
            throw new IllegalArgumentException("Price per night must be greater than or equal to 0");
        }
        
        if (category.getMaxOccupancy() == null || category.getMaxOccupancy() < 1) {
            throw new IllegalArgumentException("Max occupancy must be at least 1");
        }
    }

    /* Erstellt eine neue Category mit Validierung */
    public RoomCategory createCategory(String name, String description, Double pricePerNight, 
                                       Integer maxOccupancy, Boolean active) {
        RoomCategory category = new RoomCategory();
        category.setName(name);
        category.setDescription(description);
        category.setPricePerNight(pricePerNight);
        category.setMaxOccupancy(maxOccupancy);
        category.setActive(active != null ? active : true);
        
        validateCategory(category);
        
        return roomCategoryRepository.save(category);
    }

    /* Zählt die Anzahl aller Categories */
    public long count() {
        return roomCategoryRepository.count();
    }

    /* Prüft ob eine Category mit der ID existiert */
    public boolean existsById(Long id) {
        return roomCategoryRepository.existsById(id);
    }

    // ==================== Inner Class: Statistics DTO ====================

    /* DTO für Category-Statistiken
     * Wird von der View verwendet, um Statistiken anzuzeigen */
    public static class CategoryStatistics {
        private final long totalCategories;
        private final long activeCategories;

        public CategoryStatistics(long totalCategories, long activeCategories) {
            this.totalCategories = totalCategories;
            this.activeCategories = activeCategories;
        }

        public long getTotalCategories() {
            return totalCategories;
        }

        public long getActiveCategories() {
            return activeCategories;
        }

        public long getInactiveCategories() {
            return totalCategories - activeCategories;
        }

        @Override
        public String toString() {
            return "CategoryStatistics{" +
                    "totalCategories=" + totalCategories +
                    ", activeCategories=" + activeCategories +
                    ", inactiveCategories=" + getInactiveCategories() +
                    '}';
        }
    }
}