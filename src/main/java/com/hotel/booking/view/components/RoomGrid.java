package com.hotel.booking.view.components;

import java.time.LocalDate;
import java.util.List;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.Room;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.RoomImage;
import com.hotel.booking.entity.User;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.BookingFormService;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.RoomCategoryService;
import com.hotel.booking.view.createNewBookingForm;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

import org.springframework.beans.factory.annotation.Autowired;

// @SpringComponent: macht diese Vaadin-Komponente als Spring-Bean verfügbar.
// @UIScope: Bean lebt pro UI-Instanz (Thread-sicher für Vaadin-UI-Scope).
@SpringComponent
@UIScope
public class RoomGrid extends Div {

    private final SessionService sessionService;
    private final BookingService bookingService;
    private final BookingFormService bookingFormService;
    private final RoomCategoryService roomCategoryService;
    
    private RoomActionHandler actionHandler;

    @Autowired
    // Konstruktor: setzt Services und Grundstyling des Grids.
    public RoomGrid(SessionService sessionService,
                    BookingService bookingService,
                    BookingFormService bookingFormService,
                    RoomCategoryService roomCategoryService) {
        this.sessionService = sessionService;
        this.bookingService = bookingService;
        this.bookingFormService = bookingFormService;
        this.roomCategoryService = roomCategoryService;
        addClassName("room-grid");
        setWidthFull();
    }
    
    // Schnittstelle für konfigurierbare Aktionen (z. B. Buchen, Bearbeiten).
    public interface RoomActionHandler {
        void handleRoomAction(Room room, LocalDate checkIn, LocalDate checkOut);
        String getButtonText();
    }


    
    // Zentrales Setter: rendert Karten, optional mit Handler (ansonsten Default).
    public void setRooms(List<Room> rooms, LocalDate checkIn, LocalDate checkOut, RoomActionHandler handler) {
        removeAll();
        RoomActionHandler activeHandler = handler != null ? handler : this.actionHandler;

        if (rooms == null || rooms.isEmpty()) {
            add(new Paragraph("Keine Räume verfügbar"));
            return;
        }

        for (Room room : rooms) {
            List<RoomImage> roomImages = roomCategoryService.getAllRoomImages(room);
            RoomCard roomCard = new RoomCard(room, roomImages);
            
            if (activeHandler != null) {
                Button actionBtn = new Button(activeHandler.getButtonText());
                LocalDate finalCheckIn = checkIn;
                LocalDate finalCheckOut = checkOut;
                actionBtn.addClickListener(e -> activeHandler.handleRoomAction(room, finalCheckIn, finalCheckOut));
                roomCard.setBookButton(actionBtn);
            }
            
            add(roomCard);
        }
    }

    // Standard-Handler für Gäste: öffnet Buchungsdialog mit Button "Buchen".
    public void setDefaultBookingHandler() {
        this.actionHandler = new RoomActionHandler() {
            @Override
            public void handleRoomAction(Room room, LocalDate checkIn, LocalDate checkOut) {
                openBookingDialog(room, checkIn, checkOut);
            }
            
            @Override
            public String getButtonText() {
                return "Buchen";
            }
        };
    }
    
    // Öffnet den Dialog zum Buchen eines Zimmers inklusive Formular und Preisinfo.
    private void openBookingDialog(Room room, LocalDate checkIn, LocalDate checkOut) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Buchung bestätigen");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        String roomName = (room.getCategory() != null ? room.getCategory().getName() : "Room") 
                + " #" + room.getRoomNumber();

        LocalDate in = checkIn != null ? checkIn : LocalDate.now();
        LocalDate out = (checkOut != null && checkOut.isAfter(in)) ? checkOut : in.plusDays(1);

        content.add(new Paragraph("Zimmer: " + roomName));
        String priceText = room.getCategory() != null && room.getCategory().getPricePerNight() != null
                ? "€" + room.getCategory().getPricePerNight() + " pro Nacht"
                : "Preis nicht verfügbar";
        content.add(new Paragraph("Preis: " + priceText));
        content.add(new Paragraph("Zeitraum: " + in + " bis " + out));

        User currentUser = sessionService.getCurrentUser();

        // Erstelle Formular mit direkt übergebenen Werten (category, checkIn, checkOut)
        createNewBookingForm bookingForm = new createNewBookingForm(
                currentUser, sessionService, null, bookingFormService,
                room.getCategory(), in, out
        );

        Booking formBooking = bookingForm.getBooking();
        formBooking.setRoom(room);
        Integer max = room.getCategory() != null ? room.getCategory().getMaxOccupancy() : null;
        int defaultGuests = 1;
        if (max != null && max > 0) {
            defaultGuests = Math.min(defaultGuests, max);
        }
        formBooking.setAmount(defaultGuests);

        content.add(bookingForm);

        Button confirm = new Button("Buchen");
        confirm.addClickListener(e -> {
            try {
                bookingForm.writeBean();
                Booking booking = bookingForm.getBooking();
                RoomCategory selectedCategory = booking.getRoomCategory();
                Integer maxAllowed = selectedCategory != null ? selectedCategory.getMaxOccupancy() : null;
                Integer amount = booking.getAmount();
                if (maxAllowed != null && amount != null && amount > maxAllowed) {
                    booking.setAmount(maxAllowed);
                    Notification.show("Maximale Belegung: " + maxAllowed + " Personen. Wert wurde angepasst.");
                }
                booking.setRoom(room);
                bookingService.save(booking);
                Notification.show("Buchung erfolgreich!");
                dialog.close();
            } catch (ValidationException ex) {
                Notification.show("Bitte Eingaben prüfen.");
            }
        });

        Button cancel = new Button("Abbrechen", event -> dialog.close());

        dialog.add(content);
        dialog.getFooter().add(new HorizontalLayout(confirm, cancel));
        dialog.open();
    }

}
