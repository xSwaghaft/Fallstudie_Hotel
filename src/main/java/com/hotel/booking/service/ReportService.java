package com.hotel.booking.service;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingExtra;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

//Service-Methoden für die Reports & Analytics view
//Matthias Lohr
@Service
@Transactional
public class ReportService {

    private final BookingService bookingService;

    public ReportService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    //Gesamtumsatz durch Buchungen in einem Zeitraum für Report (Mit stream, da Service List liefert)
    //Matthias Lohr
    public String getTotalRevenueInPeriod(LocalDate from, LocalDate to) {
        BigDecimal total = bookingService.getAllBookingsInPeriod(from, to)
            .stream()
            .map(Booking::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add); //ZERO ist identity(start), BigDecimal.add wird für den Stream ausgeführt

            return String.format("%.2f €", total);
    }

    public String getMostPopularExtraInPeriod(LocalDate from, LocalDate to) {
        List<Booking> bookings = bookingService.getAllBookingsInPeriod(from, to);

        // Map<Name, Count>
        Map<String, Long> countMap = bookings.stream()
        .filter(b -> b.getExtras() != null)                  // Sicherheitscheck
        .flatMap(b -> b.getExtras().stream())                // Alle Extras aus allen Buchungen flach machen
        .map(BookingExtra::getName)                          // Nur den Namen zählen
        .collect(Collectors.groupingBy(name -> name, Collectors.counting()));

        // Kein Extra vorhanden → Rückgabe
        if (countMap.isEmpty()) {
            return "None";
        }

        // Beliebtestes Extra anhand der Häufigkeit finden
        return countMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
    }

    public String getMostPopularExtraLastPeriod(LocalDate from, LocalDate to) {
        String extra = getMostPopularExtraInPeriod(from.minusMonths(1), to.minusMonths(1));
        return "Last Period: " + extra;
    }

    //Berechnet durchschnittliche Buchungsdauer in einem Zeitraum 
    // (ChronoUnit ist ein Enum, das Zeiteinheiten Repräsentiert und Methode between liefert)
    //Matthias Lohr
    public String getAvgStayDurationInPeriod(LocalDate from, LocalDate to) {
        List<Booking> bookings = bookingService.getAllBookingsInPeriod(from, to);

        if (bookings.isEmpty()) {
            return "0.00";
        }
        double avg = bookings.stream()
            .mapToLong(b -> ChronoUnit.DAYS.between(b.getCheckInDate(), b.getCheckOutDate()))
            .average()
            .orElse(0.0);

        return String.format("%.2f days", avg);
    }


    //Gibt die meistgebuchte Kategorie in einem Zeitraum als String zurück
    //Matthias Lohr
    public String getTopCategoryInPeriod(LocalDate from, LocalDate to) {
        List<Booking> bookings = bookingService.getAllBookingsInPeriod(from, to);
        if (bookings.isEmpty()) {
            return "None";
        }
        // Map<Categorie-Name, Anzahl> wird mittels stream in die Map gespeichert
        Map<String, Long> countMap = bookings.stream()
            .filter(b -> b.getRoomCategory() != null) //Kann es null sein?
            .map(b -> b.getRoomCategory().getName())
            .collect(
                Collectors.groupingBy(name -> name, Collectors.counting())); //Gruppiert die Einträge nach Name mit Anzahl als Value
        return countMap.entrySet().stream()
            .max(Map.Entry.comparingByValue()) //Holt den Eintrag mit der höchsten Zahl als Value
            .map(Map.Entry::getKey) //Holt den dazugehörigen Katgorie-Namen oder "None" da max Optional ist
            .orElse("None");
    }

    public String getMostPopularCategoryLastPeriod(LocalDate from, LocalDate to) {
        String category = getTopCategoryInPeriod(from.minusMonths(1), to.minusMonths(1));
        return "Last Period: " + category;
    }

    //Matthias Lohr
    public String getAvgRevenuePerBookingInPeriod(LocalDate from, LocalDate to) {
                List<Booking> bookings = bookingService.getAllBookingsInPeriod(from, to);
        if (bookings.isEmpty()) {
            return "0.00 €";
        }
        double avg = bookings.stream()
            .map(Booking::getTotalPrice) //nur die Preise der Buchungen
            .filter(price -> price != null) //zur sicherheit: nur ungleich null
            .mapToDouble(BigDecimal::doubleValue) //Zu double machen, um average anzuwenden zu können
            .average() //Liefert ein OptionalDouble, daher orElse
            .orElse(0.00);
        return String.format("%.2f €", avg);
    }

    //------------------Trend-String und Trend-boolean Methoden für alle Zahlenbasierten KPIs-------------

    // Trend für die gesamte Anzahl an Bookings
    //Matthias Lohr
    public String getBookingTrendString(LocalDate startDate, LocalDate endDate) {
        int thisPeriod = bookingService.getNumberOfBookingsInPeriod(startDate, endDate);
        int comparisonPeriod = bookingService.getNumberOfBookingsInPeriod(
            startDate.minusMonths(1), endDate.minusMonths(1));
        
        return createTrendString(thisPeriod, comparisonPeriod);
    }

