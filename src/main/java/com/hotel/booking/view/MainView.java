package com.hotel.booking.view;

import com.hotel.booking.entity.Room;
import com.hotel.booking.service.RoomService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("")
public class MainView extends VerticalLayout {

    private final RoomService roomService;
    private final Grid<Room> grid = new Grid<>(Room.class);
    private final TextField typeField = new TextField("Room Type");
    private final NumberField priceField = new NumberField("Price");
    private final Checkbox availabilityCheckbox = new Checkbox("Available");
    private final Button addButton = new Button("Add Room");
    private final Button updateButton = new Button("Update Room");
    private final Button deleteButton = new Button("Delete Room");
    private Room selectedRoom;

    public MainView(RoomService roomService) {
        this.roomService = roomService;

        configureGrid();
        configureForm();

        add(grid, createFormLayout());
        updateList();
        setSpacing(true);
        setPadding(true);
    }

    private void configureGrid() {
        grid.setColumns("id", "type", "price", "availability");
        grid.asSingleSelect().addValueChangeListener(event -> {
            selectedRoom = event.getValue();
            if (selectedRoom != null) {
                typeField.setValue(selectedRoom.getType());
                priceField.setValue(selectedRoom.getPrice());
                availabilityCheckbox.setValue(selectedRoom.getAvailability());
            } else {
                clearForm();
            }
        });
    }

    private void configureForm() {
        priceField.setMin(0);
        priceField.setStep(0.01);

        addButton.addClickListener(e -> {
            Room room = new Room(
                    typeField.getValue(),
                    priceField.getValue(),
                    availabilityCheckbox.getValue()
            );
            roomService.save(room);
            updateList();
            clearForm();
        });

        updateButton.addClickListener(e -> {
            if (selectedRoom != null) {
                selectedRoom.setType(typeField.getValue());
                selectedRoom.setPrice(priceField.getValue());
                selectedRoom.setAvailability(availabilityCheckbox.getValue());
                roomService.save(selectedRoom);
                updateList();
                clearForm();
            }
        });

        deleteButton.addClickListener(e -> {
            if (selectedRoom != null) {
                roomService.delete(selectedRoom);
                updateList();
                clearForm();
            }
        });
    }

    private HorizontalLayout createFormLayout() {
        HorizontalLayout formLayout = new HorizontalLayout();
        VerticalLayout fieldsLayout = new VerticalLayout(typeField, priceField, availabilityCheckbox);
        HorizontalLayout buttonsLayout = new HorizontalLayout(addButton, updateButton, deleteButton);
        
        formLayout.add(fieldsLayout, buttonsLayout);
        formLayout.setDefaultVerticalComponentAlignment(Alignment.END);
        return formLayout;
    }

    private void updateList() {
        grid.setItems(roomService.findAll());
    }

    private void clearForm() {
        typeField.clear();
        priceField.clear();
        availabilityCheckbox.setValue(false);
        selectedRoom = null;
        grid.asSingleSelect().clear();
    }
}
