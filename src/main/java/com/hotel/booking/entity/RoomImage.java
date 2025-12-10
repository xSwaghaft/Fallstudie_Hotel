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

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_room_image_category"))
    @JsonBackReference
    private RoomCategory category;

    protected RoomImage() {
    }

    public RoomImage(String imagePath, RoomCategory category) {
        this.imagePath = imagePath;
        this.category = category;
        this.displayOrder = 0;
        this.isPrimary = false;
    }

    public RoomImage(String imagePath, String altText, String title, 
                    Integer displayOrder, Boolean isPrimary, RoomCategory category) {
        this.imagePath = imagePath;
        this.altText = altText;
        this.title = title;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
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

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
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
                ", displayOrder=" + displayOrder +
                ", isPrimary=" + isPrimary +
                '}';
    }
}

