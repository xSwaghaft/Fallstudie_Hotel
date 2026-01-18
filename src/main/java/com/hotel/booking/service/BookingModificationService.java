package com.hotel.booking.service;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingModification;
import com.hotel.booking.entity.User;
import com.hotel.booking.repository.BookingModificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

/**
 * Service class for managing booking modification operations.
 * <p>
 * Handles tracking and recording changes made to bookings, including:
 * </p>
 * <ul>
 *   <li>Comparing booking objects before and after changes</li>
 *   <li>Creating modification records for each changed field</li>
 *   <li>Tracking changes to check-in/check-out dates, guest count, price, and extras</li>
 *   <li>Sending email notifications to guests when bookings are modified</li>
 *   <li>Integrating with PaymentService to handle price changes</li>
 * </ul>
 * <p>
 * The service provides two methods for recording changes:
 * </p>
 * <ul>
 *   <li>{@code recordChanges}: Compares two Booking objects (before/after)</li>
 *   <li>{@code recordChangesFromSnapshot}: Uses snapshot values when the UI directly modifies the persistent object</li>
 * </ul>
 * <p>
 * All modifications are automatically persisted and trigger email notifications to guests.
 * </p>
 *
 * @author Ruslan Krause
 * @see BookingModification
 * @see BookingModificationRepository
 */
@Service
public class BookingModificationService {

    // Repositories for database access are injected via the constructor
    private final BookingModificationRepository modificationRepository;
    private final EmailService emailService;
    private final PaymentService paymentService;

    public BookingModificationService(
            BookingModificationRepository modificationRepository,
            EmailService emailService,
            PaymentService paymentService) {
        this.modificationRepository = modificationRepository;
        this.emailService = emailService;
        this.paymentService = paymentService;
    }

    /**
     * Retrieves all modification records from the database.
     *
     * @return a list containing all modification records
     */
    public List<BookingModification> getAll() {
        return modificationRepository.findAll();
    }

    /**
     * Retrieves all modification records for a specific booking, sorted by modification timestamp descending.
     *
     * @param bookingId the ID of the booking
     * @return a list of modification records for the specified booking, sorted by most recent first
     */
    public List<BookingModification> findByBookingId(Long bookingId) {
        if (bookingId == null) return List.of();
        List<BookingModification> list = modificationRepository.findByBookingId(bookingId);
        list.sort((a, b) -> b.getModifiedAt().compareTo(a.getModifiedAt()));
        return list;
    }

    /**
     * Retrieves a modification record by its unique identifier.
     *
     * @param id the modification ID
     * @return an Optional containing the modification if found, or empty if not found
     */
    public Optional<BookingModification> getById(Long id) {
        return modificationRepository.findById(id);
    }

    /**
     * Deletes a modification record by its unique identifier.
     *
     * @param id the modification ID to delete
     */
    public void delete(Long id) {
        modificationRepository.deleteById(id);
    }

