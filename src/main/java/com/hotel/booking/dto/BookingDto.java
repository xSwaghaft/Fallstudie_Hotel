package com.hotel.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for Booking used in REST requests/responses.
 * Keep fields public to match the lightweight DTO style used elsewhere.
 */
public class BookingDto {
    public Long id;
    public String bookingNumber;
    public Integer amount;
    public LocalDate checkInDate;
    public LocalDate checkOutDate;
    public String status; // use BookingStatus.name() in mapping
    public BigDecimal totalPrice;
    public Long guestId;
    public Long roomId;
    public Long invoiceId;
    public List<Long> paymentIds;

    public BookingDto() {}

    public BookingDto(Long id, String bookingNumber, Integer amount, LocalDate checkInDate, LocalDate checkOutDate,
                      String status, BigDecimal totalPrice, Long guestId, Long roomId, Long invoiceId, List<Long> paymentIds) {
        this.id = id;
        this.bookingNumber = bookingNumber;
        this.amount = amount;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = status;
        this.totalPrice = totalPrice;
        this.guestId = guestId;
        this.roomId = roomId;
        this.invoiceId = invoiceId;
        this.paymentIds = paymentIds;
    }
}
