package com.hotel.booking.view;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.Feedback;
import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.FeedbackService;
import com.hotel.booking.service.RoomCategoryService;
import com.hotel.booking.view.components.CardFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
//Viktor Götting

/**
 * View for managing guest feedback and reviews.
 * <p>
 * Provides staff (receptionists and managers) with feedback management capabilities including:
 * </p>
 * <ul>
 *   <li>Displaying all guest feedback in a searchable grid</li>
 *   <li>Viewing feedback details including rating, comment, and associated booking</li>
 *   <li>Deleting feedback entries with confirmation dialogs</li>
 *   <li>Filtering feedback by room category</li>
 *   <li>Viewing rating distribution statistics by category</li>
 * </ul>
 * <p>
 * Organizes feedback information by room category with category-specific statistics, individual
 * feedback ratings (1-5 stars) with visual indicators, feedback comments and creation timestamps,
 * and associated guest and booking information. Only accessible to RECEPTIONIST and MANAGER roles.
 * </p>
 *
 * @author Arman Özcanli
 * @see Feedback
 * @see FeedbackService
 * @see BookingService
 * @see RoomCategoryService
 */
@Route(value = "feedback", layout = MainLayout.class)
@PageTitle("Feedback Management")
@CssImport("./themes/hotel/styles.css")
@RolesAllowed({UserRole.RECEPTIONIST_VALUE, UserRole.MANAGER_VALUE})
public class FeedbackView extends VerticalLayout {

    private final FeedbackService feedbackService;
    private final BookingService bookingService;
    private final RoomCategoryService roomCategoryService;

    private final Grid<Feedback> grid = new Grid<>(Feedback.class, false);
    private TextField searchField;

    private String selectedCategoryName = "All";
    private Component categoryStatsRow;

    /**
     * Constructs a FeedbackView with required service dependencies.
     *
     * @param feedbackService service for managing feedback operations
     * @param bookingService service for managing bookings
     * @param roomCategoryService service for managing room categories
     */
    public FeedbackView(FeedbackService feedbackService, BookingService bookingService, RoomCategoryService roomCategoryService) {
        this.feedbackService = feedbackService;
        this.bookingService = bookingService;
        this.roomCategoryService = roomCategoryService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        configureGrid();

        categoryStatsRow = createCategoryStatsRow();
        add(createHeader(), categoryStatsRow, createFilters(), createFeedbackCard());

        loadFeedback("");
    }

    /**
     * Configures the feedback grid with columns and styling.
     * <p>
     * Sets up grid variants for text wrapping and initializes feedback columns.
     * </p>
     */
    private void configureGrid() {
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

        grid.addColumn(this::getCategoryName)
            .setHeader("Category")
            .setSortable(true)
            .setAutoWidth(true)
            .setFlexGrow(0);

        grid.addComponentColumn(this::createRatingCell)
            .setHeader("Rating")
            .setSortable(true)
            .setComparator(Feedback::getRating)
            .setAutoWidth(true)
            .setFlexGrow(0);

        grid.addColumn(feedback -> feedback != null ? feedback.getComment() : null)
            .setHeader("Comment")
            .setSortable(true)
            .setFlexGrow(3);

        grid.addColumn(Feedback::getCreatedAt)
            .setHeader("Created At")
            .setSortable(true)
            .setAutoWidth(true)
            .setFlexGrow(0);

        grid.addComponentColumn(this::createFeedbackActions)
            .setHeader("Actions")
            .setAutoWidth(true)
            .setFlexGrow(0);

        grid.setWidthFull();
        grid.setAllRowsVisible(true);
    }

    private Component createFeedbackActions(Feedback feedback) {
        /**
         * Creates action buttons for a feedback entry.
         *
         * @param feedback the feedback entity
         * @return a Component with action buttons (delete)
         */
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button deleteBtn = new Button(VaadinIcon.TRASH.create());
        deleteBtn.addClassName("user-delete-action-btn");
        deleteBtn.addClickListener(e -> confirmDelete(feedback));

        actions.add(deleteBtn);
        return actions;
    }

