package com.hotel.booking.view.components;

import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.entity.Room;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.RoomStatus;
import com.hotel.booking.service.RoomCategoryService;
import com.hotel.booking.service.RoomService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * Encapsulates all grid configurations for the Room Management View.
 * Manages grids for:
 * - Rooms
 * - Room Categories
 * - Extras
 *
 * Centralizes grid setup and column configuration logic.
 *
 * @author Artur Derr
 */
public class RoomManagementGrid {

    private final RoomService roomService;

    /**
     * Constructs a new RoomManagementGrid with the required services.
     *
     * @param roomService the service for managing room operations
     * @param roomCategoryService the service for managing room categories (kept for backward compatibility)
     */
    public RoomManagementGrid(RoomService roomService, RoomCategoryService roomCategoryService) {
        this.roomService = roomService;
    }

    // ==================== ROOM GRID ====================

    /**
     * Configures the room grid with columns and styling.
     * Sets up columns for room number, floor, category, price, status, and actions.
     *
     * @param roomGrid the grid to configure
     * @param actionListener the listener for handling edit and delete actions on rooms
     */
    public void configureRoomGrid(Grid<Room> roomGrid, GridActionListener<Room> actionListener) {
        roomGrid.removeAllColumns();
        
        roomGrid.addColumn(Room::getRoomNumber)
            .setHeader("Room Number")
            .setAutoWidth(true)
            .setSortable(true);

        roomGrid.addColumn(room -> {
            try {
                Integer floor = room.getFloor();
                return floor != null ? "Floor " + floor : "N/A";
            } catch (Exception e) {
                return "N/A";
            }
        })
            .setHeader("Floor")
            .setAutoWidth(true)
            .setSortable(true);

        roomGrid.addColumn(room -> {
            try {
                RoomCategory cat = room.getCategory();
                return cat != null ? cat.getName() : "N/A";
            } catch (Exception e) {
                return "N/A";
            }
        })
            .setHeader("Category")
            .setAutoWidth(true)
            .setSortable(true);

        roomGrid.addColumn(room -> {
            try {
                RoomCategory cat = room.getCategory();
                var categoryPrice = cat != null ? cat.getPricePerNight() : null;
                return categoryPrice != null ? String.format("%.2f €", categoryPrice.doubleValue()) : "N/A";
            } catch (Exception e) {
                return "N/A";
            }
        })
            .setHeader("Price/Night")
            .setAutoWidth(true)
            .setSortable(true);

        roomGrid.addComponentColumn(this::createAvailabilityBadge)
            .setHeader("Status")
            .setAutoWidth(true);

        roomGrid.addComponentColumn(room -> createRoomActions(room, actionListener))
            .setHeader("Actions")
            .setAutoWidth(true)
            .setFlexGrow(0);

        roomGrid.setAllRowsVisible(true);
        roomGrid.setWidthFull();
        roomGrid.setHeightFull();
    }

    /**
     * Creates an availability badge component for a room's current status.
     * Applies styling based on the room's status (available, occupied, cleaning, etc.).
     *
     * @param room the room entity
     * @return a styled Span component representing the room status
     */
    private Component createAvailabilityBadge(Room room) {
        RoomStatus status = room.getStatus();
        if (status == null) status = RoomStatus.AVAILABLE;
        
        Span badge = new Span(status.toString());
        
        switch(status) {
            case AVAILABLE:
                badge.addClassName("room-status-available");
                break;
            case OCCUPIED:
                badge.addClassName("room-status-occupied");
                break;
            case CLEANING:
                badge.addClassName("room-status-cleaning");
                break;
            case RENOVATING:
                badge.addClassName("room-status-renovating");
                break;
            case OUT_OF_SERVICE:
                badge.addClassName("room-status-out-of-service");
                break;
            default:
                badge.addClassName("room-status-default");
        }
        
        badge.addClassName("room-status-badge");
        return badge;
    }

    /**
     * Creates action buttons (Edit, Delete) for a room.
     *
     * @param room the room entity
     * @param actionListener the listener for handling edit and delete actions
     * @return a horizontal layout containing the action buttons
     */
    private Component createRoomActions(Room room, GridActionListener<Room> actionListener) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        
        Button editBtn = new Button("Edit", VaadinIcon.EDIT.create());
        editBtn.addClickListener(e -> actionListener.onEdit(room));
        
        Button deleteBtn = new Button("Delete", VaadinIcon.TRASH.create());
        deleteBtn.addClassName("delete-btn");
        deleteBtn.addClickListener(e -> actionListener.onDelete(room));
        
