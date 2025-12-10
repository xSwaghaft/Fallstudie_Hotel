package com.hotel.booking.view.components;

import java.util.List;

import com.hotel.booking.entity.Room;
import com.hotel.booking.entity.RoomImage;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

// Präsentationskomponente für ein einzelnes Zimmer (Karte mit Bild, Preis, Titel).
public class RoomCard extends Div {
    
    private final Room room;
    private final List<RoomImage> images;
    
    // Konstruktor: setzt Zimmer und Bilder, initialisiert die Karte.
    public RoomCard(Room room, List<RoomImage> images) {
        this.room = room;
        this.images = images != null ? images : List.of();
        
        addClassName("room-card");
        buildCard();
    }
    
    // Baut die sichtbare Zimmerkarte mit Titel, Preis und Primärbild.
    private void buildCard() {
        // Image Container - nur primäres Bild
        Div imageContainer = new Div();
        imageContainer.addClassName("room-card__image");
        
        if (images != null && !images.isEmpty()) {
            RoomImage firstImage = images.get(0);
            imageContainer.getStyle().set("background-image", "url('" + firstImage.getImagePath() + "')");
            imageContainer.addClickListener(e -> openGallery());
        } else {
            imageContainer.addClassName("room-card__image--empty");
            imageContainer.add(new Paragraph("Kein Bild"));
        }
        
        // Content
        VerticalLayout content = new VerticalLayout();
        content.addClassName("room-card__content");
        content.setSpacing(false);
        content.setPadding(true);
        
        String roomName = (room.getCategory() != null ? room.getCategory().getName() : "Room") 
                + " #" + room.getRoomNumber();
        
        H3 title = new H3(roomName);
        title.addClassName("room-card__title");
        
        Div priceDiv = new Div();
        priceDiv.addClassName("room-card__price");
        String priceText = room.getCategory() != null && room.getCategory().getPricePerNight() != null
                ? "€" + room.getCategory().getPricePerNight()
                : "N/A";
        Paragraph priceMain = new Paragraph(priceText);
        priceMain.addClassName("room-card__price-main");
        Paragraph priceSub = new Paragraph("pro Nacht");
        priceSub.addClassName("room-card__price-sub");
        priceDiv.add(priceMain, priceSub);
        
        String desc = room.getCategory() != null && room.getCategory().getDescription() != null
                ? room.getCategory().getDescription()
                : "Komfortables Zimmer";
        
        Paragraph description = new Paragraph(desc);
        description.addClassName("room-card__description");
        
        content.add(new HorizontalLayout(title, priceDiv), description);
        
        add(imageContainer, content);
    }
    
    // Hängt einen bereitgestellten Aktions-Button unten an den Karten-Content an.
    public void setBookButton(Button bookButton) {
        VerticalLayout content = (VerticalLayout) getChildren()
            .filter(c -> c.getClass() == VerticalLayout.class)
            .findFirst()
            .orElse(null);
        
        if (content != null) {
            bookButton.addClassName("primary-button");
            content.add(bookButton);
        }
    }
    
    // Öffnet eine Galerie-Dialogansicht mit allen Bildern des Zimmers.
    private void openGallery() {
        if (images == null || images.isEmpty()) {
            return;
        }
        
        Dialog galleryDialog = new Dialog();
        galleryDialog.setHeaderTitle("Zimmergalerie");
        galleryDialog.setWidth("90%");
        galleryDialog.setMaxWidth("1200px");
        
        Div gallery = new Div();
        gallery.addClassName("gallery-grid");
        
        for (RoomImage image : images) {
            Div imageDiv = new Div();
            imageDiv.addClassName("gallery-image");
            imageDiv.getStyle().set("background-image", "url('" + image.getImagePath() + "')");
            imageDiv.addClickListener(e -> openImageDialog(image));
            gallery.add(imageDiv);
        }
        
        Button closeButton = new Button("Schließen", ev -> galleryDialog.close());
        galleryDialog.getFooter().add(closeButton);
        galleryDialog.add(gallery);
        galleryDialog.open();
    }
    
    // Zeigt ein einzelnes Bild in großem Dialog an.
    private void openImageDialog(RoomImage image) {
        Dialog imageDialog = new Dialog();
        imageDialog.setWidth("90%");
        imageDialog.setMaxWidth("1400px");
        
        Image img = new Image(image.getImagePath(), image.getAltText() != null ? image.getAltText() : "Zimmerbild");
        img.addClassName("gallery-image-full");
        img.setWidthFull();
        
        if (image.getTitle() != null) {
            imageDialog.setHeaderTitle(image.getTitle());
        }
        
        Button closeButton = new Button("Schließen", ev -> imageDialog.close());
        imageDialog.getFooter().add(closeButton);
        imageDialog.add(img);
        imageDialog.open();
    }
    
    // Liefert das referenzierte Zimmerobjekt.
    public Room getRoom() {
        return room;
    }
}

