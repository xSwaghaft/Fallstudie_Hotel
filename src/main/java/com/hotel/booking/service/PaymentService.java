package com.hotel.booking.service;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.entity.Invoice.PaymentMethod;
import com.hotel.booking.entity.Invoice.PaymentStatus;
import com.hotel.booking.entity.Payment;
import com.hotel.booking.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final InvoiceService invoiceService;

    public PaymentService(PaymentRepository paymentRepository, 
                         BookingService bookingService,
                         InvoiceService invoiceService) {
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
        this.invoiceService = invoiceService;
    }

    @Transactional(readOnly = true)
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Payment> findByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findById(Long id) {
        return paymentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findByTransactionRef(String txRef) {
        return paymentRepository.findByTransactionRef(txRef);
    }

    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    public void deleteById(Long id) {
        paymentRepository.deleteById(id);
    }
    
    /**
     * Maps UI payment method string to PaymentMethod enum.
     * 
     * @param uiMethod the payment method string from UI (can be null)
     * @return the corresponding PaymentMethod enum value (defaults to CARD)
     */
    public PaymentMethod mapPaymentMethod(String uiMethod) {
        if (uiMethod == null) {
            return PaymentMethod.CARD;
        }
        if ("Bank Transfer".equals(uiMethod) || "Banküberweisung".equals(uiMethod)) {
            return PaymentMethod.TRANSFER;
        }
        return PaymentMethod.CARD;
    }
    
    /**
     * Processes a payment for a booking.
     * Creates the payment, updates booking status if paid, and creates invoice if needed.
     * 
     * @param booking the booking to process payment for
     * @param paymentMethodString the payment method string from UI
     * @param status the payment status (PAID or PENDING)
     * @return the created payment
     */
    @Transactional
    public Payment processPayment(Booking booking, String paymentMethodString, PaymentStatus status) {
        if (booking == null || booking.getTotalPrice() == null) {
            throw new IllegalArgumentException("Booking and total price cannot be null");
        }
        
        PaymentMethod paymentMethod = mapPaymentMethod(paymentMethodString);
        
        // Create and save payment
        Payment payment = new Payment();
        payment.setAmount(booking.getTotalPrice());
        payment.setStatus(status);
        payment.setPaidAt(status == PaymentStatus.PAID ? LocalDateTime.now() : null);
        payment.setMethod(paymentMethod);
        payment.setBooking(booking);
        Payment savedPayment = save(payment);
        
        // If payment is PAID, update booking status and create invoice
        if (status == PaymentStatus.PAID) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingService.save(booking);
            
            // Create invoice if not exists
            if (booking.getInvoice() == null) {
                invoiceService.createInvoiceForBooking(booking, paymentMethod, PaymentStatus.PAID);
            }
        }
        // If status is PENDING, booking status stays PENDING until payment is made
        
        return savedPayment;
    }
}
