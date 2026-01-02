package com.hotel.booking.view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingFormService;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

/**
 * Form component for creating and editing hotel bookings.
 * <p>
 * This Vaadin {@link FormLayout} encapsulates all input fields, validation logic
 * and data binding required to manage a {@link Booking}.
 *
 * <p>
 * The form can be instantiated empty for new bookings or pre-filled for editing
 * or portal-driven booking flows.
 *
 * <p>
 * This class is UI-focused and delegates all business logic and persistence
 * checks to {@link BookingFormService}.
 * @author Matthias Lohr
 */

@CssImport("./themes/hotel/views/booking-management.css")
public class createNewBookingForm extends FormLayout{

    final Binder<Booking> binder = new Binder<>(Booking.class);
    private Booking formBooking;
    private final User user;

    private final SessionService sessionService;
    private final BookingFormService formService;

    private TextField displayCategoryField = new TextField("Room Category");
    private EmailField userByEmailField = new EmailField("E-Mail");
    private Select<RoomCategory> roomCategorySelect = new Select<>();
    private IntegerField guestNumber = new IntegerField ("How many guests?");
    private DatePicker checkInDate = new DatePicker("Check-In Date");
    private DatePicker checkOutDate = new DatePicker("Check-Out Date");
    private CheckboxGroup<BookingExtra> extras = new CheckboxGroup<>();
    private List<BookingExtra> availableExtras = new ArrayList<>();

    private boolean editing = false;

    /**
     * Creates a booking form for creating a new booking or editing an existing one.
     * <p>
     * Initializes fields, binder configuration and optional editing state.
     *
     * @param user current user creating or editing the booking
     * @param sessionService session context and role information
     * @param existingBooking booking to edit, or {@code null} for a new booking
     * @param formService service providing booking-related data and validation
     */
    public createNewBookingForm(User user, SessionService sessionService, Booking existingBooking, BookingFormService formService) {

        this.user = user;
        this.sessionService = sessionService;
        this.formService = formService;
        this.formBooking = existingBooking;

        this.configureFields();
        this.configureBinder();
        this.setBooking(existingBooking);

        HorizontalLayout extrasRow = new HorizontalLayout();
        extrasRow.setWidthFull();
        extrasRow.setAlignItems(Alignment.END);
        extras.setWidth("300px");
        VerticalLayout extrasListBox = createExtrasListBox();
        extrasRow.add(extras, extrasListBox);

        this.add(displayCategoryField, userByEmailField, roomCategorySelect, checkInDate, checkOutDate, guestNumber, extrasRow);
    }

    /**
     * Creates a booking form pre-filled with booking data.
     * <p>
     * Intended for scenarios where booking parameters are already known
     * (e.g. booking initiated from another view).
     *
     * @param user current user
     * @param sessionService session context
     * @param existingBooking booking to edit, or {@code null} for a new booking
     * @param formService service providing booking-related data and validation
     * @param category preselected room category
     * @param checkIn predefined check-in date
     * @param checkOut predefined check-out date
     * @param occupancy predefined number of guests
     */
    public createNewBookingForm(User user, SessionService sessionService, Booking existingBooking, BookingFormService formService, 
                                 RoomCategory category, LocalDate checkIn, LocalDate checkOut, Integer occupancy) {
        this.user = user;
        this.sessionService = sessionService;
        this.formService = formService;
        this.formBooking = existingBooking;

        this.configureFields();
        this.configureBinder();
        this.setBooking(existingBooking);

        // Extras-CheckboxGroup und statische Liste nebeneinander
        HorizontalLayout extrasRow = new HorizontalLayout();
        extrasRow.setWidthFull();
        extrasRow.setAlignItems(Alignment.END);
        extras.setWidth("300px");
        VerticalLayout extrasListBox = createExtrasListBox();
        extrasRow.add(extras, extrasListBox);

        this.add(displayCategoryField, userByEmailField, roomCategorySelect, checkInDate, checkOutDate, guestNumber, extrasRow);

        Integer maxOccupancy = category != null ? category.getMaxOccupancy() : null;

        if (category != null) {
            roomCategorySelect.setVisible(false);
            displayCategoryField.setVisible(true);
            displayCategoryField.setReadOnly(true);
            displayCategoryField.setValue(category.getName());
            if (formBooking != null) {
                formBooking.setRoomCategory(category);
            }
            roomCategorySelect.setValue(category);
            
            if (maxOccupancy != null && maxOccupancy > 0) {
                guestNumber.setMax(maxOccupancy);
            }
        }

        if (checkIn != null) {
            checkInDate.setValue(checkIn);
            if (formBooking != null) {
                formBooking.setCheckInDate(checkIn);
            }
        }
        if (checkOut != null) {
            checkOutDate.setValue(checkOut);
            if (formBooking != null) {
                formBooking.setCheckOutDate(checkOut);
            }
        }
        
        if (occupancy != null && occupancy > 0) {
            int guestsToSet = occupancy;
            if (maxOccupancy != null && occupancy > maxOccupancy) {
                guestsToSet = maxOccupancy;
            }
            guestNumber.setValue(guestsToSet);
            if (formBooking != null) {
                formBooking.setAmount(guestsToSet);
            }
        }
    }

