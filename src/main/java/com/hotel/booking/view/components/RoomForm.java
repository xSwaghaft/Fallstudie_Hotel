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
 * Reusable form component for creating and editing rooms.
 * <p>
 * This form provides fields for entering room information including room number,
 * floor, category, status, and additional information. The price field is automatically
 * synchronized with the selected room category.
 * </p>
 *
 * @author Artur Derr
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

    /**
     * Constructs a RoomForm for creating or editing a room.
     *
     * @param existingRoom the Room entity to edit, or null for creating a new room
     * @param roomCategoryService the service for retrieving available room categories
     */
    public RoomForm(Room existingRoom, RoomCategoryService roomCategoryService) {
        this.roomCategoryService = roomCategoryService;
        initializeFields();
        setupBinder();
        layoutForm();
        setRoom(existingRoom);
    }

    /**
     * Initializes and configures all form input fields.
     * <p>
     * Sets up placeholders, validators, and event listeners for each field.
     * The category select automatically updates the price field when changed.
     * </p>
     */
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

    /**
     * Configures the Vaadin Binder for form validation and data binding.
     * <p>
     * Sets up validation rules for room fields. The price field is read-only
     * and its value comes from the selected category.
     * </p>
     */
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

    /**
     * Arranges the form components in a responsive layout.
     * <p>
     * Uses responsive steps to adapt to different screen sizes.
     * </p>
     */
    private void layoutForm() {
        setResponsiveSteps(
            new ResponsiveStep("0", 1),
            new ResponsiveStep("500px", 2)
        );
        setColspan(infoArea, 2);

        add(roomNumberField, floorField, categorySelect, priceField, statusSelect, infoArea);
    }

    /**
     * Sets the room to be edited in the form.
     * <p>
     * If the room is null, a new Room object is created for form entry.
     * The price field is updated based on the room's category.
     * </p>
     *
     * @param existingRoom the Room entity to edit, or null for creating a new room
     */
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

    /**
     * Retrieves the room object being edited in the form.
     *
     * @return the Room entity
     */
    public Room getRoom() {
        return formRoom;
    }

    /**
     * Writes the form data to the room bean after validation.
     *
     * @throws ValidationException if validation of form data fails
     */
    public void writeBean() throws ValidationException {
        binder.writeBean(formRoom);
    }
}
