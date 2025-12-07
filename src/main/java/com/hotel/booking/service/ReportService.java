package com.hotel.booking.service;

import com.hotel.booking.entity.Report;
import com.hotel.booking.entity.User;
import com.hotel.booking.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/* Artur Derr
 * Service-Klasse für Report-Entitäten.
 * Enthält Business-Logik und CRUD-Operationen für Reports.
 * Ersetzt die REST-Controller-Logik durch direkte Service-Methoden.
 */
@Service
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
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
}
