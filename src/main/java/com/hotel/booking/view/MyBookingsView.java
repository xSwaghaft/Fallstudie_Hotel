package com.hotel.booking.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "bookings", layout = MainLayout.class)
@CssImport("./styles/views/dashboard-view.css")
public class MyBookingsView extends VerticalLayout {

	public MyBookingsView() {
		addClassName("hotel-dashboard-view");
		setSizeFull();
		setPadding(true);

		H3 title = new H3("My Bookings");
		add(title, createBookingsGrid());
	}

	private Grid<Booking> createBookingsGrid() {
		Grid<Booking> grid = new Grid<>(Booking.class, false);

		grid.addColumn(Booking::getId).setHeader("Booking ID").setAutoWidth(true);
		grid.addColumn(Booking::getRoom).setHeader("Room").setAutoWidth(true);
		grid.addColumn(Booking::getCheckIn).setHeader("Check-in").setAutoWidth(true);
		grid.addColumn(Booking::getCheckOut).setHeader("Check-out").setAutoWidth(true);
		grid.addColumn(Booking::getAmount).setHeader("Amount").setAutoWidth(true);

		// Status badge
		grid.addComponentColumn(booking -> {
			Span statusBadge = new Span(booking.getStatus());
			statusBadge.addClassName("status-badge");
			statusBadge.addClassName(booking.getStatus().toLowerCase().replace(" ", "-"));
			return statusBadge;
		}).setHeader("Status").setAutoWidth(true);

		// Actions: edit, cancel, or rate
		grid.addComponentColumn(booking -> {
			HorizontalLayout actions = new HorizontalLayout();
			actions.setAlignItems(FlexComponent.Alignment.CENTER);

			Button edit = new Button(new Icon(VaadinIcon.EDIT));
			edit.getElement().setAttribute("title", "Edit");
			edit.addClassName("icon-button");

			Button cancel = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
			cancel.getElement().setAttribute("title", "Cancel");
			cancel.addClassName("icon-button");

			actions.add(edit, cancel);

			if ("completed".equalsIgnoreCase(booking.getStatus())) {
				Button rate = new Button("Rate Stay", new Icon(VaadinIcon.STAR));
				rate.addThemeName("primary");
				actions.add(rate);
			}

			return actions;
		}).setHeader("Actions").setAutoWidth(true);

		grid.setItems(List.of(
				new Booking("BK001", "201 - Deluxe", "2025-11-02", "2025-11-05", "$450", "confirmed"),
				new Booking("BK002", "105 - Suite", "2025-12-15", "2025-12-18", "$897", "confirmed"),
				new Booking("BK003", "302 - Standard", "2025-10-15", "2025-10-18", "$267", "completed")
		));

		grid.setWidthFull();
		grid.getStyle().set("border-radius", "10px").set("background", "white");
		grid.addClassName("my-bookings-grid");
		return grid;
	}

	// --- Datenklasse ---
	public static class Booking {
		private String id;
		private String room;
		private String checkIn;
		private String checkOut;
		private String amount;
		private String status;

		public Booking(String id, String room, String checkIn, String checkOut, String amount, String status) {
			this.id = id;
			this.room = room;
			this.checkIn = checkIn;
			this.checkOut = checkOut;
			this.amount = amount;
			this.status = status;
		}

		public String getId() { return id; }
		public String getRoom() { return room; }
		public String getCheckIn() { return checkIn; }
		public String getCheckOut() { return checkOut; }
		public String getAmount() { return amount; }
		public String getStatus() { return status; }
	}

}
