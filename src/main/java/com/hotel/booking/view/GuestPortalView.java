package com.hotel.booking.view;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingFormService;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.RoomCategoryService;
import com.hotel.booking.view.components.RoomGrid;

import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

/**
 * Guest Portal View - Main view for guests to search and book rooms.
 */
@Route(value = "guest-portal", layout = MainLayout.class)
@PageTitle("Guest Portal")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/guest-portal.css")
@RolesAllowed(UserRole.GUEST_VALUE)
public class GuestPortalView extends VerticalLayout {

    // Services
    private final SessionService sessionService;
    private final BookingService bookingService;
    private final RoomCategoryService roomCategoryService;
    private final BookingFormService bookingFormService;
    
    // UI Components
    private final RoomGrid roomGrid;
    private DatePicker checkIn;
    private DatePicker checkOut;
    private NumberField guests;
    private Select<String> type;

    public GuestPortalView(SessionService sessionService,
                           BookingService bookingService,
                           RoomCategoryService roomCategoryService,
                           BookingFormService bookingFormService) {

        this.sessionService = sessionService;
        this.bookingService = bookingService;
        this.roomCategoryService = roomCategoryService;
        this.bookingFormService = bookingFormService;
        this.roomGrid = new RoomGrid();

        // Configure layout
        setSpacing(true);
        setPadding(true);
        setWidthFull();
        setHeight(null);

        // Configure room grid
        roomGrid.setWidthFull();
        roomGrid.setHeight(null);
        
        // Add header, search form, and room grid
        add(new H1("Search Rooms"), createSearchCard(), roomGrid);
    }

    /**
     * Creates the search form card with date pickers, guest count, and room type selector.
     */
    private Div createSearchCard() {
        Div card = new Div();
        card.addClassName("guest-search-card");
        
        checkIn = new DatePicker("Check-in");
        checkIn.setMin(LocalDate.now());
        checkIn.setValue(LocalDate.now().plusDays(2));

        checkOut = new DatePicker("Check-out");
        checkOut.setValue(LocalDate.now().plusDays(5));
        checkOut.setMin(checkIn.getValue().plusDays(1));

        guests = new NumberField("Guests");
        guests.setValue(2d);

        type = new Select<>();
        type.setLabel("Room Type");
        List<String> categories = roomCategoryService.getAllRoomCategories()
            .stream()
            .map(RoomCategory::getName)
            .toList();
        type.setItems(Stream.concat(Stream.of("All Types"), categories.stream()).toList());
        type.setValue("All Types");

        checkIn.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                checkOut.setMin(e.getValue().plusDays(1));
                if (checkOut.getValue() == null || !checkOut.getValue().isAfter(e.getValue())) {
                    checkOut.setValue(e.getValue().plusDays(2));
                }
            }
        });

        Button searchBtn = new Button("Search");
        searchBtn.addClassName("primary-button");
        searchBtn.addClickListener(e -> executeSearch());

        HorizontalLayout formLayout = new HorizontalLayout(checkIn, checkOut, guests, type, searchBtn);
        formLayout.addClassName("guest-search-form");
        formLayout.setWidthFull();
        formLayout.setAlignItems(FlexComponent.Alignment.END);
        configureResponsiveFields(formLayout, checkIn, checkOut, guests, type);
        searchBtn.addClassName("search-button-responsive");
        
        card.add(formLayout);
        return card;
    }

    /**
     * Executes the search for available room categories and displays them in the grid.
     */
    private void executeSearch() {
        LocalDate in = checkIn.getValue();
        LocalDate out = checkOut.getValue();
        Double guestsValue = guests.getValue();
        String typeValue = type.getValue();

        if (in == null || out == null || guestsValue == null) {
            roomGrid.setCategories(Collections.emptyList(), null);
            return;
        }

        List<RoomCategory> categories = bookingService.availableRoomCategoriesSearch(
            in, out, guestsValue.intValue(),
            typeValue != null && !"All Types".equals(typeValue) ? typeValue : "All Types"
        );

        if (categories.isEmpty()) {
            Notification.show("No rooms available in the selected period.");
        }

        LocalDate finalCheckIn = in;
        LocalDate finalCheckOut = out;
        Integer finalOccupancy = guestsValue.intValue();
        
        roomGrid.setCategories(categories, card -> {
            double avg = bookingService.getAverageRatingForCategory(card.getCategory());
            if (avg > 0d) {
                card.setAverageRating(avg);
            }
            Button bookBtn = new Button("Book");
            bookBtn.addClickListener(e -> openCategoryBookingDialog(
                card.getCategory(), finalCheckIn, finalCheckOut, finalOccupancy));
            card.setBookButton(bookBtn);
        });
    }

    /**
     * Opens a booking dialog for the selected category with pre-filled form values.
     */
    private void openCategoryBookingDialog(RoomCategory category, LocalDate checkIn, LocalDate checkOut, Integer occupancy) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Confirm Booking");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        String categoryName = category != null ? category.getName() : "Category";
        LocalDate in = checkIn != null ? checkIn : LocalDate.now();
        LocalDate out = (checkOut != null && checkOut.isAfter(in)) ? checkOut : in.plusDays(1);

        content.add(new Paragraph("Category: " + categoryName));
        String priceText = category != null && category.getPricePerNight() != null
                ? "€" + category.getPricePerNight() + " per night"
                : "Price not available";
        content.add(new Paragraph("Price: " + priceText));
        content.add(new Paragraph("Period: " + in + " to " + out));

        User currentUser = sessionService.getCurrentUser();

        createNewBookingForm bookingForm = new createNewBookingForm(
                currentUser, sessionService, null, bookingFormService,
                category, in, out, occupancy
        );

        content.add(bookingForm);

        Button confirm = new Button("Book");
        confirm.addClickListener(e -> {
            try {
                bookingForm.writeBean();
                Booking booking = bookingForm.getBooking();
                bookingService.save(booking);
                Notification.show("Booking successful!");
                dialog.close();
            } catch (ValidationException ex) {
                Notification.show("Please check your inputs.");
            }
        });
        confirm.addClassName("primary-button");

        Button cancel = new Button("Cancel", event -> dialog.close());

        dialog.add(content);
        dialog.getFooter().add(new HorizontalLayout(confirm, cancel));
        dialog.open();
    }

    /**
     * Configures form fields for responsive design (flexGrow and full width).
     */
    private void configureResponsiveFields(HorizontalLayout layout, HasSize... fields) {
        for (HasSize field : fields) {
            layout.setFlexGrow(1, field);
            field.setWidthFull();
        }
    }
}
