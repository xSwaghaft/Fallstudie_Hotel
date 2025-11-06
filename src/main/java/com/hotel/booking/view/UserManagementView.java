package com.hotel.booking.view;

import com.hotel.booking.security.SessionService;
import com.hotel.booking.security.UserRole;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Route(value = "user-management", layout = MainLayout.class)
@CssImport("./themes/hotel/styles.css")
public class UserManagementView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private static final DateTimeFormatter GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static class User implements Serializable {
        private static final long serialVersionUID = 1L;
        private int id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String role; // "MANAGER", "RECEPTIONIST", "GUEST"
        private boolean active;
        private LocalDate createdAt;
        private LocalDate lastLogin;

        public User(int id, String username, String email, String firstName, String lastName, 
                   String role, boolean active, LocalDate createdAt, LocalDate lastLogin) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.role = role;
            this.active = active;
            this.createdAt = createdAt;
            this.lastLogin = lastLogin;
        }

        // Getters and Setters
        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getFullName() { return firstName + " " + lastName; }
        public String getRole() { return role; }
        public boolean isActive() { return active; }
        public LocalDate getCreatedAt() { return createdAt; }
        public LocalDate getLastLogin() { return lastLogin; }

        public void setUsername(String username) { this.username = username; }
        public void setEmail(String email) { this.email = email; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public void setRole(String role) { this.role = role; }
        public void setActive(boolean active) { this.active = active; }
        public void setLastLogin(LocalDate lastLogin) { this.lastLogin = lastLogin; }
    }

    private final Grid<User> grid = new Grid<>(User.class, false);
    private final List<User> users = new ArrayList<>();
    private TextField searchField;
    private Select<String> roleFilter;

    @Autowired
    public UserManagementView(SessionService sessionService) {
        this.sessionService = sessionService;
        setSpacing(true);
        setPadding(true);
        setSizeFull();

        seedData();

        add(createHeader(), createStatsRow(), createFilters(), createUsersCard());
    }

    private void seedData() {
        users.clear();
        users.add(new User(1, "david.manager", "david@hotelium.com", "David", "Manager", 
                          "MANAGER", true, LocalDate.of(2024, 1, 15), LocalDate.of(2025, 11, 5)));
        users.add(new User(2, "sarah.receptionist", "sarah@hotelium.com", "Sarah", "Johnson", 
                          "RECEPTIONIST", true, LocalDate.of(2024, 3, 20), LocalDate.of(2025, 11, 4)));
        users.add(new User(3, "john.guest", "john@email.com", "John", "Guest", 
                          "GUEST", true, LocalDate.of(2024, 6, 10), LocalDate.of(2025, 11, 3)));
        users.add(new User(4, "emma.receptionist", "emma@hotelium.com", "Emma", "Wilson", 
                          "RECEPTIONIST", true, LocalDate.of(2024, 7, 1), LocalDate.of(2025, 11, 5)));
        users.add(new User(5, "michael.guest", "michael@email.com", "Michael", "Brown", 
                          "GUEST", true, LocalDate.of(2024, 8, 15), LocalDate.of(2025, 10, 28)));
        users.add(new User(6, "lisa.guest", "lisa@email.com", "Lisa", "Anderson", 
                          "GUEST", false, LocalDate.of(2024, 9, 20), LocalDate.of(2025, 9, 15)));
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
        Div card3 = createStatCard("Managers", 
                String.valueOf(users.stream().filter(u -> "MANAGER".equals(u.getRole())).count()), VaadinIcon.USER_STAR);
        Div card4 = createStatCard("Guests", 
                String.valueOf(users.stream().filter(u -> "GUEST".equals(u.getRole())).count()), VaadinIcon.USER);
        
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
                    user.getEmail().toLowerCase().contains(search) ||
                    user.getFullName().toLowerCase().contains(search);
                
                String role = roleFilter.getValue();
                boolean matchesRole = "All Roles".equals(role) || user.getRole().equals(role);
                
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
        
        grid.addColumn(User::getEmail)
            .setHeader("Email")
            .setFlexGrow(1);
        
        grid.addComponentColumn(this::createRoleBadge)
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
        
        grid.addColumn(user -> user.getLastLogin() != null ? 
                user.getLastLogin().format(GERMAN_DATE_FORMAT) : "Never")
            .setHeader("Last Login")
            .setAutoWidth(true)
            .setFlexGrow(0);
        
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
        Span badge = new Span(user.getRole());
        badge.getStyle()
            .set("padding", "0.25rem 0.75rem")
            .set("border-radius", "0.5rem")
            .set("font-size", "0.875rem")
            .set("font-weight", "600")
            .set("text-transform", "capitalize");
        
        switch (user.getRole()) {
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

    private void openUserDialog(User existingUser) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existingUser == null ? "Add New User" : "Edit User");
        dialog.setWidth("600px");

        TextField username = new TextField("Username*");
        username.setWidthFull();
        
        EmailField email = new EmailField("Email*");
        email.setWidthFull();
        
        TextField firstName = new TextField("First Name*");
        firstName.setWidthFull();
        
        TextField lastName = new TextField("Last Name*");
        lastName.setWidthFull();
        
        Select<String> role = new Select<>();
        role.setLabel("Role*");
        role.setItems("MANAGER", "RECEPTIONIST", "GUEST");
        role.setWidthFull();
        
        PasswordField password = new PasswordField(existingUser == null ? "Password*" : "New Password (leave empty to keep current)");
        password.setWidthFull();
        
        Checkbox active = new Checkbox("Active");
        active.setValue(true);

        if (existingUser != null) {
            username.setValue(existingUser.getUsername());
            email.setValue(existingUser.getEmail());
            firstName.setValue(existingUser.getFirstName());
            lastName.setValue(existingUser.getLastName());
            role.setValue(existingUser.getRole());
            active.setValue(existingUser.isActive());
        }

        FormLayout form = new FormLayout(username, email, firstName, lastName, role, password, active);
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("600px", 2)
        );
        form.setColspan(password, 2);
        form.setColspan(active, 2);

        Button saveBtn = new Button(existingUser == null ? "Add User" : "Update User");
        saveBtn.addClassName("primary-button");
        saveBtn.addClickListener(e -> {
            if (existingUser == null) {
                User newUser = new User(
                    users.stream().mapToInt(User::getId).max().orElse(0) + 1,
                    username.getValue(),
                    email.getValue(),
                    firstName.getValue(),
                    lastName.getValue(),
                    role.getValue(),
                    active.getValue(),
                    LocalDate.now(),
                    null
                );
                users.add(newUser);
                Notification.show("User added successfully");
            } else {
                existingUser.setUsername(username.getValue());
                existingUser.setEmail(email.getValue());
                existingUser.setFirstName(firstName.getValue());
                existingUser.setLastName(lastName.getValue());
                existingUser.setRole(role.getValue());
                existingUser.setActive(active.getValue());
                Notification.show("User updated successfully");
            }
            grid.getDataProvider().refreshAll();
            dialog.close();
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> dialog.close());

        dialog.add(new VerticalLayout(form));
        dialog.getFooter().add(new HorizontalLayout(cancelBtn, saveBtn));
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
        content.add(createDetailRow("Email", user.getEmail()));
        content.add(createDetailRow("Role", user.getRole()));
        content.add(createDetailRow("Status", user.isActive() ? "Active" : "Inactive"));
        content.add(createDetailRow("Created", user.getCreatedAt().format(GERMAN_DATE_FORMAT)));
        content.add(createDetailRow("Last Login", 
            user.getLastLogin() != null ? user.getLastLogin().format(GERMAN_DATE_FORMAT) : "Never"));

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