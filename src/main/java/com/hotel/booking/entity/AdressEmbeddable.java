
/**
 * Represents an embeddable address entity for JPA.
 * <p>
 * This class is used to store address information such as street, house number, postal code, city, and country.
 * It is intended to be embedded in other JPA entities.
 * </p>
 *
 * @author Matthias Lohr
 */
package com.hotel.booking.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;


/**
 * Embeddable class for address details.
 */
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


    /**
     * Constructs an address with all fields.
     *
     * @param street      the street name
     * @param houseNumber the house number
     * @param postalCode  the postal code
     * @param city        the city
     * @param country     the country
     */
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
