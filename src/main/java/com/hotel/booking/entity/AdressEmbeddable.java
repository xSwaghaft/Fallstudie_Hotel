package com.hotel.booking.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class AdressEmbeddable implements Serializable {

    private static final long serialVersionUID = 1L;

    private String street;
    private String houseNumber;
    private String postalCode;
    private String city;
    private String country;

    // ----- Konstruktoren -----
    public AdressEmbeddable() {}

    public AdressEmbeddable(String street, String houseNumber, String postalCode, String city, String country) {
        this.street = street;
        this.houseNumber = houseNumber;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
    }

    // ----- Getter & Setter -----
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getFormatted() {
        return street + " " + houseNumber + ", " + postalCode + " " + city + ", " + country;
    }
}