    /**
     * Shows a confirmation dialog for deleting feedback.
     * <p>
     * Prompts the user to confirm feedback deletion. If confirmed, removes the feedback
     * from the database and refreshes the view.
     * </p>
     *
     * @param feedback the feedback entry to delete
     */
    private void confirmDelete(Feedback feedback) {
        if (feedback == null || feedback.getId() == null) {
            Notification.show("Cannot delete: missing feedback id", 3000, Notification.Position.TOP_CENTER);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Delete Feedback");
        dialog.setWidth("420px");

        Paragraph message = new Paragraph("Delete this feedback permanently? This cannot be undone.");
        message.getStyle().set("margin", "0");

        Button confirmBtn = new Button("Delete");
        confirmBtn.addClassName("logout-btn-header");
        confirmBtn.addClickListener(e -> {
            try {
                feedbackService.deleteById(feedback.getId());
                refreshCategoryStatsRow();
                loadFeedback(searchField != null ? searchField.getValue() : "");
                dialog.close();
                Notification.show("Feedback deleted successfully", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Error deleting feedback: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER);
                ex.printStackTrace();
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> dialog.close());

        dialog.add(message);
        dialog.getFooter().add(new HorizontalLayout(cancelBtn, confirmBtn));
        dialog.open();
    }

    /**
     * Refreshes the category statistics row with updated data.
     */
    private void refreshCategoryStatsRow() {
        Component newRow = createCategoryStatsRow();
        if (categoryStatsRow != null) {
            replace(categoryStatsRow, newRow);
        }
        categoryStatsRow = newRow;
    }

    /**
     * Creates a visual rating cell with star icon.
     *
     * @param feedback the feedback entry
     * @return a Component displaying the rating with star
     */
    private Component createRatingCell(Feedback feedback) {
        Integer rating = feedback != null ? feedback.getRating() : null;
        String ratingText = rating != null ? String.valueOf(rating) : "—";

        Span text = new Span(ratingText);

        Icon star = VaadinIcon.STAR.create();
        star.getStyle().set("margin-left", "0.25rem");

        HorizontalLayout layout = new HorizontalLayout(text, star);
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        return layout;
    }

    /**
     * Creates the view header component.
     *
     * @return a Component containing the title and subtitle
     */
    private Component createHeader() {
        H1 title = new H1("Feedback Management");
        Paragraph subtitle = new Paragraph("Manage and search customer feedback");
        Div headerLeft = new Div(title, subtitle);

        HorizontalLayout header = new HorizontalLayout(headerLeft);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        return header;
    }

    /**
     * Creates the category statistics row showing average ratings per category.
     * <p>
     * Displays rating cards for "All" and each available room category, allowing users
     * to click on a category to filter feedback.
     * </p>
     *
     * @return a Component with category rating cards
     */
    private Component createCategoryStatsRow() {
        List<RoomCategory> categories = roomCategoryService.getAllRoomCategories();

        Component[] cards = new Component[categories.size() + 1];
        cards[0] = createCategoryCard("All", calculateOverallAverage(), true);

        for (int i = 0; i < categories.size(); i++) {
            RoomCategory category = categories.get(i);
            double avg = bookingService.getAverageRatingForCategory(category);
            cards[i + 1] = createCategoryCard(category.getName(), avg, false);
        }

        HorizontalLayout row = CardFactory.createStatsRow(cards);
        row.setPadding(false);
        return row;
    }

    /**
     * Creates a clickable category card for filtering feedback.
     *
     * @param categoryName the name of the category
     * @param avg the average rating for the category
     * @param isAll whether this is the "All" card
     * @return a Component card for the category
     */
    private Component createCategoryCard(String categoryName, double avg, boolean isAll) {
        String value = avg > 0d ? String.format(Locale.US, "%.1f", avg) : "—";
        Div card = CardFactory.createStatCard(categoryName, value, VaadinIcon.STAR);

        card.getStyle().set("cursor", "pointer");
        card.addClickListener(e -> {
            selectedCategoryName = categoryName != null ? categoryName : "All";
            loadFeedback(searchField != null ? searchField.getValue() : "");
        });

        card.getElement().setProperty(
                "title",
                isAll ? "Show all feedback" : "Filter by " + categoryName
        );

        return card;
    }

    /**
     * Calculates the overall average rating from all feedback entries.
     *
     * @return the average rating, or 0.0 if no feedback exists
     */
    private double calculateOverallAverage() {
        return feedbackService.findAll().stream()
                .filter(f -> f != null && f.getRating() != null)
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0d);
    }

    /**
     * Creates the search and filter component.
     *
     * @return a Component with search field and button
     */
    private Component createFilters() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();

        H3 title = new H3("Search & Filter");
        Paragraph subtitle = new Paragraph("Find specific feedback quickly");

        searchField = new TextField("Search");
        searchField.setPlaceholder("Rating or comment...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());

        FormLayout form = new FormLayout(searchField);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1)
        );

        Button searchButton = new Button("Search", VaadinIcon.SEARCH.create());
        searchButton.addClassName("primary-button");
        searchButton.addClickListener(e -> loadFeedback(searchField.getValue()));
        searchButton.addClickShortcut(Key.ENTER);

        HorizontalLayout buttonLayout = new HorizontalLayout(searchButton);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        card.add(title, subtitle, form, buttonLayout);
        return card;
    }

