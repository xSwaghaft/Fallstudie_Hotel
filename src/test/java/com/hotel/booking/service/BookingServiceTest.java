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

import com.hotel.booking.entity.*;
import com.hotel.booking.repository.BookingRepository;
import com.hotel.booking.repository.RoomCategoryRepository;
import com.hotel.booking.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    BookingRepository bookingRepository;
    RoomRepository roomRepository;
    RoomCategoryRepository roomCategoryRepository;
    EmailService emailService;
    BookingModificationService modificationService;

    BookingService service;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        roomRepository = mock(RoomRepository.class);
        roomCategoryRepository = mock(RoomCategoryRepository.class);
        emailService = mock(EmailService.class);
        modificationService = mock(BookingModificationService.class);

        service = new BookingService(bookingRepository, roomRepository, roomCategoryRepository, emailService, modificationService);

        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void save_newBooking_assignsRoom_and_sendsEmail() throws Exception {
        Booking booking = mock(Booking.class);
        when(booking.getId()).thenReturn(null);
        when(booking.getRoom()).thenReturn(null);
        RoomCategory cat = mock(RoomCategory.class);
        when(booking.getRoomCategory()).thenReturn(cat);
        when(booking.getCheckInDate()).thenReturn(LocalDate.now().plusDays(1));
        when(booking.getCheckOutDate()).thenReturn(LocalDate.now().plusDays(3));

        Room room = mock(Room.class);
        when(room.getId()).thenReturn(11L);
        when(roomRepository.findByCategory(cat)).thenReturn(List.of(room));

        when(bookingRepository.existsByRoom_IdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualAndStatusNot(
            eq(11L), any(LocalDate.class), any(LocalDate.class), eq(BookingStatus.CANCELLED))).thenReturn(false);

        // Make the booking mock reflect setRoom(...) by returning the passed value from getRoom()
        doAnswer(inv -> {
            Room assigned = inv.getArgument(0);
            when(booking.getRoom()).thenReturn(assigned);
            return null;
        }).when(booking).setRoom(any(Room.class));

        User guest = mock(User.class);
        when(booking.getGuest()).thenReturn(guest);
        when(guest.getEmail()).thenReturn("guest@example.com");

        Booking saved = service.save(booking);

        verify(bookingRepository, times(1)).save(booking);
        verify(emailService, times(1)).sendBookingConfirmation(saved);
    }

    @Test
    void calculateBookingPrice_accounts_for_room_and_extras() throws Exception {
        Booking booking = mock(Booking.class);
        RoomCategory cat = mock(RoomCategory.class);
        when(booking.getCheckInDate()).thenReturn(LocalDate.now());
        when(booking.getCheckOutDate()).thenReturn(LocalDate.now().plusDays(2));
        when(booking.getRoomCategory()).thenReturn(cat);
        when(cat.getPricePerNight()).thenReturn(new BigDecimal("50.00"));
        when(booking.getAmount()).thenReturn(2);

        BookingExtra extra = mock(BookingExtra.class);
        when(extra.getPrice()).thenReturn(10.0);
        when(extra.isPerPerson()).thenReturn(true);
        when(booking.getExtras()).thenReturn(java.util.Set.of(extra));

        service.calculateBookingPrice(booking);

        ArgumentCaptor<BigDecimal> captor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(booking, times(1)).setTotalPrice(captor.capture());
        BigDecimal total = captor.getValue();
        // nights = 2 -> room price 50 *2 =100; extras per person 10 *2 persons =20; total = 120.00
        assertEquals(new BigDecimal("120.00"), total);
    }

    @Test
    void isRoomAvailable_true_and_false() throws Exception {
        RoomCategory cat = mock(RoomCategory.class);
        Room r1 = mock(Room.class);
        when(r1.getId()).thenReturn(1L);
        Room r2 = mock(Room.class);
        when(r2.getId()).thenReturn(2L);

        when(roomRepository.findByCategory(cat)).thenReturn(List.of(r1, r2));

        // first room overlaps, second is free
        when(bookingRepository.existsByRoom_IdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualAndStatusNot(eq(1L), any(), any(), eq(BookingStatus.CANCELLED))).thenReturn(true);
        when(bookingRepository.existsByRoom_IdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualAndStatusNot(eq(2L), any(), any(), eq(BookingStatus.CANCELLED))).thenReturn(false);

        boolean avail = service.isRoomAvailable(cat, LocalDate.now(), LocalDate.now().plusDays(1));
        assertTrue(avail);

        // if both overlap -> false
        when(bookingRepository.existsByRoom_IdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualAndStatusNot(eq(2L), any(), any(), eq(BookingStatus.CANCELLED))).thenReturn(true);
        boolean avail2 = service.isRoomAvailable(cat, LocalDate.now(), LocalDate.now().plusDays(1));
        assertFalse(avail2);
    }

    @Test
    void findAndDelegation_methods() throws Exception {
        Booking b = mock(Booking.class);
        when(bookingRepository.findById(5L)).thenReturn(java.util.Optional.of(b));
        when(bookingRepository.findByBookingNumber("BN123")).thenReturn(java.util.Optional.of(b));
        when(bookingRepository.findAll()).thenReturn(List.of(b));

        assertTrue(service.findById(5L).isPresent());
        assertTrue(service.findByBookingNumber("BN123").isPresent());
        assertEquals(1, service.findAll().size());
    }

    @Test
    void getAverageRatingForCategory_computes_average() throws Exception {
        RoomCategory cat = mock(RoomCategory.class);
        when(cat.getCategory_id()).thenReturn(7L);

        Booking b1 = mock(Booking.class);
        Booking b2 = mock(Booking.class);
        Feedback f1 = mock(Feedback.class);
        Feedback f2 = mock(Feedback.class);
        when(f1.getRating()).thenReturn(4);
        when(f2.getRating()).thenReturn(2);
        when(b1.getFeedback()).thenReturn(f1);
        when(b2.getFeedback()).thenReturn(f2);

        when(bookingRepository.findByRoomCategoryId(7L)).thenReturn(List.of(b1, b2));

        double avg = service.getAverageRatingForCategory(cat);
        assertEquals(3.0, avg);
    }
}
