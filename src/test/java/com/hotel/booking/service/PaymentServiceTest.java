
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
import java.util.List;
import java.util.Optional;

import com.hotel.booking.entity.Payment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    com.hotel.booking.repository.PaymentRepository paymentRepository;

    @InjectMocks
    PaymentService paymentService;

    @Test
    void findAll_delegatesToRepo() {
        when(paymentRepository.findAll()).thenReturn(List.of(new Payment(BigDecimal.valueOf(10), com.hotel.booking.entity.Invoice.PaymentMethod.CARD)));

        var all = paymentService.findAll();

        assertEquals(1, all.size());
        verify(paymentRepository).findAll();
    }

    @Test
    void findByBookingId_delegates() {
        when(paymentRepository.findByBookingId(5L)).thenReturn(List.of());

        var res = paymentService.findByBookingId(5L);

        assertNotNull(res);
        verify(paymentRepository).findByBookingId(5L);
    }

    @Test
    void findById_delegates() {
        Payment p = new Payment(BigDecimal.valueOf(20), com.hotel.booking.entity.Invoice.PaymentMethod.CARD);
        when(paymentRepository.findById(2L)).thenReturn(Optional.of(p));

        Optional<Payment> opt = paymentService.findById(2L);

        assertTrue(opt.isPresent());
        assertSame(p, opt.get());
    }

    @Test
    void findByTransactionRef_delegates() {
        Payment p = new Payment(BigDecimal.valueOf(5), com.hotel.booking.entity.Invoice.PaymentMethod.CARD);
        when(paymentRepository.findByTransactionRef("tx-1")).thenReturn(Optional.of(p));

        Optional<Payment> opt = paymentService.findByTransactionRef("tx-1");

        assertTrue(opt.isPresent());
        assertSame(p, opt.get());
    }

    @Test
    void save_delegatesToRepo() {
        Payment p = new Payment(BigDecimal.valueOf(33.33), com.hotel.booking.entity.Invoice.PaymentMethod.CARD);
        when(paymentRepository.save(p)).thenReturn(p);

        Payment saved = paymentService.save(p);

        assertSame(p, saved);
        verify(paymentRepository).save(p);
    }

    @Test
    void deleteById_delegates() {
        paymentService.deleteById(10L);
        verify(paymentRepository).deleteById(10L);
    }
}
