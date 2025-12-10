package com.hotel.booking.view;

import java.time.LocalDate;

import com.hotel.booking.service.BookingExtraService;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.RoomCategoryService;
import com.hotel.booking.view.components.RoomGrid;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Test Kalender")
@Route(value = "testViewcalender", layout = MainLayout.class)
@CssImport("./themes/hotel/styles.css")
@CssImport("./themes/hotel/guest.css")
public class testViewcalender extends HorizontalLayout {

    private final RoomCategoryService roomCategoryService;
    private final BookingExtraService bookingExtraService;
    private final BookingService bookingService;

    public testViewcalender(RoomCategoryService roomCategoryService,
                            BookingService bookingService,
                            BookingExtraService bookingExtraService) {

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        this.roomCategoryService = roomCategoryService;
        this.bookingExtraService = bookingExtraService;
        this.bookingService = bookingService;

        DatePicker datePicker = new DatePicker("Kalender");
        datePicker.setWidth("320px");

        RoomGrid rg = new RoomGrid(null, bookingService, roomCategoryService, bookingExtraService);

        rg.setRooms(
            bookingService.availableRoomsSearch(
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                2,
                "All Types"
            )
        );

        add(datePicker, rg);

        datePicker.getElement().addAttachListener(e ->
            datePicker.getElement().executeJs("""
                    const picker = this;
                    picker.isDateDisabled = (date) => {
                        return date.day >= 10 && date.day <= 16;
                    };
                """)
        );
    }
}