    public boolean getBookingTrendPositive(LocalDate startDate, LocalDate endDate) {
        int thisPeriod = bookingService.getNumberOfBookingsInPeriod(startDate, endDate);
        int comparisonPeriod = bookingService.getNumberOfBookingsInPeriod(
            startDate.minusMonths(1), endDate.minusMonths(1));
        return thisPeriod > comparisonPeriod;
    }

    // Trend für durchschnittliche Aufenthaltsdauer (in Tagen)
    //Matthias Lohr
    public String getAvgStayTrendString(LocalDate startDate, LocalDate endDate) {
        double thisPeriod = getAvgStayDurationValue(startDate, endDate);
        double comparisonPeriod = getAvgStayDurationValue(startDate.minusMonths(1), endDate.minusMonths(1));
        return createTrendString(thisPeriod, comparisonPeriod);
    }

    public boolean getAvgStayTrendPositive(LocalDate startDate, LocalDate endDate) {
        double thisPeriod = getAvgStayDurationValue(startDate, endDate);
        double comparisonPeriod = getAvgStayDurationValue(startDate.minusMonths(1), endDate.minusMonths(1));
        return thisPeriod > comparisonPeriod;
    }

    // Trend für durchschnittlichen Umsatz pro Buchung
    //Matthias Lohr
    public String getAvgRevenueTrendString(LocalDate startDate, LocalDate endDate) {
        double thisPeriod = getAvgRevenueValue(startDate, endDate);
        double comparisonPeriod = getAvgRevenueValue(startDate.minusMonths(1), endDate.minusMonths(1));
        return createTrendString(thisPeriod, comparisonPeriod);
    }

    public boolean getAvgRevenueTrendPositive(LocalDate startDate, LocalDate endDate) {
        double thisPeriod = getAvgRevenueValue(startDate, endDate);
        double comparisonPeriod = getAvgRevenueValue(startDate.minusMonths(1), endDate.minusMonths(1));
        return thisPeriod > comparisonPeriod;
    }

    // Trend für Gesamtumsatz
    //Matthias Lohr
    public String getTotalRevenueTrendString(LocalDate startDate, LocalDate endDate) {
        double thisPeriod = getTotalRevenueValue(startDate, endDate);
        double comparisonPeriod = getTotalRevenueValue(startDate.minusMonths(1), endDate.minusMonths(1));
        return createTrendString(thisPeriod, comparisonPeriod);
    }

    public boolean getTotalRevenueTrendPositive(LocalDate startDate, LocalDate endDate) {
        double thisPeriod = getTotalRevenueValue(startDate, endDate);
        double comparisonPeriod = getTotalRevenueValue(startDate.minusMonths(1), endDate.minusMonths(1));
        return thisPeriod > comparisonPeriod;
    }

    // Hilfsmethoden für die Werte (ohne Formatierung)
    private double getAvgStayDurationValue(LocalDate from, LocalDate to) {
        List<Booking> bookings = bookingService.getAllBookingsInPeriod(from, to);
        if (bookings.isEmpty()) return 0.0;
        return bookings.stream()
            .mapToLong(b -> ChronoUnit.DAYS.between(b.getCheckInDate(), b.getCheckOutDate()))
            .average()
            .orElse(0.0);
    }

    private double getAvgRevenueValue(LocalDate from, LocalDate to) {
        List<Booking> bookings = bookingService.getAllBookingsInPeriod(from, to);
        if (bookings.isEmpty()) return 0.0;
        return bookings.stream()
            .map(Booking::getTotalPrice)
            .filter(price -> price != null)
            .mapToDouble(BigDecimal::doubleValue)
            .average()
            .orElse(0.0);
    }

    private double getTotalRevenueValue(LocalDate from, LocalDate to) {
        List<Booking> bookings = bookingService.getAllBookingsInPeriod(from, to);
        if (bookings.isEmpty()) return 0.0;
        return bookings.stream()
            .map(Booking::getTotalPrice)
            .filter(price -> price != null)
            .mapToDouble(BigDecimal::doubleValue)
            .sum();
    }

    //Logik für die String-Erstellung ausgelagert in eigener Methode 
    //Matthias Lohr
    public String createTrendString(double thisPeriod, double comparisonPeriod) {

        String trendString;
    
        // Keine Buchungen in dieser, oder Vergleichsperiode
        if (comparisonPeriod == 0) {
            if (thisPeriod > 0) {
                trendString = "No Records in comparison period";
            } else {
                trendString = "0% from last period"; 
            }
        } else {
            // Berechne die prozentuale Veränderung
            double difference = thisPeriod - comparisonPeriod;
            double percentage = (difference / comparisonPeriod) * 100;
            
            // DecimalFormat für die Formatierung
            // Setze DecimalFormatSymbols auf US, für Punkt als Dezimaltrennzeichen
            DecimalFormatSymbols symbol = new DecimalFormatSymbols(Locale.US);
            // Definiert die Vorzeichen, '0.0' für eine Nachkommastelle
            DecimalFormat df = new DecimalFormat("+#0.0;-#0.0", symbol);
            
            // Formatiere den Wert und füge den Rest des Strings hinzu
            String formattedPercentage = df.format(percentage);
            trendString = formattedPercentage + "% from last period";
        }

        return trendString;
    }
}
