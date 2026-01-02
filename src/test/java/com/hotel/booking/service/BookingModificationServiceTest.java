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
import com.hotel.booking.entity.BookingModification;
import com.hotel.booking.entity.User;
import com.hotel.booking.repository.BookingModificationRepository;
import com.hotel.booking.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingModificationServiceTest {

    BookingModificationRepository modificationRepository;
    EmailService emailService;
    PaymentService paymentService;
    BookingModificationService service;

    @BeforeEach
    void setUp() {
        modificationRepository = mock(BookingModificationRepository.class);
        emailService = mock(EmailService.class);
        paymentService = mock(PaymentService.class);
        service = new BookingModificationService(modificationRepository, emailService, paymentService);

        when(modificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void recordChanges_detectsFieldChanges_and_sendsEmail() throws Exception {
        Booking before = mock(Booking.class);
        Booking after = mock(Booking.class);
        User user = mock(User.class);

        when(before.getCheckInDate()).thenReturn(LocalDate.now().plusDays(1));
        when(after.getCheckInDate()).thenReturn(LocalDate.now().plusDays(2));

        when(before.getCheckOutDate()).thenReturn(LocalDate.now().plusDays(4));
        when(after.getCheckOutDate()).thenReturn(LocalDate.now().plusDays(5));

        when(before.getAmount()).thenReturn(2);
        when(after.getAmount()).thenReturn(3);

        when(before.getTotalPrice()).thenReturn(null);
        when(after.getTotalPrice()).thenReturn(java.math.BigDecimal.valueOf(150));

        BookingExtra be1 = mock(BookingExtra.class);
        BookingExtra be2 = mock(BookingExtra.class);
        when(be1.getName()).thenReturn("Breakfast");
        when(be2.getName()).thenReturn("Spa");
        when(before.getExtras()).thenReturn(Set.of(be1));
        when(after.getExtras()).thenReturn(Set.of(be2));

        when(before.getGuest()).thenReturn(user);
        when(user.getEmail()).thenReturn("x@y.z");

        service.recordChanges(before, after, user, "reason");

        // capture saved modifications
        ArgumentCaptor<BookingModification> captor = ArgumentCaptor.forClass(BookingModification.class);
        verify(modificationRepository, atLeastOnce()).save(captor.capture());

        List<BookingModification> saved = captor.getAllValues();
        assertTrue(saved.size() >= 1);

        // Expect at least checkInDate/change or extras etc
        boolean hasExtrasChange = saved.stream().anyMatch(m -> "extras".equals(m.getFieldChanged()));
        assertTrue(hasExtrasChange);

        verify(emailService, times(1)).sendBookingModification(eq(before), any(BookingModification.class));
    }

    @Test
    void recordChanges_noEmail_when_guestMissingEmail() throws Exception {
        Booking before = mock(Booking.class);
        Booking after = mock(Booking.class);
        User user = mock(User.class);

        when(before.getCheckInDate()).thenReturn(LocalDate.now().plusDays(1));
        when(after.getCheckInDate()).thenReturn(LocalDate.now().plusDays(2));

        when(before.getGuest()).thenReturn(user);
        when(user.getEmail()).thenReturn(null);

        service.recordChanges(before, after, user, null);

        verify(modificationRepository, atLeastOnce()).save(any());
        verify(emailService, never()).sendBookingModification(any(), any());
    }

    @Test
    void recordChangesFromSnapshot_saves_when_different() throws Exception {
        Booking entity = mock(Booking.class);
        Booking after = mock(Booking.class);
        User user = mock(User.class);

        when(after.getCheckInDate()).thenReturn(LocalDate.now().plusDays(10));
        when(after.getCheckOutDate()).thenReturn(LocalDate.now().plusDays(12));
        when(after.getAmount()).thenReturn(2);
        when(after.getTotalPrice()).thenReturn(java.math.BigDecimal.valueOf(200));

        BookingExtra be = mock(BookingExtra.class);
        when(be.getName()).thenReturn("Parken");
        when(after.getExtras()).thenReturn(Set.of(be));

        when(entity.getGuest()).thenReturn(user);
        when(user.getEmail()).thenReturn("a@b.c");

        service.recordChangesFromSnapshot(entity, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), 1, java.math.BigDecimal.valueOf(100), Set.of(), after, user, "r");

        verify(modificationRepository, atLeastOnce()).save(any());
        verify(emailService, times(1)).sendBookingModification(eq(entity), any(BookingModification.class));
    }

    @Test
    void findByBookingId_returns_sorted() throws Exception {
        BookingModification m1 = new BookingModification();
        BookingModification m2 = new BookingModification();
        m1.setModifiedAt(LocalDateTime.now().minusDays(1));
        m2.setModifiedAt(LocalDateTime.now());

        when(modificationRepository.findByBookingId(5L)).thenReturn(new java.util.ArrayList<>(List.of(m1, m2)));

        List<BookingModification> res = service.findByBookingId(5L);

        assertEquals(2, res.size());
        // first element should be the newest
        assertTrue(res.get(0).getModifiedAt().isAfter(res.get(1).getModifiedAt()) || res.get(0).getModifiedAt().isEqual(res.get(1).getModifiedAt()));
    }

    @Test
    void getAll_delegates() throws Exception {
        when(modificationRepository.findAll()).thenReturn(List.of());
        assertNotNull(service.getAll());
        verify(modificationRepository, times(1)).findAll();
    }
}