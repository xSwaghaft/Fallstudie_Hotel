package com.hotel.booking.dto;

import java.time.LocalDate;

/**
 * Simple DTO for Guest data used in REST requests/responses.
 * Fields are public to keep the DTO minimal and similar to existing DTO style.
 */
public class GuestDto {
    public Long id;
    public Long userId;
    public String email;
    public String firstName;
    public String lastName;
    public String address;
    public String phoneNumber;
    public LocalDate birthdate;

    public GuestDto() {}

    public GuestDto(Long id, Long userId, String email, String firstName, String lastName, String address, String phoneNumber, LocalDate birthdate) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.birthdate = birthdate;
    }
}
