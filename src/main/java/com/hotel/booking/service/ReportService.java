package com.hotel.booking.service;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingExtra;
import com.hotel.booking.entity.Report;
import com.hotel.booking.entity.User;
import com.hotel.booking.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/* Artur Derr
 * Service-Klasse für Report-Entitäten.
 * Enthält Business-Logik und CRUD-Operationen für Reports.
 * Ersetzt die REST-Controller-Logik durch direkte Service-Methoden.
 */
@Service
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final BookingService bookingService;

    public ReportService(ReportRepository reportRepository, BookingService bookingService) {
        this.reportRepository = reportRepository;
        this.bookingService = bookingService;
    }

    // Gibt alle Reports zurück
    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    // Gibt alle Reports sortiert nach Erstellungsdatum (neueste zuerst) zurück
    public List<Report> findAllSorted() {
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }

    // Findet einen Report anhand der ID
    public Optional<Report> findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Report ID muss gültig sein");
        }
        return reportRepository.findById(id);
    }

    /**
     * Erstellt einen neuen Report (Create)
     */
    public Report create(String title, String description, User createdBy) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Report-Titel ist erforderlich");
        }
        
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Report-Beschreibung ist erforderlich");
        }
        
        if (createdBy == null) {
            throw new IllegalArgumentException("Report-Ersteller (User) ist erforderlich");
        }
        
        Report report = new Report(title, description, createdBy);
        return reportRepository.save(report);
    }

    /**
     * Aktualisiert einen existierenden Report (Update)
     */
    public Report update(Long id, Report reportDetails) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Report ID muss gültig sein");
        }
        
        if (reportDetails == null) {
            throw new IllegalArgumentException("Report Details dürfen nicht null sein");
        }
        
        Optional<Report> existingReportOpt = findById(id);
        if (existingReportOpt.isEmpty()) {
            throw new IllegalArgumentException("Report mit ID " + id + " nicht gefunden");
        }
        
        Report existingReport = existingReportOpt.get();
        
        // Update Titel
        if (reportDetails.getTitle() != null && !reportDetails.getTitle().trim().isEmpty()) {
            existingReport.setTitle(reportDetails.getTitle());
        }
        
        // Update Beschreibung
        if (reportDetails.getDescription() != null && !reportDetails.getDescription().trim().isEmpty()) {
            existingReport.setDescription(reportDetails.getDescription());
        }
        
        return reportRepository.save(existingReport);
    }

    /**
     * Löscht einen Report anhand der ID (Delete)
     */
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Report ID muss gültig sein");
        }
        
        if (!reportRepository.existsById(id)) {
            throw new IllegalArgumentException("Report mit ID " + id + " nicht gefunden");
        }
        
        reportRepository.deleteById(id);
    }

    /**
     * Speichert einen Report (generisch)
     */
    public Report save(Report report) {
        if (report == null) {
            throw new IllegalArgumentException("Report darf nicht null sein");
        }
        return reportRepository.save(report);
    }

    /**
     * Findet alle Reports eines bestimmten Users
     */
    public List<Report> findByUser(User user) {
        return reportRepository.findByCreatedBy(user);
    }

    /**
     * Findet alle Reports eines Users, sortiert nach Datum (neueste zuerst)
     */
    public List<Report> findByUserSorted(User user) {
        return reportRepository.findByCreatedByOrderByCreatedAtDesc(user);
    }

    /**
     * Sucht Reports nach Titel
     */
    public List<Report> searchByTitle(String title) {
        return reportRepository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * Findet Reports, die nach einem bestimmten Datum erstellt wurden
     */
    public List<Report> findCreatedAfter(LocalDateTime date) {
        return reportRepository.findByCreatedAtAfter(date);
    }

    /**
     * Zählt die Anzahl aller Reports
     */
    public long count() {
        return reportRepository.count();
    }

    /**
     * Prüft, ob ein Report mit der ID existiert
     */
    public boolean existsById(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        return reportRepository.existsById(id);
    }

    //Matthias Lohr
    public String getBookingTrendString(LocalDate startDate, LocalDate endDate) {
        int thisPeriod = bookingService.getNumberOfBookingsInPeriod(endDate, startDate);
        int comparisonPeriod = bookingService.getNumberOfBookingsInPeriod(
            endDate.minusMonths(1), startDate.minusMonths(1));
        String trendString;
    
        // Keine Buchungen in dieser, oder Vergleichsperiode
        if (comparisonPeriod == 0) {
            if (thisPeriod > 0) {
                trendString = "No Bookings in comparison period";
            } else {
                trendString = "0% from last period"; 
            }
        } else {
            // Berechne die prozentuale Veränderung (TypeCast zu double - sollte unproblematisch sein)
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

    //Matthias Lohr
    public double getBookingTrend(LocalDate startDate, LocalDate endDate) {
        int thisPeriod = bookingService.getNumberOfBookingsInPeriod(endDate, startDate);
        int comparisonPeriod = bookingService.getNumberOfBookingsInPeriod(
            endDate.minusMonths(1), startDate.minusMonths(1));
        if(comparisonPeriod != 0) {
            double trendPercent = ((thisPeriod - comparisonPeriod)/ comparisonPeriod) * 100;
            return trendPercent;
        } else {
            //0 wenn es im Vergleichszeitraum keine Buchung gab - damit positive: false
            return 0.00;
        }
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
}