    /**
     * Configures all form fields including defaults, visibility,
     * available options and value change listeners.
     */
    private void configureFields() {
        displayCategoryField.setVisible(false);

        roomCategorySelect.setLabel("Room Category");
        roomCategorySelect.setItems(formService.getAllRoomCategories());
        roomCategorySelect.setItemLabelGenerator(RoomCategory::getName);

        guestNumber.setValue(2);
        guestNumber.setMin(1);
        guestNumber.setStepButtonsVisible(true);

        extras.setLabel("Extras");
        availableExtras = formService.getAllBookingExtras();
        extras.setItems(availableExtras);
        extras.setItemLabelGenerator(BookingExtra::getName);

        checkInDate.addValueChangeListener(e -> {
            binder.validate();
        });
        checkOutDate.addValueChangeListener(e -> {
            binder.validate();
        });
    }

    /**
     * Configures the Vaadin Binder including validation rules
     * and field-to-entity bindings.
     * <p>
     * Handles business validation such as date constraints,
     * room availability and guest capacity.
     */
    private void configureBinder() {

        binder.forField(guestNumber)
            .asRequired("Guests required")
            .withValidator(n -> n > 0, "Guests must be > 0")
            .withValidator(n -> {
                //Prüft, ob die Gästeanzahl zur ausgewählten Kategorie passt
                RoomCategory selected = roomCategorySelect.getValue();
                if (selected == null || n == null) return true;
                Integer max = selected.getMaxOccupancy();
                return max == null || n <= max;
            }, "Too many guests for selected category")
            .bind(Booking::getAmount, Booking::setAmount);

        binder.forField(checkInDate)
                .asRequired("Check-In required")
                .withValidator(date -> {
                    if (date == null) return true;
                    // If editing and the date equals the original booking's check-in, allow it
                    if (editing && formBooking != null && formBooking.getCheckInDate() != null && date.equals(formBooking.getCheckInDate())) {
                        return true;
                    }
                    return !date.isBefore(LocalDate.now());
                }, "Check-In darf nicht in der Vergangenheit liegen")
                .withValidator(date -> {
                    LocalDate checkOut = checkOutDate.getValue();
                    RoomCategory category = roomCategorySelect.getValue();

                    if (date == null || checkOut == null || category == null)
                        return true; // Not checkable yet
                    // If editing an existing booking, ignore that booking when checking availability
                    if (editing && formBooking != null && formBooking.getId() != null) {
                        return formService.isRoomAvailable(category, date, checkOut, formBooking.getId());
                    }
                    return formService.isRoomAvailable(category, date, checkOut);
                }, "No Room available for selected dates")
                .bind(Booking::getCheckInDate, Booking::setCheckInDate);

        binder.forField(checkOutDate)
                .asRequired("Check-Out required")
                .withValidator(date -> {
                    LocalDate checkIn = checkInDate.getValue();
                    if (date == null) return true;
                    // If editing and date equals original booking's check-out, allow it
                    if (editing && formBooking != null && formBooking.getCheckOutDate() != null && date.equals(formBooking.getCheckOutDate())) {
                        return true;
                    }
                    if (checkIn != null && !date.isAfter(checkIn)) return false;
                    return !date.isBefore(LocalDate.now());
                }, "Check-Out must be after Check-In")
                .withValidator(date -> {                    // Is Room available
                    LocalDate checkIn = checkInDate.getValue();
                    RoomCategory category = roomCategorySelect.getValue();

                    if (checkIn == null || date == null || category == null)
                        return true; // Not checkable yet

                    if (editing && formBooking != null && formBooking.getId() != null) {
                        return formService.isRoomAvailable(category, checkIn, date, formBooking.getId());
                    }
                    return formService.isRoomAvailable(category, checkIn, date);
                }, "No Room available for selected dates")
                .bind(Booking::getCheckOutDate, Booking::setCheckOutDate);

        binder.forField(roomCategorySelect)
                .asRequired("Room category is required")
                .bind(Booking::getRoomCategory, Booking::setRoomCategory);

        binder.forField(extras)
            .bind(
                booking -> {
                if (booking.getExtras() == null) {
                    booking.setExtras(new HashSet<>()); 
                }
                return booking.getExtras();
                },
                (booking, selectedExtras) -> booking.setExtras(selectedExtras)
            );
    }

