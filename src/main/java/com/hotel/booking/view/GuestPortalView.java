package com.hotel.booking.view;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingFormService;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.PaymentService;
import com.hotel.booking.service.RoomCategoryService;
import com.hotel.booking.view.components.RoomGrid;
import com.hotel.booking.view.components.PaymentDialog;
import com.hotel.booking.view.components.ReviewsSection;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main view for guests to search and book rooms.
 * 
 * @author Viktor Götting
 */
@Route(value = "guest-portal", layout = MainLayout.class)
@PageTitle("Guest Portal")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/views/guest-portal.css")
@RolesAllowed(UserRole.GUEST_VALUE)
public class GuestPortalView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(GuestPortalView.class);
    
    /** Default date offsets */
    private static final int DEFAULT_CHECK_IN_DAYS = 2;
    private static final int DEFAULT_CHECK_OUT_DAYS = 5;
    private static final int MIN_STAY_DAYS = 1;
    private static final int AUTO_ADJUST_CHECKOUT_DAYS = 2;
    
    /** Default values */
    private static final double DEFAULT_GUESTS = 2.0;
    private static final String ALL_TYPES_OPTION = "All Types";
    
    /** Notification durations */
    private static final int NOTIFICATION_DURATION_SHORT = 3000;
    private static final int NOTIFICATION_DURATION_LONG = 5000;
    
    /** UI text constants */
    private static final String HEADER_TITLE = "Search Rooms";
    private static final String BUTTON_SEARCH = "Search";
    private static final String BUTTON_BOOK = "Book";
    private static final String BUTTON_CANCEL = "Cancel";
    private static final String DIALOG_TITLE_CONFIRM_BOOKING = "Confirm Booking";
    private static final String LABEL_CHECK_IN = "Check-in";
    private static final String LABEL_CHECK_OUT = "Check-out";
    private static final String LABEL_GUESTS = "Guests";
    private static final String LABEL_ROOM_TYPE = "Room Type";
    private static final String DEFAULT_CATEGORY_NAME = "Category";
    private static final String DEFAULT_PRICE_TEXT = "Price not available";
    private static final String CURRENCY_PREFIX = "€";
    private static final String PER_NIGHT_SUFFIX = " per night";
    private static final String LABEL_CATEGORY = "Category: ";
    private static final String LABEL_PRICE = "Price: ";
    private static final String LABEL_PERIOD = "Period: ";
    private static final String PERIOD_SEPARATOR = " to ";
    
    /** Messages */
    private static final String MSG_NO_ROOMS = "No rooms available in the selected period.";
    private static final String MSG_BOOKING_SUCCESS = "Booking successful!";
    private static final String MSG_PAYMENT_ERROR = "Error: Could not process payment. Missing booking data.";
    private static final String MSG_VALIDATION_ERROR = "Please check your inputs.";
    private static final String MSG_PRICE_MISSING = "Error: Total price is missing";
    private static final String MSG_PAYMENT_COMPLETED = "Payment completed! Booking confirmed.";
    

    // Services
    private final SessionService sessionService;
    private final BookingService bookingService;
    private final RoomCategoryService roomCategoryService;
    private final BookingFormService bookingFormService;
    private final PaymentService paymentService;
    private final ReviewsSection reviewsSection;
    
    // UI Components
    private final RoomGrid roomGrid;
    private DatePicker checkIn;
    private DatePicker checkOut;
    private NumberField guests;
    private Select<String> type;

    public GuestPortalView(SessionService sessionService,
                           BookingService bookingService,
                           RoomCategoryService roomCategoryService,
                           BookingFormService bookingFormService,
                           PaymentService paymentService,
                           ReviewsSection reviewsSection) {

        this.sessionService = sessionService;
        this.bookingService = bookingService;
        this.roomCategoryService = roomCategoryService;
        this.bookingFormService = bookingFormService;
        this.paymentService = paymentService;
        this.reviewsSection = reviewsSection;
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
        add(new H1(HEADER_TITLE), createSearchCard(), roomGrid);
    }

    /**
     * Creates the search form card.
     */
    private Div createSearchCard() {
        Div card = new Div();
        card.addClassName("guest-search-card");
        
        checkIn = new DatePicker(LABEL_CHECK_IN);
        checkIn.setMin(LocalDate.now());
        checkIn.setValue(LocalDate.now().plusDays(DEFAULT_CHECK_IN_DAYS));

        checkOut = new DatePicker(LABEL_CHECK_OUT);
        checkOut.setValue(LocalDate.now().plusDays(DEFAULT_CHECK_OUT_DAYS));
        checkOut.setMin(checkIn.getValue().plusDays(MIN_STAY_DAYS));

        guests = new NumberField(LABEL_GUESTS);
        guests.setValue(DEFAULT_GUESTS);

        type = new Select<>();
        type.setLabel(LABEL_ROOM_TYPE);
        List<String> categories = roomCategoryService.getAllRoomCategories()
            .stream()
            .map(RoomCategory::getName)
            .toList();
        type.setItems(Stream.concat(Stream.of(ALL_TYPES_OPTION), categories.stream()).toList());
        type.setValue(ALL_TYPES_OPTION);

        checkIn.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                checkOut.setMin(e.getValue().plusDays(MIN_STAY_DAYS));
                if (checkOut.getValue() == null || !checkOut.getValue().isAfter(e.getValue())) {
                    checkOut.setValue(e.getValue().plusDays(AUTO_ADJUST_CHECKOUT_DAYS));
                }
            }
        });

        Button searchBtn = new Button(BUTTON_SEARCH);
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
     * Executes the search for available room categories.
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
            typeValue != null && !ALL_TYPES_OPTION.equals(typeValue) ? typeValue : ALL_TYPES_OPTION
        );

        if (categories.isEmpty()) {
            Notification.show(MSG_NO_ROOMS);
        }

        LocalDate finalCheckIn = in;
        LocalDate finalCheckOut = out;
        Integer finalOccupancy = guestsValue.intValue();
        
        roomGrid.setCategories(categories, card -> {
            double avg = bookingService.getAverageRatingForCategory(card.getCategory());
            if (avg > 0d) {
                card.setAverageRating(avg);
            }
            // Setze Provider für Reviews-Content in Gallery
            card.setReviewsContentProvider(reviewsDiv -> reviewsSection.populateReviews(reviewsDiv, card.getCategory()));
            
            Button bookBtn = new Button(BUTTON_BOOK);
            bookBtn.addClickListener(e -> openCategoryBookingDialog(
                card.getCategory(), finalCheckIn, finalCheckOut, finalOccupancy));
            card.setBookButton(bookBtn);
        });
    }

    /**
     * Opens a booking dialog for the selected category.
     */
    private void openCategoryBookingDialog(RoomCategory category, LocalDate checkIn, LocalDate checkOut, Integer occupancy) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(DIALOG_TITLE_CONFIRM_BOOKING);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        String categoryName = category != null ? category.getName() : DEFAULT_CATEGORY_NAME;
        LocalDate in = checkIn != null ? checkIn : LocalDate.now();
        LocalDate out = (checkOut != null && checkOut.isAfter(in)) ? checkOut : in.plusDays(MIN_STAY_DAYS);

        content.add(new Paragraph(LABEL_CATEGORY + categoryName));
        String priceText = category != null && category.getPricePerNight() != null
                ? CURRENCY_PREFIX + category.getPricePerNight() + PER_NIGHT_SUFFIX
                : DEFAULT_PRICE_TEXT;
        content.add(new Paragraph(LABEL_PRICE + priceText));
        content.add(new Paragraph(LABEL_PERIOD + in + PERIOD_SEPARATOR + out));

        User currentUser = sessionService.getCurrentUser();

        createNewBookingForm bookingForm = new createNewBookingForm(
                currentUser, sessionService, null, bookingFormService,
                category, in, out, occupancy
        );

        content.add(bookingForm);

        Button confirm = new Button(BUTTON_BOOK);
        confirm.addClickListener(e -> {
            try {
                bookingForm.writeBean();
                Booking booking = bookingForm.getBooking();
                log.debug("Booking created: {}", booking);
                
                bookingService.save(booking);
                
                // Calculate total price AFTER booking is saved
                bookingService.calculateBookingPrice(booking);
                log.debug("Calculated Total Price: {}", booking.getTotalPrice());
                
                Notification.show(MSG_BOOKING_SUCCESS);
                dialog.close();
                
                // Open payment dialog after successful booking
                if (booking != null && booking.getTotalPrice() != null && booking.getTotalPrice().signum() > 0) {
                    openPaymentDialog(booking);
                } else {
                    Notification.show(MSG_PAYMENT_ERROR, NOTIFICATION_DURATION_LONG, Notification.Position.TOP_CENTER);
                }
            } catch (ValidationException ex) {
                Notification.show(MSG_VALIDATION_ERROR);
            }
        });
        confirm.addClassName("primary-button");

        Button cancel = new Button(BUTTON_CANCEL, event -> dialog.close());

        dialog.add(content);
        dialog.getFooter().add(new HorizontalLayout(confirm, cancel));
        dialog.open();
    }

    /**
     * Configures form fields for responsive design.
     */
    private void configureResponsiveFields(HorizontalLayout layout, HasSize... fields) {
        for (HasSize field : fields) {
            layout.setFlexGrow(1, field);
            field.setWidthFull();
        }
    }

    /**
     * Opens payment dialog for the booking.
     */
    private void openPaymentDialog(Booking booking) {
        if (booking.getTotalPrice() == null) {
            Notification.show(MSG_PRICE_MISSING, NOTIFICATION_DURATION_LONG, Notification.Position.TOP_CENTER);
            return;
        }
        
        PaymentDialog paymentDialog = new PaymentDialog(booking.getTotalPrice());
        paymentDialog.setOnPaymentSuccess(() -> {
            try {
                paymentService.processPayment(booking, null, paymentDialog.getSelectedPaymentMethod(), Invoice.PaymentStatus.PAID);
                Notification.show(MSG_PAYMENT_COMPLETED, NOTIFICATION_DURATION_SHORT, Notification.Position.TOP_CENTER);
            } catch (Exception ex) {
                log.error("Error processing payment", ex);
                Notification.show("Error processing payment. Please try again.", NOTIFICATION_DURATION_LONG, Notification.Position.TOP_CENTER);
            }
        });
        
        paymentDialog.setOnPaymentDeferred(() -> {
            try {
                paymentService.processPayment(booking, null, paymentDialog.getSelectedPaymentMethod(), Invoice.PaymentStatus.PENDING);
            } catch (Exception ex) {
                log.error("Error processing deferred payment", ex);
                Notification.show("Error processing payment. Please try again.", NOTIFICATION_DURATION_LONG, Notification.Position.TOP_CENTER);
            }
        });
        
        paymentDialog.open();
    }


}
