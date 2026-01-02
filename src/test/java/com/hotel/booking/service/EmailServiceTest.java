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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Properties;
import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.lang.reflect.Field;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingCancellation;
import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.User;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailServiceTest {

    @Mock
    JavaMailSender mailSender;

    @Mock
    InvoicePdfService invoicePdfService;

    @Mock
    BookingModificationService modificationService;

    @InjectMocks
    EmailService emailService;

    @BeforeEach
    void setUp() throws Exception {
        // ensure defaultFrom is set (value injection not performed by Mockito)
        Field f = EmailService.class.getDeclaredField("defaultFrom");
        f.setAccessible(true);
        f.set(emailService, "no-reply@example.com");
    }

    @Test
    void sendSimpleMessage_sendsSimpleMail() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendSimpleMessage("a@b.com", "subj", "hello");

        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertEquals("a@b.com", sent.getTo()[0]);
        assertEquals("subj", sent.getSubject());
        assertEquals("hello", sent.getText());
    }

    @Test
    void sendHtmlMessage_createsAndSendsMimeMessage() throws Exception {
        MimeMessage mime = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mime);

        emailService.sendHtmlMessage("x@y.com", "hi", "<b>bold</b>");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
        assertEquals("hi", mime.getSubject());
        String content = (String) mime.getContent();
        assertTrue(content.contains("<b>bold</b>") || content.contains("bold"));
    }

    @Test
    void sendHtmlMessageWithAttachment_sendsMimeWithAttachment() throws Exception {
        MimeMessage mime = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mime);

        byte[] payload = "pdf-content".getBytes();
        emailService.sendHtmlMessageWithAttachment("c@d.com", "inv", "<p>here</p>", payload, "inv.pdf", "application/pdf");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
        assertEquals("inv", mime.getSubject());
    }

    @Test
    void sendBookingConfirmation_noEmail_doesNothing() throws Exception {
        Booking booking = mock(Booking.class);
        when(booking.getGuest()).thenReturn(null);

        emailService.sendBookingConfirmation(booking);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendBookingConfirmation_withEmail_sendsHtml() throws Exception {
        MimeMessage mime = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mime);

        Booking booking = mock(Booking.class);
        User guest = mock(User.class);
        when(guest.getEmail()).thenReturn("user@example.com");
        when(booking.getGuest()).thenReturn(guest);
        when(booking.getBookingNumber()).thenReturn("B123");
        when(booking.getTotalPrice()).thenReturn(null);
        when(booking.getExtras()).thenReturn(null);

        emailService.sendBookingConfirmation(booking);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
        assertEquals("Booking Confirmation - B123", mime.getSubject());
    }

    @Test
    void sendInvoiceCreated_attachesPdfAndSends() throws Exception {
        MimeMessage mime = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mime);

        Invoice invoice = mock(Invoice.class);
        Booking booking = mock(Booking.class);
        User guest = mock(User.class);
        when(guest.getEmail()).thenReturn("inv@example.com");
        when(booking.getGuest()).thenReturn(guest);
        when(invoice.getBooking()).thenReturn(booking);
        when(invoice.getInvoiceNumber()).thenReturn("INV-1");
        when(invoicePdfService.generateInvoicePdf(invoice)).thenReturn("pdf".getBytes());

        emailService.sendInvoiceCreated(invoice);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
        assertEquals("Invoice - INV-1", mime.getSubject());
    }

    @Test
    void sendBookingCancellation_noCancellation_doesNothing() throws Exception {
        Booking booking = mock(Booking.class);
        when(booking.getGuest()).thenReturn(null);

        emailService.sendBookingCancellation(booking, null);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendBookingCancellation_withCancellation_sends() throws Exception {
        MimeMessage mime = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mime);

        Booking booking = mock(Booking.class);
        User guest = mock(User.class);
        when(guest.getEmail()).thenReturn("c@example.com");
        when(booking.getGuest()).thenReturn(guest);
        when(booking.getBookingNumber()).thenReturn("B-CAN-1");

        BookingCancellation cancellation = mock(BookingCancellation.class);
        when(cancellation.getCancellationFee()).thenReturn(BigDecimal.ZERO);
        when(cancellation.getRefundedAmount()).thenReturn(BigDecimal.ZERO);
        when(cancellation.getReason()).thenReturn("nope");

        emailService.sendBookingCancellation(booking, cancellation);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
        assertEquals("Booking Cancelled - B-CAN-1", mime.getSubject());
    }
}
