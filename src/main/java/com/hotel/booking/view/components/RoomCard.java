package com.hotel.booking.view.components;

import java.util.List;

import com.hotel.booking.entity.Amenities;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.RoomImage;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Component for displaying a room category as a card.
 * @author Viktor Götting
 */
public class RoomCard extends Div {
    
    /** Default values */
    private static final String DEFAULT_CATEGORY_NAME = "Unknown";
    private static final String DEFAULT_DESCRIPTION = "Comfortable Room";
    private static final String DEFAULT_PRICE_TEXT = "N/A";
    
    /** UI text constants */
    private static final String NO_IMAGE_TEXT = "No Image";
    private static final String PER_NIGHT_TEXT = "per night";
    private static final String CURRENCY_PREFIX = "€";
  
    private final RoomCategory category;
    private final List<RoomImage> images;
    private VerticalLayout contentArea; // Cache for direct access
    private VerticalLayout rightSide; // Cache für Rating-Platzierung rechts
    private HorizontalLayout amenitiesRatingRow; // Cache für Rating-Platzierung auf derselben Ebene wie Amenities
    private RoomGalleryDialog galleryDialog;
    
    /**
     * Creates a RoomCard with the specified category.
     */
    public RoomCard(RoomCategory category) {
        this.category = category;
        this.images = getCategoryImages();
        addClassName("room-card");
        buildCard();
    }
    
    /**
     * Returns the room category displayed by this card.
     */
    public RoomCategory getCategory() {
        return category;
    }
    
    /**
     * Retrieves all images associated with the room category.
     */
    private List<RoomImage> getCategoryImages() {
        return category != null && category.getImages() != null && !category.getImages().isEmpty() 
                ? category.getImages() 
                : List.of();
    }

    /**
     * Builds the visible card structure with image container and content area.
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
     */
    private Div createImageContainer() {
        Div imageContainer = new Div();
        imageContainer.addClassName("room-card__image");
        
        if (images != null && !images.isEmpty()) {
            // Show the first image as the main image
            RoomImage firstImage = images.get(0);
            // Use CSS variable for dynamic background image
            imageContainer.getStyle().set("--card-image-url", "url('" + firstImage.getImagePath() + "')");
            // Clicking on the image opens the gallery
            imageContainer.addClickListener(e -> openGallery());
        } else {
            // No image available: Show placeholder
            imageContainer.addClassName("room-card__image--empty");
            imageContainer.add(new Paragraph(NO_IMAGE_TEXT));
        }
        
        return imageContainer;
    }
    
    /**
     * Creates the content area with title, price, and description.
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
        
        rightSide = createStyledLayout("room-card__right-side", false, false);
        H3 title = new H3(getCategoryName());
        title.addClassName("room-card__title");
        Paragraph description = new Paragraph(getDescription());
        description.addClassName("room-card__description");
        rightSide.add(title, description);
        
        // Amenities und Rating auf derselben Ebene
        amenitiesRatingRow = createStyledHorizontalLayout("room-card__amenities-rating-row", true);
        amenitiesRatingRow.setWidthFull();
        amenitiesRatingRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        amenitiesRatingRow.setAlignItems(Alignment.CENTER);
        
        Div amenitiesContainer = createAmenitiesContainer();
        if (amenitiesContainer != null) {
            amenitiesRatingRow.add(amenitiesContainer);
            amenitiesRatingRow.setFlexGrow(1, amenitiesContainer);
        }
        
        rightSide.add(amenitiesRatingRow);
        
        mainLayout.add(rightSide);
        mainLayout.setFlexGrow(0, priceDiv);
        mainLayout.setFlexGrow(1, rightSide);
        content.add(mainLayout);
        
        return content;
    }

    /**
     * Sets a provider that will populate the reviews content area in the gallery dialog.
     * 
     * @param provider Consumer that receives the reviews Div to populate
     */
    public void setReviewsContentProvider(java.util.function.Consumer<Div> provider) {
        // Initialize gallery dialog if not already created
        if (galleryDialog == null) {
            galleryDialog = new RoomGalleryDialog(category, images);
        }
        galleryDialog.setReviewsContentProvider(provider);
    }
    
    /**
     * Sets the average rating and displays it as clickable stars.
     * Stars open the gallery dialog with Reviews tab when clicked.
     * 
     * @param average the average rating value
     */
    public void setAverageRating(double average) {
        if (amenitiesRatingRow == null || average <= 0d) return;
        
        Runnable onRatingClick = () -> openGallery(true);
        StarRating starRating = new StarRating(average, onRatingClick);
        amenitiesRatingRow.add(starRating);
    }
    
    /**
     * Creates a styled VerticalLayout with the specified class name and spacing/padding settings.
     */
    private VerticalLayout createStyledLayout(String className, boolean spacing, boolean padding) {
        VerticalLayout layout = new VerticalLayout();
        layout.addClassName(className);
        layout.setSpacing(spacing);
        layout.setPadding(padding);
        layout.setMargin(false);
        return layout;
    }
    
    /**
     * Creates a styled HorizontalLayout with the specified class name and spacing setting.
     */
    private HorizontalLayout createStyledHorizontalLayout(String className, boolean spacing) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.addClassName(className);
        layout.setSpacing(spacing);
        layout.setPadding(false);
        layout.setMargin(false);
        return layout;
    }
    
    /**
     * Gets the category name, or a default value if null.
     */
    private String getCategoryName() {
        return category != null ? category.getName() : DEFAULT_CATEGORY_NAME;
    }
    
    /**
     * Gets the category description, or a default value if null.
     */
    private String getDescription() {
        return category != null && category.getDescription() != null
                ? category.getDescription()
                : DEFAULT_DESCRIPTION;
    }
    
    /**
     * Creates the price area with main price and "per night" text.
     */
    private Div createPriceDiv() {
        String priceText = category != null && category.getPricePerNight() != null
                ? CURRENCY_PREFIX + category.getPricePerNight()
                : DEFAULT_PRICE_TEXT;
        
        Paragraph priceMain = new Paragraph(priceText);
        priceMain.addClassName("room-card__price-main");
        Paragraph priceSub = new Paragraph(PER_NIGHT_TEXT);
        priceSub.addClassName("room-card__price-sub");
        
        Div priceDiv = new Div(priceMain, priceSub);
        priceDiv.addClassName("room-card__price");
        return priceDiv;
    }
    
    /**
     * Creates the amenities container with badges for each amenity.
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
     * Formats amenity enum name from UPPER_CASE to "Upper Case" format.
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
     * Opens a dialog with a gallery of all category images.
     * 
     * @param showReviewsTab if true, opens with Reviews tab selected
     */
    public void openGallery(boolean showReviewsTab) {
        if (galleryDialog == null) {
            galleryDialog = new RoomGalleryDialog(category, images);
        }
        galleryDialog.open(showReviewsTab);
    }
    
    /**
     * Opens a dialog with a gallery of all category images (default: Images tab).
     */
    private void openGallery() {
        openGallery(false);
    }
    
}
