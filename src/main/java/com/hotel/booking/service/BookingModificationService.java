package com.hotel.booking.service;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingModification;
import com.hotel.booking.entity.User;
import com.hotel.booking.repository.BookingModificationRepository;
import com.hotel.booking.repository.BookingRepository;
import com.hotel.booking.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class BookingModificationService {

    // Repositories für Datenbankzugriffe werden über den Konstruktor injiziert
    private final BookingModificationRepository modificationRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public BookingModificationService(
            BookingModificationRepository modificationRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository) {
        this.modificationRepository = modificationRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    // Gibt alle Buchungsänderungen als Liste zurück
    public List<BookingModification> getAll() {
        return modificationRepository.findAll();
    }

    // Liefert alle Modifikationen für eine bestimmte Buchung, absteigend nach Zeitpunkt
    public List<BookingModification> findByBookingId(Long bookingId) {
        if (bookingId == null) return List.of();
        List<BookingModification> list = modificationRepository.findByBookingId(bookingId);
        list.sort((a, b) -> b.getModifiedAt().compareTo(a.getModifiedAt()));
        return list;
    }

    // Sucht eine Buchungsänderung anhand der ID und gibt sie als Optional zurück
    public Optional<BookingModification> getById(Long id) {
        return modificationRepository.findById(id);
    }

    // Löscht eine Buchungsänderung anhand der ID aus der Datenbank
    public void delete(Long id) {
        modificationRepository.deleteById(id);
    }

    /**
     * Vergleicht zwei Booking-Objekte (vorher/nachher) und legt für jede Änderung
     * einen BookingModification-Eintrag an.
     *
     * @param before das persistierte Booking vor der Änderung (must not be null)
     * @param after das Booking mit den neuen Werten
     * @param handledBy der User, der die Änderung durchgeführt hat (optional)
     * @param reason optionaler Grund
     */
    public void recordChanges(Booking before, Booking after, User handledBy, String reason) {
        if (before == null || after == null) return;

        // Prüfe einzelne Felder und speichere Änderungen
        LocalDateTime now = LocalDateTime.now();

        if (before.getCheckInDate() != null && !before.getCheckInDate().equals(after.getCheckInDate())) {
            BookingModification m = new BookingModification();
            m.setBooking(before);
            m.setModifiedAt(now);
            m.setFieldChanged("checkInDate");
            m.setOldValue(before.getCheckInDate().toString());
            m.setNewValue(after.getCheckInDate() != null ? after.getCheckInDate().toString() : null);
            m.setHandledBy(handledBy);
            m.setReason(reason);
            modificationRepository.save(m);
        }

        if (before.getCheckOutDate() != null && !before.getCheckOutDate().equals(after.getCheckOutDate())) {
            BookingModification m = new BookingModification();
            m.setBooking(before);
            m.setModifiedAt(now);
            m.setFieldChanged("checkOutDate");
            m.setOldValue(before.getCheckOutDate().toString());
            m.setNewValue(after.getCheckOutDate() != null ? after.getCheckOutDate().toString() : null);
            m.setHandledBy(handledBy);
            m.setReason(reason);
            modificationRepository.save(m);
        }

        if (before.getAmount() != null && !before.getAmount().equals(after.getAmount())) {
            BookingModification m = new BookingModification();
            m.setBooking(before);
            m.setModifiedAt(now);
            m.setFieldChanged("amount");
            m.setOldValue(String.valueOf(before.getAmount()));
            m.setNewValue(after.getAmount() != null ? String.valueOf(after.getAmount()) : null);
            m.setHandledBy(handledBy);
            m.setReason(reason);
            modificationRepository.save(m);
        }

        if (before.getTotalPrice() != null && !before.getTotalPrice().equals(after.getTotalPrice())) {
            BookingModification m = new BookingModification();
            m.setBooking(before);
            m.setModifiedAt(now);
            m.setFieldChanged("totalPrice");
            m.setOldValue(before.getTotalPrice().toString());
            m.setNewValue(after.getTotalPrice() != null ? after.getTotalPrice().toString() : null);
            m.setHandledBy(handledBy);
            m.setReason(reason);
            modificationRepository.save(m);
        }

        // Extras comparison
        java.util.Set<com.hotel.booking.entity.BookingExtra> prevExtras = before.getExtras();
        java.util.Set<com.hotel.booking.entity.BookingExtra> newExtras = after.getExtras();
        java.util.Set<String> prevNames = new java.util.LinkedHashSet<>();
        java.util.Set<String> newNames = new java.util.LinkedHashSet<>();
        if (prevExtras != null) {
            for (com.hotel.booking.entity.BookingExtra be : prevExtras) {
                /*
                 * BookingExtra hat in diesem Projekt keinen `getId()`-Getter mit dem Namen
                 * `getId()`. Die persistente ID heißt `BookingExtra_id` und wird über
                 * `getBookingExtra_id()` bereitgestellt. Zur Anzeige/Protokollierung benutzen
                 * wir bevorzugt den Namen (`getName()`); falls der fehlt, fällt die Anzeige
                 * auf die ID zurück. Dadurch sind Audit-Einträge stabil und aussagekräftig.
                 */
                prevNames.add(be != null && be.getName() != null ? be.getName() : String.valueOf(be != null ? be.getBookingExtra_id() : "null"));
            }
        }
        if (newExtras != null) {
            for (com.hotel.booking.entity.BookingExtra be : newExtras) {
                newNames.add(be != null && be.getName() != null ? be.getName() : String.valueOf(be != null ? be.getBookingExtra_id() : "null"));
            }
        }
        if (!prevNames.equals(newNames)) {
            BookingModification m = new BookingModification();
            m.setBooking(before);
            m.setModifiedAt(now);
            m.setFieldChanged("extras");
            m.setOldValue(String.join(", ", prevNames));
            m.setNewValue(String.join(", ", newNames));
            m.setHandledBy(handledBy);
            m.setReason(reason);
            modificationRepository.save(m);
        }
    }

    /**
     * Variante für den Fall, dass die UI das persistente Booking-Objekt direkt
     * verändert (Binder schreibt in dasselbe Objekt). Hier übergeben wir die
     * referenz auf das persistierte Booking (bookingEntity) und liefern die
     * vorherigen Werte als Snapshot-Parameter.
     */
    public void recordChangesFromSnapshot(Booking bookingEntity,
                                          java.time.LocalDate previousCheckIn,
                                          java.time.LocalDate previousCheckOut,
                                          Integer previousAmount,
                                          java.math.BigDecimal previousTotalPrice,
                                          java.util.Set<com.hotel.booking.entity.BookingExtra> previousExtras,
                                          Booking after,
                                          User handledBy,
                                          String reason) {
        if (bookingEntity == null || after == null) return;

        LocalDateTime now = LocalDateTime.now();

        if (previousCheckIn != null && !previousCheckIn.equals(after.getCheckInDate())) {
            BookingModification m = new BookingModification();
            m.setBooking(bookingEntity);
            m.setModifiedAt(now);
            m.setFieldChanged("checkInDate");
            m.setOldValue(previousCheckIn.toString());
            m.setNewValue(after.getCheckInDate() != null ? after.getCheckInDate().toString() : null);
            m.setHandledBy(handledBy);
            m.setReason(reason);
            modificationRepository.save(m);
        }

        if (previousCheckOut != null && !previousCheckOut.equals(after.getCheckOutDate())) {
            BookingModification m = new BookingModification();
            m.setBooking(bookingEntity);
            m.setModifiedAt(now);
            m.setFieldChanged("checkOutDate");
            m.setOldValue(previousCheckOut.toString());
            m.setNewValue(after.getCheckOutDate() != null ? after.getCheckOutDate().toString() : null);
            m.setHandledBy(handledBy);
            m.setReason(reason);
            modificationRepository.save(m);
        }

        if (previousAmount != null && !previousAmount.equals(after.getAmount())) {
            BookingModification m = new BookingModification();
            m.setBooking(bookingEntity);
            m.setModifiedAt(now);
            m.setFieldChanged("amount");
            m.setOldValue(String.valueOf(previousAmount));
            m.setNewValue(after.getAmount() != null ? String.valueOf(after.getAmount()) : null);
            m.setHandledBy(handledBy);
            m.setReason(reason);
            modificationRepository.save(m);
        }

        if (previousTotalPrice != null && (after.getTotalPrice() == null || previousTotalPrice.compareTo(after.getTotalPrice()) != 0)) {
            BookingModification m = new BookingModification();
            m.setBooking(bookingEntity);
            m.setModifiedAt(now);
            m.setFieldChanged("totalPrice");
            m.setOldValue(previousTotalPrice.toString());
            m.setNewValue(after.getTotalPrice() != null ? after.getTotalPrice().toString() : null);
            m.setHandledBy(handledBy);
            m.setReason(reason);
            modificationRepository.save(m);
        }

        // Extras comparison: vergleiche nach Namen
        java.util.Set<com.hotel.booking.entity.BookingExtra> newExtras = after.getExtras();
        java.util.Set<String> prevNames = new java.util.LinkedHashSet<>();
        java.util.Set<String> newNames = new java.util.LinkedHashSet<>();
        if (previousExtras != null) {
            for (com.hotel.booking.entity.BookingExtra be : previousExtras) {
                // Wie oben: benutze Name, sonst ID, damit Audit-Einträge lesbar sind
                prevNames.add(be != null && be.getName() != null ? be.getName() : String.valueOf(be != null ? be.getBookingExtra_id() : "null"));
            }
        }
        if (newExtras != null) {
            for (com.hotel.booking.entity.BookingExtra be : newExtras) {
                newNames.add(be != null && be.getName() != null ? be.getName() : String.valueOf(be != null ? be.getBookingExtra_id() : "null"));
            }
        }

        if (!prevNames.equals(newNames)) {
            BookingModification m = new BookingModification();
            m.setBooking(bookingEntity);
            m.setModifiedAt(LocalDateTime.now());
            m.setFieldChanged("extras");
            m.setOldValue(String.join(", ", prevNames));
            m.setNewValue(String.join(", ", newNames));
            m.setHandledBy(handledBy);
            m.setReason(reason);
            modificationRepository.save(m);
        }
    }
}