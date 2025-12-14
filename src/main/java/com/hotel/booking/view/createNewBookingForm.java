package com.hotel.booking.view;

import java.time.LocalDate;
import java.util.HashSet;

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
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

//Matthias Lohr
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
    // cache the available extras so we reuse the same instances for selection matching
    // Cache aller verfügbaren `BookingExtra`-Instanzen.
    // Zweck: Die Checkbox-Gruppe wird mit genau diesen Instanzen gefüllt und
    // beim Vorauswählen wieder auf diese Objekte gemappt. Das vermeidet
    // Instance-Equality-Probleme (detached vs. managed Instanzen) und sorgt
    // dafür, dass bereits zugeordnete Extras korrekt als ausgewählt angezeigt werden.
    private java.util.List<BookingExtra> availableExtras = new java.util.ArrayList<>();

    // Flag, das angibt, ob das Formular im Editier-Modus ist.
    // - `false`: Erstellen einer neuen Buchung — strengere Validierung (z. B. keine Past-Dates).
    // - `true`: Bearbeiten einer bestehenden Buchung — erlaubt das Beibehalten alter Werte.
    private boolean editing = false;

    public createNewBookingForm(User user, SessionService sessionService, Booking existingBooking, BookingFormService formService) {

        this.user = user;
        this.sessionService = sessionService;
        this.formService = formService;
        this.formBooking = existingBooking;

        this.configureFields();
        this.configureBinder();
        this.setBooking(existingBooking);

        this.add(displayCategoryField, userByEmailField, roomCategorySelect, checkInDate, checkOutDate, guestNumber, extras);
    }

    //Konstruktor für die GuestView - Kategorie kann übergeben werden
    public createNewBookingForm(User user, SessionService sessionService, Booking existingBooking, BookingFormService formService, RoomCategory category) {
        this.user = user;
        this.sessionService = sessionService;
        this.formService = formService;
        this.formBooking = existingBooking;

        this.configureFields();
        this.configureBinder();
        this.setBooking(existingBooking);

        this.add(displayCategoryField, userByEmailField, roomCategorySelect, checkInDate, checkOutDate, guestNumber, extras);

        // Wenn eine feste Kategorie übergeben wurde, zeige sie im Feld an
        if (category != null) {
            roomCategorySelect.setVisible(false);
            displayCategoryField.setVisible(true);
            displayCategoryField.setReadOnly(true);
            displayCategoryField.setValue(category.getName());
            if (formBooking != null) {
                formBooking.setRoomCategory(category);
            }
        }
    }

    //Konstruktor für RoomGrid - Kategorie, CheckIn und CheckOut können übergeben werden
    public createNewBookingForm(User user, SessionService sessionService, Booking existingBooking, BookingFormService formService, 
                                 RoomCategory category, LocalDate checkIn, LocalDate checkOut) {
        this.user = user;
        this.sessionService = sessionService;
        this.formService = formService;
        this.formBooking = existingBooking;

        this.configureFields();
        this.configureBinder();
        this.setBooking(existingBooking);

        this.add(displayCategoryField, userByEmailField, roomCategorySelect, checkInDate, checkOutDate, guestNumber, extras);

        // Wenn eine feste Kategorie übergeben wurde, zeige sie im Feld an
        if (category != null) {
            roomCategorySelect.setVisible(false);
            displayCategoryField.setVisible(true);
            displayCategoryField.setReadOnly(true);
            displayCategoryField.setValue(category.getName());
            if (formBooking != null) {
                formBooking.setRoomCategory(category);
            }
        }

        // Setze CheckIn und CheckOut direkt in den DatePickern
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
        
        // Aktualisiere das Booking-Objekt im Binder
        // `setBooking` führt bereits `binder.readBean(formBooking)` aus,
        // deshalb hier kein erneuter Aufruf, um UI-Überschreibungen zu vermeiden.
    }

    private void configureFields() {
        //Anzeigefeld, wenn die Kategorie nicht änderbar ist
        displayCategoryField.setVisible(false);

        //Select Category
        roomCategorySelect.setLabel("Room Category");
        roomCategorySelect.setItems(formService.getAllRoomCategories());
        roomCategorySelect.setItemLabelGenerator(RoomCategory::getName);

        // Gästezahl
        guestNumber.setValue(2);
        guestNumber.setMin(1);
        guestNumber.setStepButtonsVisible(true);

        // Extras
        extras.setLabel("Extras");
        // load once and keep the same instances for the CheckboxGroup
        availableExtras = formService.getAllBookingExtras();
        extras.setItems(availableExtras);
        extras.setItemLabelGenerator(BookingExtra::getName);

        // ValueChangeListener für Verfügbarkeitsprüfung an die Datepicker, damit auch
        //  bei Änderung im jeweils anderen Feld neu validiert wird
        checkInDate.addValueChangeListener(e -> {
            binder.validate();
        });
        checkOutDate.addValueChangeListener(e -> {
            binder.validate();
        });
    }

    private void configureBinder() {

        // Binder-Konfiguration
        // Wichtige Punkte:
        // - Validators unterscheiden zwischen Erstellen und Bearbeiten (siehe `editing`).
        // - Availability-Validatoren rufen bei Editieren die Variante mit `excludeBookingId`
        //   auf, damit die aktuell bearbeitete Buchung sich nicht selbst blockiert.

        // Guests
        binder.forField(guestNumber)
                .asRequired("Guests required")
                .withValidator(n -> n > 0, "Guests must be > 0")
                .bind(Booking::getAmount, Booking::setAmount);

        // Check-In
        binder.forField(checkInDate)
                .asRequired("Check-In required")
                .withValidator(date -> {
                    if (date == null) return true;
                    // If editing and the date equals the original booking's check-in, allow it
                    if (editing && formBooking != null && formBooking.getCheckInDate() != null && date.equals(formBooking.getCheckInDate())) {
                        return true;
                    }
                    // Check-in darf nicht in der Vergangenheit liegen für neue Buchungen oder Änderungen
                    return !date.isBefore(LocalDate.now());
                }, "Check-In darf nicht in der Vergangenheit liegen")
                .withValidator(date -> {
                    LocalDate checkOut = checkOutDate.getValue();
                    RoomCategory category = roomCategorySelect.getValue();

                    if (date == null || checkOut == null || category == null)
                        return true; // noch nicht prüfbar

                    // If editing an existing booking, ignore that booking when checking availability
                    if (editing && formBooking != null && formBooking.getId() != null) {
                        return formService.isRoomAvailable(category, date, checkOut, formBooking.getId());
                    }
                    return formService.isRoomAvailable(category, date, checkOut);
                }, "No Room available for selected dates")
                .bind(Booking::getCheckInDate, Booking::setCheckInDate);

        // Check-Out
        binder.forField(checkOutDate)
                .asRequired("Check-Out required")
                .withValidator(date -> {
                    LocalDate checkIn = checkInDate.getValue();
                    if (date == null) return true;
                    // If editing and date equals original booking's check-out, allow it
                    if (editing && formBooking != null && formBooking.getCheckOutDate() != null && date.equals(formBooking.getCheckOutDate())) {
                        return true;
                    }
                    // Check-out muss nach Check-in liegen
                    if (checkIn != null && !date.isAfter(checkIn)) return false;
                    // Check-out darf nicht in der Vergangenheit liegen für neue Buchungen oder Änderungen
                    return !date.isBefore(LocalDate.now());
                }, "Check-Out must be after Check-In")
                // Zimmer-Verfügbarkeitsprüfung
                .withValidator(date -> {
                    LocalDate checkIn = checkInDate.getValue();
                    RoomCategory category = roomCategorySelect.getValue();

                    if (checkIn == null || date == null || category == null)
                        return true; // noch nicht prüfbar

                    // If editing an existing booking, ignore that booking when checking availability
                    if (editing && formBooking != null && formBooking.getId() != null) {
                        return formService.isRoomAvailable(category, checkIn, date, formBooking.getId());
                    }
                    return formService.isRoomAvailable(category, checkIn, date);
                }, "No Room available for selected dates")
                .bind(Booking::getCheckOutDate, Booking::setCheckOutDate);

        // Room Category
        binder.forField(roomCategorySelect)
                .asRequired("Room category is required")
                .bind(Booking::getRoomCategory, Booking::setRoomCategory);

        // Extras: Many-To-Many
        binder.forField(extras)
            .bind(
                 booking -> {
            if (booking.getExtras() == null)
                booking.setExtras(new HashSet<>()); 
            return booking.getExtras();
        },
                (booking, selectedExtras) -> booking.setExtras(selectedExtras)
            );
    }

    public void setBooking(Booking existingBooking) {
    boolean isNew = existingBooking == null;
    this.editing = !isNew;
    if (isNew) {
        formBooking = new Booking("", LocalDate.now(), LocalDate.now().plusDays(1), BookingStatus.PENDING, user, null);
        //Feld nur für Manager oder Receptionist sichtbar, nur beim neu anlegen
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

    // Set bean (initial values anzeigen)       
    binder.readBean(formBooking);

    // Ensure that extras which are already part of the booking are shown as checked
    try {
        java.util.Map<Long, BookingExtra> byId = new java.util.HashMap<>();
        for (BookingExtra be : availableExtras) {
            if (be != null && be.getBookingExtra_id() != null) {
                byId.put(be.getBookingExtra_id(), be);
            }
        }

        java.util.Set<BookingExtra> toSelect = new java.util.HashSet<>();
        if (formBooking.getExtras() != null) {
            for (BookingExtra be : formBooking.getExtras()) {
                if (be == null)
                    continue;
                // Prefer matching by id
                Long id = be.getBookingExtra_id();
                    if (id != null && byId.containsKey(id)) {
                        toSelect.add(byId.get(id));
                } else {
                    // Fallback: match by name
                    String name = be.getName();
                    if (name != null) {
                        for (BookingExtra candidate : availableExtras) {
                            if (name.equals(candidate.getName())) {
                                toSelect.add(candidate);
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Apply selection to the CheckboxGroup so items are shown as checked
        // Always set the value (empty set if none) to ensure UI reflects current booking
        extras.setValue(toSelect);
    } catch (Exception e) {
        // silently ignore UI mapping issues; binder still holds the booking
    }
    }

    public Booking getBooking() {
        return formBooking;
    }

    public void writeBean() throws ValidationException {
        if (userByEmailField.isVisible()) {
        String email = userByEmailField.getValue();
        if (email != null && !email.isBlank()) {
            User foundUser = formService.findUserByEmail(email);
            formBooking.setGuest(foundUser);
            }}
        binder.writeBean(formBooking);
        
        // Stelle sicher, dass der Gast immer gesetzt ist
        if (formBooking.getGuest() == null) {
            formBooking.setGuest(user);
        }
    }
    
}