    /**
     * Creates the feedback grid card component.
     *
     * @return a Component with the feedback grid
     */
    private Component createFeedbackCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();

        H3 title = new H3("Feedback");
        card.add(title, grid);
        return card;
    }

    /**
     * Loads and displays feedback items based on category and search filters.
     * <p>
     * Applies both category filtering and search query filtering before displaying results.
     * </p>
     *
     * @param query the search query string
     */
    private void loadFeedback(String query) {
        List<Feedback> items = feedbackService.findAll();
        items = applyCategoryFilter(items, selectedCategoryName);
        items = applySearchFilter(items, query);
        grid.setItems(items);
    }

    /**
     * Filters feedback items by room category.
     *
     * @param items the list of feedback items to filter
     * @param categoryName the category name to filter by, or "All" for all categories
     * @return the filtered list of feedback items
     */
    private List<Feedback> applyCategoryFilter(List<Feedback> items, String categoryName) {
        if (items == null || items.isEmpty()) {
            return items;
        }
        if (categoryName == null || categoryName.isBlank() || "All".equals(categoryName)) {
            return items;
        }
        return items.stream()
                .filter(f -> categoryName.equals(getCategoryName(f)))
                .collect(Collectors.toList());
    }

    /**
     * Filters feedback items by search query.
     * <p>
     * Supports searching by rating (integer) or comment text (case-insensitive substring match).
     * </p>
     *
     * @param items the list of feedback items to filter
     * @param query the search query string
     * @return the filtered list of feedback items
     */
    private List<Feedback> applySearchFilter(List<Feedback> items, String query) {
        if (query == null || query.isBlank()) {
            return items;
        }
        return items.stream()
                .filter(feedback -> matchesFeedback(feedback, query))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a feedback entry matches the given search query.
     * <p>
     * Tries to match as a rating (integer), falls back to comment text search (case-insensitive).
     * </p>
     *
     * @param feedback the feedback entry to check
     * @param query the search query
     * @return true if the feedback matches the query, false otherwise
     */
    private boolean matchesFeedback(Feedback feedback, String query) {
        if (feedback == null) {
            return false;
        }

        String trimmed = query.trim();
        try {
            Integer rating = Integer.parseInt(trimmed);
            return feedback.getRating() != null && feedback.getRating().equals(rating);
        } catch (NumberFormatException ignored) {
            return feedback.getComment() != null
                    && feedback.getComment().toLowerCase(Locale.ROOT).contains(trimmed.toLowerCase(Locale.ROOT));
        }
    }

    /**
     * Gets the room category name for a feedback entry.
     *
     * @param feedback the feedback entry
     * @return the category name, or "—" if not available
     */
    private String getCategoryName(Feedback feedback) {
        if (feedback == null) {
            return "—";
        }
        Booking booking = feedback.getBooking();
        if (booking == null || booking.getRoomCategory() == null || booking.getRoomCategory().getName() == null) {
            return "—";
        }
        return booking.getRoomCategory().getName();
    }

}
