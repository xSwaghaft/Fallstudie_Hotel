package com.hotel.booking.view.components;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;

public class PaymentMethodManager {

    private RadioButtonGroup<String> paymentMethodGroup;
    private VerticalLayout cardSection;
    private VerticalLayout bankSection;
    private String selectedPaymentMethod;

    public PaymentMethodManager() {
        setupPaymentMethodGroup();
    }

    public RadioButtonGroup<String> getPaymentMethodGroup() {
        return paymentMethodGroup;
    }

    public VerticalLayout buildCardSection(com.vaadin.flow.component.Component... fields) {
        cardSection = new VerticalLayout();
        cardSection.setPadding(false);
        cardSection.setSpacing(true);
        cardSection.add(fields);
        cardSection.setVisible(true);
        return cardSection;
    }

    public VerticalLayout buildBankSection(com.vaadin.flow.component.Component... fields) {
        bankSection = new VerticalLayout();
        bankSection.setPadding(false);
        bankSection.setSpacing(true);
        bankSection.add(fields);
        bankSection.setVisible(false);
        return bankSection;
    }

    public void togglePaymentMethod(String method) {
        selectedPaymentMethod = method;
        if ("Credit Card".equals(method)) {
            cardSection.setVisible(true);
            bankSection.setVisible(false);
        } else {
            cardSection.setVisible(false);
            bankSection.setVisible(true);
        }
    }

    public String getSelectedPaymentMethod() {
        return selectedPaymentMethod != null ? selectedPaymentMethod : "Credit Card";
    }

    private void setupPaymentMethodGroup() {
        paymentMethodGroup = new RadioButtonGroup<>();
        paymentMethodGroup.setLabel("Payment Method");
        paymentMethodGroup.setItems("Credit Card", "Bank Transfer");
        paymentMethodGroup.setValue("Credit Card");
    }
}
