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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

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
        * <p>
        * This method applies view-specific presentation rules:
        * </p>
        * <ul>
        *   <li>Hide the {@code bookings} column (to avoid large/unhelpful output in the grid).</li>
        *   <li>Hide the technical identifier column ({@code BookingExtra_id}/{@code bookingExtra_id}).</li>
        *   <li>Render the {@code price} column in German currency format (e.g. {@code 12,50€}).</li>
        * </ul>
        * <p>
        * It also sets the grid sizing so that it fills the card container.
        * </p>
     *
     * @param extraGrid the grid to configure
     */
    public void configureExtraGrid(Grid<BookingExtra> extraGrid, GridActionListener<BookingExtra> actionListener) {
        var bookingsCol = extraGrid.getColumnByKey("bookings");
        if (bookingsCol != null) {
            bookingsCol.setVisible(false);
        }

        var idCol = extraGrid.getColumnByKey("bookingExtra_id");
        if (idCol == null) {
            idCol = extraGrid.getColumnByKey("BookingExtra_id");
        }
        if (idCol != null) {
            idCol.setVisible(false);
        }

        var priceCol = extraGrid.getColumnByKey("price");
        if (priceCol != null) {
            priceCol.setRenderer(new TextRenderer<>(extra -> formatEuroDe(extra != null ? extra.getPrice() : null)));
        }

        var existingActionsCol = extraGrid.getColumnByKey("actions");
        if (existingActionsCol != null) {
            extraGrid.removeColumn(existingActionsCol);
        }

        Grid.Column<BookingExtra> actionsCol = null;
        if (actionListener != null) {
            actionsCol = extraGrid.addComponentColumn(extra -> createExtraActions(extra, actionListener))
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setKey("actions");
        }

        var nameCol = extraGrid.getColumnByKey("name");
        var descriptionCol = extraGrid.getColumnByKey("description");
        var perPersonCol = extraGrid.getColumnByKey("perPerson");
        if (perPersonCol != null) {
            perPersonCol.setRenderer(new ComponentRenderer<>(extra -> createPerPersonIndicator(extra)));
        }

        java.util.ArrayList<Grid.Column<BookingExtra>> columnOrder = new java.util.ArrayList<>();
        java.util.HashSet<Grid.Column<BookingExtra>> seen = new java.util.HashSet<>();
        java.util.function.Consumer<Grid.Column<BookingExtra>> add = col -> {
            if (col != null && seen.add(col)) {
                columnOrder.add(col);
            }
        };

        // Desired order: name first, then the commown fields.
        add.accept(nameCol);
        add.accept(descriptionCol);
        add.accept(priceCol);
        add.accept(perPersonCol);
        add.accept(actionsCol);

        // Append remaining columns (including hidden ones) to satisfy Vaadin's ordering requirements.
        for (var col : extraGrid.getColumns()) {
            add.accept(col);
        }

        if (!columnOrder.isEmpty()) {
            extraGrid.setColumnOrder(columnOrder);
        }
        extraGrid.setHeightFull();
        extraGrid.setWidthFull();
        extraGrid.setAllRowsVisible(true);
    }

    private Component createExtraActions(BookingExtra extra, GridActionListener<BookingExtra> actionListener) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button editBtn = new Button("Edit", VaadinIcon.EDIT.create());
        editBtn.addClickListener(e -> actionListener.onEdit(extra));

        Button deleteBtn = new Button("Delete", VaadinIcon.TRASH.create());
        deleteBtn.addClassName("delete-btn");
        deleteBtn.addClickListener(e -> actionListener.onDelete(extra));

        actions.add(editBtn, deleteBtn);
        return actions;
    }

    private Component createPerPersonIndicator(BookingExtra extra) {
        boolean perPerson = extra != null && extra.isPerPerson();
        Icon icon = perPerson ? VaadinIcon.CHECK.create() : VaadinIcon.CLOSE_SMALL.create();
        icon.getElement().setAttribute("title", perPerson ? "Per person" : "Per booking");
        icon.getStyle().set("color", perPerson ? "var(--lumo-success-color)" : "var(--lumo-error-color)");
        return icon;
    }

    /**
     * Formats a numeric value as a German-style Euro amount.
     * <p>
     * Uses a comma as decimal separator and always prints two decimals, followed by {@code €}
     * (no space), e.g. {@code 12,50€}. Returns {@code "N/A"} for {@code null} values.
     * </p>
     *
     * @param value amount to format
     * @return formatted amount or {@code "N/A"}
     */
    private static String formatEuroDe(Double value) {
        if (value == null) {
            return "N/A";
        }

        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.GERMANY);
        DecimalFormat df = new DecimalFormat("0.00", symbols);
        return df.format(value) + "€";
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
