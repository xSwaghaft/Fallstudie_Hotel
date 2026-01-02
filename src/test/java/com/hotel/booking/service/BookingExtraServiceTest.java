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
import com.hotel.booking.repository.BookingExtraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingExtraServiceTest {

    BookingExtraRepository repository;
    BookingExtraService service;

    @BeforeEach
    void setUp() {
        repository = mock(BookingExtraRepository.class);
        service = new BookingExtraService(repository);
    }

    @Test
    void getAllBookingExtras_returnsList() {
        BookingExtra e1 = new BookingExtra();
        BookingExtra e2 = new BookingExtra();
        List<BookingExtra> list = List.of(e1, e2);

        when(repository.findAll()).thenReturn(list);

        var result = service.getAllBookingExtras();

        assertEquals(2, result.size());
        assertSame(list, result);
        verify(repository, times(1)).findAll();
    }

    @Test
    void getBookingExtraById_returnsOptional() {
        BookingExtra extra = new BookingExtra();
        when(repository.findById(5L)).thenReturn(Optional.of(extra));

        Optional<BookingExtra> opt = service.getBookingExtraById(5L);

        assertTrue(opt.isPresent());
        assertSame(extra, opt.get());
        verify(repository, times(1)).findById(5L);
    }

    @Test
    void saveBookingExtra_callsRepository() {
        BookingExtra extra = new BookingExtra();
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BookingExtra saved = service.saveBookingExtra(extra);

        assertNotNull(saved);
        verify(repository, times(1)).save(extra);
    }

    @Test
    void deleteBookingExtra_validId_callsRepoMethods() {
        Long id = 7L;

        // repository methods are void; ensure they don't throw
        doNothing().when(repository).deleteBookingExtraRelations(id);
        doNothing().when(repository).deleteById(id);

        service.deleteBookingExtra(id);

        verify(repository, times(1)).deleteBookingExtraRelations(id);
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    void deleteBookingExtra_nullId_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.deleteBookingExtra(null));
    }
}
