package com.hotel.booking.service;

import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.RoomImage;
import com.hotel.booking.entity.Room;
import com.hotel.booking.entity.Booking;
import com.hotel.booking.repository.RoomCategoryRepository;
import com.hotel.booking.repository.RoomImageRepository;
import com.hotel.booking.repository.RoomRepository;
import com.hotel.booking.repository.InvoiceRepository;
import com.hotel.booking.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/* Service-Klasse für RoomCategory-Entität.
 * Enthält Business-Logik und CRUD-Operationen für Zimmerkategorien. */
@Service
@Transactional
public class RoomCategoryService {

    private static final Logger log = LoggerFactory.getLogger(RoomCategoryService.class);

    private final RoomCategoryRepository roomCategoryRepository;
    private final RoomImageRepository roomImageRepository;
    private final RoomRepository roomRepository;
    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public RoomCategoryService(RoomCategoryRepository roomCategoryRepository,
                                RoomImageRepository roomImageRepository,
                                RoomRepository roomRepository,
                                InvoiceRepository invoiceRepository,
                                BookingRepository bookingRepository) {
        this.roomCategoryRepository = roomCategoryRepository;
        this.roomImageRepository = roomImageRepository;
        this.roomRepository = roomRepository;
        this.invoiceRepository = invoiceRepository;
        this.bookingRepository = bookingRepository;
    }

    // ==================== GETTER FOR REPOSITORIES ====================

