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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    // Error messages
    private static final String CATEGORY_NOT_FOUND_MESSAGE = "RoomCategory mit ID %d nicht gefunden";
    private static final String CATEGORY_NAME_REQUIRED_MESSAGE = "Category name is required";
    private static final String PRICE_VALIDATION_MESSAGE = "Price per night must be >= 0";
    private static final String OCCUPANCY_VALIDATION_MESSAGE = "Max occupancy must be at least 1";

    private final RoomCategoryRepository roomCategoryRepository;
    private final RoomImageRepository roomImageRepository;
    private final RoomRepository roomRepository;
    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;

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

    /**
     * Safely retrieves the list of rooms for a category.
     *
     * @param category the RoomCategory
     * @return a list of rooms, or an empty list if category or rooms are null
     */
    private List<Room> getRoomsForCategory(RoomCategory category) {
        return Optional.ofNullable(category)
            .map(RoomCategory::getRooms)
            .orElse(List.of());
    }

    /**
     * Validates that a category can be permanently deleted.
     * Checks for both related bookings and invoices.
     *
     * @param category the RoomCategory to validate
     * @throws IllegalStateException if category has related bookings or invoices
     */
    private void validateCategoryForPermanentDeletion(RoomCategory category) {
        if (hasDeletionBlockers(category)) {
            log.warn("Cannot delete category \"%s\": Has active status, related bookings or invoices", category.getName());
            throw new IllegalStateException("Cannot delete category with active status, related bookings or invoices");
        }
    }

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
        RoomCategory category = findCategoryById(id);

        // If status is not inactive, set it to inactive
        if (isCategoryActive(category)) {
            deactivateCategory(category);
            return;
        }

        // Validate no related bookings or invoices
        validateCategoryForPermanentDeletion(category);

        // Decouple all rooms from this category
        decoupleRoomsFromCategory(category);

        // Delete the Category
        roomCategoryRepository.delete(category);
        log.info("RoomCategory {} (ID: {}) deleted", category.getName(), id);
    }

    /**
     * Decouples all rooms of a category from that category using batch update.
     * This is more efficient than saving each room individually.
     *
     * @param category the RoomCategory to decouple from
     */
    private void decoupleRoomsFromCategory(RoomCategory category) {
        List<Room> affectedRooms = getRoomsForCategory(category);
        if (affectedRooms.isEmpty()) {
            return;
        }
        
        // Batch update: set category to null for all affected rooms
        for (Room room : affectedRooms) {
            room.setCategory(null);
        }
        roomRepository.saveAll(affectedRooms);
        log.info("Decoupled {} rooms from category {} ({})", affectedRooms.size(), category.getName(), category.getCategory_id());
    }

    /**
     * Determines the appropriate deletion action type for a category.
     * <p>
     * Returns information about whether the category can be deactivated, permanently deleted,
     * or if deletion is blocked due to related invoices or bookings.
     * </p>
     *
     * @param categoryId the ID of the room category to check
     * @return the RoomManagementDeleteActionType for this category
     * @throws IllegalArgumentException if the category with the specified ID is not found
     */
    public RoomManagementDeleteActionType getDeletionActionType(Long categoryId) {
        RoomCategory category = findCategoryById(categoryId);

        // If active, it can be set to inactive
        if (isCategoryActive(category)) {
            return RoomManagementDeleteActionType.SET_INACTIVE;
        }

        // Check if category is blocked by bookings or invoices
        if (hasDeletionBlockers(category)) {
            return RoomManagementDeleteActionType.BLOCKED_BY_BOOKINGS;
        }

        // No blockers found -> actual deletion possible
        return RoomManagementDeleteActionType.PERMANENT_DELETE;
    }

    /**
     * Finds the first room with invoices for a category and returns both the room and invoice count.
     * Optimized to avoid redundant database queries by checking each room only once.
     *
     * @param category the RoomCategory to check
     * @return an Optional containing an Entry with the room and its invoice count, or empty if no invoices found
     */
    private Optional<Map.Entry<Room, Long>> findFirstRoomWithInvoiceCount(RoomCategory category) {
        List<Room> affectedRooms = getRoomsForCategory(category);
        for (Room room : affectedRooms) {
            var invoices = invoiceRepository.findByBookingRoomId(room.getId());
            if (!invoices.isEmpty()) {
                return Optional.of(Map.entry(room, (long) invoices.size()));
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves all active room categories.
     * <p>
     * Uses the repository method for database-level filtering efficiency.
     * </p>
     *
     * @return a list of all active RoomCategory entities
     */
    public List<RoomCategory> getActiveCategories() {
        return roomCategoryRepository.findAllActive();
    }

    /**
     * Retrieves all inactive room categories.
     * <p>
     * Uses the repository method for database-level filtering efficiency.
     * </p>
     *
     * @return a list of all inactive RoomCategory entities
     */
    public List<RoomCategory> getInactiveCategories() {
        return roomCategoryRepository.findAllInactive();
    }

    /**
     * Retrieves statistics about all room categories.
     * <p>
     * Returns information about the total number of categories and active categories.
     * This method is called by the view to display category statistics.
     * Uses repository count methods for optimal database performance.
     * </p>
     *
     * @return an array with [totalCount, activeCount, inactiveCount]
     */
    public long[] getStatistics() {
        long total = roomCategoryRepository.count();
        long active = roomCategoryRepository.countActive();
        long inactive = total - active;
        return new long[]{total, active, inactive};
    }

    /**
     * Toggles the active status of a room category.
     *
     * @param categoryId the ID of the room category to toggle
     * @return the updated RoomCategory with the new active status
     * @throws IllegalArgumentException if the category with the specified ID is not found
     */
    public RoomCategory toggleActive(Long categoryId) {
        RoomCategory category = findCategoryById(categoryId);
        category.setActive(!category.getActive());
        return roomCategoryRepository.save(category);
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
            throw new IllegalArgumentException(CATEGORY_NAME_REQUIRED_MESSAGE);
        }

        if (category.getPricePerNight() == null || category.getPricePerNight().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(PRICE_VALIDATION_MESSAGE);
        }

        if (category.getMaxOccupancy() == null || category.getMaxOccupancy() < 1) {
            throw new IllegalArgumentException(OCCUPANCY_VALIDATION_MESSAGE);
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
        category.setActive(Objects.requireNonNullElse(active, true));
        
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

    // ==================== Private Helper Methods ====================

    /**
     * Finds a category by ID or throws an exception if not found.
     *
     * @param id the ID of the category to find
     * @return the RoomCategory entity
     * @throws IllegalArgumentException if the category is not found
     */
    private RoomCategory findCategoryById(Long id) {
        return roomCategoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(String.format(CATEGORY_NOT_FOUND_MESSAGE, id)));
    }

    /**
     * Checks if a category is active.
     *
     * @param category the RoomCategory to check
     * @return {@code true} if the category is active, {@code false} otherwise
     */
    private boolean isCategoryActive(RoomCategory category) {
        return Objects.requireNonNullElse(category.getActive(), false);
    }

    /**
     * Deactivates a category.
     *
     * @param category the RoomCategory to deactivate
     */
    private void deactivateCategory(RoomCategory category) {
        category.setActive(false);
        roomCategoryRepository.save(category);
        log.info("RoomCategory {} (ID: {}) set to inactive", category.getName(), category.getCategory_id());
    }

    /**
     * Retrieves bookings that reference a specific category.
     *
     * @param categoryId the ID of the category
     * @return a list of related bookings
     */
    public List<Booking> getRelatedBookings(Long categoryId) {
        return bookingRepository.findByRoomCategoryId(categoryId);
    }

    /**
     * Retrieves all images for a specific category.
     *
     * @param categoryId the ID of the category
     * @return a list of RoomImage entities ordered by primary image first
     */
    public List<RoomImage> getCategoryImages(Long categoryId) {
        return roomImageRepository.findByCategoryIdOrderByPrimaryFirst(categoryId);
    }

    /**
     * Checks if a category has blockers for deletion (bookings or invoices).
     *
     * @param category the RoomCategory to check
     * @return true if category has related bookings or invoices, false otherwise
     */
    private boolean hasDeletionBlockers(RoomCategory category) {
        List<Booking> relatedBookings = bookingRepository.findByRoomCategoryId(category.getCategory_id());
        return !relatedBookings.isEmpty() || findFirstRoomWithInvoiceCount(category).isPresent();
    }
}