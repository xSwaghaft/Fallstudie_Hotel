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
import com.hotel.booking.entity.BookingCancellation;
import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.Payment;
import com.hotel.booking.entity.User;
import com.hotel.booking.repository.BookingCancellationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingCancellationServiceTest {

    BookingCancellationRepository cancellationRepository;
    BookingService bookingService;
    PaymentService paymentService;
    InvoiceService invoiceService;
    EmailService emailService;

    BookingCancellationService service;

    @BeforeEach
    void setUp() {
        cancellationRepository = mock(BookingCancellationRepository.class);
        bookingService = mock(BookingService.class);
        paymentService = mock(PaymentService.class);
        invoiceService = mock(InvoiceService.class);
        emailService = mock(EmailService.class);

        service = new BookingCancellationService(cancellationRepository, bookingService, paymentService, invoiceService, emailService);
        // By default return the passed entity when saving a cancellation to avoid nulls in service
        when(cancellationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void calculateCancellationFee_variousWindows() {
        Booking b = mock(Booking.class);
        BigDecimal total = new BigDecimal("100.00");

        // 30+ days -> 0%
        when(b.getCheckInDate()).thenReturn(LocalDate.now().plusDays(40));
        assertEquals(new BigDecimal("0.00"), service.calculateCancellationFee(b, total));

        // 7-29 days -> 20%
        when(b.getCheckInDate()).thenReturn(LocalDate.now().plusDays(10));
        assertEquals(new BigDecimal("20.00"), service.calculateCancellationFee(b, total));

        // 1-6 days -> 50%
        when(b.getCheckInDate()).thenReturn(LocalDate.now().plusDays(3));
        assertEquals(new BigDecimal("50.00"), service.calculateCancellationFee(b, total));

        // 0 days (same day) -> 100%
        when(b.getCheckInDate()).thenReturn(LocalDate.now());
        assertEquals(new BigDecimal("100.00"), service.calculateCancellationFee(b, total));
    }

    @Test
    void save_sendsEmail_and_returnsSaved() throws Exception {
        BookingCancellation bc = new BookingCancellation();
        Booking booking = mock(Booking.class);
        when(booking.getId()).thenReturn(13L);
        User guest = mock(User.class);
        // mock booking.getGuest() and guest.getEmail() so the service will attempt to send the email
        when(booking.getGuest()).thenReturn(guest);
        when(guest.getEmail()).thenReturn("test@example.com");
        bc.setBooking(booking);
        bc.setCancelledAt(LocalDateTime.now());

        when(cancellationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BookingCancellation saved = service.save(bc);

        assertNotNull(saved);
        verify(cancellationRepository, times(1)).save(bc);
        // emailService should be called (guest email may be null but method handles it)
        verify(emailService, times(1)).sendBookingCancellation(eq(booking), eq(bc));
    }

    @Test
    void processCancellation_updatesBookingPaymentAndInvoice() {
        Booking booking = mock(Booking.class);
        when(booking.getId()).thenReturn(100L);

        BookingCancellation bc = new BookingCancellation();
        bc.setBooking(booking);
        bc.setCancelledAt(LocalDateTime.now());

        // Mock payments: one paid payment of 100
        Payment p = mock(Payment.class);
        when(p.getStatus()).thenReturn(Invoice.PaymentStatus.PAID);
        when(p.getAmount()).thenReturn(new BigDecimal("100.00"));

        when(paymentService.findByBookingId(100L)).thenReturn(List.of(p));

        // Mock invoice
        Invoice inv = mock(Invoice.class);
        when(inv.getAmount()).thenReturn(new BigDecimal("100.00"));
        when(invoiceService.findByBookingId(100L)).thenReturn(Optional.of(inv));

        // Run with refundedAmount = 50
        service.processCancellation(booking, bc, new BigDecimal("50.00"));

        // Booking should be saved as CANCELLED
        verify(bookingService, times(1)).save(booking);

        // Cancellation should be saved
        verify(cancellationRepository, times(1)).save(bc);

        // Payment should be saved and partially refunded
        verify(paymentService, times(1)).save(p);
        verify(p, times(1)).setStatus(Invoice.PaymentStatus.PARTIAL);
        verify(p, times(1)).setRefundedAmount(new BigDecimal("50.00"));

        // Invoice saved and partial
        verify(invoiceService, times(1)).save(inv);
        verify(inv, times(1)).setInvoiceStatus(Invoice.PaymentStatus.PARTIAL);
    }
}
