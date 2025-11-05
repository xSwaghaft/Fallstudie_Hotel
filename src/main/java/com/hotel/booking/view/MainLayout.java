package com.hotel.booking.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;

// Das MainLayout dient als gemeinsames AppLayout für alle Views, die es verwenden.
@CssImport("./styles/views/dashboard-view.css") // Importiert das Styling
public class MainLayout extends AppLayout {

    public MainLayout() {
        // AppLayout Basisstruktur wird hier definiert
        addToDrawer(createSidebar());
        addToNavbar(createHeader());
        
        // Fügt eine CSS-Klasse für globale Layout-Styles hinzu
        getElement().getClassList().add("main-app-layout");
        setPrimarySection(Section.DRAWER);
    }

    // --- 1. Seitenleiste (Navigation) ---
    private VerticalLayout createSidebar() {
        VerticalLayout navLayout = new VerticalLayout();
        navLayout.addClassName("sidebar-nav");

        // Logo/Titel
        HorizontalLayout logo = new HorizontalLayout(
            new Icon(VaadinIcon.BUILDING),
            new H3("HOTELIUM")
        );
        logo.setAlignItems(FlexComponent.Alignment.CENTER);

        navLayout.add(logo);

        // Navigationselemente verwenden RouterLink für die Navigation
        // Navigationen zeigen auf die View-Klassen. Views verwenden MainLayout als Layout.
        navLayout.add(
            createNavItem(VaadinIcon.DASHBOARD, "Dashboard", com.hotel.booking.view.MDashboardView.class), // Manager Dashboard
            createNavItem(VaadinIcon.CALENDAR, "Bookings", com.hotel.booking.view.MyBookingsView.class),
            createNavItem(VaadinIcon.HOME, "Room Management", com.hotel.booking.view.DashboardView.class),
            createNavItem(VaadinIcon.USERS, "User Management", com.hotel.booking.view.DashboardView.class),
            createNavItem(VaadinIcon.FILE_TEXT, "Invoices", com.hotel.booking.view.DashboardView.class),
            createNavItem(VaadinIcon.BAR_CHART, "Reports", com.hotel.booking.view.DashboardView.class)
        );
        
        return navLayout;
    }

    private Div createNavItem(VaadinIcon icon, String text, Class<? extends com.vaadin.flow.component.Component> navigationTarget) {
        // RouterLink verweist direkt auf die View-Klasse; HighlightCondition sorgt für Hervorhebung
        RouterLink link = new RouterLink(text, navigationTarget);
        link.setHighlightCondition(HighlightConditions.sameLocation()); // Hebt den Link bei gleicher Route hervor

        HorizontalLayout item = new HorizontalLayout(new Icon(icon), link);
        item.setAlignItems(FlexComponent.Alignment.CENTER);
        item.setWidthFull();
        item.addClassName("nav-item");
        
        // Die Klasse "selected-nav-item" wird automatisch durch RouterLink hinzugefügt, 
        // wenn HighlightConditions.sameLocation() verwendet wird.

        return new Div(item);
    }

    // --- 2. Navbar (Kopfzeile) ---
    private HorizontalLayout createHeader() {
        H1 title = new H1("Bookings");
        Span subtitle = new Span("Manage your bookings"); 
        
        VerticalLayout leftHeader = new VerticalLayout(title, subtitle);
        leftHeader.addClassName("header-title-area");

        // Rechts: Manager-Info
        Span managerName = new Span("guest");
        VerticalLayout managerInfo = new VerticalLayout(managerName);
        managerInfo.addClassName("manager-info");
        
        Div avatar = new Div(new Span("M"));
        avatar.addClassName("avatar-circle");

        HorizontalLayout rightHeader = new HorizontalLayout(managerInfo, avatar, new Icon(VaadinIcon.ARROW_RIGHT));
        rightHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        rightHeader.addClassName("header-profile-area");

        HorizontalLayout header = new HorizontalLayout(leftHeader, rightHeader);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.addClassName("main-header");
        
        return header;
    }
}