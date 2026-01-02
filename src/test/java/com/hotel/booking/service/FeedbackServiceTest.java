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
import com.hotel.booking.entity.Feedback;
import com.hotel.booking.entity.User;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.repository.BookingRepository;
import com.hotel.booking.repository.FeedbackRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    FeedbackRepository feedbackRepository;

    @Mock
    BookingRepository bookingRepository;

    @InjectMocks
    FeedbackService feedbackService;

    @BeforeEach
    void setUp() {
        // noop
    }

    @Test
    void findAll_delegatesToRepository() {
        when(feedbackRepository.findAll()).thenReturn(List.of(new Feedback(5, "nice")));

        List<Feedback> all = feedbackService.findAll();

        assertEquals(1, all.size());
        verify(feedbackRepository, times(1)).findAll();
    }

    @Test
    void findByGuestId_delegates() {
        when(feedbackRepository.findByGuestId(10L)).thenReturn(List.of());

        List<Feedback> res = feedbackService.findByGuestId(10L);

        assertNotNull(res);
        verify(feedbackRepository).findByGuestId(10L);
    }

    @Test
    void findByBookingId_delegates() {
        when(feedbackRepository.findByBookingId(20L)).thenReturn(List.of());

        List<Feedback> res = feedbackService.findByBookingId(20L);

        assertNotNull(res);
        verify(feedbackRepository).findByBookingId(20L);
    }

    @Test
    void findById_delegates() {
        Feedback f = new Feedback(4, "ok");
        when(feedbackRepository.findById(123L)).thenReturn(Optional.of(f));

        Optional<Feedback> opt = feedbackService.findById(123L);

        assertTrue(opt.isPresent());
        assertEquals(f, opt.get());
    }

    @Test
    void save_delegatesToRepository() {
        Feedback f = new Feedback(3, "meh");
        when(feedbackRepository.save(f)).thenReturn(f);

        Feedback saved = feedbackService.save(f);

        assertSame(f, saved);
        verify(feedbackRepository).save(f);
    }

    @Test
    void deleteById_nullId_doesNothing() {
        feedbackService.deleteById(null);

        verifyNoInteractions(feedbackRepository);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void deleteById_notFound_doesNothing() {
        when(feedbackRepository.findById(5L)).thenReturn(Optional.empty());

        feedbackService.deleteById(5L);

        verify(feedbackRepository).findById(5L);
        verify(feedbackRepository, never()).delete(any());
    }

    @Test
    void deleteById_existingFeedback_detachesAndDeletes() {
        // create real entities and wire them
        User guest = new User("u1","First","Last", null, "g@example.com", "pw", null, true);
        RoomCategory rc = new RoomCategory();
        Booking booking = new Booking("B-1", LocalDate.now(), LocalDate.now().plusDays(1), BookingStatus.PENDING, guest, rc);
        Feedback feedback = new Feedback(5, "great");
        feedback.setBooking(booking);
        booking.setFeedback(feedback);

        when(feedbackRepository.findById(7L)).thenReturn(Optional.of(feedback));

        feedbackService.deleteById(7L);

        // booking should have been updated and persisted
        verify(bookingRepository).save(booking);
        // feedback should be deleted
        verify(feedbackRepository).delete(feedback);
        assertNull(booking.getFeedback());
        assertNull(feedback.getBooking());
    }
}