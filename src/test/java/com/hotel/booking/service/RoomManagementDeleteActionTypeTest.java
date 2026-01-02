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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RoomManagementDeleteActionTypeTest {

    @Test
    public void enumContainsExpectedValues() {
        RoomManagementDeleteActionType[] values = RoomManagementDeleteActionType.values();
        assertEquals(3, values.length);
        assertTrue(java.util.Arrays.asList(values).contains(RoomManagementDeleteActionType.SET_INACTIVE));
        assertTrue(java.util.Arrays.asList(values).contains(RoomManagementDeleteActionType.PERMANENT_DELETE));
        assertTrue(java.util.Arrays.asList(values).contains(RoomManagementDeleteActionType.BLOCKED_BY_BOOKINGS));
    }

    @Test
    public void valueOfReturnsCorrectEnum() {
        assertEquals(RoomManagementDeleteActionType.SET_INACTIVE, RoomManagementDeleteActionType.valueOf("SET_INACTIVE"));
        assertEquals(RoomManagementDeleteActionType.PERMANENT_DELETE, RoomManagementDeleteActionType.valueOf("PERMANENT_DELETE"));
    }
}