    /**
     * Initializes the form with the given booking.
     * <p>
     * Determines whether the form is in create or edit mode,
     * adjusts field visibility and reads booking data into the binder.
     *
     * @param existingBooking booking to edit, or {@code null} for a new booking
     */
    public void setBooking(Booking existingBooking) {
    boolean isNew = existingBooking == null;
    this.editing = !isNew;
    if (isNew) {
        formBooking = new Booking("", LocalDate.now(), LocalDate.now().plusDays(1), BookingStatus.PENDING, user, null);
        userByEmailField.setVisible(
        (sessionService.getCurrentRole() == UserRole.MANAGER 
        || sessionService.getCurrentRole() == UserRole.RECEPTIONIST));
    } else {
        formBooking = existingBooking;
        roomCategorySelect.setVisible(false);
        displayCategoryField.setVisible(true);
        displayCategoryField.setReadOnly(true);
        displayCategoryField.setValue(formBooking.getRoomCategory().getName());
        formBooking.setStatus(BookingStatus.MODIFIED);
        userByEmailField.setVisible(false);
    }

    binder.readBean(formBooking);

    Map<Long, BookingExtra> byId = new HashMap<>();
    for (BookingExtra be : availableExtras) {
        if (be != null && be.getBookingExtra_id() != null) {
            byId.put(be.getBookingExtra_id(), be);
        }
    }

    Set<BookingExtra> toSelect = new HashSet<>();
    if (formBooking.getExtras() != null) {
        for (BookingExtra be : formBooking.getExtras()) {
            if (be == null) {
                continue;
            }
            Long id = be.getBookingExtra_id();
                if (id != null && byId.containsKey(id)) {
                    toSelect.add(byId.get(id));
            }
        }
    }
    extras.setValue(toSelect);
    }

    /**
     * Returns the booking instance currently bound to the form.
     *
     * @return booking being edited or created
     */
    public Booking getBooking() {
        return formBooking;
    }

    /**
     * Writes validated form values into the bound booking entity.
     * <p>
     * Resolves the guest by email if provided and ensures
     * a valid guest is always assigned.
     *
     * @throws ValidationException if binder validation fails
     */
    public void writeBean() throws ValidationException {
        if (userByEmailField.isVisible()) {
        String email = userByEmailField.getValue();
        if (email != null && !email.isBlank() && formService.existsByEmail(email)) {
            User foundUser = formService.findUserByEmail(email);
            formBooking.setGuest(foundUser);
            }}
        binder.writeBean(formBooking);
        
        if (formBooking.getGuest() == null) {
            formBooking.setGuest(user);
        }
    }

    /**
     * Creates a static list component displaying available booking extras
     * with pricing information.
     *
     * @return layout containing the extras information list
     */
    private VerticalLayout createExtrasListBox() {
        VerticalLayout listBox = new VerticalLayout();
        listBox.addClassName("extras-list-box");
        listBox.setSpacing(false);
        listBox.setPadding(false);
        
        Span title = new Span("Available Extras:");
        title.getStyle().set("font-weight", "bold");
        title.getStyle().set("margin-bottom", "8px");
        listBox.add(title);
        
        formService.getAllBookingExtras().forEach(extra -> {
            Span extraInfo = new Span(extra.getName() + " - €" + String.format("%.2f", extra.getPrice()));
            if (extra.isPerPerson()) {
                extraInfo.getStyle().set("font-size", "11px");
                extraInfo.getStyle().set("color", "#666");
            }
            listBox.add(extraInfo);
        });
        
        return listBox;
    }
}
