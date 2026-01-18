package com.hotel.booking.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * Represents a category for hotel rooms.
 * <p>
 * This class contains details about a room category, such as its name, description, price per night, maximum occupancy, amenities, and associated rooms and images.
 * It is mapped to the <code>room_category</code> table in the database.
 * </p>
 *
 * @author Matthias Lohr
 */
@Entity
@Table(name = "room_category")
public class RoomCategory {


    /**
     * Unique identifier for the room category.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long category_id;


    /**
     * Name of the room category (e.g., Single, Double, Suite).
     */
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price_per_night", nullable = false)
    private BigDecimal pricePerNight;


    /**
     * Maximum number of occupants allowed in this category.
     */
    @Column(name = "max_occupancy", nullable = false)
    private Integer maxOccupancy;


    /**
     * Indicates if the category is active (true) or inactive (false).
     */
    @Column(name = "active", nullable = false)
    private Boolean active;


    /**
     * Set of amenities available for this room category.
     * Amenities are loaded eagerly and are not a separate entity.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @JoinTable(name = "room_category_amenities", joinColumns = @JoinColumn(name = "category_id"))
    @Column(name = "amenity")
    private Set<Amenities> amenities = new HashSet<>();


    /**
     * List of rooms belonging to this category.
     * This side is the parent in the bidirectional relationship.
     */
    @OneToMany(mappedBy = "category")
    @JsonManagedReference
    private List<Room> rooms = new ArrayList<>();


    /**
     * List of images associated with this room category.
     * Images are loaded eagerly and ordered by primary flag and ID.
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("isPrimary DESC, id ASC")
    @JsonManagedReference
    private List<RoomImage> images = new ArrayList<>();



    /**
     * Default constructor.
     */
    public RoomCategory() {
    }


    /**
     * Constructs a RoomCategory with all main fields.
     *
     * @param category_id unique identifier
     * @param name name of the category
     * @param description description of the category
     * @param pricePerNight price per night
     * @param maxOccupancy maximum occupancy
     * @param active whether the category is active
     * @param rooms list of rooms in this category
     */
    public RoomCategory(Long category_id, String name, String description, BigDecimal pricePerNight, Integer maxOccupancy, Boolean active, List<Room> rooms) {
        this.category_id = category_id;
        this.name = name;
        this.description = description;
        this.pricePerNight = pricePerNight;
        this.maxOccupancy = maxOccupancy;
        this.active = active;
        this.rooms = rooms;
    }

    // Getters and setters
    public Long getCategory_id() {
        return category_id;
    }

    public void setCategory_id(Long category_id) {
        this.category_id = category_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public Integer getMaxOccupancy() {
        return maxOccupancy;
    }

    public void setMaxOccupancy(Integer maxOccupancy) {
        this.maxOccupancy = maxOccupancy;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public List<RoomImage> getImages() {
        return images;
    }

    public void setImages(List<RoomImage> images) {
        this.images = images;
    }


    /**
     * Adds an image to the category and sets the category reference in the image.
     * @param image the RoomImage to add
     */
    public void addImage(RoomImage image) {
        images.add(image);
        image.setCategory(this);
    }


    /**
     * Removes an image from the category and clears the category reference in the image.
     * @param image the RoomImage to remove
     */
    public void removeImage(RoomImage image) {
        images.remove(image);
        image.setCategory(null);
    }


    /**
     * Returns the primary image for this category, or the first image if none is marked as primary.
     * @return the primary RoomImage, or null if no images exist
     */
    public RoomImage getPrimaryImage() {
        return images.stream()
                .filter(RoomImage::getIsPrimary)
                .findFirst()
                .orElse(images.isEmpty() ? null : images.get(0));
    }

    public Set<Amenities> getAmenities() {
        return amenities;
    }

    public void setAmenities(Set<Amenities> amenities) {
        this.amenities = amenities;
    }

    /**
     * Compares this RoomCategory with another object based on the category ID.
     * <p>
     * Two RoomCategory instances are considered equal if they have the same category ID.
     * This implementation is required for proper behavior in Vaadin Select dropdowns and
     * collection comparisons.
     * </p>
     *
     * @param o the object to compare with
     * @return true if the objects are equal (same category ID), false otherwise
     * @author Artur Derr
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomCategory that = (RoomCategory) o;
        return category_id != null && category_id.equals(that.category_id);
    }

    /**
     * Returns the hash code of this RoomCategory based on the category ID.
     * <p>
     * This implementation ensures consistency with the equals method and is required
     * for proper behavior in hash-based collections (HashSet, HashMap, etc.).
     * </p>
     *
     * @return the hash code based on the category ID, or 0 if category ID is null
     * @author Artur Derr
     */
    @Override
    public int hashCode() {
        return category_id != null ? category_id.hashCode() : 0;
    }
}
