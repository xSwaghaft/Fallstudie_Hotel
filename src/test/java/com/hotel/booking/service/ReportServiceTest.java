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

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.DisplayName;




@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    BookingService bookingService;

    @InjectMocks
    ReportService reportService;

    @Test
    @DisplayName("Total revenue sums and formats correctly")
    public void getTotalRevenueInPeriod_sumsAndFormats() {
        LocalDate from = LocalDate.now().minusDays(10);
        LocalDate to = LocalDate.now();

        Booking b1 = new Booking("B1", LocalDate.now(), LocalDate.now().plusDays(1), BookingStatus.PENDING, mock(User.class), new RoomCategory());
        b1.setTotalPrice(new BigDecimal("100.00"));
        Booking b2 = new Booking("B2", LocalDate.now(), LocalDate.now().plusDays(2), BookingStatus.PENDING, mock(User.class), new RoomCategory());
        b2.setTotalPrice(new BigDecimal("50.50"));

        when(bookingService.getAllBookingsInPeriod(from, to)).thenReturn(List.of(b1, b2));

        String result = reportService.getTotalRevenueInPeriod(from, to);
        // Normalize numeric part (accept comma or dot as decimal separator) and compare numerically
        String numeric = result.replaceAll("[^0-9,\\.]", "");
        numeric = numeric.replace(',', '.');
        java.math.BigDecimal actual = new java.math.BigDecimal(numeric);
        org.junit.jupiter.api.Assertions.assertEquals(0, actual.compareTo(new java.math.BigDecimal("150.50")));
    }

    @Test
    public void getMostPopularExtraInPeriod_returnsTop() {
        LocalDate from = LocalDate.now().minusDays(30);
        LocalDate to = LocalDate.now();

        BookingExtra breakfast = new BookingExtra(1L, "Breakfast", "Desc", 5.0);
        BookingExtra parking = new BookingExtra(2L, "Parking", "Desc", 0.0);

        Booking b1 = new Booking("B1", LocalDate.now(), LocalDate.now().plusDays(1), BookingStatus.PENDING, mock(User.class), new RoomCategory());
        b1.setExtras(Set.of(breakfast, parking));
        Booking b2 = new Booking("B2", LocalDate.now(), LocalDate.now().plusDays(1), BookingStatus.PENDING, mock(User.class), new RoomCategory());
        b2.setExtras(Set.of(breakfast));

        when(bookingService.getAllBookingsInPeriod(from, to)).thenReturn(List.of(b1, b2));

        String top = reportService.getMostPopularExtraInPeriod(from, to);
        assertEquals("Breakfast", top);
    }

    @Test
    public void getAvgStayDurationInPeriod_empty_returnsZero() {
        LocalDate from = LocalDate.now().minusDays(10);
        LocalDate to = LocalDate.now();

        when(bookingService.getAllBookingsInPeriod(from, to)).thenReturn(List.of());

        String avg = reportService.getAvgStayDurationInPeriod(from, to);
        assertEquals("0.00", avg);
    }

    @Test
    public void createTrendString_handlesComparisonZero() {
        String s1 = reportService.createTrendString(5.0, 0.0);
        assertEquals("No Records in comparison period", s1);

        String s2 = reportService.createTrendString(0.0, 0.0);
        assertEquals("0% from last period", s2);
    }
}