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
 - when(mock.method(...)).thenReturn(value): Stubbt das Rückgabeverhalten einesMock-Objekts.
 - any(): Matcher, der jeden Wert passenden Typs akzeptiert (z.B. any(String.class)).
 - times(n) / never(): Geben an, wie oft eine Mock-Methode erwartet wird (z.B. verify(mock, times(1))).
 - doReturn()/doThrow(): Alternative Stubbing-Syntax (z. B. für void-Methoden oder Spies).
 - MimeMessage: Repräsentiert eine E-Mail (HTML/Multipart) aus dem JavaMail API.
 - verify(mock).method(...): Überprüft Aufrufe auf Mocks; oft kombiniert mit ArgumentCaptor oder Matchern.
*/

import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.RoomImage;
import com.hotel.booking.entity.Booking;
import com.hotel.booking.repository.RoomCategoryRepository;
import com.hotel.booking.repository.RoomImageRepository;
import com.hotel.booking.repository.RoomRepository;
import com.hotel.booking.repository.InvoiceRepository;
import com.hotel.booking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomCategoryServiceTest {

    @Mock
    RoomCategoryRepository roomCategoryRepository;

    @Mock
    RoomImageRepository roomImageRepository;

    @Mock
    RoomRepository roomRepository;

    @Mock
    InvoiceRepository invoiceRepository;

    @Mock
    BookingRepository bookingRepository;

    @InjectMocks
    RoomCategoryService roomCategoryService;

    @BeforeEach
    void setUp() {
        // InjectMocks will construct the service with mocked dependencies
    }

    @Test
    void getAllRoomCategories_returnsList() {
        RoomCategory c = new RoomCategory();
        when(roomCategoryRepository.findAll()).thenReturn(List.of(c));

        var res = roomCategoryService.getAllRoomCategories();
        assertEquals(1, res.size());
    }

    @Test
    void createCategory_saves() {
        when(roomCategoryRepository.save(any(RoomCategory.class))).thenAnswer(i -> i.getArgument(0));

        var res = roomCategoryService.createCategory("Single", "desc", new BigDecimal("10"), 1, true);
        assertNotNull(res);
        assertEquals("Single", res.getName());
    }

    @Test
    void getCategoryImages_returnsImages() {
        RoomImage img = mock(RoomImage.class);
        when(roomImageRepository.findByCategoryIdOrderByPrimaryFirst(1L)).thenReturn(List.of(img));
        var res = roomCategoryService.getCategoryImages(1L);
        assertEquals(1, res.size());
    }

    @Test
    void validateCategory_throwsForBadValues() {
        RoomCategory c1 = new RoomCategory();
        c1.setName("");
        c1.setPricePerNight(new BigDecimal("10"));
        c1.setMaxOccupancy(1);
        assertThrows(IllegalArgumentException.class, () -> roomCategoryService.validateCategory(c1));

        RoomCategory c2 = new RoomCategory();
        c2.setName("A");
        c2.setPricePerNight(new BigDecimal("-1"));
        c2.setMaxOccupancy(1);
        assertThrows(IllegalArgumentException.class, () -> roomCategoryService.validateCategory(c2));

        RoomCategory c3 = new RoomCategory();
        c3.setName("A");
        c3.setPricePerNight(new BigDecimal("1"));
        c3.setMaxOccupancy(0);
        assertThrows(IllegalArgumentException.class, () -> roomCategoryService.validateCategory(c3));
    }

    @Test
    public void getStatistics_returnsCounts() {
        when(roomCategoryRepository.count()).thenReturn(5L);
        when(roomCategoryRepository.countActive()).thenReturn(3L);

        long[] stats = roomCategoryService.getStatistics();
        assertArrayEquals(new long[]{5L, 3L, 2L}, stats);
    }

    @Test
    public void toggleActive_flipsAndSaves() {
        RoomCategory category = new RoomCategory();
        category.setCategory_id(10L);
        category.setActive(true);
        when(roomCategoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(roomCategoryRepository.save(any(RoomCategory.class))).thenAnswer(i -> i.getArgument(0));

        RoomCategory toggled = roomCategoryService.toggleActive(10L);
        assertFalse(toggled.getActive());
    }

    @Test
    public void getDeletionActionType_blockedByBookings() {
        RoomCategory category = new RoomCategory();
        category.setCategory_id(20L);
        category.setActive(false);

        when(roomCategoryRepository.findById(20L)).thenReturn(Optional.of(category));
        when(bookingRepository.findByRoomCategoryId(20L)).thenReturn(List.of(mock(Booking.class)));

        var action = roomCategoryService.getDeletionActionType(20L);
        assertEquals(RoomManagementDeleteActionType.BLOCKED_BY_BOOKINGS, action);
    }

    @Test
    public void getCategoryImages_delegatesToRepo() {
        RoomImage img = mock(RoomImage.class);
        when(roomImageRepository.findByCategoryIdOrderByPrimaryFirst(5L)).thenReturn(List.of(img));

        var images = roomCategoryService.getCategoryImages(5L);
        assertEquals(1, images.size());
    }
}
