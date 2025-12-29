package com.hotel.booking.view;

import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.PermitAll;

@CssImport("./themes/hotel/styles.css")
/**
 * Main application layout component with navigation and header.
 * <p>
 * Provides a responsive app layout with drawer navigation, header with user info,
 * and role-based menu items. The navigation items are dynamically shown based on
 * the user's role (GUEST, RECEPTIONIST, or MANAGER).
 * </p>
 *
 * @author Artur Derr
 */
@PermitAll
public class MainLayout extends AppLayout {

    private final SessionService sessionService;

    /**
     * Constructs a MainLayout with header and navigation drawer.
     * <p>
     * Initializes the application layout with a primary drawer section and creates
     * the header with welcome message and logout button, as well as the navigation drawer
     * with role-based menu items.
     * </p>
     *
     * @param sessionService the service for managing user session and authentication
     */
    public MainLayout(SessionService sessionService) {
        this.sessionService = sessionService;

        setPrimarySection(Section.DRAWER);
        createHeader();
        createDrawer();
    }

    /* =========================================================
       HEADER
       ========================================================= */
    /**
     * Creates and configures the application header.
     * <p>
     * The header displays a drawer toggle, welcome message with username,
     * user avatar, and logout button.
     * </p>
     */
    private void createHeader() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.addClassName("drawer-toggle");

        // Welcome message
        String username = sessionService.getCurrentUser() != null
                ? sessionService.getCurrentUser().getUsername()
                : "Guest";
        
        Div welcomeContainer = new Div();
        welcomeContainer.addClassName("welcome-container");
        
        Span welcomeText = new Span("Welcome back,");
        welcomeText.addClassName("welcome-text");
        
        Span usernameText = new Span(username);
        usernameText.addClassName("username-text");
        
        welcomeContainer.add(welcomeText, usernameText);

        HorizontalLayout headerLeft = new HorizontalLayout(toggle, welcomeContainer);
        headerLeft.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLeft.addClassName("header-left");

        // Right side: Avatar, Theme Toggle, Logout
        Avatar avatar = new Avatar(username);
        avatar.addClassName("user-avatar");

        Icon logoutIcon = VaadinIcon.SIGN_OUT.create();
        Button logoutButton = new Button("Logout", logoutIcon);
        logoutButton.addClassName("logout-btn-header");
        logoutButton.addClickListener(e -> showLogoutDialog());

        HorizontalLayout headerRight = new HorizontalLayout(avatar, logoutButton);
        headerRight.setAlignItems(FlexComponent.Alignment.CENTER);
        headerRight.addClassName("header-right");

        HorizontalLayout header = new HorizontalLayout(headerLeft, headerRight);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.addClassName("main-header");

