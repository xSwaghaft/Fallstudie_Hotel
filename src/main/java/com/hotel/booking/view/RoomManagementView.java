package com.hotel.booking.view;

import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.entity.Room;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.service.BookingExtraService;
import com.hotel.booking.service.RoomCategoryService;
import com.hotel.booking.service.RoomManagementDeleteActionType;
import com.hotel.booking.service.RoomService;
import com.hotel.booking.view.components.CardFactory;
import com.hotel.booking.view.components.RoomManagementDialog;
import com.hotel.booking.view.components.RoomManagementGrid;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import jakarta.annotation.security.RolesAllowed;

/**
 * Room Management View - Main user interface for managing rooms, room categories, and extras.
 * Only accessible to RECEPTIONIST or MANAGER roles.
 *
 * Delegates business logic to services and UI coordination to helper classes.
 *
 * @author Artur Derr
 */
@Route(value = "rooms", layout = MainLayout.class)
@PageTitle("Room Management")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/card-factory.css")
@CssImport("./themes/hotel/views/room-management.css")
@RolesAllowed({UserRole.RECEPTIONIST_VALUE, UserRole.MANAGER_VALUE})
public class RoomManagementView extends VerticalLayout {

    /** Service for room CRUD operations */
    private final RoomService roomService;
    /** Service for managing room categories */
    private final RoomCategoryService roomCategoryService;
    /** Service for managing booking extras */
    private final BookingExtraService extraService;
    /** Dialog manager for all room management dialogs */
    private final RoomManagementDialog dialogManager;
    /** Grid configuration manager for all grids */
    private final RoomManagementGrid gridManager;

    /** Grid displaying all rooms */
    private final Grid<Room> roomGrid = new Grid<>(Room.class, false);
    /** Grid displaying all room categories */
    private final Grid<RoomCategory> categoryGrid = new Grid<>(RoomCategory.class, false);
    /** Grid displaying all available booking extras */
    private final Grid<BookingExtra> extraGrid = new Grid<>(BookingExtra.class, true);
    /** Component container for statistics row */
    private Component statsRow;

    /**
     * Constructs the RoomManagementView and initializes all services and UI components.
     *
     * @param sessionService service for session management
     * @param roomService service for room operations
     * @param roomCategoryService service for room category operations
     * @param extraService service for booking extras operations
     */
    public RoomManagementView( 
                               RoomService roomService,
                               RoomCategoryService roomCategoryService,
                                BookingExtraService extraService) {
        this.roomService = roomService;
        this.roomCategoryService = roomCategoryService;
        this.extraService = extraService;
        this.dialogManager = new RoomManagementDialog(roomService, roomCategoryService, extraService, this::refreshData);
        this.gridManager = new RoomManagementGrid(roomService, roomCategoryService);
        
        initializeLayout();
        configureGrids();
        initializeData();
    }

    /**
     * Initializes the main layout structure with header, statistics, and content cards.
     * Sets up spacing, padding, and adds the header, statistics row, and three content cards for
     * rooms, room categories, and extras management.
     */
    private void initializeLayout() {
        setSpacing(true);
        setPadding(true);
        setSizeFull();
        add(createHeader(), statsRow = createStatsRow(), 
            addCardClass(createCard("Individual Rooms", "Manage room availability", "Add Room", 
                dialogManager::openAddRoomDialog, roomGrid), "card-rooms"),
            addCardClass(createCard("Room Categories", "Define types and pricing", "Add Room Category", 
                () -> dialogManager.openCategoryDialog(null), categoryGrid), "card-categories"),
            addCardClass(createCard("Extras", "Manage available extras", "Add Extra", 
                dialogManager::openExtraDialog, extraGrid), "card-extras"));
    }

    /**
     * Configures all grids (rooms, categories, extras) with columns and action listeners.
     * Sets up edit and delete action listeners for each grid type.
     */
    private void configureGrids() {
        gridManager.configureRoomGrid(roomGrid, new RoomManagementGrid.GridActionListener<Room>() {
            public void onEdit(Room room) { dialogManager.openEditRoomDialog(room); }
            public void onDelete(Room room) { deleteRoom(room); }
        });
        
        gridManager.configureCategoryGrid(categoryGrid, new RoomManagementGrid.GridActionListener<RoomCategory>() {
            public void onEdit(RoomCategory category) { dialogManager.openCategoryDialog(category); }
            public void onDelete(RoomCategory category) { deleteCategory(category); }
        });

        gridManager.configureExtraGrid(extraGrid, new RoomManagementGrid.GridActionListener<BookingExtra>() {
            public void onEdit(BookingExtra extra) { dialogManager.openEditExtraDialog(extra); }
            public void onDelete(BookingExtra extra) { deleteExtra(extra); }
        });
    }

