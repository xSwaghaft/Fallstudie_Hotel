// Ruslan Krause
package com.hotel.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingCancellationRequest {
    public Long bookingId;
    public LocalDateTime cancelledAt;
    public String reason;
    public BigDecimal refundedAmount;
    public Long handledById;
}