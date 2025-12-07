package com.hotel.booking.view.components;

import com.hotel.booking.entity.Room;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.RoomStatus;
import com.hotel.booking.service.RoomCategoryService;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

/**
 * RoomForm Component - Reusable form for creating and editing rooms.
 * Similar pattern to AddUserForm for dialog integration.
 * Automatically synchronizes price from selected room category.
 */
public class RoomForm extends FormLayout {

    private final Binder<Room> binder = new Binder<>(Room.class);
    private final RoomCategoryService roomCategoryService;
    private Room formRoom;

    private final TextField roomNumberField = new TextField("Room Number *");
    private final NumberField floorField = new NumberField("Floor");
    private final Select<RoomCategory> categorySelect = new Select<>();
    private final NumberField priceField = new NumberField("Price per Night (€)");
    private final Select<RoomStatus> statusSelect = new Select<>();
    private final TextArea infoArea = new TextArea("Additional Information");

    public RoomForm(Room existingRoom, RoomCategoryService roomCategoryService) {
        this.roomCategoryService = roomCategoryService;
        initializeFields();
        setupBinder();
        layoutForm();
        setRoom(existingRoom);
    }

    private void initializeFields() {
        roomNumberField.setPlaceholder("e.g., 101, 102, A01");
        roomNumberField.setWidthFull();
        roomNumberField.setRequiredIndicatorVisible(true);

        floorField.setMin(0);
        floorField.setStep(1);
        floorField.setWidthFull();

        categorySelect.setLabel("Room Category *");
        categorySelect.setItems(roomCategoryService.getAllRoomCategories());
        categorySelect.setItemLabelGenerator(RoomCategory::getName);
        categorySelect.setWidthFull();
        categorySelect.setRequiredIndicatorVisible(true);
        categorySelect.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                priceField.setValue(event.getValue().getPricePerNight().doubleValue());
            }
        });

        priceField.setWidthFull();
        priceField.setReadOnly(true);
        priceField.setHelperText("Price is set by the room category");

        statusSelect.setLabel("Status *");
        statusSelect.setItems(RoomStatus.values());
        statusSelect.setValue(RoomStatus.AVAILABLE);
        statusSelect.setWidthFull();
        statusSelect.setRequiredIndicatorVisible(true);

        infoArea.setWidthFull();
        infoArea.setHeight("100px");
        infoArea.setPlaceholder("Optional notes about this room...");
    }

    private void setupBinder() {
        binder.forField(roomNumberField)
            .asRequired("Room number is required")
            .bind(Room::getRoomNumber, Room::setRoomNumber);

        binder.forField(floorField)
            .bind(room -> room.getFloor() != null ? room.getFloor().doubleValue() : 0,
                  (room, value) -> room.setFloor(value != null ? value.intValue() : null));

        binder.forField(categorySelect)
            .asRequired("Category is required")
            .bind(Room::getCategory, Room::setCategory);

        // Price field is read-only - set by the category
        // Not included in binder since price comes from category.getPricePerNight()

        binder.forField(statusSelect)
            .asRequired("Status is required")
            .bind(Room::getStatus, Room::setStatus);

        binder.forField(infoArea)
            .bind(Room::getInformation, Room::setInformation);
    }

    private void layoutForm() {
        setResponsiveSteps(
            new ResponsiveStep("0", 1),
            new ResponsiveStep("500px", 2)
        );
        setColspan(infoArea, 2);

        add(roomNumberField, floorField, categorySelect, priceField, statusSelect, infoArea);
    }

    private void setRoom(Room existingRoom) {
        if (existingRoom == null) {
            formRoom = new Room();
            statusSelect.setValue(RoomStatus.AVAILABLE);
            priceField.setValue(0.0);
        } else {
            formRoom = existingRoom;
        }
        
        binder.readBean(formRoom);
        
        // Display price from category
        if (formRoom.getCategory() != null && formRoom.getCategory().getPricePerNight() != null) {
            priceField.setValue(formRoom.getCategory().getPricePerNight().doubleValue());
        } else {
            priceField.setValue(0.0);
        }
    }

    public Room getRoom() {
        return formRoom;
    }

    public void writeBean() throws ValidationException {
        binder.writeBean(formRoom);
    }
}
