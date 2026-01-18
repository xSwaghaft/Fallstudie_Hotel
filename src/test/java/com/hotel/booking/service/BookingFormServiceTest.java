package com.hotel.booking.service;

/*
 Kurze Erklärungen zu gängigen Test-Annotationen und Hilfsobjekten:
 - @Mock: Erzeugt ein Mock-Objekt, das Aufrufe aufzeichnet und Verhalten stubbed.
 - @InjectMocks: Erzeugt das zu testende Objekt und injiziert @Mock-Felder darin.
 - @BeforeEach: Diese Methode läuft vor jedem Test zur Vorbereitung (Setup).
 - @Test: Kennzeichnet eine Testmethode (JUnit 5 / Jupiter).
 - assertEquals(expected, actual): Prüft, ob erwarteter und tatsächlicher Wert gleich sind (Reihenfolge wichtig).
 - assertTrue(condition) / assertFalse(condition): Prüfen Wahrheitswerte in Tests.
 - assertNull(x) / assertNotNull(x): Prüfen, ob ein Objekt (nicht) null ist.
 - ArgumentCaptor<T>: Fängt Argumente ab, die an Mock-Methoden übergeben wurden, zum genaueren Prüfen.
 - mock(Class.class): Erstellt ein Mockito-Mock-Objekt zur Isolierung von Abhängigkeiten.
 - when(mock.method(...)).thenReturn(value): Stubbt das Rückgabeverhalten eines Mock-Objekts.
 - any(): Matcher, der jeden Wert passenden Typs akzeptiert (z.B. any(String.class)).
 - times(n) / never(): Geben an, wie oft eine Mock-Methode erwartet wird (z.B. verify(mock, times(1))).
 - doReturn()/doThrow(): Alternative Stubbing-Syntax (z. B. für void-Methoden oder Spies).
 - MimeMessage: Repräsentiert eine E-Mail (HTML/Multipart) aus dem JavaMail API.
 - verify(mock).method(...): Überprüft Aufrufe auf Mocks; oft kombiniert mit ArgumentCaptor oder Matchern.
*/

import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingFormServiceTest {

    BookingService bookingService;
    BookingExtraService bookingExtraService;
    RoomCategoryService roomCategoryService;
    UserService userService;

    BookingFormService service;

    @BeforeEach
    void setUp() {
        bookingService = mock(BookingService.class);
        bookingExtraService = mock(BookingExtraService.class);
        roomCategoryService = mock(RoomCategoryService.class);
        userService = mock(UserService.class);

        // Note: constructor ordering of BookingFormService requires bookingService, bookingExtraService, RoomService (not used), roomCategoryService, userService
        service = new BookingFormService(bookingService, bookingExtraService, null, roomCategoryService, userService);
    }

    @Test
    void getAllRoomCategories_delegates() {
        RoomCategory r1 = new RoomCategory();
        RoomCategory r2 = new RoomCategory();
        List<RoomCategory> list = List.of(r1, r2);

        when(roomCategoryService.getAllRoomCategories()).thenReturn(list);

        var res = service.getAllRoomCategories();

        assertSame(list, res);
        verify(roomCategoryService, times(1)).getAllRoomCategories();
    }

    @Test
    void getAllBookingExtras_delegates() {
        BookingExtra e1 = new BookingExtra();
        BookingExtra e2 = new BookingExtra();
        List<BookingExtra> list = List.of(e1, e2);

        when(bookingExtraService.getAllBookingExtras()).thenReturn(list);

        var res = service.getAllBookingExtras();

        assertSame(list, res);
        verify(bookingExtraService, times(1)).getAllBookingExtras();
    }

    @Test
    void isRoomAvailable_delegates() {
        RoomCategory cat = new RoomCategory();
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);

        when(bookingService.isRoomAvailable(cat, start, end)).thenReturn(true);

        assertTrue(service.isRoomAvailable(cat, start, end));
        verify(bookingService, times(1)).isRoomAvailable(cat, start, end);
    }

    @Test
    void isRoomAvailable_withExclude_delegates() {
        RoomCategory cat = new RoomCategory();
        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = LocalDate.now().plusDays(4);

        when(bookingService.isRoomAvailable(cat, start, end, 5L)).thenReturn(false);

        assertFalse(service.isRoomAvailable(cat, start, end, 5L));
        verify(bookingService, times(1)).isRoomAvailable(cat, start, end, 5L);
    }

    @Test
    void findUserByEmail_and_existsByEmail_delegate() {
        User u = mock(User.class);
        when(userService.findUserByEmail("a@b.c")).thenReturn(u);
        when(userService.existsByEmail("a@b.c")).thenReturn(true);

        assertSame(u, service.findUserByEmail("a@b.c"));
        assertTrue(service.existsByEmail("a@b.c"));

        verify(userService, times(1)).findUserByEmail("a@b.c");
        verify(userService, times(1)).existsByEmail("a@b.c");
    }
}