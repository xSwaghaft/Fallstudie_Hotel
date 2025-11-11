package com.hotel.booking.view;

import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalTime;

@Route(value = "settings", layout = MainLayout.class)
@CssImport("./themes/hotel/styles.css")
public class SettingsView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private Div contentArea;

    @Autowired
    public SettingsView(SessionService sessionService) {
        this.sessionService = sessionService;
        setSpacing(true);
        setPadding(true);
        setSizeFull();

        add(createHeader(), createTabsAndContent());
    }

    private Component createHeader() {
        H1 title = new H1("Settings");
        title.getStyle().set("margin", "0");
        
        Paragraph subtitle = new Paragraph("Configure hotel system settings and preferences");
        subtitle.getStyle().set("margin", "0");
        
        return new Div(title, subtitle);
    }

    private Component createTabsAndContent() {
        Tab generalTab = new Tab(VaadinIcon.COG.create(), new Span("General"));
        Tab bookingTab = new Tab(VaadinIcon.CALENDAR.create(), new Span("Booking"));
        Tab paymentTab = new Tab(VaadinIcon.CREDIT_CARD.create(), new Span("Payment"));
        Tab notificationTab = new Tab(VaadinIcon.BELL.create(), new Span("Notifications"));
        Tab securityTab = new Tab(VaadinIcon.LOCK.create(), new Span("Security"));
        
        Tabs tabs = new Tabs(generalTab, bookingTab, paymentTab, notificationTab, securityTab);
        tabs.setWidthFull();
        
        contentArea = new Div();
        contentArea.setWidthFull();
        
        // Initial content
        updateContent(0);
        
        tabs.addSelectedChangeListener(e -> {
            updateContent(tabs.getSelectedIndex());
        });
        
        VerticalLayout container = new VerticalLayout(tabs, contentArea);
        container.setSpacing(true);
        container.setPadding(false);
        container.setWidthFull();
        
        return container;
    }

    private void updateContent(int tabIndex) {
        contentArea.removeAll();
        
        switch (tabIndex) {
            case 0 -> contentArea.add(createGeneralSettings());
            case 1 -> contentArea.add(createBookingSettings());
            case 2 -> contentArea.add(createPaymentSettings());
            case 3 -> contentArea.add(createNotificationSettings());
            case 4 -> contentArea.add(createSecuritySettings());
        }
    }

    private Component createGeneralSettings() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);
        
        // Hotel Information
        Div hotelCard = new Div();
        hotelCard.addClassName("card");
        hotelCard.setWidthFull();
        
        H3 hotelTitle = new H3("Hotel Information");
        hotelTitle.getStyle().set("margin", "0 0 1rem 0");
        
        TextField hotelName = new TextField("Hotel Name");
        hotelName.setValue("Hotelium");
        hotelName.setWidthFull();
        
        TextField address = new TextField("Address");
        address.setValue("123 Luxury Avenue, Berlin, Germany");
        address.setWidthFull();
        
        EmailField email = new EmailField("Contact Email");
        email.setValue("contact@hotelium.com");
        email.setWidthFull();
        
        TextField phone = new TextField("Phone Number");
        phone.setValue("+49 30 12345678");
        phone.setWidthFull();
        
        Button saveHotel = new Button("Save Changes");
        saveHotel.addClassName("primary-button");
        saveHotel.addClickListener(e -> Notification.show("Hotel information saved"));
        
        hotelCard.add(hotelTitle, hotelName, address, email, phone, saveHotel);
        
        // Operational Settings
        Div operationalCard = new Div();
        operationalCard.addClassName("card");
        operationalCard.setWidthFull();
        
        H3 operationalTitle = new H3("Operational Settings");
        operationalTitle.getStyle().set("margin", "0 0 1rem 0");
        
        TimePicker checkInTime = new TimePicker("Default Check-in Time");
        checkInTime.setValue(LocalTime.of(14, 0));
        checkInTime.setWidthFull();
        
        TimePicker checkOutTime = new TimePicker("Default Check-out Time");
        checkOutTime.setValue(LocalTime.of(11, 0));
        checkOutTime.setWidthFull();
        
        Select<String> timezone = new Select<>();
        timezone.setLabel("Timezone");
        timezone.setItems("Europe/Berlin", "Europe/London", "America/New_York", "Asia/Tokyo");
        timezone.setValue("Europe/Berlin");
        timezone.setWidthFull();
        
        Select<String> currency = new Select<>();
        currency.setLabel("Currency");
        currency.setItems("EUR (€)", "USD ($)", "GBP (£)");
        currency.setValue("EUR (€)");
        currency.setWidthFull();
        
        Button saveOperational = new Button("Save Changes");
        saveOperational.addClassName("primary-button");
        saveOperational.addClickListener(e -> Notification.show("Operational settings saved"));
        
        operationalCard.add(operationalTitle, checkInTime, checkOutTime, timezone, currency, saveOperational);
        
        layout.add(hotelCard, operationalCard);
        return layout;
    }

    private Component createBookingSettings() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);
        
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        
        H3 title = new H3("Booking Configuration");
        title.getStyle().set("margin", "0 0 1rem 0");
        
        IntegerField maxAdvanceBooking = new IntegerField("Maximum Advance Booking (days)");
        maxAdvanceBooking.setValue(365);
        maxAdvanceBooking.setWidthFull();
        maxAdvanceBooking.setHelperText("How far in advance can guests book rooms");
        
        IntegerField minStayDuration = new IntegerField("Minimum Stay Duration (nights)");
        minStayDuration.setValue(1);
        minStayDuration.setWidthFull();
        
        IntegerField maxStayDuration = new IntegerField("Maximum Stay Duration (nights)");
        maxStayDuration.setValue(30);
        maxStayDuration.setWidthFull();
        
        Checkbox allowSameDayBooking = new Checkbox("Allow Same-Day Booking");
        allowSameDayBooking.setValue(true);
        
        Checkbox autoConfirmBooking = new Checkbox("Auto-Confirm Bookings");
        autoConfirmBooking.setValue(false);
        
        Checkbox sendConfirmationEmail = new Checkbox("Send Confirmation Emails");
        sendConfirmationEmail.setValue(true);
        
        NumberField cancellationFee = new NumberField("Cancellation Fee (%)");
        cancellationFee.setValue(10.0);
        cancellationFee.setWidthFull();
        cancellationFee.setHelperText("Percentage of booking amount charged for cancellations");
        
        IntegerField freeCancellationDays = new IntegerField("Free Cancellation Period (days)");
        freeCancellationDays.setValue(7);
        freeCancellationDays.setWidthFull();
        freeCancellationDays.setHelperText("Days before check-in for free cancellation");
        
        Button saveBooking = new Button("Save Changes");
        saveBooking.addClassName("primary-button");
        saveBooking.addClickListener(e -> Notification.show("Booking settings saved"));
        
        card.add(title, maxAdvanceBooking, minStayDuration, maxStayDuration, 
                allowSameDayBooking, autoConfirmBooking, sendConfirmationEmail,
                cancellationFee, freeCancellationDays, saveBooking);
        
        layout.add(card);
        return layout;
    }

    private Component createPaymentSettings() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);
        
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        
        H3 title = new H3("Payment Configuration");
        title.getStyle().set("margin", "0 0 1rem 0");
        
        Checkbox acceptCreditCard = new Checkbox("Accept Credit Cards");
        acceptCreditCard.setValue(true);
        
        Checkbox acceptDebitCard = new Checkbox("Accept Debit Cards");
        acceptDebitCard.setValue(true);
        
        Checkbox acceptPayPal = new Checkbox("Accept PayPal");
        acceptPayPal.setValue(true);
        
        Checkbox acceptBankTransfer = new Checkbox("Accept Bank Transfer");
        acceptBankTransfer.setValue(false);
        
        NumberField depositPercentage = new NumberField("Required Deposit (%)");
        depositPercentage.setValue(20.0);
        depositPercentage.setWidthFull();
        depositPercentage.setHelperText("Percentage of total amount required as deposit");
        
        IntegerField paymentDueDays = new IntegerField("Payment Due (days before check-in)");
        paymentDueDays.setValue(7);
        paymentDueDays.setWidthFull();
        
        NumberField lateFeePercentage = new NumberField("Late Payment Fee (%)");
        lateFeePercentage.setValue(5.0);
        lateFeePercentage.setWidthFull();
        
        TextField taxRate = new TextField("Tax Rate (%)");
        taxRate.setValue("19");
        taxRate.setWidthFull();
        taxRate.setHelperText("VAT/Sales tax rate");
        
        Checkbox autoInvoiceGeneration = new Checkbox("Auto-Generate Invoices");
        autoInvoiceGeneration.setValue(true);
        
        Button savePayment = new Button("Save Changes");
        savePayment.addClassName("primary-button");
        savePayment.addClickListener(e -> Notification.show("Payment settings saved"));
        
        card.add(title, acceptCreditCard, acceptDebitCard, acceptPayPal, acceptBankTransfer,
                depositPercentage, paymentDueDays, lateFeePercentage, taxRate, 
                autoInvoiceGeneration, savePayment);
        
        layout.add(card);
        return layout;
    }

    private Component createNotificationSettings() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);
        
        // Email Notifications
        Div emailCard = new Div();
        emailCard.addClassName("card");
        emailCard.setWidthFull();
        
        H3 emailTitle = new H3("Email Notifications");
        emailTitle.getStyle().set("margin", "0 0 1rem 0");
        
        Checkbox newBookingEmail = new Checkbox("New Booking Notifications");
        newBookingEmail.setValue(true);
        
        Checkbox cancellationEmail = new Checkbox("Cancellation Notifications");
        cancellationEmail.setValue(true);
        
        Checkbox paymentEmail = new Checkbox("Payment Confirmations");
        paymentEmail.setValue(true);
        
        Checkbox reminderEmail = new Checkbox("Check-in Reminders");
        reminderEmail.setValue(true);
        
        Checkbox reviewRequestEmail = new Checkbox("Review Requests");
        reviewRequestEmail.setValue(true);
        
        emailCard.add(emailTitle, newBookingEmail, cancellationEmail, paymentEmail, 
                     reminderEmail, reviewRequestEmail);
        
        // System Notifications
        Div systemCard = new Div();
        systemCard.addClassName("card");
        systemCard.setWidthFull();
        
        H3 systemTitle = new H3("System Notifications");
        systemTitle.getStyle().set("margin", "0 0 1rem 0");
        
        Checkbox maintenanceAlerts = new Checkbox("Room Maintenance Alerts");
        maintenanceAlerts.setValue(true);
        
        Checkbox lowInventoryAlerts = new Checkbox("Low Inventory Alerts");
        lowInventoryAlerts.setValue(true);
        
        Checkbox systemErrors = new Checkbox("System Error Notifications");
        systemErrors.setValue(true);
        
        Checkbox dailySummary = new Checkbox("Daily Summary Reports");
        dailySummary.setValue(true);
        
        systemCard.add(systemTitle, maintenanceAlerts, lowInventoryAlerts, 
                      systemErrors, dailySummary);
        
        Button saveNotifications = new Button("Save Changes");
        saveNotifications.addClassName("primary-button");
        saveNotifications.addClickListener(e -> Notification.show("Notification settings saved"));
        
        layout.add(emailCard, systemCard, saveNotifications);
        return layout;
    }

    private Component createSecuritySettings() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);
        
        // Password Policy
        Div passwordCard = new Div();
        passwordCard.addClassName("card");
        passwordCard.setWidthFull();
        
        H3 passwordTitle = new H3("Password Policy");
        passwordTitle.getStyle().set("margin", "0 0 1rem 0");
        
        IntegerField minPasswordLength = new IntegerField("Minimum Password Length");
        minPasswordLength.setValue(8);
        minPasswordLength.setWidthFull();
        
        Checkbox requireUppercase = new Checkbox("Require Uppercase Letters");
        requireUppercase.setValue(true);
        
        Checkbox requireNumbers = new Checkbox("Require Numbers");
        requireNumbers.setValue(true);
        
        Checkbox requireSpecialChars = new Checkbox("Require Special Characters");
        requireSpecialChars.setValue(false);
        
        IntegerField passwordExpiry = new IntegerField("Password Expiry (days)");
        passwordExpiry.setValue(90);
        passwordExpiry.setWidthFull();
        passwordExpiry.setHelperText("Users must change password after this period (0 = never)");
        
        passwordCard.add(passwordTitle, minPasswordLength, requireUppercase, 
                        requireNumbers, requireSpecialChars, passwordExpiry);
        
        // Session & Access
        Div sessionCard = new Div();
        sessionCard.addClassName("card");
        sessionCard.setWidthFull();
        
        H3 sessionTitle = new H3("Session & Access Control");
        sessionTitle.getStyle().set("margin", "0 0 1rem 0");
        
        IntegerField sessionTimeout = new IntegerField("Session Timeout (minutes)");
        sessionTimeout.setValue(30);
        sessionTimeout.setWidthFull();
        
        Checkbox twoFactorAuth = new Checkbox("Enable Two-Factor Authentication");
        twoFactorAuth.setValue(false);
        
        Checkbox loginNotifications = new Checkbox("Login Notifications");
        loginNotifications.setValue(true);
        
        IntegerField maxLoginAttempts = new IntegerField("Max Failed Login Attempts");
        maxLoginAttempts.setValue(5);
        maxLoginAttempts.setWidthFull();
        
        Checkbox ipWhitelist = new Checkbox("Enable IP Whitelist");
        ipWhitelist.setValue(false);
        
        sessionCard.add(sessionTitle, sessionTimeout, twoFactorAuth, loginNotifications, 
                       maxLoginAttempts, ipWhitelist);
        
        // Data & Privacy
        Div dataCard = new Div();
        dataCard.addClassName("card");
        dataCard.setWidthFull();
        
        H3 dataTitle = new H3("Data & Privacy");
        dataTitle.getStyle().set("margin", "0 0 1rem 0");
        
        Checkbox autoBackup = new Checkbox("Enable Automatic Backups");
        autoBackup.setValue(true);
        
        IntegerField dataRetention = new IntegerField("Data Retention Period (days)");
        dataRetention.setValue(365);
        dataRetention.setWidthFull();
        dataRetention.setHelperText("How long to keep deleted records");
        
        Checkbox gdprCompliance = new Checkbox("GDPR Compliance Mode");
        gdprCompliance.setValue(true);
        
        Checkbox auditLog = new Checkbox("Enable Audit Logging");
        auditLog.setValue(true);
        
        dataCard.add(dataTitle, autoBackup, dataRetention, gdprCompliance, auditLog);
        
        Button saveSecurity = new Button("Save Changes");
        saveSecurity.addClassName("primary-button");
        saveSecurity.addClickListener(e -> Notification.show("Security settings saved"));
        
        layout.add(passwordCard, sessionCard, dataCard, saveSecurity);
        return layout;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.hasRole(UserRole.MANAGER)) {
            event.rerouteTo(LoginView.class);
        }
    }
}