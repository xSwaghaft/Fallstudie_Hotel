package com.hotel.booking.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.Payment;
import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.PaymentService;
import com.hotel.booking.service.InvoiceService;
import com.hotel.booking.view.components.PaymentDialog;
import com.hotel.booking.view.components.BookingDetailsDialog;
import com.hotel.booking.view.components.BookingCard;
import com.hotel.booking.view.components.EditBookingDialog;
import com.hotel.booking.view.components.CancellationDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import jakarta.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;

// @Route: registriert die View unter /my-bookings im MainLayout.
// @CssImport: bindet globale und Guest-spezifische Styles ein.
@Route(value = "my-bookings", layout = MainLayout.class)
@PageTitle("My Bookings")
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/guest.css")
@RolesAllowed(UserRole.GUEST_VALUE)
public class MyBookingsView extends VerticalLayout {

    // Tab labels
    private static final String TAB_UPCOMING = "Upcoming";
    private static final String TAB_PAST = "Past";
    private static final String TAB_CANCELLED = "Cancelled";

    private final SessionService sessionService;
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final InvoiceService invoiceService;
    
    // Components
    private final BookingDetailsDialog bookingDetailsDialog;
    private final BookingCard bookingCard;
    private final EditBookingDialog editBookingDialog;
    private final CancellationDialog cancellationDialog;

    private Tabs tabs;
    private Div contentArea;
    private List<Booking> allBookings;

    @Autowired
    public MyBookingsView(SessionService sessionService, 
                         BookingService bookingService, 
                         PaymentService paymentService, 
                         InvoiceService invoiceService,
                         BookingDetailsDialog bookingDetailsDialog,
                         BookingCard bookingCard,
                         EditBookingDialog editBookingDialog,
                         CancellationDialog cancellationDialog) {
        this.sessionService = sessionService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.invoiceService = invoiceService;
        this.bookingDetailsDialog = bookingDetailsDialog;
        this.bookingCard = bookingCard;
        this.editBookingDialog = editBookingDialog;
        this.cancellationDialog = cancellationDialog;

        setSpacing(true);
        setPadding(true);
        setSizeFull();
        setWidthFull();
        getStyle().set("overflow-x", "hidden");

        allBookings = loadAllBookingsForCurrentUser();

        add(new H1("My Bookings"));
        
        if (allBookings.isEmpty()) {
            add(new Paragraph("No bookings found."));
        } else {
            createTabsAndContent();
        }
    }
    
    // Erstellt die Tabs (Bevorstehend/Vergangen/Storniert) und den Content-Bereich.
    private void createTabsAndContent() {
        Tab upcomingTab = new Tab(TAB_UPCOMING);
        Tab pastTab = new Tab(TAB_PAST);
        Tab cancelledTab = new Tab(TAB_CANCELLED);
        
        tabs = new Tabs(upcomingTab, pastTab, cancelledTab);
        tabs.addClassName("bookings-tabs");
        tabs.addSelectedChangeListener(e -> updateContent());
        
        contentArea = new Div();
        contentArea.addClassName("bookings-content-area");
        contentArea.setWidthFull();
        
        add(tabs, contentArea);
        updateContent();
    }
    
    // Filtert Buchungen je nach gewähltem Tab und rendert die Kartenliste.
    private void updateContent() {
        contentArea.removeAll();
        
        Tab selectedTab = tabs.getSelectedTab();
        if (selectedTab == null) return;

        String tabLabel = selectedTab.getLabel();
        
        // IMPORTANT: Reload all bookings from database to get fresh payment data
        User currentUser = sessionService.getCurrentUser();
        if (currentUser != null) {
            allBookings = bookingService.findAllBookingsForGuest(currentUser.getId());
            allBookings.forEach(bookingService::calculateBookingPrice);
        }
        
        List<Booking> filteredBookings = filterBookingsByTabType(tabLabel);
        
        if (filteredBookings.isEmpty()) {
            Paragraph emptyMessage = new Paragraph("No bookings in this category.");
            emptyMessage.getStyle().set("padding", "var(--spacing-xl)");
            emptyMessage.getStyle().set("text-align", "center");
            emptyMessage.getStyle().set("color", "var(--color-text-secondary)");
            contentArea.add(emptyMessage);
        } else {
            Div bookingsContainer = new Div();
            bookingsContainer.addClassName("bookings-container");
            
            for (Booking booking : filteredBookings) {
                HorizontalLayout actionButtons = createActionButtons(booking, tabLabel);
                Div card = bookingCard.create(booking, 
                    () -> bookingDetailsDialog.open(booking), 
                    actionButtons);
                bookingsContainer.add(card);
            }
            
            contentArea.add(bookingsContainer);
        }
    }
    
    // Hilfsmethode: Filtert Buchungen basierend auf dem gewählten Tab-Type
    private List<Booking> filterBookingsByTabType(String tabLabel) {
        LocalDate today = LocalDate.now();
        
        switch (tabLabel) {
            case TAB_UPCOMING:
                // Buchungen, die noch nicht begonnen haben ODER gerade laufen
                return allBookings.stream()
                    .filter(b -> b.getCheckInDate().isAfter(today) || 
                                (!b.getCheckInDate().isAfter(today) && !b.getCheckOutDate().isBefore(today)))
                    .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                    .collect(Collectors.toList());
            case TAB_PAST:
                return allBookings.stream()
                    .filter(b -> b.getCheckOutDate().isBefore(today))
                    .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                    .collect(Collectors.toList());
            case TAB_CANCELLED:
                return allBookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                    .collect(Collectors.toList());
            default:
                return new ArrayList<>();
        }
    }

    
    /**
     * Creates a "Pay" button if booking has a pending payment
     */
    private Button createPayButtonIfNeeded(Booking booking) {
        // Check if booking has a pending payment
        List<Payment> pendingPayments = paymentService.findByBookingId(booking.getId()).stream()
                .filter(p -> p.getStatus() == Invoice.PaymentStatus.PENDING)
                .toList();
        
        if (pendingPayments.isEmpty()) {
            return null; // No pending payment, no button needed
        }
        
        Button payBtn = new Button("Pay Now", e -> openPaymentDialog(booking));
        payBtn.addClassName("primary-button");
        return payBtn;
    }
    
