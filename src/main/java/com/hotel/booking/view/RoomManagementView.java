package com.hotel.booking.view;

import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.entity.Room;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.RoomStatus;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingExtraService;
import com.hotel.booking.service.RoomCategoryService;
import com.hotel.booking.service.RoomService;
import com.hotel.booking.view.components.RoomForm;
import com.hotel.booking.view.components.RoomCategoryForm;
import com.hotel.booking.view.components.AddExtraForm;
import com.hotel.booking.view.components.CardFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.*;

import java.util.List;

// Room Management View - Verwaltung von Zimmern und Zimmerkategorien
@Route(value = "rooms", layout = MainLayout.class)
@PageTitle("Room Management")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/card-factory.css")
@CssImport("./themes/hotel/views/room-management.css")
public class RoomManagementView extends VerticalLayout implements BeforeEnterObserver {

    // Services (direkt injiziert)
    private final SessionService sessionService;
    private final RoomService roomService;
    private final RoomCategoryService roomCategoryService;
    private final BookingExtraService extraService;

    // UI-Komponenten
    private final Grid<Room> roomGrid = new Grid<>(Room.class, false);
    private final Grid<RoomCategory> categoryGrid = new Grid<>(RoomCategory.class, false);
    private final Grid<BookingExtra> extraGrid = new Grid<>(BookingExtra.class, true);
    
    // Statistiken-Komponenten für Live-Updates
    private Component statsRow;

