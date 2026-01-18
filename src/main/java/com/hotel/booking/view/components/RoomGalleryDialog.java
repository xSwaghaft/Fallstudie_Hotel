package com.hotel.booking.view.components;

import java.util.List;
import java.util.function.Consumer;

import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.RoomImage;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

/**
 * Dialog component for displaying room category gallery with images and reviews.
 * 
 * @author Viktor Götting
 */
public class RoomGalleryDialog {
    
    /** Single image count threshold */
    private static final int SINGLE_IMAGE_COUNT = 1;
    
    /** Dialog dimensions */
    private static final String DIALOG_WIDTH = "90%";
    private static final String DIALOG_MAX_WIDTH = "1200px";
    
    /** Default values */
    private static final String DEFAULT_ROOM_NAME = "Room";
    private static final String DEFAULT_IMAGE_ALT = "Room Image";
    
    /** UI text constants */
    private static final String GALLERY_TITLE_SUFFIX = " - Gallery & Reviews";
    private static final String TAB_IMAGES = "Images";
    private static final String TAB_REVIEWS = "Reviews";
    private static final String BUTTON_CLOSE = "Close";
    private static final String NO_IMAGES_MESSAGE = "No images available.";
    private static final String NO_REVIEWS_MESSAGE = "No reviews available.";
    
    private final RoomCategory category;
    private final List<RoomImage> images;
    private Consumer<Div> reviewsContentProvider;
    
    /**
     * Creates a new RoomGalleryDialog.
     * 
     * @param category the room category
     * @param images the list of room images
     */
    public RoomGalleryDialog(RoomCategory category, List<RoomImage> images) {
        this.category = category;
        this.images = images;
    }
    
    /**
     * Sets a provider that will populate the reviews content area.
     * 
     * @param provider Consumer that receives the reviews Div to populate
     */
    public void setReviewsContentProvider(Consumer<Div> provider) {
        this.reviewsContentProvider = provider;
    }
    
    /**
     * Opens the gallery dialog.
     * 
     * @param showReviewsTab if true, shows Reviews tab initially
     */
    public void open(boolean showReviewsTab) {
        createDialog(showReviewsTab).open();
    }
    
    /**
     * Opens the gallery dialog with Images tab selected.
     */
    public void open() {
        open(false);
    }
    
    /**
     * Creates the gallery dialog with tabs for Images and Reviews.
     * 
     * @param showReviewsTab if true, shows Reviews tab initially
     * @return the created dialog
     */
    private Dialog createDialog(boolean showReviewsTab) {
        Dialog dialog = new Dialog();
        String categoryName = category != null && category.getName() != null 
                ? category.getName() 
                : DEFAULT_ROOM_NAME;
        dialog.setHeaderTitle(categoryName + GALLERY_TITLE_SUFFIX);
        dialog.addClassName("room-gallery-dialog");
        dialog.setWidth(DIALOG_WIDTH);
        dialog.setMaxWidth(DIALOG_MAX_WIDTH);
        
        // Create tabs
        Tab imagesTab = new Tab(TAB_IMAGES);
        Tab reviewsTab = new Tab(TAB_REVIEWS);
        Tabs tabs = new Tabs(imagesTab, reviewsTab);
        
        // Create content areas
        Div imagesContent = createImagesContent();
        Div reviewsContent = createReviewsContent();
        
        // Container for tab content
        Div pages = new Div(imagesContent, reviewsContent);
        pages.addClassName("room-gallery-tabs-container");
        pages.setWidthFull();
        
        // Initially show Images tab, or Reviews if requested
        if (showReviewsTab) {
            tabs.setSelectedTab(reviewsTab);
            imagesContent.setVisible(false);
            reviewsContent.setVisible(true);
        } else {
            reviewsContent.setVisible(false);
        }
        
        // Tab change listener
        tabs.addSelectedChangeListener(ev -> {
            boolean isReviewsTab = tabs.getSelectedTab() == reviewsTab;
            imagesContent.setVisible(!isReviewsTab);
            reviewsContent.setVisible(isReviewsTab);
        });
        
        VerticalLayout content = createStyledLayout("room-gallery-content", false, true);
        content.setWidthFull();
        content.add(tabs, pages);
        content.setFlexGrow(1, pages);
        
        Button closeButton = new Button(BUTTON_CLOSE, ev -> dialog.close());
        closeButton.addClassName("primary-button");
        dialog.getFooter().add(closeButton);
        dialog.add(content);
        
        return dialog;
    }
    
    /**
     * Creates the images content area.
     */
    private Div createImagesContent() {
        Div imagesDiv = new Div();
        imagesDiv.setWidthFull();
        
        if (images == null || images.isEmpty()) {
            Paragraph empty = new Paragraph(NO_IMAGES_MESSAGE);
            empty.addClassName("reviews-empty");
            imagesDiv.add(empty);
        } else {
            Div gallery = createGalleryGrid();
            imagesDiv.add(gallery);
        }
        
        return imagesDiv;
    }
    
    /**
     * Creates the reviews content area and populates it via callback if available.
     */
    private Div createReviewsContent() {
        Div reviewsDiv = new Div();
        reviewsDiv.setWidthFull();
        reviewsDiv.addClassName("room-gallery-reviews-content");
        
        // Populate via callback if available
        if (reviewsContentProvider != null) {
            reviewsContentProvider.accept(reviewsDiv);
        } else {
            Paragraph empty = new Paragraph(NO_REVIEWS_MESSAGE);
            empty.addClassName("reviews-empty");
            reviewsDiv.add(empty);
        }
        
        return reviewsDiv;
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
     * Creates the grid with all category images.
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
        if (validImageCount == SINGLE_IMAGE_COUNT) {
            gallery.addClassName("room-gallery-grid--single");
        }
        
        return gallery;
    }
    
    /**
     * Opens a dialog with a large view of a single image.
     */
    private void openImageDialog(RoomImage image) {
        String altText = image.getAltText() != null ? image.getAltText() : DEFAULT_IMAGE_ALT;
        Image img = new Image(image.getImagePath(), altText);
        img.addClassName("gallery-image-full");
        img.setWidthFull();
        
        String dialogTitle = image.getTitle() != null ? image.getTitle() : DEFAULT_IMAGE_ALT;
        Dialog dialog = createImageDialog(dialogTitle, img);
        dialog.addClassName("room-gallery-image-dialog");
        dialog.open();
    }
    
    /**
     * Creates a dialog with the specified title and content components.
     */
    private Dialog createImageDialog(String title, com.vaadin.flow.component.Component... content) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title);
        Button closeButton = new Button(BUTTON_CLOSE, ev -> dialog.close());
        closeButton.addClassName("primary-button");
        dialog.getFooter().add(closeButton);
        dialog.add(content);
        return dialog;
    }
}
