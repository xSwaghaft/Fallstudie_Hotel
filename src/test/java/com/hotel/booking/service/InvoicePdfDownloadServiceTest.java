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

import java.util.Optional;

import com.hotel.booking.entity.Invoice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoicePdfDownloadServiceTest {

    @Mock
    InvoiceService invoiceService;

    @Mock
    InvoicePdfService invoicePdfService;

    @InjectMocks
    InvoicePdfDownloadService service;

    @Test
    void generatePdfForInvoice_found_returnsBytes() {
        Long id = 42L;
        Invoice invoice = mock(Invoice.class);
        byte[] pdf = "pdf-content".getBytes();

        when(invoiceService.findById(id)).thenReturn(Optional.of(invoice));
        when(invoicePdfService.generateInvoicePdf(invoice)).thenReturn(pdf);

        byte[] result = service.generatePdfForInvoice(id);

        assertArrayEquals(pdf, result);
        verify(invoicePdfService).generateInvoicePdf(invoice);
    }

    @Test
    void generatePdfForInvoice_notFound_throws() {
        Long id = 100L;
        when(invoiceService.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.generatePdfForInvoice(id));
    }

    @Test
    void getInvoiceFileName_found_returnsExpected() {
        Long id = 7L;
        Invoice invoice = mock(Invoice.class);
        when(invoiceService.findById(id)).thenReturn(Optional.of(invoice));
        when(invoice.getInvoiceNumber()).thenReturn("INV-7");

        String fileName = service.getInvoiceFileName(id);

        assertEquals("Invoice_INV-7.pdf", fileName);
    }

    @Test
    void getInvoiceFileName_notFound_throws() {
        Long id = 9L;
        when(invoiceService.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getInvoiceFileName(id));
    }
}
