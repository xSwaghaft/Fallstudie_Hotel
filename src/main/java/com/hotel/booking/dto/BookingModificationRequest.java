// Ruslan Krause
package com.hotel.booking.dto;

import java.time.LocalDateTime;

public class BookingModificationRequest {
    public Long bookingId;
    public LocalDateTime modifiedAt;
    public String fieldChanged;
    public String oldValue;
    public String newValue;
    public String reason;
    public Long handledById;
}