    /**
     * Opens payment dialog for the booking
     */
    private void openPaymentDialog(Booking booking) {
        try {
            if (booking.getTotalPrice() == null) {
                Notification.show("Error: Total price is missing", 5000, Notification.Position.TOP_CENTER);
                return;
            }
            
            Long bookingId = booking.getId(); // Store booking ID
            PaymentDialog paymentDialog = new PaymentDialog(booking.getTotalPrice());
            
            paymentDialog.setOnPaymentSuccess(() -> {
                // Update existing PENDING payment to PAID
                updatePendingPaymentToPaid(bookingId, paymentDialog.getSelectedPaymentMethod());
                
                Notification.show("Payment completed! Thank you.", 3000, Notification.Position.TOP_CENTER);
                
                // Refresh only the current tab content to update badge status
                updateContent();
            });
            
            paymentDialog.setOnPaymentDeferred(() -> {
                System.out.println("DEBUG: Payment deferred!");
                Notification.show("Payment postponed.", 3000, Notification.Position.TOP_CENTER);
            });
            
            paymentDialog.open();
        } catch (Exception ex) {
            System.err.println("DEBUG: Error opening payment dialog: " + ex.getMessage());
            ex.printStackTrace();
            Notification.show("Error opening payment dialog", 5000, Notification.Position.TOP_CENTER);
        }
    }
    
    /**
     * Updates the PENDING payment for a booking to PAID status
     * and updates the booking status to CONFIRMED
     */
    private void updatePendingPaymentToPaid(Long bookingId, String selectedMethod) {
        try {
            // Update payment status
            List<Payment> payments = paymentService.findByBookingId(bookingId);
            Payment paidPayment = null;
            
            for (Payment p : payments) {
                if (p.getStatus() == Invoice.PaymentStatus.PENDING) {
                    p.setStatus(Invoice.PaymentStatus.PAID);
                    p.setPaidAt(LocalDateTime.now());
                    p.setMethod(paymentService.mapPaymentMethod(selectedMethod));
                    paymentService.save(p);
                    paidPayment = p;
                    System.out.println("DEBUG: Updated payment " + p.getId() + " to PAID");
                    break;
                }
            }
            
            // Load booking fresh from database and update status to CONFIRMED
            var bookingOpt = bookingService.findById(bookingId);
            if (bookingOpt.isPresent()) {
                Booking booking = bookingOpt.get();
                if (booking.getStatus() == BookingStatus.PENDING) {
                    booking.setStatus(BookingStatus.CONFIRMED);
                    bookingService.save(booking);
                    System.out.println("DEBUG: Updated booking " + booking.getId() + " status to CONFIRMED");
                    
                    // Automatisch auf COMPLETED setzen, wenn check_out_date in der Vergangenheit liegt
                    if (booking.getCheckOutDate() != null && booking.getCheckOutDate().isBefore(java.time.LocalDate.now())) {
                        booking.setStatus(BookingStatus.COMPLETED);
                        bookingService.save(booking);
                        System.out.println("DEBUG: Auto-completed booking " + booking.getId() + " (check-out was in the past)");
                    }
                    
                    // Create Invoice if not exists
                    if (booking.getInvoice() == null && paidPayment != null) {
                        invoiceService.createInvoiceForBooking(booking, paidPayment.getMethod(), Invoice.PaymentStatus.PAID);
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("DEBUG: Error updating payment/booking: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    


    // Lädt alle Buchungen des aktuellen Nutzers.
    private List<Booking> loadAllBookingsForCurrentUser() {
        User user = sessionService.getCurrentUser();
        if (user == null) {
            return List.of();
        }
        return bookingService.findAllBookingsForGuest(user.getId());
    }



    private HorizontalLayout createActionButtons(Booking booking, String tabLabel) {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);

        if (TAB_UPCOMING.equals(tabLabel)) {
            Button editBtn = new Button("Edit");
            editBtn.addClassName("primary-button");
            editBtn.addClickListener(e -> editBookingDialog.open(booking, () -> {
                allBookings = loadAllBookingsForCurrentUser();
                updateContent();
            }));

            Button cancelBtn = new Button("Cancel");
            cancelBtn.addClassName("secondary-button");
            cancelBtn.addClickListener(e -> cancellationDialog.open(booking, () -> {
                allBookings = loadAllBookingsForCurrentUser();
                updateContent();
            }));
            
            // Add Pay button if payment is pending
            Button payBtn = createPayButtonIfNeeded(booking);
            if (payBtn != null) {
                buttonsLayout.add(payBtn, editBtn, cancelBtn);
            } else {
                buttonsLayout.add(editBtn, cancelBtn);
            }
        } else if (TAB_PAST.equals(tabLabel)) {
            RouterLink reviewLink = new RouterLink("Write Review", MyReviewsView.class);
            reviewLink.addClassName("primary-button");
            buttonsLayout.add(reviewLink);
        }
        
        return buttonsLayout;
    }
}
