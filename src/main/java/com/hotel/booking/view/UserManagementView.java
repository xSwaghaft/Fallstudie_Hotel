package com.hotel.booking.view;

import com.hotel.booking.entity.UserRole;
import com.hotel.booking.entity.User;
import com.hotel.booking.service.UserService;
import com.hotel.booking.security.SessionService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.*;
import com.vaadin.flow.component.dependency.CssImport;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@Route(value = "user-management", layout = MainLayout.class)
@CssImport("./themes/hotel/styles.css")
public class UserManagementView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private final UserService userService;
    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Grid<User> grid = new Grid<>(User.class, false);
    private final List<User> users = new ArrayList<>();
    private TextField searchField;
    private Select<String> roleFilter;

    public UserManagementView(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
        setSpacing(true);
        setPadding(true);
        setSizeFull();
        users.addAll(userService.findAll());
        add(createHeader(), createStatsRow(), createFilters(), createUsersCard());
    }

    private Component createHeader() {
        H1 title = new H1("User Management");
        title.getStyle().set("margin", "0");
        
        Paragraph subtitle = new Paragraph("Manage system users and their permissions");
        

        subtitle.getStyle().set("margin", "0");
        
        Div headerLeft = new Div(title, subtitle);
        
        Button addUser = new Button("Add User", VaadinIcon.PLUS.create());
        addUser.addClassName("primary-button");
        addUser.addClickListener(e -> openUserDialog(null));
        
        HorizontalLayout header = new HorizontalLayout(headerLeft, addUser);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        
        return header;
    }

    private Component createStatsRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);

        Div card1 = createStatCard("Total Users", String.valueOf(users.size()), VaadinIcon.USERS);
        Div card2 = createStatCard("Active Users", 
                String.valueOf(users.stream().filter(User::isActive).count()), VaadinIcon.CHECK_CIRCLE);
        Div card3 = createStatCard("Employees", 
                String.valueOf(users.stream().filter(u -> u.getRole() == UserRole.MANAGER || u.getRole() == UserRole.RECEPTIONIST).count()), VaadinIcon.USER_STAR);
        Div card4 = createStatCard("Guests", 
                String.valueOf(users.stream().filter(u -> "GUEST".equals(u.getRole().name())).count()), VaadinIcon.USER);
        
        row.add(card1, card2, card3, card4);
        row.expand(card1, card2, card3, card4);

        return row;
    }

    private Div createStatCard(String label, String value, VaadinIcon iconType) {
        Div card = new Div();
        card.addClassName("kpi-card");
        
        Span labelSpan = new Span(label);
        labelSpan.addClassName("kpi-card-title");
        
        Icon icon = iconType.create();
        icon.addClassName("kpi-card-icon");
        
        HorizontalLayout cardHeader = new HorizontalLayout(labelSpan, icon);
        cardHeader.setWidthFull();
        cardHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        cardHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        cardHeader.getStyle().set("margin-bottom", "0.5rem");
        
        H2 valueHeading = new H2(value);
        valueHeading.getStyle().set("margin", "0");
        
        card.add(cardHeader, valueHeading);
        return card;
    }

    private Component createFilters() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        
        H3 title = new H3("Search & Filter");
        title.getStyle().set("margin", "0 0 0.5rem 0");
        
        Paragraph subtitle = new Paragraph("Find specific users quickly");
        subtitle.getStyle().set("margin", "0 0 1rem 0");

        searchField = new TextField("Search");
        searchField.setPlaceholder("Username, email, or name...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.addValueChangeListener(e -> filterUsers());

        roleFilter = new Select<>();
        roleFilter.setLabel("Role");
        roleFilter.setItems("All Roles", "MANAGER", "RECEPTIONIST", "GUEST");
        roleFilter.setValue("All Roles");
        roleFilter.addValueChangeListener(e -> filterUsers());

        Select<String> statusFilter = new Select<>();
        statusFilter.setLabel("Status");
        statusFilter.setItems("All Status", "Active", "Inactive");
        statusFilter.setValue("All Status");
        statusFilter.addValueChangeListener(e -> filterUsers());

        FormLayout form = new FormLayout(searchField, roleFilter, statusFilter);
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("600px", 3)
        );

        card.add(title, subtitle, form);
        return card;
    }

    private void filterUsers() {
        List<User> filtered = users.stream()
            .filter(user -> {
                String search = searchField.getValue().toLowerCase();
                boolean matchesSearch = search.isEmpty() || 
                    user.getUsername().toLowerCase().contains(search) ||
                    user.getEmail().toLowerCase().contains(search);
                    user.getFullName().toLowerCase().contains(search);
                
                String role = roleFilter.getValue();
                boolean matchesRole = "All Roles".equals(role) || user.getRole().name().equals(role);
                
                return matchesSearch && matchesRole;
            })
            .toList();
        
        grid.setItems(filtered);
    }

    private Component createUsersCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        
        H3 title = new H3("All Users");
        title.getStyle().set("margin", "0 0 1rem 0");

        grid.addColumn(User::getId)
            .setHeader("ID")
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        grid.addColumn(User::getUsername)
            .setHeader("Username")
            .setFlexGrow(1);
        
        grid.addColumn(User::getFullName)
            .setHeader("Full Name")
            .setFlexGrow(1);
        
        grid.addColumn(User::getEmail)  //AddColumn für plain Text
            .setHeader("Email")
            .setFlexGrow(1);
        
        grid.addComponentColumn(this::createRoleBadge) //Componente für mehr als nur Text
            .setHeader("Role")
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        grid.addComponentColumn(this::createStatusBadge)
            .setHeader("Status")
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        grid.addColumn(user -> user.getCreatedAt().format(GERMAN_DATE_FORMAT))
            .setHeader("Created")
            .setAutoWidth(true)
            .setFlexGrow(0);
        
        // grid.addColumn(user -> user.getLastLogin() != null ? 
        //         user.getLastLogin().format(GERMAN_DATE_FORMAT) : "Never")
        //     .setHeader("Last Login")
        //     .setAutoWidth(true)
        //     .setFlexGrow(0);
        
        grid.addComponentColumn(this::createUserActions)
            .setHeader("Actions")
            .setAutoWidth(true)
            .setFlexGrow(0);

        grid.setItems(users);
        grid.setAllRowsVisible(true);
        grid.setWidthFull();

        card.add(title, grid);
        return card;
    }

    private Component createRoleBadge(User user) {
        Span badge = new Span(user.getRole().name());
        badge.getStyle()
            .set("padding", "0.25rem 0.75rem")
            .set("border-radius", "0.5rem")
            .set("font-size", "0.875rem")
            .set("font-weight", "600")
            .set("text-transform", "capitalize");
        
        switch (user.getRole().name()) {
            case "MANAGER" -> badge.getStyle()
                .set("background", "#fef3c7")
                .set("color", "#f59e0b");
            case "RECEPTIONIST" -> badge.getStyle()
                .set("background", "#dbeafe")
                .set("color", "#3b82f6");
            case "GUEST" -> badge.getStyle()
                .set("background", "#f3f4f6")
                .set("color", "#6b7280");
        }
        
        return badge;
    }

    private Component createStatusBadge(User user) {
        Span badge = new Span(user.isActive() ? "Active" : "Inactive");
        badge.addClassName("status-badge");
        badge.addClassName(user.isActive() ? "status-confirmed" : "status-pending");
        return badge;
    }

    private Component createUserActions(User user) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        
        Button viewBtn = new Button(VaadinIcon.EYE.create());
        viewBtn.addClickListener(e -> openUserDetailsDialog(user));
        
        Button editBtn = new Button(VaadinIcon.EDIT.create());
        editBtn.addClickListener(e -> openUserDialog(user));
        
        Button deleteBtn = new Button(VaadinIcon.TRASH.create());
        deleteBtn.getStyle().set("color", "#ef4444");
        deleteBtn.addClickListener(e -> confirmDelete(user));
        
        actions.add(viewBtn, editBtn, deleteBtn);
        return actions;
    }

    //Matthias Lohr (Dialog zum Hinzufügen/Bearbeiten von Benutzern)
    private void openUserDialog(User existingUser) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existingUser == null ? "Add New User" : "Edit User");
        dialog.setWidth("600px");

        AddUserForm form = new AddUserForm(existingUser, userService);

        Button saveButton = new Button("Save", e -> {
            try {
                form.writeBean(); // Überträgt die Formulardaten in das User-Objekt
                userService.save(form.getUser()); // Speichert das User-Objekt aus dem Formular in der Datenbank
                users.clear();
                users.addAll(userService.findAll());
                grid.getDataProvider().refreshAll();
                dialog.close();
                Notification.show("User saved successfully.", 3000, Notification.Position.BOTTOM_START);
            } catch (ValidationException ex) {
                Notification.show("Please fix validation errors before saving.", 3000, Notification.Position.MIDDLE);
            }
        });

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.addClassName("primary-button");

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(form, buttonLayout);

        dialog.open();
    }

    private void openUserDetailsDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("User Details - " + user.getUsername());
        dialog.setWidth("500px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        
        content.add(createDetailRow("ID", String.valueOf(user.getId())));
        content.add(createDetailRow("Username", user.getUsername()));
        content.add(createDetailRow("Full Name", user.getFullName()));
        content.add(createDetailRow("Address", user.getAdressString()));
        content.add(createDetailRow("Email", user.getEmail()));
        content.add(createDetailRow("Role", user.getRole().name()));
        content.add(createDetailRow("Status", user.isActive() ? "Active" : "Inactive"));
        content.add(createDetailRow("Created", user.getCreatedAt().format(GERMAN_DATE_FORMAT)));
        // content.add(createDetailRow("Last Login", 
        //     user.getLastLogin() != null ? user.getLastLogin().format(GERMAN_DATE_FORMAT) : "Never"));

        Button closeBtn = new Button("Close");
        closeBtn.addClickListener(e -> dialog.close());

        dialog.add(content);
        dialog.getFooter().add(closeBtn);
        dialog.open();
    }

    private Component createDetailRow(String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        row.getStyle().set("padding", "0.75rem 0").set("border-bottom", "1px solid #e5e7eb");
        
        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("font-weight", "600").set("color", "var(--color-text-secondary)");
        
        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("color", "var(--color-text-primary)");
        
        row.add(labelSpan, valueSpan);
        return row;
    }

    private void confirmDelete(User user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Confirm Delete");
        dialog.setWidth("400px");

        Paragraph message = new Paragraph("Are you sure you want to delete user " + user.getUsername() + "?");
        message.getStyle().set("margin", "0");

        Button confirmBtn = new Button("Yes, Delete");
        confirmBtn.addClassName("logout-btn-header");
        confirmBtn.addClickListener(e -> {
            userService.delete(user); // Use the service to delete the user from the database
            users.remove(user);
            grid.getDataProvider().refreshAll();
            dialog.close();
            Notification.show("User deleted successfully");
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> dialog.close());

        dialog.add(message);
        dialog.getFooter().add(new HorizontalLayout(cancelBtn, confirmBtn));
        dialog.open();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasRole(UserRole.MANAGER)) {
            event.rerouteTo(LoginView.class);
        }
    }
}