    /**
     * Initiates the booking extra deletion process with a confirmation dialog.
     *
     * @param extra the extra to be deleted
     */
    private void deleteExtra(BookingExtra extra) {
        if (extra == null) {
            showError("Error: Extra is null");
            return;
        }

        String title = "Delete Extra";
        String message = "Delete extra '" + extra.getName() + "' permanently? This cannot be undone!";
        String buttonLabel = "Delete Permanently";

        dialogManager.showConfirmDialog(title, message, buttonLabel, () -> executeDeleteExtra(extra));
    }

    /**
     * Executes the booking extra deletion and refreshes the data grid.
     * Calls {@link BookingExtraService#deleteBookingExtra(Long)} and updates the grid on success.
     *
     * @param extra the extra to delete
     */
    private void executeDeleteExtra(BookingExtra extra) {
        try {
            extraService.deleteBookingExtra(extra.getBookingExtra_id());
            refreshData();
            showSuccess("Extra operation successful!");
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    /**
     * Loads initial data from services into the grids.
     * Attempts to refresh all data and displays an error if the operation fails.
     */
    private void initializeData() {
        try {
            refreshData();
        } catch (Exception e) {
            showError("Error loading view: " + e.getMessage());
        }
    }

    /**
     * Refreshes all grid data from services and updates statistics.
     * Reloads rooms, categories, and extras from the database and recalculates statistics.
     * Displays an error notification if the operation fails.
     */
    private void refreshData() {
        try {
            roomGrid.setItems(roomService.getAllRooms());
            categoryGrid.setItems(roomCategoryService.getAllRoomCategories());
            extraGrid.setItems(extraService.getAllBookingExtras());
            
            Component newStats = createStatsRow();
            if (statsRow != null) replace(statsRow, newStats);
            statsRow = newStats;
        } catch (Exception e) {
            showError("Error loading data: " + e.getMessage());
        }
    }

    /**
     * Creates the header component with title and subtitle.
     * Displays "Room Management" as the title with a descriptive subtitle.
     *
     * @return the header component as a Div
     */
    private Component createHeader() {
        H1 title = new H1("Room Management");
        title.addClassName("room-management-header-title");
        Paragraph subtitle = new Paragraph("Manage rooms, categories, pricing, and availability");
        subtitle.addClassName("room-management-header-subtitle");
        Div headerDiv = new Div(title, subtitle);
        headerDiv.addClassName("room-management-header");
        return headerDiv;
    }

    /**
     * Creates the statistics row component showing room availability status and counts.
     * Displays cards for total rooms, availability, status distribution, and categories.
     * Uses {@link RoomService#calculateStatistics()} to gather the metrics.
     *
     * @return the statistics row component
     */
    private Component createStatsRow() {
        try {
            RoomService.RoomStatistics stats = roomService.calculateStatistics();
            return CardFactory.createStatsRow(
                addCardClass(CardFactory.createStatCard("Total Rooms", String.valueOf(stats.totalRooms)), "card-stats"),
                addCardClass(CardFactory.createStatCard("Available", String.valueOf(stats.availableRooms)), "card-stats-available"),
                addCardClass(CardFactory.createStatCard("Occupied", String.valueOf(stats.occupiedRooms)), "card-stats-occupied"),
                addCardClass(CardFactory.createStatCard("Cleaning", String.valueOf(stats.cleaningRooms)), "card-stats-cleaning"),
                addCardClass(CardFactory.createStatCard("Renovating", String.valueOf(stats.renovatingRooms)), "card-stats-renovating"),
                addCardClass(CardFactory.createStatCard("Out of Service", String.valueOf(stats.outOfServiceRooms)), "card-stats-out-of-service"),
                addCardClass(CardFactory.createStatCard("Inactive", String.valueOf(stats.inactiveRooms)), "card-stats-inactive"),
                addCardClass(CardFactory.createStatCard("Categories", String.valueOf(stats.totalCategories)), "card-stats-categories"));
        } catch (Exception e) {
            return new Div(new Paragraph("Error loading statistics"));
        }
    }

    /**
     * Creates a content card with title, subtitle, button, and grid.
     * Wraps the card creation logic via {@link CardFactory#createContentCard(String, String, String, Runnable, Grid)}.
     *
     * @param title the card title
     * @param subtitle the card subtitle
     * @param buttonLabel the label for the action button
     * @param onAdd callback executed when the action button is clicked
     * @param grid the grid component to display in the card
     * @return the created content card component
     */
    private Component createCard(String title, String subtitle, String buttonLabel, 
                                  Runnable onAdd, Grid<?> grid) {
        return CardFactory.createContentCard(title, subtitle, buttonLabel, onAdd, grid);
    }

    /**
     * Adds a CSS class to a component and returns it for method chaining.
     * Useful for styling components in a fluent API style.
     *
     * @param card the component to add the class to
     * @param className the CSS class name to add
     * @return the modified component for method chaining
     */
    private Component addCardClass(Component card, String className) {
        card.addClassName(className);
        return card;
    }

    /**
     * Initiates the room deletion process with appropriate confirmation dialog.
     * Checks the deletion action type and prevents deletion of rooms with active bookings.
     * Shows a deactivation or permanent deletion confirmation based on room status.
     *
     * @param room the room to be deleted
     */
    private void deleteRoom(Room room) {
        RoomManagementDeleteActionType actionType = roomService.getDeletionActionType(room.getId());
        
        if (actionType == RoomManagementDeleteActionType.BLOCKED_BY_BOOKINGS) {
            showErrorDialog("Cannot Delete Room", 
                "Cannot delete Room \"" + room.getRoomNumber() + "\": Active bookings reference this room");
            return;
        }
        
        String title = actionType == RoomManagementDeleteActionType.SET_INACTIVE 
            ? "Deactivate Room" : "Delete Room Permanently";
        String message = actionType == RoomManagementDeleteActionType.SET_INACTIVE
            ? "Set Room " + room.getRoomNumber() + " to INACTIVE? It will no longer be available for bookings."
            : "Delete Room " + room.getRoomNumber() + " permanently? This cannot be undone!";
        String buttonLabel = actionType == RoomManagementDeleteActionType.SET_INACTIVE 
            ? "Set to INACTIVE" : "Delete Permanently";
        
        dialogManager.showConfirmDialog(title, message, buttonLabel, () -> executeDelete(room));
    }

    /**
     * Initiates the room category deletion process with appropriate confirmation dialog.
     * Checks the deletion action type and prevents deletion of categories with active bookings.
     * Shows a deactivation or permanent deletion confirmation based on category status.
     *
     * @param category the room category to be deleted
     */
    private void deleteCategory(RoomCategory category) {
        RoomManagementDeleteActionType actionType = roomCategoryService.getDeletionActionType(category.getCategory_id());
        
        if (actionType == RoomManagementDeleteActionType.BLOCKED_BY_BOOKINGS) {
            showErrorDialog("Cannot Delete Category", 
                "Cannot delete category \"" + category.getName() + "\": Active bookings reference this category");
            return;
        }
        
        String title = actionType == RoomManagementDeleteActionType.SET_INACTIVE 
            ? "Deactivate Category" : "Delete Category Permanently";
        String message = actionType == RoomManagementDeleteActionType.SET_INACTIVE
            ? "Set category '" + category.getName() + "' to INACTIVE? It can be reactivated later."
            : "Delete category '" + category.getName() + "' permanently? This cannot be undone!";
        String buttonLabel = actionType == RoomManagementDeleteActionType.SET_INACTIVE 
            ? "Set to INACTIVE" : "Delete Permanently";
        
        dialogManager.showConfirmDialog(title, message, buttonLabel, () -> executeDeleteCategory(category));
    }

    /**
     * Executes the room deletion and refreshes the data grid.
     * Calls {@link RoomService#deleteRoom(Long)} and updates the grid on success.
     * Displays an error notification if the operation fails.
     *
     * @param room the room to delete
     */
    private void executeDelete(Room room) {
        try {
            roomService.deleteRoom(room.getId());
            refreshData();
            showSuccess("Room operation successful!");
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    /**
     * Executes the room category deletion and refreshes the data grid.
     * Calls {@link RoomCategoryService#deleteRoomCategory(Long)} and updates the grid on success.
     * Displays an error notification if the operation fails.
     *
     * @param category the room category to delete
     */
    private void executeDeleteCategory(RoomCategory category) {
        try {
            roomCategoryService.deleteRoomCategory(category.getCategory_id());
            refreshData();
            showSuccess("Category operation successful!");
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    /**
     * Displays a success notification to the user.
     * Shows a green success notification at the bottom left for 3 seconds.
     *
     * @param message the success message to display
     */
    private void showSuccess(String message) {
        Notification.show(message, 3000, Notification.Position.BOTTOM_START)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Displays an error notification to the user.
     * Shows a red error notification in the middle of the screen for 5 seconds.
     *
     * @param message the error message to display
     */
    private void showError(String message) {
        Notification.show(message, 5000, Notification.Position.MIDDLE)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    /**
     * Displays an error dialog to the user.
     * Delegates to the {@link RoomManagementDialog#showErrorDialog(String, String)} method.
     *
     * @param title the dialog title
     * @param message the error message
     */
    private void showErrorDialog(String title, String message) {
        dialogManager.showErrorDialog(title, message);
    }

}