package com.hotel.booking.view.components;

import java.util.List;

import com.hotel.booking.entity.Amenities;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.RoomImage;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Component for displaying a room category as a card.
 * 
 * Shows the main image, name, price per night, and description.
 * Clicking on the image opens a gallery with all available images of the category.
 */
public class RoomCard extends Div {
  
    private final RoomCategory category;
    private List<RoomImage> images;
    private VerticalLayout contentArea; // Cache for direct access
    
    /**
     * Creates a RoomCard with the specified category.
     * Images are loaded directly from the category (EAGER fetch).
     * 
     * @param category The room category to display
     */
    public RoomCard(RoomCategory category) {
        this.category = category;
        this.images = getCategoryImages();
        addClassName("room-card");
        buildCard();
    }
    
    /**
     * Returns the room category displayed by this card.
     * 
     * @return The room category
     */
    public RoomCategory getCategory() {
        return category;
    }
    
    private List<RoomImage> getCategoryImages() {
        return category != null && category.getImages() != null && !category.getImages().isEmpty() 
                ? category.getImages() 
                : List.of();
    }

    /**
     * Builds the visible card with image, title, price, and description.
     * The first image is displayed as the main image. Clicking on it opens the gallery.
     */
    private void buildCard() {
        // Image container: Shows the first image of the category or a placeholder
        Div imageContainer = createImageContainer();
        
        // Content area: Title, price, and description
        VerticalLayout content = createContentArea();
        
        add(imageContainer, content);
    }
    
    /**
     * Creates the image container with the main image or a placeholder.
     * 
     * @return The image container Div
     */
    private Div createImageContainer() {
        Div imageContainer = new Div();
        imageContainer.addClassName("room-card__image");
        
        if (images != null && !images.isEmpty()) {
            // Show the first image as the main image
            RoomImage firstImage = images.get(0);
            imageContainer.getStyle().set("background-image", "url('" + firstImage.getImagePath() + "')");
            // Clicking on the image opens the gallery
            imageContainer.addClickListener(e -> openGallery());
        } else {
            // No image available: Show placeholder
            imageContainer.addClassName("room-card__image--empty");
            imageContainer.add(new Paragraph("No Image"));
        }
        
        return imageContainer;
    }
    
    /**
     * Creates the content area with title, price, and description.
     * 
     * @return The content area as VerticalLayout
     */
    private VerticalLayout createContentArea() {
        VerticalLayout content = createStyledLayout("room-card__content", false, true);
        this.contentArea = content; // Cache for setBookButton
        
        HorizontalLayout mainLayout = createStyledHorizontalLayout("room-card__main-layout", true);
        mainLayout.setWidthFull();
        mainLayout.setAlignItems(Alignment.START);
        
        Div priceDiv = createPriceDiv();
        priceDiv.addClassName("room-card__price-container");
        mainLayout.add(priceDiv);
        
        VerticalLayout rightSide = createStyledLayout("room-card__right-side", false, false);
        H3 title = new H3(getCategoryName());
        title.addClassName("room-card__title");
        Paragraph description = new Paragraph(getDescription());
        description.addClassName("room-card__description");
        rightSide.add(title, description);
        
        Div amenitiesContainer = createAmenitiesContainer();
        if (amenitiesContainer != null) {
            rightSide.add(amenitiesContainer);
        }
        
        mainLayout.add(rightSide);
        mainLayout.setFlexGrow(0, priceDiv);
        mainLayout.setFlexGrow(1, rightSide);
        content.add(mainLayout);
        
        return content;
    }
    
    private VerticalLayout createStyledLayout(String className, boolean spacing, boolean padding) {
        VerticalLayout layout = new VerticalLayout();
        layout.addClassName(className);
        layout.setSpacing(spacing);
        layout.setPadding(padding);
        layout.setMargin(false);
        return layout;
    }
    
