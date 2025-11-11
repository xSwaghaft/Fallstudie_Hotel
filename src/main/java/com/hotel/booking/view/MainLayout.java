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
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;

@CssImport("./themes/hotel/styles.css")
public class MainLayout extends AppLayout {

    private final SessionService sessionService;

    @Autowired
    public MainLayout(SessionService sessionService) {
        this.sessionService = sessionService;

        setPrimarySection(Section.DRAWER);
        createHeader();
        createDrawer();

        // Theme restoration on page load
        UI.getCurrent().getPage().executeJs(
            "const theme = localStorage.getItem('theme');" +
            "if(theme === 'dark') {" +
            "  document.documentElement.classList.add('dark-theme');" +
            "}"
        );
    }

    /* =========================================================
       HEADER
       ========================================================= */
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

        Icon themeIcon = VaadinIcon.MOON.create();
        themeIcon.addClassName("theme-icon");
        Button themeToggle = new Button(themeIcon);
        themeToggle.addClassName("theme-toggle-btn");
        themeToggle.addClickListener(e -> toggleTheme());

        Icon logoutIcon = VaadinIcon.SIGN_OUT.create();
        Button logoutButton = new Button("Logout", logoutIcon);
        logoutButton.addClassName("logout-btn-header");
        logoutButton.addClickListener(e -> showLogoutDialog());

        HorizontalLayout headerRight = new HorizontalLayout(avatar, themeToggle, logoutButton);
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
    private void createDrawer() {
        VerticalLayout drawer = new VerticalLayout();
        drawer.addClassName("main-drawer");

        // Logo/Title
        H2 logoTitle = new H2("Hotelium");
        logoTitle.addClassName("drawer-logo");

        // Subtitle based on role
        UserRole role = sessionService.getCurrentRole();
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
                createNavLink("My Reviews", MyReviewsView.class, VaadinIcon.COMMENT)
            );
        } else if (role == UserRole.RECEPTIONIST) {
            navLinks.add(
                createNavLink("Dashboard", DashboardView.class, VaadinIcon.DASHBOARD),
                createNavLink("Bookings", BookingManagementView.class, VaadinIcon.CALENDAR),
                createNavLink("Room Management", RoomManagementView.class, VaadinIcon.BED)
            );
        } else if (role == UserRole.MANAGER) {
            navLinks.add(
                createNavLink("Dashboard", DashboardView.class, VaadinIcon.DASHBOARD),
                createNavLink("Bookings", BookingManagementView.class, VaadinIcon.CALENDAR),
                createNavLink("Room Management", RoomManagementView.class, VaadinIcon.BED),
                createNavLink("User Management", UserManagementView.class, VaadinIcon.USERS),
                createNavLink("Invoices", InvoiceView.class, VaadinIcon.FILE_TEXT),
                createNavLink("Reports & Analytics", ReportsView.class, VaadinIcon.CHART),
                createNavLink("Settings", SettingsView.class, VaadinIcon.COG)
            );
        }

        // Footer with Logout Button
        Div drawerFooter = new Div();
        drawerFooter.addClassName("drawer-footer");
        
        // Sidebar Logout Button (roter Hintergrund)
        Icon logoutIcon = VaadinIcon.SIGN_OUT.create();
        Button sidebarLogout = new Button("Logout", logoutIcon);
        sidebarLogout.addClassName("sidebar-logout-btn");
        sidebarLogout.setWidthFull();
        sidebarLogout.addClickListener(e -> showLogoutDialog());
        
        drawerFooter.add(sidebarLogout);

        drawer.add(drawerHeader, navLinks, drawerFooter);
        addToDrawer(drawer);
    }

    /* =========================================================
       HELPER METHODS
       ========================================================= */
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

    private void toggleTheme() {
        UI ui = UI.getCurrent();
        boolean isDark = ui.getElement().getClassList().contains("dark-theme");
        
        if (isDark) {
            ui.getElement().getClassList().remove("dark-theme");
            VaadinSession.getCurrent().setAttribute("darkMode", false);
            ui.getPage().executeJs("localStorage.setItem('theme', 'light'); document.documentElement.classList.remove('dark-theme');");
        } else {
            ui.getElement().getClassList().add("dark-theme");
            VaadinSession.getCurrent().setAttribute("darkMode", true);
            ui.getPage().executeJs("localStorage.setItem('theme', 'dark'); document.documentElement.classList.add('dark-theme');");
        }
    }
}