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

/**
 * Service class for managing RoomCategory entities.
 * <p>
 * Provides CRUD operations and business logic for room categories including:
 * </p>
 * <ul>
 *   <li>Creating, reading, updating, and deleting room categories</li>
 *   <li>Category activation/deactivation (soft delete)</li>
 *   <li>Validation of category data</li>
 *   <li>Retrieval of category statistics</li>
 *   <li>Management of room images associated with categories</li>
 * </ul>
 *
 * @author Artur Derr
 */
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

    /**
     * Retrieves the RoomImageRepository instance.
     *
     * @return the RoomImageRepository
     */
    public RoomImageRepository getRoomImageRepository() {
        return roomImageRepository;
    }

    // ==================== CRUD-Operationen ====================

    /**
     * Retrieves all room categories from the database.
     *
     * @return a list of all RoomCategory entities
     */
    public List<RoomCategory> getAllRoomCategories() {
        return roomCategoryRepository.findAll();
    }

    /**
     * Retrieves a room category by its ID.
     *
     * @param id the ID of the room category to retrieve
     * @return an Optional containing the RoomCategory if found, empty otherwise
     */
    public Optional<RoomCategory> getRoomCategoryById(Long id) {
        return roomCategoryRepository.findById(id);
    }

    /**
     * Saves a room category (create or update operation).
     *
     * @param roomCategory the RoomCategory entity to save
     * @return the saved RoomCategory
     */
    public RoomCategory saveRoomCategory(RoomCategory roomCategory) {
        return roomCategoryRepository.save(roomCategory);
    }

    /**
     * Deletes a room category by ID (soft delete with invoice validation).
     * <p>
     * If the category is active, it will be set to inactive instead of permanent deletion.
     * Permanent deletion only occurs if there are no related bookings or invoices.
     * </p>
     *
     * @param id the ID of the room category to delete
     * @throws IllegalStateException if the category cannot be deleted due to related bookings or invoices
     * @throws IllegalArgumentException if the category with the specified ID is not found
     */
    public void deleteRoomCategory(Long id) {
        Optional<RoomCategory> categoryOpt = roomCategoryRepository.findById(id);
        
        if (categoryOpt.isPresent()) {
            RoomCategory category = categoryOpt.get();
            
            // If status is not inactive, set it to inactive
            if (category.getActive() != null && category.getActive()) {
                category.setActive(false);
                roomCategoryRepository.save(category);
                log.info("RoomCategory {} (ID: {}) set to inactive", category.getName(), id);
                return;
            }
            
            // Check for bookings that reference this RoomCategory
            List<Booking> relatedBookings = bookingRepository.findByRoomCategoryId(id);
            if (relatedBookings != null && !relatedBookings.isEmpty()) {
                log.warn("Cannot delete category {}: {} bookings reference this category", 
                    category.getName(), relatedBookings.size());
                throw new IllegalStateException(
                    "Cannot delete category \"" + category.getName() + "\": " +
                    relatedBookings.size() + " bookings reference this category");
            }
            
            // If already inactive, check for invoices before actual deletion
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
                
                // Decouple all Rooms from this Category
                for (Room room : affectedRooms) {
                    room.setCategory(null);
                    roomRepository.save(room);
                }
                log.info("Decoupled {} rooms from category {} ({})", affectedRooms.size(), category.getName(), id);
            }
            
            // Delete the Category
            roomCategoryRepository.delete(category);
            log.info("RoomCategory {} (ID: {}) deleted", category.getName(), id);
        } else {
            throw new IllegalArgumentException("RoomCategory with ID " + id + " not found");
        }
    }

    /**
     * Determines the appropriate deletion action for a category and returns action details.
     * <p>
     * Returns information about whether the category can be deactivated, permanently deleted,
     * or if deletion is blocked due to related invoices or bookings.
     * </p>
     *
     * @param categoryId the ID of the room category to check
     * @return a CategoryDeleteAction with the appropriate action type and details
     * @throws IllegalArgumentException if the category with the specified ID is not found
     */
    public CategoryDeleteAction getDeletionAction(Long categoryId) {
        Optional<RoomCategory> categoryOpt = roomCategoryRepository.findById(categoryId);
        
        if (categoryOpt.isPresent()) {
            RoomCategory category = categoryOpt.get();
            
            // If active, it can be set to inactive
            if (category.getActive() != null && category.getActive()) {
                return new CategoryDeleteAction(CategoryDeleteActionType.SET_INACTIVE, 
                    null, "Deactivate Category", "Set to INACTIVE",
                    "Set category '{categoryName}' to INACTIVE? It can be reactivated later.",
                    "Category set to INACTIVE!");
            }
            
            // Check for bookings that reference this RoomCategory
            List<Booking> relatedBookings = bookingRepository.findByRoomCategoryId(categoryId);
            if (relatedBookings != null && !relatedBookings.isEmpty()) {
                String errorMsg = "Cannot delete category \"" + category.getName() + "\": " +
                    relatedBookings.size() + " bookings reference this category";
                return new CategoryDeleteAction(CategoryDeleteActionType.BLOCKED_BY_INVOICES, 
                    errorMsg, "Cannot Delete Category", null, null, null);
            }
            
            // If inactive, check for invoices
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
            
            // No invoices found -> actual deletion possible
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

    /**
     * Retrieves all active room categories.
     *
     * @return a list of all active RoomCategory entities
     */
    public List<RoomCategory> getActiveCategories() {
        return roomCategoryRepository.findAll().stream()
            .filter(cat -> cat.getActive() != null && cat.getActive())
            .toList();
    }

    /**
     * Retrieves all inactive room categories.
     *
     * @return a list of all inactive RoomCategory entities
     */
    public List<RoomCategory> getInactiveCategories() {
        return roomCategoryRepository.findAll().stream()
            .filter(cat -> cat.getActive() == null || !cat.getActive())
            .toList();
    }

    /**
     * Retrieves statistics about all room categories.
     * <p>
     * Returns information about the total number of categories and active categories.
     * This method is called by the view to display category statistics.
     * </p>
     *
     * @return a CategoryStatistics object containing aggregated category data
     */
    public CategoryStatistics getStatistics() {
        List<RoomCategory> allCategories = roomCategoryRepository.findAll();
        
        long totalCategories = allCategories.size();
        
        long activeCategories = allCategories.stream()
            .filter(cat -> cat.getActive() != null && cat.getActive())
            .count();
        
        return new CategoryStatistics(totalCategories, activeCategories);
    }

    /**
     * Toggles the active status of a room category.
     *
     * @param categoryId the ID of the room category to toggle
     * @return the updated RoomCategory with the new active status
     * @throws IllegalArgumentException if the category with the specified ID is not found
     */
    public RoomCategory toggleActive(Long categoryId) {
        Optional<RoomCategory> categoryOpt = roomCategoryRepository.findById(categoryId);
        if (categoryOpt.isPresent()) {
            RoomCategory category = categoryOpt.get();
            category.setActive(!category.getActive());
            return roomCategoryRepository.save(category);
        }
        throw new IllegalArgumentException("Category with ID " + categoryId + " not found");
    }

    /**
     * Validates a room category before saving.
     * <p>
     * Checks that the category name is not empty, price per night is non-negative,
     * and max occupancy is at least 1.
     * </p>
     *
     * @param category the RoomCategory to validate
     * @throws IllegalArgumentException if validation fails
     */
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

    /**
     * Creates a new room category with validation.
     *
     * @param name the name of the category
     * @param description the description of the category
     * @param pricePerNight the price per night for rooms in this category
     * @param maxOccupancy the maximum occupancy for rooms in this category
     * @param active whether the category is initially active
     * @return the created and saved RoomCategory
     * @throws IllegalArgumentException if validation fails
     */
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

    /**
     * Counts the total number of room categories.
     *
     * @return the total count of RoomCategory entities
     */
    public long count() {
        return roomCategoryRepository.count();
    }

    /**
     * Checks if a room category with the specified ID exists.
     *
     * @param id the ID to check
     * @return {@code true} if a category with the specified ID exists, {@code false} otherwise
     */
    public boolean existsById(Long id) {
        return roomCategoryRepository.existsById(id);
    }

    // ==================== Inner Class: Statistics DTO ====================

    /**
     * Data Transfer Object for room category statistics.
     * <p>
     * This class is used by the view to display category statistics.
     * </p>
     */
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

    /**
     * Retrieves all room images for a specific room's category.
     *
     * @param room the Room entity whose category images are to be retrieved
     * @return a list of RoomImage entities ordered by primary image first, or an empty list if no images are found
     */
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
    
    /**
     * Retrieves all room images for a specific room category (overloaded method).
     *
     * @param category the RoomCategory whose images are to be retrieved
     * @return a list of RoomImage entities ordered by primary image first, or an empty list if no images are found
     */
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