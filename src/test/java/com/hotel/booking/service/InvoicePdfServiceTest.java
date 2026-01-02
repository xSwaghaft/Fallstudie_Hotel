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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.User;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class InvoicePdfServiceTest {

    @Mock
    com.hotel.booking.repository.BookingCancellationRepository bookingCancellationRepository;

    @InjectMocks
    InvoicePdfService service;

    @Test
    void generateInvoicePdf_basicInvoice_generatesBytes() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-100");
        invoice.setAmount(BigDecimal.valueOf(199.95));
        invoice.setIssuedAt(LocalDateTime.now());

        Booking booking = mock(Booking.class);
        User guest = mock(User.class);
        when(guest.getFirstName()).thenReturn("Max");
        when(guest.getLastName()).thenReturn("Mustermann");
        when(guest.getEmail()).thenReturn("max@example.com");

        when(booking.getGuest()).thenReturn(guest);
        when(booking.getBookingNumber()).thenReturn("B100");
        when(booking.getCheckInDate()).thenReturn(LocalDate.now());
        when(booking.getCheckOutDate()).thenReturn(LocalDate.now().plusDays(1));
        when(booking.getTotalPrice()).thenReturn(BigDecimal.valueOf(199.95));
        when(booking.getExtras()).thenReturn(null);

        invoice.setBooking(booking);

        byte[] pdf = service.generateInvoicePdf(invoice);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0, "Generated PDF should not be empty");
    }

    @Test
    void generateInvoicePdf_refunded_includesRefundLookup() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-200");
        invoice.setAmount(BigDecimal.valueOf(50));
        invoice.setIssuedAt(LocalDateTime.now());
        invoice.setInvoiceStatus(Invoice.PaymentStatus.REFUNDED);

        Booking booking = mock(Booking.class);
        when(booking.getId()).thenReturn(55L);
        when(booking.getBookingNumber()).thenReturn("B-REF");
        when(booking.getTotalPrice()).thenReturn(BigDecimal.valueOf(50));
        invoice.setBooking(booking);

        com.hotel.booking.entity.BookingCancellation cancellation = mock(com.hotel.booking.entity.BookingCancellation.class);
        when(cancellation.getRefundedAmount()).thenReturn(BigDecimal.valueOf(10.00));

        when(bookingCancellationRepository.findTopByBookingIdOrderByCancelledAtDesc(55L)).thenReturn(Optional.of(cancellation));

        byte[] pdf = service.generateInvoicePdf(invoice);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        verify(bookingCancellationRepository).findTopByBookingIdOrderByCancelledAtDesc(55L);
    }

    @Test
    void generateInvoicePdf_missingBooking_doesNotThrow() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-300");
        invoice.setAmount(BigDecimal.valueOf(0));
        invoice.setIssuedAt(LocalDateTime.now());

        // no booking set

        byte[] pdf = service.generateInvoicePdf(invoice);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}
