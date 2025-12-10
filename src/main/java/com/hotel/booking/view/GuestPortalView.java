package com.hotel.booking.view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;

import com.hotel.booking.entity.Room;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.RoomCategoryService;
import com.hotel.booking.view.components.RoomGrid;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

// @Route: registriert das Gäste-Portal unter /guest-portal im MainLayout.
// @CssImport: lädt globale und Guest-spezifische Styles.
@Route(value = "guest-portal", layout = MainLayout.class)
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/guest.css")
public class GuestPortalView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private final BookingService bookingService;
    private final RoomCategoryService roomCategoryService;
    private final RoomGrid roomGrid;

    private DatePicker checkIn;
    private DatePicker checkOut;
    private NumberField guests;
    private Select<String> type;

    @Autowired
    public GuestPortalView(SessionService sessionService,
                           BookingService bookingService,
                           RoomCategoryService roomCategoryService,
                           RoomGrid roomGrid) {

        this.sessionService = sessionService;
        this.bookingService = bookingService;
        this.roomCategoryService = roomCategoryService;
        this.roomGrid = roomGrid;

        setSpacing(true);
        setPadding(true);
        setSizeFull();
        setWidthFull();
        getStyle().set("overflow-x", "hidden");

        roomGrid.setWidthFull();
        roomGrid.setDefaultBookingHandler();
        add(new H1("Zimmer suchen"), createSearchCard(), roomGrid);
    }

    // Baut die Suchkarte mit Datum-, Gäste- und Typ-Filtern.
    private Div createSearchCard() {
        Div card = new Div();
        card.addClassName("guest-search-card");
        
        checkIn = new DatePicker("Check-in");
        checkIn.setMin(LocalDate.now());
        checkIn.setValue(LocalDate.now().plusDays(2));

        checkOut = new DatePicker("Check-out");
        checkOut.setValue(LocalDate.now().plusDays(5));
        checkOut.setMin(checkIn.getValue().plusDays(1));

        guests = new NumberField("Gäste");
        guests.setValue(2d);

        type = new Select<>();
        type.setLabel("Zimmertyp");
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

        Button searchBtn = new Button("Suchen");
        searchBtn.addClassName("primary-button");
        searchBtn.addClickListener(e -> executeSearch());

        HorizontalLayout formLayout = new HorizontalLayout(checkIn, checkOut, guests, type, searchBtn);
        formLayout.addClassName("guest-search-form");
        formLayout.setWidthFull();
        formLayout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);
        formLayout.setFlexGrow(1, checkIn);
        formLayout.setFlexGrow(1, checkOut);
        formLayout.setFlexGrow(1, guests);
        formLayout.setFlexGrow(1, type);
        
        card.add(formLayout);
        return card;
    }

    // Führt die Suche aus und befüllt das RoomGrid mit verfügbaren Zimmern.
    private void executeSearch() {
        LocalDate in = checkIn.getValue();
        LocalDate out = checkOut.getValue();
        Double guestsValue = guests.getValue();
        String typeValue = type.getValue();

        if (in == null || out == null || guestsValue == null) {
            // Keine vollständigen Filter -> leere Liste ohne Handler
            roomGrid.setRooms(new ArrayList<>(), null, null, null);
            return;
        }

        List<Room> rooms = bookingService.availableRoomsSearch(
            in, out, guestsValue.intValue(),
            typeValue != null && !"All Types".equals(typeValue) ? typeValue : "All Types"
        );

        if (rooms.isEmpty()) {
            Notification.show("Keine Zimmer im gewählten Zeitraum verfügbar.");
        }

        // Übergibt Räume mit Datum an den aktiven (Default-)Handler
        roomGrid.setRooms(rooms, in, out, null);
    }

    // Zugriffsschutz: nur eingeloggte Gäste dürfen das Portal sehen.
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasRole(UserRole.GUEST)) {
            event.rerouteTo(LoginView.class);
        }
    }
}