    private HorizontalLayout createStyledHorizontalLayout(String className, boolean spacing) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.addClassName(className);
        layout.setSpacing(spacing);
        layout.setPadding(false);
        layout.setMargin(false);
        return layout;
    }
    
    private String getCategoryName() {
        return category != null ? category.getName() : "Unknown";
    }
    
    private String getDescription() {
        return category != null && category.getDescription() != null
                ? category.getDescription()
                : "Comfortable Room";
    }
    
    /**
     * Creates the price area with main price and "per night" text.
     * 
     * @return The price area as Div
     */
    private Div createPriceDiv() {
        String priceText = category != null && category.getPricePerNight() != null
                ? "€" + category.getPricePerNight()
                : "N/A";
        
        Paragraph priceMain = new Paragraph(priceText);
        priceMain.addClassName("room-card__price-main");
        Paragraph priceSub = new Paragraph("per night");
        priceSub.addClassName("room-card__price-sub");
        
        Div priceDiv = new Div(priceMain, priceSub);
        priceDiv.addClassName("room-card__price");
        return priceDiv;
    }
    
    /**
     * Creates the amenities container with badges for each amenity.
     * 
     * @return The amenities container Div, or null if no amenities available
     */
    private Div createAmenitiesContainer() {
        if (category == null || category.getAmenities() == null || category.getAmenities().isEmpty()) {
            return null;
        }
        
        HorizontalLayout amenitiesLayout = createStyledHorizontalLayout("room-card__amenities-layout", true);
        category.getAmenities().forEach(amenity -> {
            if (amenity != null) {
                Span badge = new Span(formatAmenityName(amenity));
                badge.addClassName("room-card__amenity-badge");
                amenitiesLayout.add(badge);
            }
        });
        
        Div amenitiesContainer = new Div(amenitiesLayout);
        amenitiesContainer.addClassName("room-card__amenities");
        return amenitiesContainer;
    }
    
    /**
     * Formats the amenity enum name to a readable string.
     * Converts UPPER_CASE to "Upper Case" format (e.g., AIR_CONDITIONING -> "Air Conditioning").
     * 
     * @param amenity The amenity enum value
     * @return Formatted amenity name
     */
    private String formatAmenityName(Amenities amenity) {
        if (amenity == null) return "";
        String name = amenity.name();
        String[] parts = name.split("_");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) result.append(" ");
            result.append(parts[i].charAt(0))
                  .append(parts[i].substring(1).toLowerCase());
        }
        return result.toString();
    }
 
    /**
     * Adds a booking button at the end of the card.
     * 
     * @param bookButton The button to add
     */
    public void setBookButton(Button bookButton) {
        if (contentArea != null) {
            bookButton.addClassName("primary-button");
            bookButton.addClassName("room-card__book-button");
            bookButton.setWidthFull();
            contentArea.add(bookButton);
        }
    }
 
    /**
     * Opens a dialog with a gallery of all images of the category.
     * Clicking on an image in the gallery opens a larger view.
     */
    private void openGallery() {
        if ((images == null || images.isEmpty()) && category != null) {
            images = getCategoryImages();
        }
        
        if (images == null || images.isEmpty()) {
            createDialog("Room Gallery", new Paragraph("No images available.")).open();
            return;
        }
        
        createGalleryDialog().open();
    }
    
    /**
     * Creates the gallery dialog with all images of the category.
     * 
     * @return The fully configured gallery dialog
     */
    private Dialog createGalleryDialog() {
        VerticalLayout content = createStyledLayout("room-gallery-content", false, false);
        content.setWidthFull();
        
        Div gallery = createGalleryGrid();
        content.add(gallery);
        content.setFlexGrow(1, gallery);
        
        Dialog dialog = createDialog("Room Gallery", content);
        dialog.addClassName("room-gallery-dialog");
        dialog.setWidth("90%");
        dialog.setMaxWidth("1200px");
        return dialog;
    }
    
    private Dialog createDialog(String title, com.vaadin.flow.component.Component... content) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title);
        Button closeButton = new Button("Close", ev -> dialog.close());
        closeButton.addClassName("primary-button");
        dialog.getFooter().add(closeButton);
        dialog.add(content);
        return dialog;
    }
    
    /**
     * Creates the grid with all images of the category.
     * Each image is clickable and opens a larger view.
     * 
     * @return The grid Div with all images
     */
    private Div createGalleryGrid() {
        Div gallery = new Div();
        gallery.addClassName("room-gallery-grid");
        gallery.setWidthFull();
        
        int validImageCount = 0;
        for (RoomImage image : images) {
            if (image == null || image.getImagePath() == null || image.getImagePath().isEmpty()) {
                continue;
            }
            
            Div imageDiv = new Div();
            imageDiv.addClassName("room-gallery-image");
            // CSS variable for dynamic background image
            imageDiv.getStyle().set("--gallery-image-url", "url('" + image.getImagePath() + "')");
            // Clicking opens a larger view
            imageDiv.addClickListener(e -> openImageDialog(image));
            gallery.add(imageDiv);
            validImageCount++;
        }
        
        // If only one image, add class to make it take full width
        if (validImageCount == 1) {
            gallery.addClassName("room-gallery-grid--single");
        }
        
        return gallery;
    }
    
    /**
     * Opens a dialog with a large view of a single image.
     * 
     * @param image The image to display in full size
     */
    private void openImageDialog(RoomImage image) {
        Image img = new Image(image.getImagePath(), image.getAltText() != null ? image.getAltText() : "Room Image");
        img.addClassName("gallery-image-full");
        img.setWidthFull();
        
        Dialog dialog = createDialog(image.getTitle() != null ? image.getTitle() : "Room Image", img);
        dialog.addClassName("room-gallery-image-dialog");
        dialog.open();
    }
    
}
