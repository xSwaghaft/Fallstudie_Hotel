package com.hotel.booking.view;

import com.hotel.booking.entity.Room;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.RoomCategoryService;
import com.hotel.booking.service.RoomService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;

import java.util.List;

// Room Management View - Verwaltung von Zimmern und Zimmerkategorien
@Route(value = "rooms", layout = MainLayout.class)
@CssImport("./themes/hotel/styles.css")
public class RoomManagementView extends VerticalLayout implements BeforeEnterObserver {

    // Services (direkt injiziert)
    private final SessionService sessionService;
    private final RoomService roomService;
    private final RoomCategoryService roomCategoryService;

    // UI-Komponenten
    private final Grid<Room> roomGrid = new Grid<>(Room.class, false);
    private final Grid<RoomCategory> categoryGrid = new Grid<>(RoomCategory.class, false);
    
    // Statistiken-Komponenten für Live-Updates
    private Component statsRow;

    public RoomManagementView(SessionService sessionService, 
                               RoomService roomService,
                               RoomCategoryService roomCategoryService) {
        this.sessionService = sessionService;
        this.roomService = roomService;
        this.roomCategoryService = roomCategoryService;
        
        setSpacing(true);
        setPadding(true);
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
        setFlexGrow(0, createHeader());
        setFlexGrow(0, createStatsRow());

        try {
            // Grids konfigurieren VOR dem Laden von Daten
            configureRoomGrid();
            configureCategoryGrid();
            
            // Statistiken-Zeile speichern für Updates
            statsRow = createStatsRow();
            
            // Layout aufbauen: Header → Stats → ROOMS (oben) → CATEGORIES (unten)
            add(
                createHeader(), 
                statsRow, 
                createRoomsCard(),      // ← ROOMS OBEN
                createCategoriesCard()  // ← CATEGORIES UNTEN
            );
            
            // Daten aus Datenbank laden
            refreshData();
        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("Fehler beim Laden der View: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    // Lädt alle Daten aus der Datenbank und aktualisiert die Grids + Statistiken
    private void refreshData() {
        // Direkt aus Service holen 
        try {
            List<Room> rooms = roomService.getAllRooms();
            List<RoomCategory> categories = roomCategoryService.getAllRoomCategories();
            
            roomGrid.setItems(rooms);
            categoryGrid.setItems(categories);
            
            // Statistiken aktualisieren
            if (statsRow != null) {
                // Alte Komponente entfernen und neue hinzufügen
                replace(statsRow, createStatsRow());
                statsRow = getComponentAt(1); // Statistiken-Zeile ist an Position 1
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
        title.getStyle().set("margin", "0");
        
        Paragraph subtitle = new Paragraph("Manage rooms, categories, pricing, and availability");
        subtitle.getStyle().set("margin", "0").set("color", "#6b7280");
        
        Div headerLeft = new Div(title, subtitle);
        
        HorizontalLayout header = new HorizontalLayout(headerLeft);
        header.setWidthFull();
        
        return header;
    }

    // ==================== STATISTIKEN ====================

    // Statistiken-Zeile: Total Rooms, Available, Occupied, Categories 
    private Component createStatsRow() {
        try {
            // Service-Methode aufrufen
            RoomService.RoomStatistics roomStats = roomService.getStatistics();
            RoomCategoryService.CategoryStatistics categoryStats = roomCategoryService.getStatistics();

            HorizontalLayout stats = new HorizontalLayout();
            stats.setWidthFull();
            stats.setSpacing(true);
            
            stats.add(
                createStatCard("Total Rooms", String.valueOf(roomStats.getTotalRooms()), "#3b82f6"),
                createStatCard("Available", String.valueOf(roomStats.getAvailableRooms()), "#10b981"),
                createStatCard("Occupied", String.valueOf(roomStats.getOccupiedRooms()), "#ef4444"),
                createStatCard("Maintenance", String.valueOf(roomStats.getMaintenanceRooms()), "#d97706"),
                createStatCard("Categories", String.valueOf(categoryStats.getTotalCategories()), "#8b5cf6")
            );
            
            return stats;
        } catch (Exception e) {
            e.printStackTrace();
            return new Div(new Paragraph("Error loading statistics"));
        }
    }

    private Component createStatCard(String label, String value, String color) {
        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("padding", "1.5rem")
            .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)")
            .set("flex", "1");

        Paragraph labelP = new Paragraph(label);
        labelP.getStyle()
            .set("margin", "0 0 0.5rem 0")
            .set("color", "#6b7280")
            .set("font-size", "0.875rem");

        H2 valueH = new H2(value);
        valueH.getStyle()
            .set("margin", "0")
            .set("color", color)
            .set("font-size", "2rem")
            .set("font-weight", "700");

        card.add(labelP, valueH);
        return card;
    }

    // ==================== ROOMS CARD (OBEN) ====================

    // ROOMS CARD - Wird OBEN angezeigt
    private Component createRoomsCard() {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("padding", "1.5rem")
            .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)")
            .set("flex-grow", "1");
        card.setPadding(true);
        card.setSpacing(true);

        // Header mit Titel und Button
        H3 title = new H3("Individual Rooms");
        title.getStyle().set("margin", "0");

        Paragraph subtitle = new Paragraph("Manage individual room availability and pricing");
        subtitle.getStyle()
            .set("margin", "0.25rem 0 0 0")
            .set("color", "#6b7280")
            .set("font-size", "0.875rem");

        // Button zum Hinzufügen von Rooms
        Button addRoomBtn = new Button("Add Room", VaadinIcon.PLUS.create());
        addRoomBtn.addClassName("primary-button");
        addRoomBtn.getStyle().set("background", "#10b981").set("color", "white");
        addRoomBtn.addClickListener(e -> openAddRoomDialog());

        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);
        
        Div titleBox = new Div(title, subtitle);
        headerRow.add(titleBox, addRoomBtn);

        // Grid wird bereits in Konstruktor konfiguriert
        roomGrid.setWidthFull();
        roomGrid.setHeightFull();
        
        card.add(headerRow, roomGrid);
        card.setFlexGrow(1, roomGrid);
        return card;
    }

    // Konfiguriert das Room Grid mit allen Spalten
    private void configureRoomGrid() {
        try {
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
                    Double categoryPrice = cat != null ? cat.getPricePerNight() : null;
                    return categoryPrice != null ? String.format("%.2f €", categoryPrice) : "N/A";
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Erstellt Availability Badge (Available/Maintenance/Occupied)
    private Component createAvailabilityBadge(Room room) {
        String status = room.getAvailability();
        if (status == null) status = "Available";
        
        Span badge = new Span(status);
        badge.getStyle()
            .set("padding", "0.25rem 0.75rem")
            .set("border-radius", "0.5rem")
            .set("font-size", "0.875rem")
            .set("font-weight", "600")
            .set("text-transform", "capitalize");
        
        switch(status) {
            case "Available":
                badge.getStyle()
                    .set("background", "#d1fae5")
                    .set("color", "#10b981");
                break;
            case "Occupied":
                badge.getStyle()
                    .set("background", "#fee2e2")
                    .set("color", "#ef4444");
                break;
            case "Maintenance":
                badge.getStyle()
                    .set("background", "#fef3c7")
                    .set("color", "#d97706");
                break;
            default:
                badge.getStyle()
                    .set("background", "#f3f4f6")
                    .set("color", "#6b7280");
        }
        
        return badge;
    }

    // Erstellt Action-Buttons für Rooms (Edit, Delete)
    private Component createRoomActions(Room room) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        
        Button editBtn = new Button("Edit", VaadinIcon.EDIT.create());
        editBtn.addClickListener(e -> openEditRoomDialog(room));
        
        Button deleteBtn = new Button("Delete", VaadinIcon.TRASH.create());
        deleteBtn.getStyle().set("color", "#ef4444");
        deleteBtn.addClickListener(e -> deleteRoom(room));
        
        actions.add(editBtn, deleteBtn);
        return actions;
    }

    // ==================== CATEGORIES CARD (UNTEN) ====================

    // CATEGORIES CARD
    private Component createCategoriesCard() {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("padding", "1.5rem")
            .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)")
            .set("flex-grow", "1");
        card.setPadding(true);
        card.setSpacing(true);

        H3 title = new H3("Room Categories");
        title.getStyle().set("margin", "0");

        Paragraph subtitle = new Paragraph("Define room types, pricing, and maximum occupancy");
        subtitle.getStyle()
            .set("margin", "0.25rem 0 0 0")
            .set("color", "#6b7280")
            .set("font-size", "0.875rem");

        // Button zum Hinzufügen von Categories
        Button addCategoryBtn = new Button("Add Room Category", VaadinIcon.PLUS.create());
        addCategoryBtn.addClassName("primary-button");
        addCategoryBtn.getStyle().set("background", "#8b5cf6").set("color", "white");
        addCategoryBtn.addClickListener(e -> openCategoryDialog(null));

        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);
        
        Div titleBox = new Div(title, subtitle);
        headerRow.add(titleBox, addCategoryBtn);

        // Grid wird bereits in Konstruktor konfiguriert
        categoryGrid.setWidthFull();
        categoryGrid.setHeightFull();
        
        card.add(headerRow, categoryGrid);
        card.setFlexGrow(1, categoryGrid);
        return card;
    }

    // Konfiguriert das Category Grid mit allen Spalten
    private void configureCategoryGrid() {
        try {
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
                    Double price = cat.getPricePerNight();
                    return price != null ? String.format("%.2f €", price) : "N/A";
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Erstellt Status Badge für Category (Active/Inactive)
    private Component createCategoryStatusBadge(RoomCategory category) {
        Boolean isActive = category.getActive();
        String text = (isActive != null && isActive) ? "Active" : "Inactive";
        
        Span badge = new Span(text);
        badge.getStyle()
            .set("padding", "0.25rem 0.75rem")
            .set("border-radius", "0.5rem")
            .set("font-size", "0.875rem")
            .set("font-weight", "600");
        
        if (isActive != null && isActive) {
            badge.getStyle()
                .set("background", "#d1fae5")
                .set("color", "#10b981");
        } else {
            badge.getStyle()
                .set("background", "#f3f4f6")
                .set("color", "#6b7280");
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
        deleteBtn.getStyle().set("color", "#ef4444");
        deleteBtn.addClickListener(e -> deleteCategory(category));
        
        actions.add(editBtn, deleteBtn);
        return actions;
    }

    // ==================== ROOM DIALOGS ====================

    // Dialog zum Hinzufügen eines neuen Rooms
    private void openAddRoomDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add New Room");
        dialog.setWidth("600px");

        // Formular-Felder
        TextField roomNumberField = new TextField("Room Number *");
        roomNumberField.setWidthFull();
        roomNumberField.setPlaceholder("e.g., 101, 102, A01");

        NumberField floorField = new NumberField("Floor");
        floorField.setWidthFull();
        floorField.setMin(0);
        floorField.setStep(1);

        Select<RoomCategory> categorySelect = new Select<>();
        categorySelect.setLabel("Room Category *");
        categorySelect.setItems(roomCategoryService.getAllRoomCategories());
        categorySelect.setItemLabelGenerator(RoomCategory::getName);
        categorySelect.setWidthFull();

        // Price wird automatisch von Category übernommen, aber readonly
        NumberField priceField = new NumberField("Price per Night (€)");
        priceField.setWidthFull();
        priceField.setReadOnly(true);
        priceField.setHelperText("Automatically set from category");

        // Availability Status
        Select<String> statusSelect = new Select<>();
        statusSelect.setLabel("Status *");
        statusSelect.setItems("Available", "Maintenance", "Occupied");
        statusSelect.setValue("Available");
        statusSelect.setWidthFull();

        TextArea infoArea = new TextArea("Additional Information");
        infoArea.setWidthFull();
        infoArea.setHeight("100px");
        infoArea.setPlaceholder("Optional notes about this room...");

        // Listener um Price automatisch zu updaten wenn Category sich ändert
        categorySelect.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                priceField.setValue(event.getValue().getPricePerNight());
            }
        });

        FormLayout form = new FormLayout(roomNumberField, floorField, categorySelect, priceField, statusSelect, infoArea);
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        form.setColspan(infoArea, 2);

        // Buttons
        Button saveBtn = new Button("Add Room");
        saveBtn.addClassName("primary-button");
        saveBtn.addClickListener(e -> {
            // Validierung
            if (roomNumberField.isEmpty() || categorySelect.isEmpty() || statusSelect.isEmpty()) {
                Notification.show("Please fill all required fields", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Room erstellen und in Datenbank speichern
            Room newRoom = new Room();
            newRoom.setRoomNumber(roomNumberField.getValue());
            newRoom.setFloor(floorField.getValue() != null ? floorField.getValue().intValue() : null);
            newRoom.setCategory(categorySelect.getValue());
            newRoom.setPrice(categorySelect.getValue().getPricePerNight());
            newRoom.setAvailability(statusSelect.getValue());
            newRoom.setInformation(infoArea.getValue());
            
            try {
                roomService.saveRoom(newRoom);
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

        dialog.add(form);
        dialog.getFooter().add(new HorizontalLayout(cancelBtn, saveBtn));
        dialog.open();
    }

    // Dialog zum Bearbeiten eines existierenden Rooms
    private void openEditRoomDialog(Room room) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Room " + room.getRoomNumber());
        dialog.setWidth("600px");

        // Formular-Felder (vorausgefüllt)
        TextField roomNumberField = new TextField("Room Number *");
        roomNumberField.setValue(room.getRoomNumber());
        roomNumberField.setWidthFull();

        NumberField floorField = new NumberField("Floor");
        floorField.setValue(room.getFloor() != null ? room.getFloor().doubleValue() : 0);
        floorField.setWidthFull();
        floorField.setMin(0);
        floorField.setStep(1);

        Select<RoomCategory> categorySelect = new Select<>();
        categorySelect.setLabel("Room Category *");
        categorySelect.setItems(roomCategoryService.getAllRoomCategories());
        categorySelect.setItemLabelGenerator(RoomCategory::getName);
        categorySelect.setValue(room.getCategory());
        categorySelect.setWidthFull();

        NumberField priceField = new NumberField("Price per Night (€)");
        priceField.setValue(room.getPrice());
        priceField.setWidthFull();
        priceField.setReadOnly(true);
        priceField.setHelperText("Automatically set from category");

        Select<String> statusSelect = new Select<>();
        statusSelect.setLabel("Status *");
        statusSelect.setItems("Available", "Maintenance", "Occupied");
        statusSelect.setValue(room.getAvailability());
        statusSelect.setWidthFull();

        // Listener um Price automatisch zu updaten wenn Category sich ändert
        categorySelect.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                priceField.setValue(event.getValue().getPricePerNight());
            }
        });

        FormLayout form = new FormLayout(roomNumberField, floorField, categorySelect, priceField, statusSelect);
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );

        // Buttons
        Button saveBtn = new Button("Update Room");
        saveBtn.addClassName("primary-button");
        saveBtn.addClickListener(e -> {
            // Validierung
            if (roomNumberField.isEmpty() || categorySelect.isEmpty() || statusSelect.isEmpty()) {
                Notification.show("Please fill all required fields", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Room aktualisieren
            room.setRoomNumber(roomNumberField.getValue());
            room.setFloor(floorField.getValue() != null ? floorField.getValue().intValue() : null);
            room.setCategory(categorySelect.getValue());
            room.setPrice(categorySelect.getValue().getPricePerNight());
            room.setAvailability(statusSelect.getValue());
            
            try {
                roomService.saveRoom(room);
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

        dialog.add(form);
        dialog.getFooter().add(new HorizontalLayout(cancelBtn, saveBtn));
        dialog.open();
    }

    // Löscht einen Room aus der Datenbank
    private void deleteRoom(Room room) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Delete Room");
        
        Paragraph message = new Paragraph("Are you sure you want to delete Room " + room.getRoomNumber() + "?");
        message.getStyle().set("margin", "0");
        
        Button confirmBtn = new Button("Delete");
        confirmBtn.getStyle().set("background", "#ef4444").set("color", "white");
        confirmBtn.addClickListener(e -> {
            try {
                roomService.deleteRoom(room.getId());
                refreshData();
                confirmDialog.close();
                
                Notification.show("Room deleted successfully!", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> confirmDialog.close());
        
        confirmDialog.add(message);
        confirmDialog.getFooter().add(new HorizontalLayout(cancelBtn, confirmBtn));
        confirmDialog.open();
    }

    // ==================== CATEGORY DIALOGS ====================

    // Dialog zum Hinzufügen/Bearbeiten einer Category
    private void openCategoryDialog(RoomCategory existing) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existing == null ? "Add New Room Category" : "Edit Room Category");
        dialog.setWidth("700px");

        // Formular-Felder
        TextField nameField = new TextField("Category Name *");
        nameField.setWidthFull();
        
        TextArea descArea = new TextArea("Description *");
        descArea.setWidthFull();
        descArea.setHeight("100px");
        
        NumberField priceField = new NumberField("Price per Night (€) *");
        priceField.setWidthFull();
        priceField.setMin(0);
        priceField.setStep(0.01);
        
        NumberField maxOccupancyField = new NumberField("Max Occupancy *");
        maxOccupancyField.setWidthFull();
        maxOccupancyField.setMin(1);
        maxOccupancyField.setStep(1);

        Checkbox activeCheck = new Checkbox("Active");
        activeCheck.setValue(true);

        // Wenn existing vorhanden, Felder vorausfüllen
        if (existing != null) {
            nameField.setValue(existing.getName() != null ? existing.getName() : "");
            descArea.setValue(existing.getDescription() != null ? existing.getDescription() : "");
            priceField.setValue(existing.getPricePerNight());
            maxOccupancyField.setValue(existing.getMaxOccupancy().doubleValue());
            activeCheck.setValue(existing.getActive());
        }

        FormLayout form = new FormLayout(nameField, priceField, descArea, maxOccupancyField, activeCheck);
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("600px", 2)
        );
        form.setColspan(descArea, 2);

        // Buttons
        Button saveBtn = new Button(existing == null ? "Add Category" : "Update Category");
        saveBtn.addClassName("primary-button");
        saveBtn.addClickListener(e -> {
            // Validierung
            if (nameField.isEmpty() || descArea.isEmpty() || priceField.isEmpty() || maxOccupancyField.isEmpty()) {
                Notification.show("Please fill all required fields", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                if (existing == null) {
                    // Neue Category erstellen
                    RoomCategory newCategory = new RoomCategory();
                    newCategory.setName(nameField.getValue());
                    newCategory.setDescription(descArea.getValue());
                    newCategory.setPricePerNight(priceField.getValue());
                    newCategory.setMaxOccupancy(maxOccupancyField.getValue().intValue());
                    newCategory.setActive(activeCheck.getValue());
                    
                    roomCategoryService.saveRoomCategory(newCategory);
                } else {
                    // Existierende Category aktualisieren
                    existing.setName(nameField.getValue());
                    existing.setDescription(descArea.getValue());
                    existing.setPricePerNight(priceField.getValue());
                    existing.setMaxOccupancy(maxOccupancyField.getValue().intValue());
                    existing.setActive(activeCheck.getValue());
                    
                    roomCategoryService.saveRoomCategory(existing);
                }
                
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

        dialog.add(form);
        dialog.getFooter().add(new HorizontalLayout(cancelBtn, saveBtn));
        dialog.open();
    }

    // Löscht eine Category aus der Datenbank
    private void deleteCategory(RoomCategory category) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Delete Category");
        
        Paragraph message = new Paragraph("Are you sure you want to delete category '" + category.getName() + "'?");
        message.getStyle().set("margin", "0");
        
        Paragraph warning = new Paragraph("Warning: This will also affect all rooms in this category!");
        warning.getStyle().set("margin", "0.5rem 0 0 0").set("color", "#ef4444").set("font-weight", "600");
        
        Button confirmBtn = new Button("Delete");
        confirmBtn.getStyle().set("background", "#ef4444").set("color", "white");
        confirmBtn.addClickListener(e -> {
            try {
                roomCategoryService.deleteRoomCategory(category.getCategory_id());
                refreshData();
                confirmDialog.close();
                
                Notification.show("Category deleted successfully!", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> confirmDialog.close());
        
        confirmDialog.add(message, warning);
        confirmDialog.getFooter().add(new HorizontalLayout(cancelBtn, confirmBtn));
        confirmDialog.open();
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