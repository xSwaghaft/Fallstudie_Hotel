package com.hotel.booking.view;

import com.hotel.booking.entity.Feedback;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.hotel.booking.service.FeedbackService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

// Uses FeedbackFDO for form binding
@Route(value = "feedback", layout = MainLayout.class)
@CssImport("./themes/hotel/styles.css")
public class FeedbackView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private final FeedbackService feedbackService;
    private Grid<Feedback> grid;

    // FDO for Feedback form - simple JavaBean for UI binding
    public static class FeedbackFDO {
        private Integer rating = 5;
        private String comment = "";

        // Getters and setters
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }

    public FeedbackView(SessionService sessionService, FeedbackService feedbackService) {
        this.sessionService = sessionService;
        this.feedbackService = feedbackService;

        //simple layout 
        VerticalLayout layout = new VerticalLayout();

        TextField searchField = new TextField("Search Feedback (by rating or comment)");
        Button searchButton = new Button("Search");
        Button addButton = new Button("Add Feedback");

        HorizontalLayout buttonLayout = new HorizontalLayout(searchButton, addButton);

        //grid 
        grid = new Grid<>(Feedback.class, false);
        grid.addColumn(Feedback::getId).setHeader("ID").setSortable(true);
        grid.addColumn(Feedback::getRating).setHeader("Rating").setSortable(true);
        grid.addColumn(feedback -> 
            feedback.getComment() != null && feedback.getComment().length() > 50 ? 
            feedback.getComment().substring(0, 50) + "..." : 
            feedback.getComment()
        ).setHeader("Comment").setSortable(true);
        grid.addColumn(Feedback::getCreatedAt).setHeader("Created At").setSortable(true);

        // Load initial data (all feedback)
        loadFeedback("");

        // Search button click
        searchButton.addClickListener(e -> {
            String query = searchField.getValue();
            loadFeedback(query);
        });
        searchButton.addClickShortcut(Key.ENTER);

        // Add button click
        addButton.addClickListener(e -> openAddFeedbackDialog());

        layout.add(searchField, buttonLayout, grid);
        var link = new RouterLink("Home", MainLayout.class);
        add(link, layout);

        add(new H1("Feedback Management"), new Span("Manage and search customer feedback"));
    }

    //search method
    private void loadFeedback(String query) {
        List<Feedback> items;
        if (query == null || query.isBlank()) {
            items = feedbackService.findAll();
        } else {
            // Search by rating or comment
            items = feedbackService.findAll().stream()
                    .filter(feedback -> {
                        // Try to match rating first
                        try {
                            Integer rating = Integer.parseInt(query);
                            return feedback.getRating() != null && feedback.getRating().equals(rating);
                        } catch (NumberFormatException e) {
                            // If not a number, search in comment
                            return feedback.getComment() != null && 
                                    feedback.getComment().toLowerCase().contains(query.toLowerCase());
                        }
                    })
                    .collect(Collectors.toList());
        }
        grid.setItems(items);
    }

    //dialog for adding new feedback with Binder
    private void openAddFeedbackDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add New Feedback");

        // Create FDO and Binder
        FeedbackFDO formData = new FeedbackFDO();
        Binder<FeedbackFDO> binder = new Binder<>(FeedbackFDO.class);

        // Form fields
        Select<Integer> ratingSelect = new Select<>();
        ratingSelect.setLabel("Rating");
        ratingSelect.setItems(1, 2, 3, 4, 5);
        
        TextArea commentArea = new TextArea("Comment");
        commentArea.setPlaceholder("Enter your feedback here...");
        commentArea.setMaxLength(1000);

        // Bind fields to FDO using Binder
        binder.forField(ratingSelect)
                .asRequired("Rating is required")
                .bind(FeedbackFDO::getRating, FeedbackFDO::setRating);
        
        binder.forField(commentArea)
                .bind(FeedbackFDO::getComment, FeedbackFDO::setComment);

        // Set default values
        binder.readBean(formData);

        // Form layout
        FormLayout formLayout = new FormLayout();
        formLayout.add(ratingSelect, commentArea);
        
        // Buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");
        
        saveButton.addClickListener(e -> {
            try {
                binder.writeBean(formData);
                
                // Create new Feedback entity from FDO
                Feedback newFeedback = new Feedback();
                newFeedback.setRating(formData.getRating());
                newFeedback.setComment(formData.getComment());
                
                // Save through service
                feedbackService.save(newFeedback);
                
                // Refresh grid and close dialog
                loadFeedback("");
                dialog.close();
                Notification.show("Feedback saved successfully!");
                
            } catch (ValidationException ex) {
                Notification.show("Please fix the errors in the form");
            }
        });
        
        cancelButton.addClickListener(e -> dialog.close());
        
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(formLayout, buttonLayout);
        dialog.open();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasAnyRole(UserRole.RECEPTIONIST, UserRole.MANAGER)) {
            event.rerouteTo(LoginView.class);
        }
    }
}
