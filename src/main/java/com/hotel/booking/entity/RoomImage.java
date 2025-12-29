package com.hotel.booking.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Represents an image associated with a hotel room category.
 *
 * <p>Each {@code RoomImage} stores metadata about a room image file, including its filesystem path,
 * alternative text for accessibility, a display title, and a flag indicating whether it is the
 * primary (thumbnail) image for the room category.
 *
 * <p><b>Relationship:</b> A room image belongs to exactly one {@link RoomCategory}. Multiple images
 * can be associated with the same room category to provide different views and perspectives.
 *
 * <p><b>Persistence:</b> Images are persisted in the {@code room_images} table and are lazily
 * loaded via their parent {@code RoomCategory}.
 *
 * @author Artur Derr
 * @author Viktor Götting
 * @see RoomCategory
 */
@Entity
@Table(name = "room_images")
public class RoomImage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique identifier for this room image. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    /** Filesystem path or URL to the image file. */
    @Column(name = "image_path", nullable = false, length = 500)
    private String imagePath;

    /** Alternative text for screen readers and image display fallback. */
    @Column(name = "alt_text", length = 255)
    private String altText;

    /** Display title for the image (e.g., shown as a caption). */
    @Column(name = "title", length = 255)
    private String title;

    /** Flag indicating whether this is the primary (thumbnail) image for the room category. */
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    /** Reference to the parent room category. */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "category_id", nullable = true,
                foreignKey = @ForeignKey(name = "fk_room_image_category"))
    @JsonBackReference
    private RoomCategory category;

    /**
     * Protected no-arg constructor for JPA/Hibernate.
     */
    protected RoomImage() {
    }

    /**
     * Public no-arg constructor for form creation and utility purposes.
     *
     * @param dummy unused parameter to distinguish from protected constructor
     */
    public RoomImage(RoomImage dummy) {
    }

    /**
     * Creates a new room image with the given filesystem path and room category.
     *
     * @param imagePath the filesystem path or URL to the image file
     * @param category the room category this image belongs to
     */
    public RoomImage(String imagePath, RoomCategory category) {
        this.imagePath = imagePath;
        this.category = category;
        this.isPrimary = false;
    }

    /**
     * Creates a new room image with all details specified.
     *
     * @param imagePath the filesystem path or URL to the image file
     * @param altText alternative text for accessibility
     * @param title display title for the image
     * @param isPrimary whether this is the primary image for the category
     * @param category the room category this image belongs to
     */
    public RoomImage(String imagePath, String altText, String title,
                    Boolean isPrimary, RoomCategory category) {
        this.imagePath = imagePath;
        this.altText = altText;
        this.title = title;
        this.isPrimary = isPrimary != null ? isPrimary : false;
        this.category = category;
    }

    /** @return the unique identifier of this room image */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this room image.
     *
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return the filesystem path or URL of this image */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * Sets the filesystem path or URL of this image.
     *
     * @param imagePath the path to set
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    /** @return the alternative text for this image */
    public String getAltText() {
        return altText;
    }

    /**
     * Sets the alternative text for this image.
     *
     * @param altText the alt text to set
     */
    public void setAltText(String altText) {
        this.altText = altText;
    }

    /** @return the display title of this image */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the display title for this image.
     *
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /** @return whether this is the primary image for its room category */
    public Boolean getIsPrimary() {
        return isPrimary;
    }

    /**
     * Sets whether this image is the primary image for its room category.
     *
     * @param isPrimary {@code true} if this should be the primary image, {@code false} otherwise
     */
    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary != null ? isPrimary : false;
    }

    /** @return the room category this image belongs to */
    public RoomCategory getCategory() {
        return category;
    }

    public void setCategory(RoomCategory category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "RoomImage{" +
                "id=" + id +
                ", imagePath='" + imagePath + '\'' +
                ", isPrimary=" + isPrimary +
                '}';
    }
}

