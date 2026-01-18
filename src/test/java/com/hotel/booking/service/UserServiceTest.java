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

import com.hotel.booking.entity.AdressEmbeddable;
import com.hotel.booking.entity.User;
import com.hotel.booking.repository.BookingRepository;
import com.hotel.booking.repository.BookingCancellationRepository;
import com.hotel.booking.repository.BookingModificationRepository;
import com.hotel.booking.repository.UserRepository;
import com.hotel.booking.security.BcryptPasswordEncoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepo;

    @Mock
    BookingModificationRepository modRepo;

    @Mock
    BookingCancellationRepository canRepo;

    @Mock
    BookingRepository bookingRepo;

    @Mock
    BcryptPasswordEncoder encoder;

    @Mock
    EmailService emailService;

    @InjectMocks
    UserService svc;

    @Test
    public void authenticate_hashedPassword_matches() {
        User user = mock(User.class);
        when(userRepo.findByUsername("u")).thenReturn(Optional.of(user));
        when(user.getPassword()).thenReturn("$2a$hashed");
        when(encoder.matches("pw", "$2a$hashed")).thenReturn(true);
        when(user.isActive()).thenReturn(true);

        var res = svc.authenticate("u", "pw");
        assertTrue(res.isPresent());
        assertEquals(user, res.get());
    }

    @Test
    public void authenticate_plaintext_password_getsHashedAndSaved() {
        User user = mock(User.class);
        when(userRepo.findByUsername("u2")).thenReturn(Optional.of(user));
        when(user.getPassword()).thenReturn("plain");
        when(user.isActive()).thenReturn(true);
        when(encoder.encode("plain")).thenReturn("$2a$encoded");

        var res = svc.authenticate("u2", "plain");
        assertTrue(res.isPresent());
        verify(user).setPassword("$2a$encoded");
        verify(userRepo).save(user);
    }

    @Test
    public void create_validUser_hashesPassword_and_sendsWelcomeEmail() throws Exception {
        User user = new User("alice", "Alice", "L", new AdressEmbeddable(), "alice@example.com", "secret", null, true);

        when(userRepo.existsByUsername("alice")).thenReturn(false);
        when(userRepo.existsByEmail("alice@example.com")).thenReturn(false);
        when(encoder.encode("secret")).thenReturn("$2a$enc");
        when(userRepo.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User created = svc.create(user);
        assertNotNull(created);
        assertEquals("alice", created.getUsername());
        verify(emailService).sendWelcomeEmail(created);
    }

    @Test
    public void setInactive_findsUser_and_saves() {
        User user = mock(User.class);
        when(userRepo.findById(5L)).thenReturn(Optional.of(user));
        when(user.isActive()).thenReturn(true);

        svc.setInactive(5L);

        verify(user).setActive(false);
        verify(userRepo).save(user);
    }

    @Test
    public void getDeletionAction_blocksWhenInactiveAndHasBookings() {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("bob");
        when(user.isActive()).thenReturn(false);
        when(userRepo.findById(7L)).thenReturn(Optional.of(user));
        when(bookingRepo.findByGuest_Id(7L)).thenReturn(List.of(mock(com.hotel.booking.entity.Booking.class)));

        UserService.DeleteAction action = svc.getDeletionAction(7L);
        assertTrue(action.isBlocked);
        assertNotNull(action.errorMessage);
    }

    @Test
    public void findByEmail_delegates() {
        User u = mock(User.class);
        when(userRepo.findByEmail("x@x.com")).thenReturn(Optional.of(u));

        var res = svc.findByEmail("x@x.com");
        assertTrue(res.isPresent());
        assertEquals(u, res.get());
    }
}