    public RoomManagementView(SessionService sessionService, 
                               RoomService roomService,
                               RoomCategoryService roomCategoryService,
                                BookingExtraService extraService) {
        this.sessionService = sessionService;
        this.roomService = roomService;
        this.roomCategoryService = roomCategoryService;
        this.extraService = extraService;
        
        setSpacing(true);
        setPadding(true);
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        try {
            configureRoomGrid();
            configureCategoryGrid();
            configureExtraGrid();
            
            statsRow = createStatsRow();
            
            add(
                createHeader(), 
                statsRow, 
                createRoomsCard(),
                createCategoriesCard(),
                createExtraCard()
            );
            
            // Daten aus Datenbank laden
            refreshData();
        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("Fehler beim Laden der View: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void refreshData() {
        try {
            List<Room> rooms = roomService.getAllRooms();
            List<RoomCategory> categories = roomCategoryService.getAllRoomCategories();
            List<BookingExtra> extras = extraService.getAllBookingExtras();
            
            roomGrid.setItems(rooms);
            categoryGrid.setItems(categories);
            extraGrid.setItems(extras);
            
            if (statsRow != null) {
                replace(statsRow, createStatsRow());
                statsRow = getComponentAt(1);
            }
        } catch (Exception e) {
            Notification.show("Error loading data: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    // ==================== HEADER ====================

    // Header mit Titel
    private Component createHeader() {
        H1 title = new H1("Room Management");
        title.addClassName("room-management-header-title");
        
        Paragraph subtitle = new Paragraph("Manage rooms, categories, pricing, and availability");
        subtitle.addClassName("room-management-header-subtitle");
        
        Div headerLeft = new Div(title, subtitle);
        headerLeft.addClassName("room-management-header");
        
        HorizontalLayout header = new HorizontalLayout(headerLeft);
        header.setWidthFull();
        
        return header;
    }

    // Statistiken-Zeile: Total Rooms, Available, Occupied, Cleaning, Categories 
    private Component createStatsRow() {
        try {
            RoomService.RoomStatistics roomStats = roomService.getStatistics();
            RoomCategoryService.CategoryStatistics categoryStats = roomCategoryService.getStatistics();

            return CardFactory.createStatsRow(
                CardFactory.createStatCard("Total Rooms", String.valueOf(roomStats.getTotalRooms()), "#3b82f6"),
                CardFactory.createStatCard("Available", String.valueOf(roomStats.getAvailableRooms()), "#10b981"),
                CardFactory.createStatCard("Occupied", String.valueOf(roomStats.getOccupiedRooms()), "#ef4444"),
                CardFactory.createStatCard("Cleaning", String.valueOf(roomStats.getCleaningRooms()), "#d97706"),
                CardFactory.createStatCard("Categories", String.valueOf(categoryStats.getTotalCategories()), "#8b5cf6")
            );
        } catch (Exception e) {
            e.printStackTrace();
            return new Div(new Paragraph("Error loading statistics"));
        }
    }



    // ==================== ROOMS CARD ====================

    private Component createRoomsCard() {
        return CardFactory.createContentCard(
            "Individual Rooms",
            "Manage individual room availability and pricing",
            "Add Room",
            this::openAddRoomDialog,
            "#10b981",
            roomGrid
        );
    }

    private void configureRoomGrid() {
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
                    // ✅ CHANGED: Get price from category, not from room.price field
                    // This ensures the price updates live when category price changes
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

            roomGrid.addComponentColumn(this::createRoomActions)
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

            roomGrid.setAllRowsVisible(true);
            roomGrid.setWidthFull();
            roomGrid.setHeightFull();
    }

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

    // Erstellt Action-Buttons für Rooms (Edit, Delete)
    private Component createRoomActions(Room room) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        
        Button editBtn = new Button("Edit", VaadinIcon.EDIT.create());
        editBtn.addClickListener(e -> openEditRoomDialog(room));
        
        Button deleteBtn = new Button("Delete", VaadinIcon.TRASH.create());
        deleteBtn.addClassName("delete-btn");
        deleteBtn.addClickListener(e -> deleteRoom(room));
        
        actions.add(editBtn, deleteBtn);
        return actions;
    }

    // ==================== CATEGORIES CARD ==
    private Component createCategoriesCard() {
        return CardFactory.createContentCard(
            "Room Categories",
            "Define room types, pricing, and maximum occupancy",
            "Add Room Category",
            () -> openCategoryDialog(null),
            "#8b5cf6",
            categoryGrid
        );
    }

    private void configureCategoryGrid() {
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
                    // Verwenden von der Service-Methode zum Zählen der Zimmer pro Kategorie
                    // Dies funktioniert auch bei Lazy Loading und ist genau
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

            categoryGrid.addComponentColumn(this::createCategoryActions)
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

            categoryGrid.setAllRowsVisible(true);
            categoryGrid.setWidthFull();
            categoryGrid.setHeightFull();
    }

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

    // Erstellt Action-Buttons für Categories (Edit, Delete)
    private Component createCategoryActions(RoomCategory category) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        
        Button editBtn = new Button("Edit", VaadinIcon.EDIT.create());
        editBtn.addClickListener(e -> openCategoryDialog(category));
        
        Button deleteBtn = new Button("Delete", VaadinIcon.TRASH.create());
        deleteBtn.addClassName("delete-btn");
        deleteBtn.addClickListener(e -> deleteCategory(category));
        
        actions.add(editBtn, deleteBtn);
        return actions;
    }

    // =================== EXTRA CARD =====================
    private Component createExtraCard() {
        return CardFactory.createContentCard(
            "Extras",
            "Manage available Extras",
            "Add Extra",
            () -> openExtraDialog(extraService),
            "#8b5cf6",
            extraGrid
        );
    }

    private void configureExtraGrid() {
        extraGrid.getColumnByKey("bookings").setVisible(false);
        extraGrid.setHeightFull();
        extraGrid.setWidthFull();
        extraGrid.setAllRowsVisible(true);
    }

    // ==================== ROOM DIALOGS ====================

    // Dialog zum Hinzufügen eines neuen Rooms
    private void openAddRoomDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add New Room");
        dialog.setWidth("600px");

        RoomForm form = new RoomForm(null, roomCategoryService);

        Button saveBtn = new Button("Add Room");
        saveBtn.addClassName("primary-button");
        saveBtn.addClickListener(e -> {
            try {
                form.writeBean();
                roomService.saveRoom(form.getRoom());
                refreshData();
                dialog.close();
                Notification.show("Room added successfully!", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelBtn, saveBtn);
        buttonLayout.setSpacing(true);
        dialog.add(form, buttonLayout);
        dialog.open();
    }

    private void openEditRoomDialog(Room room) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Room " + room.getRoomNumber());
        dialog.setWidth("600px");

        RoomForm form = new RoomForm(room, roomCategoryService);

        Button saveBtn = new Button("Update Room");
        saveBtn.addClassName("primary-button");
        saveBtn.addClickListener(e -> {
            try {
                form.writeBean();
                roomService.saveRoom(form.getRoom());
                refreshData();
                dialog.close();
                Notification.show("Room updated successfully!", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelBtn, saveBtn);
        buttonLayout.setSpacing(true);
        dialog.add(form, buttonLayout);
        dialog.open();
    }

    // Löscht einen Room (Dialog)
    private void deleteRoom(Room room) {
        RoomService.RoomDeleteAction action = roomService.getDeletionAction(room.getId());
        
        // Wenn durch Bookings blockiert
        if (action.isBlocked()) {
            Dialog errorDialog = new Dialog();
            errorDialog.setHeaderTitle("Cannot Delete Room");
            Paragraph message = new Paragraph(action.getErrorMessage());
            message.addClassName("dialog-message");
            Button okBtn = new Button("OK", e -> errorDialog.close());
            errorDialog.add(message);
            errorDialog.getFooter().add(okBtn);
            errorDialog.open();
            return;
        }
        
        String message = action.getMessageTemplate().replace("{roomNumber}", room.getRoomNumber());
        
        showConfirmDialog(action.getDialogTitle(), message, action.getButtonLabel(), () -> {
            try {
                roomService.deleteRoom(room.getId());
                refreshData();
                Notification.show(action.getSuccessMessage(), 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }

    // Hilfsmethode für generische Bestätigungsdialoge
    private void showConfirmDialog(String title, String message, String confirmLabel, Runnable onConfirm) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title);
        
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);
        
        // Teile Text in Absätze auf (getrennt durch doppelte Zeilenumbrüche)
        String[] paragraphs = message.split("\n\n");
        for (String para : paragraphs) {
            if (!para.trim().isEmpty()) {
                Paragraph p = new Paragraph(para.trim());
                p.addClassName("dialog-message");
                content.add(p);
            }
        }
        
        Button confirmBtn = new Button(confirmLabel);
        confirmBtn.addClassName("confirm-delete-btn");
        confirmBtn.addClickListener(e -> {
            dialog.close();
            onConfirm.run();
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> dialog.close());
        
        dialog.add(content);
        dialog.getFooter().add(new HorizontalLayout(cancelBtn, confirmBtn));
        dialog.open();
    }

    // ==================== CATEGORY DIALOGS ====================

    private void openCategoryDialog(RoomCategory existing) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existing == null ? "Add New Room Category" : "Edit Room Category");
        dialog.setWidth("600px");

        RoomCategoryForm form = new RoomCategoryForm(existing);

        Button saveBtn = new Button(existing == null ? "Add Category" : "Update Category");
        saveBtn.addClassName("primary-button");
        saveBtn.addClickListener(e -> {
            try {
                form.writeBean();
                roomCategoryService.saveRoomCategory(form.getCategory());
                refreshData();
                dialog.close();
                Notification.show("Category saved successfully!", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelBtn, saveBtn);
        buttonLayout.setSpacing(true);
        dialog.add(form, buttonLayout);
        dialog.open();
    }

    // Löscht eine Category (Dialog)
    private void deleteCategory(RoomCategory category) {
        RoomCategoryService.CategoryDeleteAction action = roomCategoryService.getDeletionAction(category.getCategory_id());
        
        // Wenn durch Invoices blockiert
        if (action.isBlocked()) {
            Dialog errorDialog = new Dialog();
            errorDialog.setHeaderTitle("Cannot Delete Category");
            Paragraph message = new Paragraph(action.getErrorMessage());
            message.addClassName("dialog-message");
            Button okBtn = new Button("OK", e -> errorDialog.close());
            errorDialog.add(message);
            errorDialog.getFooter().add(okBtn);
            errorDialog.open();
            return;
        }
        
        String message = action.getMessageTemplate().replace("{categoryName}", category.getName());
        
        showConfirmDialog(action.getDialogTitle(), message, action.getButtonLabel(), () -> {
            try {
                roomCategoryService.deleteRoomCategory(category.getCategory_id());
                refreshData();
                Notification.show(action.getSuccessMessage(), 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }

    // ============ Extra Dialog =====================

    private void openExtraDialog (BookingExtraService extraService) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add Extra");
        dialog.setWidth("500px");

        AddExtraForm form = new AddExtraForm(extraService, null);
        form.setExtra(new BookingExtra());

        Button saveBtn = new Button("Add Extra");
        saveBtn.addClassName("primary-button");
        saveBtn.addClickListener(e -> {
            if (form.writeBeanIfValid()) {
                refreshData();
                dialog.close();
                Notification.show("Extra added successfully!", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification.show("Please fill all required fields.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveBtn, cancelBtn);
        buttonLayout.setSpacing(true);
        dialog.add(form, buttonLayout);
        dialog.open();
    }

    // ==================== SECURITY ====================

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        try {
            if (sessionService == null || !sessionService.isLoggedIn() || 
                !sessionService.hasAnyRole(UserRole.RECEPTIONIST, UserRole.MANAGER)) {
                event.rerouteTo(LoginView.class);
            }
        } catch (Exception e) {
            event.rerouteTo(LoginView.class);
        }
    }
}