        addToNavbar(header);
    }

    /* =========================================================
       DRAWER / SIDEBAR
       ========================================================= */
    /**
     * Creates and configures the navigation drawer.
     * <p>
     * The drawer displays the application logo, role-specific subtitle, and
     * navigation links. The menu items are dynamically generated based on the user's role.
     * </p>
     */
    private void createDrawer() {
        VerticalLayout drawer = new VerticalLayout();
        drawer.addClassName("main-drawer");

        // Logo/Title
        H2 logoTitle = new H2("Hotelium");
        logoTitle.addClassName("drawer-logo");

        // Subtitle based on role
        UserRole role = sessionService.getCurrentRole();
        // If no role is available (not logged in), treat as GUEST for drawer rendering
        if (role == null) {
            role = UserRole.GUEST;
        }
        String subtitle = switch (role) {
            case GUEST -> "Guest Portal";
            case RECEPTIONIST -> "Receptionist Portal";
            case MANAGER -> "Management Portal";
            default -> "Portal";
        };
        
        Paragraph subtitleText = new Paragraph(subtitle);
        subtitleText.addClassName("drawer-subtitle");

        Div drawerHeader = new Div(logoTitle, subtitleText);
        drawerHeader.addClassName("drawer-header");

        // Navigation Links
        VerticalLayout navLinks = new VerticalLayout();
        navLinks.addClassName("nav-links");

        if (role == UserRole.GUEST) {
            navLinks.add(
                createNavLink("Search Rooms", GuestPortalView.class, VaadinIcon.BED),
                createNavLink("My Bookings", MyBookingsView.class, VaadinIcon.CALENDAR),
                createNavLink("My Payments", PaymentView.class, VaadinIcon.CREDIT_CARD),
                createNavLink("My Reviews", MyReviewsView.class, VaadinIcon.COMMENT)
            );
        } else if (role == UserRole.RECEPTIONIST) {
            navLinks.add(
                createNavLink("Dashboard", DashboardView.class, VaadinIcon.DASHBOARD),
                createNavLink("Bookings", BookingManagementView.class, VaadinIcon.CALENDAR),
                createNavLink("Payments", PaymentView.class, VaadinIcon.CREDIT_CARD),
                createNavLink("Invoices", InvoiceView.class, VaadinIcon.FILE_TEXT),
                createNavLink("Room Management", RoomManagementView.class, VaadinIcon.BED),
                createNavLink("Image Management", ImageManagementView.class, VaadinIcon.PICTURE)
            );
        } else if (role == UserRole.MANAGER) {
            navLinks.add(
                createNavLink("Dashboard", DashboardView.class, VaadinIcon.DASHBOARD),
                createNavLink("Bookings", BookingManagementView.class, VaadinIcon.CALENDAR),
                createNavLink("Payments", PaymentView.class, VaadinIcon.CREDIT_CARD),
                createNavLink("Invoices", InvoiceView.class, VaadinIcon.FILE_TEXT),
                createNavLink("Room Management", RoomManagementView.class, VaadinIcon.BED),
                createNavLink("Image Management", ImageManagementView.class, VaadinIcon.PICTURE),
                createNavLink("User Management", UserManagementView.class, VaadinIcon.USERS),
                createNavLink("Reports & Analytics", ReportsView.class, VaadinIcon.CHART)
            );
        }

        drawer.add(drawerHeader, navLinks);
        addToDrawer(drawer);
    }

    /* =========================================================
       HELPER METHODS
       ========================================================= */
    /**
     * Creates a navigation link for the drawer menu.
     *
     * @param text the display text for the navigation link
     * @param target the Vaadin View class to navigate to
     * @param iconType the VaadinIcon to display next to the link text
     * @return a RouterLink configured for navigation
     */
    private RouterLink createNavLink(String text, Class<? extends com.vaadin.flow.component.Component> target, VaadinIcon iconType) {
        Icon icon = iconType.create();
        icon.addClassName("nav-icon");

        RouterLink link = new RouterLink();
        link.add(icon, new Span(text));
        link.setRoute(target);
        link.setHighlightCondition(HighlightConditions.sameLocation());
        link.addClassName("nav-link");

        return link;
    }

    /**
     * Displays a confirmation dialog for user logout.
     * <p>
     * Shows a modal dialog asking the user to confirm their logout action.
     * Upon confirmation, the user is logged out and redirected to the home page.
     * </p>
     */
    private void showLogoutDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Confirm Logout");
        dialog.setWidth("400px");
        
        VerticalLayout content = new VerticalLayout();
        content.add(new Paragraph("Are you sure you want to log out?"));
        content.setPadding(false);
        
        Button confirmBtn = new Button("Yes, Logout");
        confirmBtn.addClassName("logout-btn-header");
        confirmBtn.addClickListener(e -> {
            sessionService.logout();
            dialog.close();
            UI.getCurrent().getPage().setLocation("/");
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> dialog.close());
        
        HorizontalLayout buttons = new HorizontalLayout(cancelBtn, confirmBtn);
        buttons.setSpacing(true);
        
        dialog.add(content);
        dialog.getFooter().add(buttons);
        dialog.open();
    }
}