    public RoomImageRepository getRoomImageRepository() {
        return roomImageRepository;
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

    /* Löscht eine Room Category anhand der ID (Soft Delete mit Invoice-Check) */
    public void deleteRoomCategory(Long id) {
        Optional<RoomCategory> categoryOpt = roomCategoryRepository.findById(id);
        
        if (categoryOpt.isPresent()) {
            RoomCategory category = categoryOpt.get();
            
            // Wenn Status nicht Inactive ist, setze ihn auf Inactive
            if (category.getActive() != null && category.getActive()) {
                category.setActive(false);
                roomCategoryRepository.save(category);
                log.info("RoomCategory {} (ID: {}) set to inactive", category.getName(), id);
                return;
            }
            
            // Prüfe auf Bookings, die diese RoomCategory referenzieren
            List<Booking> relatedBookings = bookingRepository.findByRoomCategoryId(id);
            if (relatedBookings != null && !relatedBookings.isEmpty()) {
                log.warn("Cannot delete category {}: {} bookings reference this category", 
                    category.getName(), relatedBookings.size());
                throw new IllegalStateException(
                    "Cannot delete category \"" + category.getName() + "\": " +
                    relatedBookings.size() + " bookings reference this category");
            }
            
            // Wenn bereits Inactive, prüfe auf Invoices bevor echtes Löschen
            List<Room> affectedRooms = category.getRooms();
            if (affectedRooms != null && !affectedRooms.isEmpty()) {
                for (Room room : affectedRooms) {
                    long invoiceCount = invoiceRepository.findByBookingRoomId(room.getId()).size();
                    if (invoiceCount > 0) {
                        log.warn("Cannot delete category {}: Room {} has {} invoices", 
                            category.getName(), room.getRoomNumber(), invoiceCount);
                        throw new IllegalStateException(
                            "Cannot delete category \"" + category.getName() + "\": " +
                            "Room " + room.getRoomNumber() + " still has " + invoiceCount + " invoices");
                    }
                }
                
                // Entkopple alle Rooms von dieser Category
                for (Room room : affectedRooms) {
                    room.setCategory(null);
                    roomRepository.save(room);
                }
                log.info("Decoupled {} rooms from category {} ({})", affectedRooms.size(), category.getName(), id);
            }
            
            // Lösche die Category
            roomCategoryRepository.delete(category);
            log.info("RoomCategory {} (ID: {}) deleted", category.getName(), id);
        } else {
            throw new IllegalArgumentException("RoomCategory with ID " + id + " not found");
        }
    }

    /* Prüft ob eine Category gelöscht werden kann und gibt die Aktion zurück */
    public CategoryDeleteAction getDeletionAction(Long categoryId) {
        Optional<RoomCategory> categoryOpt = roomCategoryRepository.findById(categoryId);
        
        if (categoryOpt.isPresent()) {
            RoomCategory category = categoryOpt.get();
            
            // Wenn aktiv, dann kann auf Inactive gesetzt werden
            if (category.getActive() != null && category.getActive()) {
                return new CategoryDeleteAction(CategoryDeleteActionType.SET_INACTIVE, 
                    null, "Deactivate Category", "Set to INACTIVE",
                    "Set category '{categoryName}' to INACTIVE? It can be reactivated later.",
                    "Category set to INACTIVE!");
            }
            
            // Prüfe auf Bookings mit dieser RoomCategory
            List<Booking> relatedBookings = bookingRepository.findByRoomCategoryId(categoryId);
            if (relatedBookings != null && !relatedBookings.isEmpty()) {
                String errorMsg = "Cannot delete category \"" + category.getName() + "\": " +
                    relatedBookings.size() + " bookings reference this category";
                return new CategoryDeleteAction(CategoryDeleteActionType.BLOCKED_BY_INVOICES, 
                    errorMsg, "Cannot Delete Category", null, null, null);
            }
            
            // Wenn Inactive, prüfe auf Invoices
            List<Room> affectedRooms = category.getRooms();
            if (affectedRooms != null && !affectedRooms.isEmpty()) {
                for (Room room : affectedRooms) {
                    long invoiceCount = invoiceRepository.findByBookingRoomId(room.getId()).size();
                    if (invoiceCount > 0) {
                        String errorMsg = "Cannot delete category \"" + category.getName() + "\": " +
                            "Room " + room.getRoomNumber() + " still has " + invoiceCount + " invoices";
                        return new CategoryDeleteAction(CategoryDeleteActionType.BLOCKED_BY_INVOICES, 
                            errorMsg, "Cannot Delete Category", null, null, null);
                    }
                }
            }
            
            // Keine Invoices gefunden -> echtes Löschen möglich
            return new CategoryDeleteAction(CategoryDeleteActionType.PERMANENT_DELETE, 
                null, "Delete Category Permanently", "Delete Permanently",
                "This category is currently INACTIVE. Delete it permanently? This cannot be undone!\n\nNote: This will affect all rooms in this category.",
                "Category deleted permanently!");
        }
        
        throw new IllegalArgumentException("RoomCategory mit ID " + categoryId + " nicht gefunden");
    }

    // ==================== Inner Classes ====================

    public enum CategoryDeleteActionType {
        SET_INACTIVE,
        PERMANENT_DELETE,
        BLOCKED_BY_INVOICES
    }

    public static class CategoryDeleteAction {
        private final CategoryDeleteActionType type;
        private final String errorMessage;
        private final String dialogTitle;
        private final String buttonLabel;
        private final String messageTemplate;
        private final String successMessage;

        public CategoryDeleteAction(CategoryDeleteActionType type, String errorMessage, 
                                     String dialogTitle, String buttonLabel, 
                                     String messageTemplate, String successMessage) {
            this.type = type;
            this.errorMessage = errorMessage;
            this.dialogTitle = dialogTitle;
            this.buttonLabel = buttonLabel;
            this.messageTemplate = messageTemplate;
            this.successMessage = successMessage;
        }

        public CategoryDeleteActionType getType() {
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
            return type == CategoryDeleteActionType.BLOCKED_BY_INVOICES;
        }
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
        
        if (category.getPricePerNight() == null || category.getPricePerNight().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price per night must be greater than or equal to 0");
        }
        
        if (category.getMaxOccupancy() == null || category.getMaxOccupancy() < 1) {
            throw new IllegalArgumentException("Max occupancy must be at least 1");
        }
    }

    /* Erstellt eine neue Category mit Validierung */
    public RoomCategory createCategory(String name, String description, BigDecimal pricePerNight, 
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


    //Viktor Götting Gibt alle bilder einer Kategorie zurück

    public List<RoomImage> getAllRoomImages(Room room){
        if (room == null || room.getCategory() == null) {
            return List.of();
        }

        if (room.getCategory().getCategory_id() == null) {
            return List.of();
        }
        
        List<RoomImage> images = roomImageRepository.findByCategoryIdOrderByPrimaryFirst(room.getCategory().getCategory_id());
        return images != null ? images : List.of();
    }
    
    /* Überladene Methode: Gibt alle Bilder einer Kategorie direkt zurück */
    public List<RoomImage> getAllRoomImages(RoomCategory category) {
        if (category == null) {
            return List.of();
        }

        if (category.getCategory_id() == null) {
            return List.of();
        }
        
        List<RoomImage> images = roomImageRepository.findByCategoryIdOrderByPrimaryFirst(category.getCategory_id());
        return images != null ? images : List.of();
    }
}