    /**
     * Compares two Booking objects (before/after) and creates a BookingModification record for each change.
     *
     * @param before the persisted Booking before the change (must not be null)
     * @param after the Booking with the new values
     * @param handledBy the User who performed the change (optional)
     * @param reason optional reason
     */
    public void recordChanges(Booking before, Booking after, User handledBy, String reason) {
        if (before == null || after == null) return;

        // Check individual fields and save changes
        LocalDateTime now = LocalDateTime.now();
        BookingModification firstModification = null; // Track first modification for email

        if (before.getCheckInDate() != null && !before.getCheckInDate().equals(after.getCheckInDate())) {
            BookingModification m = new BookingModification();
            m.setBooking(before);
            m.setModifiedAt(now);
            m.setFieldChanged("checkInDate");
            m.setOldValue(before.getCheckInDate().toString());
            m.setNewValue(after.getCheckInDate() != null ? after.getCheckInDate().toString() : null);
            m.setHandledBy(handledBy);
            m.setReason(reason);
            BookingModification saved = modificationRepository.save(m);
            if (firstModification == null) firstModification = saved;
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
            BookingModification saved = modificationRepository.save(m);
            if (firstModification == null) firstModification = saved;
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
            BookingModification saved = modificationRepository.save(m);
            if (firstModification == null) firstModification = saved;
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
            BookingModification saved = modificationRepository.save(m);
            if (firstModification == null) firstModification = saved;

            // Inform payment service about price change
            paymentService.handlePriceChange(before, before.getTotalPrice(), after.getTotalPrice());
        }

        // Extras comparison
        java.util.Set<com.hotel.booking.entity.BookingExtra> prevExtras = before.getExtras();
        java.util.Set<com.hotel.booking.entity.BookingExtra> newExtras = after.getExtras();
        java.util.Set<String> prevNames = new java.util.LinkedHashSet<>();
        java.util.Set<String> newNames = new java.util.LinkedHashSet<>();
        if (prevExtras != null) {
            for (com.hotel.booking.entity.BookingExtra be : prevExtras) {
                /*
                 * BookingExtra has no `getId()`-Getter with the name `getId()`. The persistent ID is called `BookingExtra_id` and is provided via `getBookingExtra_id()`. For display/logging, we prefer the name (`getName()`); if it's missing, the display falls back to the ID. This makes audit entries stable and informative.
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
            BookingModification saved = modificationRepository.save(m);
            if (firstModification == null) firstModification = saved;
        }
        
        // Send email if at least one modification was created
        if (firstModification != null && before.getGuest() != null && before.getGuest().getEmail() != null) {
            try {
                emailService.sendBookingModification(before, firstModification);
            } catch (Exception e) {
                // Log error but don't fail the modification save
                System.err.println("Failed to send booking modification email: " + e.getMessage());
            }
        }
    }

    /**
     * Variant for the case where the UI directly modifies the persistent Booking object
     * (Binder writes to the same object). Here we pass the reference to the persistent
     * Booking (bookingEntity) and provide the previous values as snapshot parameters.
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
        BookingModification firstModification = null; // Track first modification for email

        if (previousCheckIn != null && !previousCheckIn.equals(after.getCheckInDate())) {
            BookingModification m = new BookingModification();
            m.setBooking(bookingEntity);
            m.setModifiedAt(now);
            m.setFieldChanged("checkInDate");
            m.setOldValue(previousCheckIn.toString());
            m.setNewValue(after.getCheckInDate() != null ? after.getCheckInDate().toString() : null);
            m.setHandledBy(handledBy);
            m.setReason(reason);
            BookingModification saved = modificationRepository.save(m);
            if (firstModification == null) firstModification = saved;
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
            BookingModification saved = modificationRepository.save(m);
            if (firstModification == null) firstModification = saved;
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
            BookingModification saved = modificationRepository.save(m);
            if (firstModification == null) firstModification = saved;
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
            BookingModification saved = modificationRepository.save(m);
            if (firstModification == null) firstModification = saved;

            // Inform payment service about price change
            paymentService.handlePriceChange(bookingEntity, previousTotalPrice, after.getTotalPrice());
        }

        // Extras comparison: vergleiche nach Namen
        java.util.Set<com.hotel.booking.entity.BookingExtra> newExtras = after.getExtras();
        java.util.Set<String> prevNames = new java.util.LinkedHashSet<>();
        java.util.Set<String> newNames = new java.util.LinkedHashSet<>();
        if (previousExtras != null) {
            for (com.hotel.booking.entity.BookingExtra be : previousExtras) {
                // Use Name, otherwise ID, so audit entries are readable
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
            BookingModification saved = modificationRepository.save(m);
            if (firstModification == null) firstModification = saved;
        }
        
        // Send email if at least one modification was created
        if (firstModification != null && bookingEntity.getGuest() != null && bookingEntity.getGuest().getEmail() != null) {
            try {
                emailService.sendBookingModification(bookingEntity, firstModification);
            } catch (Exception e) {
                // Log error but don't fail the modification save
                System.err.println("Failed to send booking modification email: " + e.getMessage());
            }
        }
    }
}