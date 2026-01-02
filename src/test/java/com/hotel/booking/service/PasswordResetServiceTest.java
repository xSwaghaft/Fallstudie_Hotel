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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import com.hotel.booking.entity.PasswordResetToken;
import com.hotel.booking.entity.User;
import com.hotel.booking.repository.PasswordResetTokenRepository;
import com.hotel.booking.security.BcryptPasswordEncoder;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    UserService userService;

    @Mock
    PasswordResetTokenRepository tokenRepository;

    @Mock
    EmailService emailService;

    @Mock
    BcryptPasswordEncoder passwordEncoder;

    @InjectMocks
    PasswordResetService service;

    @BeforeEach
    void setUp() {
    }

    @Test
    void verifyToken_notFound_returnsEmpty() {
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.empty());

        Optional<String> res = service.verifyToken("abc");

        assertTrue(res.isEmpty());
    }

    @Test
    void verifyToken_expired_returnsEmpty() {
        PasswordResetToken prt = new PasswordResetToken("t", "u@example.com", Instant.now().minus(2, ChronoUnit.HOURS));
        when(tokenRepository.findByToken("t")).thenReturn(Optional.of(prt));

        Optional<String> res = service.verifyToken("t");
        assertTrue(res.isEmpty());
    }

    @Test
    void verifyToken_valid_returnsEmail() {
        PasswordResetToken prt = new PasswordResetToken("t2", "u2@example.com", Instant.now().plus(30, ChronoUnit.MINUTES));
        when(tokenRepository.findByToken("t2")).thenReturn(Optional.of(prt));

        Optional<String> res = service.verifyToken("t2");
        assertTrue(res.isPresent());
        assertEquals("u2@example.com", res.get());
    }

    @Test
    void resetPassword_tokenNotFound_returnsFalse() {
        when(tokenRepository.findByToken("x")).thenReturn(Optional.empty());
        assertFalse(service.resetPassword("x", "newPass"));
    }

    @Test
    void resetPassword_expired_deletesTokenAndReturnsFalse() {
        PasswordResetToken prt = new PasswordResetToken("et", "e@example.com", Instant.now().minus(1, ChronoUnit.HOURS));
        when(tokenRepository.findByToken("et")).thenReturn(Optional.of(prt));

        boolean res = service.resetPassword("et", "np");

        assertFalse(res);
        verify(tokenRepository).delete(prt);
    }

    @Test
    void resetPassword_userNotFound_returnsFalse() {
        PasswordResetToken prt = new PasswordResetToken("nt", "noone@example.com", Instant.now().plus(1, ChronoUnit.HOURS));
        when(tokenRepository.findByToken("nt")).thenReturn(Optional.of(prt));
        when(userService.findByEmail("noone@example.com")).thenReturn(Optional.empty());

        boolean res = service.resetPassword("nt", "pw");

        assertFalse(res);
    }

    @Test
    void resetPassword_success_hashesAndSavesUser_andDeletesToken() {
        PasswordResetToken prt = new PasswordResetToken("ok", "u@example.com", Instant.now().plus(1, ChronoUnit.HOURS));
        when(tokenRepository.findByToken("ok")).thenReturn(Optional.of(prt));

        User user = mock(User.class);
        when(userService.findByEmail("u@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newp")).thenReturn("hashed");

        boolean res = service.resetPassword("ok", "newp");

        assertTrue(res);
        verify(user).setPassword("hashed");
        verify(userService).save(user);
        verify(tokenRepository).delete(prt);
    }

    @Test
    void createTokenAndSend_userNotFound_returnsFalse() throws Exception {
        when(userService.findByEmail("absent@example.com")).thenReturn(Optional.empty());
        boolean res = service.createTokenAndSend("absent@example.com");
        assertFalse(res);
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendHtmlMessage(any(), any(), any());
    }

    @Test
    void createTokenAndSend_success_sendsHtml() throws Exception {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("u1");
        when(userService.findByEmail("u1@example.com")).thenReturn(Optional.of(user));

        doNothing().when(emailService).sendHtmlMessage(any(), any(), any());

        boolean res = service.createTokenAndSend("u1@example.com");

        assertTrue(res);
        ArgumentCaptor<PasswordResetToken> cap = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(cap.capture());
        PasswordResetToken saved = cap.getValue();
        assertEquals("u1@example.com", saved.getEmail());
        verify(emailService).sendHtmlMessage(eq("u1@example.com"), any(), any());
    }

    @Test
    void createTokenAndSend_htmlFails_fallbackPlain_succeeds() throws Exception {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("u2");
        when(userService.findByEmail("u2@example.com")).thenReturn(Optional.of(user));

        doAnswer(invocation -> { throw new MessagingException("boom"); }).when(emailService).sendHtmlMessage(any(), any(), any());
        doNothing().when(emailService).sendSimpleMessage(any(), any(), any());

        boolean res = service.createTokenAndSend("u2@example.com");

        assertTrue(res);
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendSimpleMessage(eq("u2@example.com"), any(), any());
    }

    @Test
    void createTokenAndSend_bothEmailsFail_deletesTokenAndReturnsFalse() throws Exception {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("u3");
        when(userService.findByEmail("u3@example.com")).thenReturn(Optional.of(user));

        doAnswer(invocation -> { throw new MessagingException("boom"); }).when(emailService).sendHtmlMessage(any(), any(), any());
        doThrow(new RuntimeException("plain fail")).when(emailService).sendSimpleMessage(any(), any(), any());

        boolean res = service.createTokenAndSend("u3@example.com");

        assertFalse(res);
        // tokenRepository.save called then tokenRepository.delete should be called on failure
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(tokenRepository).delete(any(PasswordResetToken.class));
    }
}