        actions.add(editBtn, deleteBtn);
        return actions;
    }

    // ==================== CATEGORY GRID ====================

    /**
     * Configures the category grid with columns and styling.
     * Sets up columns for name, description, price, occupancy, room count, status, and actions.
     *
     * @param categoryGrid the grid to configure
     * @param actionListener the listener for handling edit and delete actions on categories
     */
    public void configureCategoryGrid(Grid<RoomCategory> categoryGrid, GridActionListener<RoomCategory> actionListener) {
        categoryGrid.removeAllColumns();            
        categoryGrid.addColumn(RoomCategory::getName)
            .setHeader("Name")
            .setAutoWidth(true)
            .setSortable(true);

        categoryGrid.addColumn(RoomCategory::getDescription)
            .setHeader("Description")
            .setFlexGrow(1);

        categoryGrid.addColumn(cat -> {
            try {
                var price = cat.getPricePerNight();
                return price != null ? String.format("%.2f €", price.doubleValue()) : "N/A";
            } catch (Exception e) {
                return "N/A";
            }
        })
            .setHeader("Price/Night")
            .setAutoWidth(true)
            .setSortable(true);

        categoryGrid.addColumn(RoomCategory::getMaxOccupancy)
            .setHeader("Max Guests")
            .setAutoWidth(true)
            .setSortable(true);

        categoryGrid.addColumn(cat -> {
            try {
                long roomCount = roomService.countRoomsByCategory(cat);
                return roomCount;
            } catch (Exception e) {
                return 0;
            }
        })
            .setHeader("Total Rooms")
            .setAutoWidth(true)
            .setSortable(true);

        categoryGrid.addComponentColumn(this::createCategoryStatusBadge)
            .setHeader("Status")
            .setAutoWidth(true);

        categoryGrid.addComponentColumn(cat -> createCategoryActions(cat, actionListener))
            .setHeader("Actions")
            .setAutoWidth(true)
            .setFlexGrow(0);

        categoryGrid.setAllRowsVisible(true);
        categoryGrid.setWidthFull();
        categoryGrid.setHeightFull();
    }

    /**
     * Creates a status badge component for a room category.
     * Displays "Active" or "Inactive" status with appropriate styling.
     *
     * @param category the room category entity
     * @return a styled Span component representing the category's active status
     */
    private Component createCategoryStatusBadge(RoomCategory category) {
        Boolean isActive = category.getActive();
        String text = (isActive != null && isActive) ? "Active" : "Inactive";
        
        Span badge = new Span(text);
        badge.addClassName("category-status-badge");
        
        if (isActive != null && isActive) {
            badge.addClassName("category-status-active");
        } else {
            badge.addClassName("category-status-inactive");
        }
        
        return badge;
    }

    /**
     * Creates action buttons (Edit, Delete) for a room category.
     *
     * @param category the room category entity
     * @param actionListener the listener for handling edit and delete actions
     * @return a horizontal layout containing the action buttons
     */
    private Component createCategoryActions(RoomCategory category, GridActionListener<RoomCategory> actionListener) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        
        Button editBtn = new Button("Edit", VaadinIcon.EDIT.create());
        editBtn.addClickListener(e -> actionListener.onEdit(category));
        
        Button deleteBtn = new Button("Delete", VaadinIcon.TRASH.create());
        deleteBtn.addClassName("delete-btn");
        deleteBtn.addClickListener(e -> actionListener.onDelete(category));
        
        actions.add(editBtn, deleteBtn);
        return actions;
    }

    // ==================== EXTRA GRID ====================

    /**
     * Configures the booking extras grid.
     * Hides the bookings column and sets up sizing properties.
     *
     * @param extraGrid the grid to configure
     */
    public void configureExtraGrid(Grid<BookingExtra> extraGrid) {
        extraGrid.getColumnByKey("bookings").setVisible(false);
        extraGrid.setHeightFull();
        extraGrid.setWidthFull();
        extraGrid.setAllRowsVisible(true);
    }

    // ==================== LISTENER INTERFACE ====================

    /**
     * Interface for grid action callbacks.
     * Defines callbacks for edit and delete operations on grid items.
     *
     * @param <T> the type of item in the grid
     */
    public interface GridActionListener<T> {
        /**
         * Invoked when an edit action is triggered for an item.
         *
         * @param item the item to edit
         */
        void onEdit(T item);

        /**
         * Invoked when a delete action is triggered for an item.
         *
         * @param item the item to delete
         */
        void onDelete(T item);
    }
}
