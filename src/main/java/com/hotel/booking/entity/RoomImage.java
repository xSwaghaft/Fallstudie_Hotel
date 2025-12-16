package com.hotel.booking.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "room_images")
public class RoomImage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @Column(name = "image_path", nullable = false, length = 500)
    private String imagePath;

    @Column(name = "alt_text", length = 255)
    private String altText;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "category_id", nullable = true,
                foreignKey = @ForeignKey(name = "fk_room_image_category"))
    @JsonBackReference
    private RoomCategory category;

    protected RoomImage() {
    }

    // Public no-arg constructor for form creation
    public RoomImage(RoomImage dummy) {
    }

    public RoomImage(String imagePath, RoomCategory category) {
        this.imagePath = imagePath;
        this.category = category;
        this.isPrimary = false;
    }

    public RoomImage(String imagePath, String altText, String title,
                    Boolean isPrimary, RoomCategory category) {
        this.imagePath = imagePath;
        this.altText = altText;
        this.title = title;
        this.isPrimary = isPrimary != null ? isPrimary : false;
        this.category = category;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary != null ? isPrimary : false;
    }

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

