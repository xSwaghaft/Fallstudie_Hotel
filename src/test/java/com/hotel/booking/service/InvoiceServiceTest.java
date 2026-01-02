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


import java.util.List;
import java.util.Optional;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.User;
import com.hotel.booking.repository.InvoiceRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    InvoiceRepository invoiceRepository;

    @Mock
    EmailService emailService;

    @InjectMocks
    InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void findAll_delegates() {
        when(invoiceRepository.findAll()).thenReturn(List.of(new Invoice()));

        var res = invoiceService.findAll();

        assertEquals(1, res.size());
        verify(invoiceRepository).findAll();
    }

    @Test
    void findById_delegates() {
        Invoice inv = new Invoice();
        when(invoiceRepository.findById(5L)).thenReturn(Optional.of(inv));

        Optional<Invoice> opt = invoiceService.findById(5L);

        assertTrue(opt.isPresent());
        assertSame(inv, opt.get());
    }

    @Test
    void findByInvoiceNumber_delegates() {
        Invoice inv = new Invoice();
        when(invoiceRepository.findByInvoiceNumber("X")).thenReturn(Optional.of(inv));

        Optional<Invoice> opt = invoiceService.findByInvoiceNumber("X");

        assertTrue(opt.isPresent());
    }

    @Test
    void findByBookingId_null_returnsEmpty() {
        Optional<Invoice> opt = invoiceService.findByBookingId(null);
        assertTrue(opt.isEmpty());
    }

    @Test
    void save_newInvoice_sendsEmail_whenGuestHasEmail() throws Exception {
        Invoice inv = new Invoice();
        Booking booking = mock(Booking.class);
        User guest = mock(User.class);
        when(guest.getEmail()).thenReturn("g@example.com");
        when(booking.getGuest()).thenReturn(guest);
        inv.setBooking(booking);

        when(invoiceRepository.save(inv)).thenReturn(inv);
        doNothing().when(emailService).sendInvoiceCreated(inv);

        Invoice saved = invoiceService.save(inv);

        assertSame(inv, saved);
        verify(invoiceRepository).save(inv);
        verify(emailService).sendInvoiceCreated(inv);
    }

    @Test
    void save_existingInvoice_doesNotSendEmail() throws Exception {
        Invoice inv = new Invoice();
        inv.setId(99L);
        when(invoiceRepository.save(inv)).thenReturn(inv);

        Invoice saved = invoiceService.save(inv);

        verify(emailService, never()).sendInvoiceCreated(any());
        assertSame(inv, saved);
    }

    @Test
    void deleteById_delegates() {
        invoiceService.deleteById(7L);
        verify(invoiceRepository).deleteById(7L);
    }

    @Test
    void getNumberOfPendingInvoices_returnsCount() {
        when(invoiceRepository.findByInvoiceStatus(Invoice.PaymentStatus.PENDING)).thenReturn(List.of(new Invoice(), new Invoice()));
        int count = invoiceService.getNumberOfPendingInvoices();
        assertEquals(2, count);
    }
}