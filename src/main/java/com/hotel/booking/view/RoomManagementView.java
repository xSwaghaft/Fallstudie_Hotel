package com.hotel.booking.view;

import com.hotel.booking.security.SessionService;
import com.hotel.booking.security.UserRole;
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
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "rooms", layout = MainLayout.class)
@CssImport("./themes/hotel/styles.css")
public class RoomManagementView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;

    public static class RoomCategory {
        private int id;
        private String name;
        private String description;
        private int pricePerNight;
        private int totalRooms;
        private int maxGuests;
        private List<String> amenities = new ArrayList<>();
        private boolean active;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getPricePerNight() { return pricePerNight; }
        public int getTotalRooms() { return totalRooms; }
        public int getMaxGuests() { return maxGuests; }
        public String getAmenities() { return String.join(", ", amenities); }
        public List<String> getAmenitiesList() { return amenities; }
        public boolean isActive() { return active; }

        public void setId(int id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public void setDescription(String description) { this.description = description; }
        public void setPricePerNight(int pricePerNight) { this.pricePerNight = pricePerNight; }
        public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }
        public void setMaxGuests(int maxGuests) { this.maxGuests = maxGuests; }
        public void setAmenitiesFromString(String s) {
            this.amenities = s == null || s.isBlank() ? new ArrayList<>() :
                    List.of(s.split(",")).stream().map(String::trim).collect(Collectors.toList());
        }
        public void setActive(boolean active) { this.active = active; }
    }

        public static class Room {
        private String roomNumber;
        private RoomCategory category;
        private String status; // "available", "occupied", "maintenance", "cleaning"
        private String notes;
        private LocalDateTime statusChangedAt;

        public Room(String roomNumber, RoomCategory category, String status) {
            this.roomNumber = roomNumber;
            this.category = category;
            setStatus(status); // nutzt Methode, damit statusChangedAt automatisch gesetzt wird
        }

        // --- Getter ---
        public String getRoomNumber() { 
            return roomNumber; 
        }

        public RoomCategory getCategory() { 
            return category; 
        }

        public String getStatus() { 
            return status; 
        }

        public String getNotes() { 
            return notes; 
        }

        public LocalDateTime getStatusChangedAt() { 
            return statusChangedAt; 
        }

        // --- Setter ---
        public void setStatus(String status) { 
            this.status = status; 
            this.statusChangedAt = LocalDateTime.now(); // Zeitpunkt des Statuswechsels speichern
        }

        public void setNotes(String notes) { 
            this.notes = notes; 
        }

        // --- Optional, aber hilfreich ---
        @Override
        public String toString() {
            return "Room{" +
                    "roomNumber='" + roomNumber + '\'' +
                    ", category=" + category +
                    ", status='" + status + '\'' +
                    ", notes='" + notes + '\'' +
                    ", statusChangedAt=" + statusChangedAt +
                    '}';
        }
    }


    private final Grid<RoomCategory> categoryGrid = new Grid<>(RoomCategory.class, false);
    private final Grid<Room> roomGrid = new Grid<>(Room.class, false);
    private final List<RoomCategory> categories = new ArrayList<>();
    private final List<Room> rooms = new ArrayList<>();

    @Autowired
    public RoomManagementView(SessionService sessionService) {
        this.sessionService = sessionService;
        setSpacing(true);
        setPadding(true);
        setSizeFull();

        seedData();

        add(createHeader(), createStatsRow(), createCategoriesCard(), createRoomsCard());
    }

    private void seedData() {
        categories.clear();
        categories.add(makeCategory(1,"Standard Room","Comfortable and cozy room perfect for budget travelers with essential amenities",89,20,2,"WiFi, TV, AC, Room Service",true));
        categories.add(makeCategory(2,"Deluxe Room","Spacious room with premium amenities and beautiful city view",149,15,3,"WiFi, TV, AC, Mini Bar, Room Service, City View",true));
        categories.add(makeCategory(3,"Luxury Suite","Ultimate luxury experience with separate living area and premium services",299,8,4,"WiFi, TV, AC, Mini Bar, Coffee Maker, Room Service, Ocean View, Jacuzzi",true));
        categories.add(makeCategory(4,"Family Suite","Perfect for families with multiple bedrooms and spacious common area",199,5,6,"WiFi, TV, AC, Kitchenette, Room Service, Garden View",false));
        
        // Create individual rooms
        rooms.clear();
        rooms.add(new Room("101", categories.get(0), "available"));
        rooms.add(new Room("102", categories.get(0), "occupied"));
        rooms.add(new Room("103", categories.get(0), "cleaning"));
        rooms.add(new Room("201", categories.get(1), "available"));
        rooms.add(new Room("202", categories.get(1), "occupied"));
        rooms.add(new Room("203", categories.get(1), "maintenance"));
        rooms.add(new Room("301", categories.get(2), "available"));
        rooms.add(new Room("302", categories.get(2), "available"));
    }

    private RoomCategory makeCategory(int id, String name, String desc, int price, int total, int maxGuests, String amenities, boolean active) {
        var r = new RoomCategory();
        r.setId(id); r.setName(name); r.setDescription(desc); r.setPricePerNight(price);
        r.setTotalRooms(total); r.setMaxGuests(maxGuests); r.setAmenitiesFromString(amenities); r.setActive(active);
        return r;
    }

    private Component createHeader() {
        H1 title = new H1("Room Management");
        title.getStyle().set("margin", "0");
        
        Paragraph subtitle = new Paragraph("Manage room categories, pricing, and availability");
        subtitle.getStyle().set("margin", "0");
        
        Div headerLeft = new Div(title, subtitle);
        
        Button addCategory = new Button("Add Room Category", VaadinIcon.PLUS.create());
        addCategory.addClassName("primary-button");
        addCategory.addClickListener(e -> openCategoryDialog(null));
        
        HorizontalLayout header = new HorizontalLayout(headerLeft, addCategory);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        
        return header;
    }

    private Component createStatsRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);

        Div card1 = createStatCard("Total Categories", String.valueOf(categories.size()), null);
        Div card2 = createStatCard("Total Rooms", String.valueOf(rooms.size()), null);
        Div card3 = createStatCard("Active Categories", 
                String.valueOf(categories.stream().filter(RoomCategory::isActive).count()), null);
        
        int avgPrice = categories.isEmpty() ? 0 : (int) Math.round(
            categories.stream().mapToInt(RoomCategory::getPricePerNight).average().orElse(0)
        );
        Div card4 = createStatCard("Avg Price/Night", "€" + avgPrice, "#D4AF37");
        
        row.add(card1, card2, card3, card4);
        row.expand(card1, card2, card3, card4);

        return row;
    }

    private Div createStatCard(String label, String value, String valueColor) {
        Div card = new Div();
        card.addClassName("kpi-card");
        
        Span labelSpan = new Span(label);
        labelSpan.addClassName("kpi-card-title");
        
        H2 valueHeading = new H2(value);
        valueHeading.getStyle().set("margin", "0");
        if (valueColor != null) {
            valueHeading.getStyle().set("color", valueColor);
        }
        
        card.add(labelSpan, valueHeading);
        return card;
    }

    private Component createCategoriesCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        
        H3 title = new H3("Room Categories");
        title.getStyle().set("margin", "0 0 0.5rem 0");
        
        Paragraph subtitle = new Paragraph("All available room types and their configurations");
        subtitle.getStyle().set("margin", "0 0 1rem 0");

        categoryGrid.addColumn(RoomCategory::getName)
            .setHeader("Name")
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        categoryGrid.addColumn(RoomCategory::getDescription)
            .setHeader("Description")
            .setFlexGrow(2);
        
        categoryGrid.addColumn(rc -> "€" + rc.getPricePerNight())
            .setHeader("Price/Night")
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        categoryGrid.addColumn(RoomCategory::getTotalRooms)
            .setHeader("Total Rooms")
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        categoryGrid.addColumn(RoomCategory::getMaxGuests)
            .setHeader("Max Guests")
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        categoryGrid.addComponentColumn(this::createAmenitiesBadges)
            .setHeader("Amenities")
            .setFlexGrow(1);
        
        categoryGrid.addComponentColumn(this::createStatusToggle)
            .setHeader("Status")
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        categoryGrid.addComponentColumn(this::createCategoryActions)
            .setHeader("Actions")
            .setAutoWidth(true)
            .setFlexGrow(0);

        categoryGrid.setItems(categories);
        categoryGrid.setAllRowsVisible(true);
        categoryGrid.setWidthFull();

        card.add(title, subtitle, categoryGrid);
        return card;
    }

    private Component createAmenitiesBadges(RoomCategory category) {
        HorizontalLayout badges = new HorizontalLayout();
        badges.setSpacing(true);
        badges.getStyle().set("flex-wrap", "wrap").set("gap", "0.5rem");
        
        for (String amenity : category.getAmenitiesList()) {
            Span badge = new Span(amenity);
            badge.getStyle()
                .set("background", "#f3f4f6")
                .set("padding", "0.25rem 0.5rem")
                .set("border-radius", "0.375rem")
                .set("font-size", "0.75rem")
                .set("font-weight", "500");
            
            // Zeige nur erste 3 Amenities
            if (badges.getComponentCount() < 3) {
                badges.add(badge);
            }
        }
        
        if (category.getAmenitiesList().size() > 3) {
            Span more = new Span("+" + (category.getAmenitiesList().size() - 3));
            more.getStyle()
                .set("color", "var(--color-text-secondary)")
                .set("font-size", "0.75rem");
            badges.add(more);
        }
        
        return badges;
    }

    private Component createStatusToggle(RoomCategory category) {
        Checkbox toggle = new Checkbox();
        toggle.setValue(category.isActive());
        toggle.setLabel(category.isActive() ? "Active" : "Inactive");
        toggle.addValueChangeListener(e -> {
            category.setActive(e.getValue());
            toggle.setLabel(e.getValue() ? "Active" : "Inactive");
            categoryGrid.getDataProvider().refreshItem(category);
        });
        return toggle;
    }

    private Component createCategoryActions(RoomCategory category) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        
        Button editBtn = new Button(VaadinIcon.EDIT.create());
        editBtn.addClickListener(e -> openCategoryDialog(category));
        
        Button deleteBtn = new Button(VaadinIcon.TRASH.create());
        deleteBtn.getStyle().set("color", "#ef4444");
        deleteBtn.addClickListener(e -> {
            categories.remove(category);
            categoryGrid.getDataProvider().refreshAll();
            Notification.show("Category deleted");
        });
        
        actions.add(editBtn, deleteBtn);
        return actions;
    }

    private Component createRoomsCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        card.getStyle().set("margin-top", "1rem");
        
        H3 title = new H3("Individual Rooms");
        title.getStyle().set("margin", "0 0 0.5rem 0");
        
        Paragraph subtitle = new Paragraph("Manage individual room status and maintenance");
        subtitle.getStyle().set("margin", "0 0 1rem 0");

        roomGrid.addColumn(Room::getRoomNumber)
            .setHeader("Room #")
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        roomGrid.addColumn(room -> room.getCategory().getName())
            .setHeader("Category")
            .setFlexGrow(1);
        
        roomGrid.addComponentColumn(this::createRoomStatusBadge)
            .setHeader("Status")
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        roomGrid.addComponentColumn(this::createRoomActions)
            .setHeader("Actions")
            .setAutoWidth(true)
            .setFlexGrow(0);

        roomGrid.setItems(rooms);
        roomGrid.setAllRowsVisible(true);
        roomGrid.setWidthFull();

        card.add(title, subtitle, roomGrid);
        return card;
    }

    private Component createRoomStatusBadge(Room room) {
        Span badge = new Span(formatStatus(room.getStatus()));
        badge.getStyle()
            .set("padding", "0.25rem 0.75rem")
            .set("border-radius", "0.5rem")
            .set("font-size", "0.875rem")
            .set("font-weight", "600")
            .set("text-transform", "capitalize");
        
        switch (room.getStatus()) {
            case "available" -> badge.getStyle()
                .set("background", "#d1fae5")
                .set("color", "#10b981");
            case "occupied" -> badge.getStyle()
                .set("background", "#dbeafe")
                .set("color", "#3b82f6");
            case "cleaning" -> badge.getStyle()
                .set("background", "#fef3c7")
                .set("color", "#f59e0b");
            case "maintenance" -> badge.getStyle()
                .set("background", "#fee2e2")
                .set("color", "#ef4444");
        }
        
        return badge;
    }

    private String formatStatus(String status) {
        return switch (status) {
            case "available" -> "Available";
            case "occupied" -> "Occupied";
            case "cleaning" -> "Cleaning";
            case "maintenance" -> "Maintenance";
            default -> status;
        };
    }

    private Component createRoomActions(Room room) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        
        Button changeStatusBtn = new Button("Change Status");
        changeStatusBtn.addClickListener(e -> openRoomStatusDialog(room));
        
        actions.add(changeStatusBtn);
        return actions;
    }

    private void openRoomStatusDialog(Room room) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Change Room Status - " + room.getRoomNumber());
        dialog.setWidth("500px");

        VerticalLayout content = new VerticalLayout();
        
        Paragraph info = new Paragraph("Current Status: " + formatStatus(room.getStatus()));
        info.getStyle().set("margin", "0 0 1rem 0");
        
        Select<String> statusSelect = new Select<>();
        statusSelect.setLabel("New Status");
        statusSelect.setItems("available", "occupied", "cleaning", "maintenance");
        statusSelect.setValue(room.getStatus());
        statusSelect.setItemLabelGenerator(this::formatStatus);
        statusSelect.setWidthFull();
        
        TextArea notesArea = new TextArea("Notes (optional)");
        notesArea.setPlaceholder("Add any notes about the status change...");
        notesArea.setWidthFull();
        notesArea.setHeight("100px");
        if (room.getNotes() != null) {
            notesArea.setValue(room.getNotes());
        }
        
        content.add(info, statusSelect, notesArea);

        Button saveBtn = new Button("Update Status");
        saveBtn.addClassName("primary-button");
        saveBtn.addClickListener(e -> {
            room.setStatus(statusSelect.getValue());
            room.setNotes(notesArea.getValue());
            roomGrid.getDataProvider().refreshItem(room);
            dialog.close();
            Notification.show("Room status updated to " + formatStatus(statusSelect.getValue()));
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> dialog.close());

        dialog.add(content);
        dialog.getFooter().add(new HorizontalLayout(cancelBtn, saveBtn));
        dialog.open();
    }

    private void openCategoryDialog(RoomCategory existing) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existing == null ? "Add New Room Category" : "Edit Room Category");
        dialog.setWidth("700px");

        TextField name = new TextField("Room Category Name*");
        name.setWidthFull();
        
        NumberField price = new NumberField("Price per Night (€)*");
        price.setWidthFull();
        
        TextArea desc = new TextArea("Description*");
        desc.setWidthFull();
        desc.setHeight("100px");
        
        NumberField totalRooms = new NumberField("Total Rooms*");
        totalRooms.setWidthFull();
        
        NumberField maxGuests = new NumberField("Max Guests*");
        maxGuests.setWidthFull();
        
        TextField amenities = new TextField("Amenities (comma separated)");
        amenities.setWidthFull();
        amenities.setPlaceholder("WiFi, TV, AC, Mini Bar...");

        if (existing != null) {
            name.setValue(existing.getName());
            price.setValue((double) existing.getPricePerNight());
            desc.setValue(existing.getDescription());
            totalRooms.setValue((double) existing.getTotalRooms());
            maxGuests.setValue((double) existing.getMaxGuests());
            amenities.setValue(existing.getAmenities());
        }

        FormLayout form = new FormLayout(name, price, desc, totalRooms, maxGuests, amenities);
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("600px", 2)
        );

        Button save = new Button(existing == null ? "Add Category" : "Update Category");
        save.addClassName("primary-button");
        save.addClickListener(e -> {
            if (existing == null) {
                var r = new RoomCategory();
                r.setId(categories.stream().mapToInt(RoomCategory::getId).max().orElse(0) + 1);
                r.setName(name.getValue());
                r.setPricePerNight(price.getValue() == null ? 0 : price.getValue().intValue());
                r.setDescription(desc.getValue());
                r.setTotalRooms(totalRooms.getValue() == null ? 0 : totalRooms.getValue().intValue());
                r.setMaxGuests(maxGuests.getValue() == null ? 0 : maxGuests.getValue().intValue());
                r.setAmenitiesFromString(amenities.getValue());
                r.setActive(true);
                categories.add(r);
            } else {
                existing.setName(name.getValue());
                existing.setPricePerNight(price.getValue() == null ? 0 : price.getValue().intValue());
                existing.setDescription(desc.getValue());
                existing.setTotalRooms(totalRooms.getValue() == null ? 0 : totalRooms.getValue().intValue());
                existing.setMaxGuests(maxGuests.getValue() == null ? 0 : maxGuests.getValue().intValue());
                existing.setAmenitiesFromString(amenities.getValue());
            }
            categoryGrid.getDataProvider().refreshAll();
            dialog.close();
            Notification.show("Saved successfully");
        });

        Button cancel = new Button("Cancel");
        cancel.addClickListener(e -> dialog.close());

        dialog.add(new VerticalLayout(form));
        dialog.getFooter().add(new HorizontalLayout(cancel, save));
        dialog.open();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasAnyRole(UserRole.RECEPTIONIST, UserRole.MANAGER)) {
            event.rerouteTo(LoginView.class);
        }
    }
}