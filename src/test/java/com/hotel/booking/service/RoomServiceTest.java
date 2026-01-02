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
import com.hotel.booking.entity.Room;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.RoomStatus;
import com.hotel.booking.repository.BookingRepository;
import com.hotel.booking.repository.RoomRepository;
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
public class RoomServiceTest {

    @Mock
    RoomRepository roomRepo;

    @Mock
    BookingRepository bookingRepo;

    @Mock
    RoomCategoryService catSvc;

    @InjectMocks
    RoomService svc;

    @Test
    public void getDeletionActionType_setInactiveIfActive() {
        Room room = mock(Room.class);
        when(room.getActive()).thenReturn(true);
        when(roomRepo.findById(1L)).thenReturn(Optional.of(room));

        var action = svc.getDeletionActionType(1L);
        assertEquals(RoomManagementDeleteActionType.SET_INACTIVE, action);
    }

    @Test
    public void getDeletionActionType_blockedByBookingsIfInactiveAndBookingsExist() {
        Room room = mock(Room.class);
        when(room.getActive()).thenReturn(false);
        when(roomRepo.findById(2L)).thenReturn(Optional.of(room));
        when(bookingRepo.findByRoom_Id(2L)).thenReturn(List.of(mock(Booking.class)));

        var action = svc.getDeletionActionType(2L);
        assertEquals(RoomManagementDeleteActionType.BLOCKED_BY_BOOKINGS, action);
    }

    @Test
    public void getDeletionActionType_permanentDeleteIfNoBookings() {
        Room room = mock(Room.class);
        when(room.getActive()).thenReturn(false);
        when(roomRepo.findById(3L)).thenReturn(Optional.of(room));
        when(bookingRepo.findByRoom_Id(3L)).thenReturn(List.of());

        var action = svc.getDeletionActionType(3L);
        assertEquals(RoomManagementDeleteActionType.PERMANENT_DELETE, action);
    }

    @Test
    public void deleteRoom_deactivatesWhenActive() {
        Room room = mock(Room.class);
        when(room.getActive()).thenReturn(true);
        when(room.getId()).thenReturn(11L);
        when(roomRepo.findById(11L)).thenReturn(Optional.of(room));
        when(roomRepo.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));

        svc.deleteRoom(11L);

        verify(room).setActive(false);
        verify(room).setStatus(RoomStatus.INACTIVE);
        verify(roomRepo).save(room);
    }

    @Test
    public void deleteRoom_throwsIfBookingsExist() {
        Room room = mock(Room.class);
        when(room.getActive()).thenReturn(false);
        when(room.getRoomNumber()).thenReturn("101");
        when(roomRepo.findById(12L)).thenReturn(Optional.of(room));
        when(bookingRepo.findByRoom_Id(12L)).thenReturn(List.of(mock(Booking.class)));

        assertThrows(IllegalStateException.class, () -> svc.deleteRoom(12L));
    }

    @Test
    public void changeStatus_updatesAndSaves() {
        Room room = mock(Room.class);
        when(roomRepo.findById(20L)).thenReturn(Optional.of(room));
        when(roomRepo.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));

        Room res = svc.changeStatus(20L, RoomStatus.OCCUPIED);
        verify(room).setStatus(RoomStatus.OCCUPIED);
        verify(roomRepo).save(room);
        assertEquals(room, res);
    }

    @Test
    public void validateRoom_throwsForInvalidCategoryOrNumberOrStatus() {
        // Use real Room instances instead of mocks to avoid unnecessary stubbings
        // Category null
        Room r1 = new Room(null, RoomStatus.AVAILABLE, true);
        r1.setRoomNumber("101");
        assertThrows(IllegalArgumentException.class, () -> svc.validateRoom(r1));

        // Price null in category
        RoomCategory cat = new RoomCategory(); // price is null
        Room r2 = new Room(cat, RoomStatus.AVAILABLE, true);
        r2.setRoomNumber("101");
        assertThrows(IllegalArgumentException.class, () -> svc.validateRoom(r2));

        // Room number missing
        Room r3 = new Room(new RoomCategory(), RoomStatus.AVAILABLE, true);
        r3.setRoomNumber("");
        assertThrows(IllegalArgumentException.class, () -> svc.validateRoom(r3));

        // Status null
        Room r4 = new Room(new RoomCategory(), null, true);
        r4.getCategory().setPricePerNight(new java.math.BigDecimal("10"));
        r4.setRoomNumber("101");
        assertThrows(IllegalArgumentException.class, () -> svc.validateRoom(r4));
    }

    @Test
    public void calculateStatistics_aggregatesCounts() {
        when(roomRepo.count()).thenReturn(10L);
        when(roomRepo.countByStatus(RoomStatus.AVAILABLE)).thenReturn(4L);
        when(roomRepo.countByStatus(RoomStatus.OCCUPIED)).thenReturn(2L);
        when(roomRepo.countByStatus(RoomStatus.CLEANING)).thenReturn(1L);
        when(roomRepo.countByStatus(RoomStatus.RENOVATING)).thenReturn(0L);
        when(roomRepo.countByStatus(RoomStatus.OUT_OF_SERVICE)).thenReturn(1L);
        when(roomRepo.countByStatus(RoomStatus.INACTIVE)).thenReturn(2L);
        when(catSvc.getStatistics()).thenReturn(new long[]{3L,2L,1L});

        var stats = svc.calculateStatistics();

        assertEquals(10L, stats.totalRooms);
        assertEquals(4L, stats.availableRooms);
        assertEquals(2L, stats.occupiedRooms);
        assertEquals(1L, stats.cleaningRooms);
        assertEquals(0L, stats.renovatingRooms);
        assertEquals(1L, stats.outOfServiceRooms);
        assertEquals(2L, stats.inactiveRooms);
        assertEquals(3L, stats.totalCategories);
    }
}
