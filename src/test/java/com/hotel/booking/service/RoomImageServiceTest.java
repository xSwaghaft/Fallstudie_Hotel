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

import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.RoomImage;
import com.hotel.booking.repository.RoomImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomImageServiceTest {

    @Mock
    RoomImageRepository roomImageRepository;

    RoomImageService svc;

    @BeforeEach
    void setUp() {
        svc = new RoomImageService(roomImageRepository, "target/test-images");
    }

    @Test
    public void createTargetFile_sanitizesAndCreatesName() {
        File f = svc.createTargetFile("te/st:<>?.jpg");
        assertTrue(f.getName().endsWith("_te_st____.jpg") || f.getName().contains("te_st"));
    }

    @Test
    public void createAndSaveUploadedImage_setsWebPathAndTitle() {
        when(roomImageRepository.save(any(RoomImage.class))).thenAnswer(i -> i.getArgument(0));

        RoomImage saved = svc.createAndSaveUploadedImage("orig.jpg", "stored.jpg");
        assertEquals("/images/rooms/stored.jpg", saved.getImagePath());
        assertEquals("orig.jpg", saved.getTitle());
    }

    @Test
    public void assignImageToCategory_setsCategoryAndSaves() {
        when(roomImageRepository.save(any(RoomImage.class))).thenAnswer(i -> i.getArgument(0));

        RoomImage img = new RoomImage("/images/rooms/x.jpg", null);
        RoomCategory cat = new RoomCategory();
        cat.setCategory_id(7L);

        RoomImage res = svc.assignImageToCategory(img, cat);
        assertNotNull(res.getCategory());
        assertEquals(7L, res.getCategory().getCategory_id());
    }

    @Test
    public void updateImage_unmarksOtherPrimaries_andSavesAll() {
        RoomImageService svc = this.svc;

        RoomCategory cat = new RoomCategory();
        cat.setCategory_id(9L);

        RoomImage other = new RoomImage("/images/rooms/a.jpg", cat);
        other.setId(1L);
        other.setIsPrimary(true);

        when(roomImageRepository.findPrimaryByCategoryId(9L)).thenReturn(List.of(other));
        when(roomImageRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
        when(roomImageRepository.save(any(RoomImage.class))).thenAnswer(i -> i.getArgument(0));

        RoomImage updated = new RoomImage("/images/rooms/b.jpg", cat);
        updated.setId(2L);
        updated.setIsPrimary(true);

        RoomImage result = svc.updateImage(updated);

        ArgumentCaptor<java.util.List<RoomImage>> captor = ArgumentCaptor.forClass(java.util.List.class);
        verify(roomImageRepository).saveAll(captor.capture());
        List<RoomImage> savedList = captor.getValue();
        assertFalse(savedList.get(0).getIsPrimary());
        assertEquals(result, updated);
    }

    @Test
    public void deleteImage_withoutDiskPath_deletesFromRepo() {
        RoomImage img = new RoomImage("/images/rooms/", null);
        // imagePath ends with slash -> extractFileName returns null -> resolveDiskPath null

        svc.deleteImage(img);

        verify(roomImageRepository).delete(img);
    }
}