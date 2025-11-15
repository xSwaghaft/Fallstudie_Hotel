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
 * Enthält Business-Logik und CRUD-Operationen für Reports. */
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
        return reportRepository.findById(id);
    }

    // Speichert oder aktualisiert einen Report
    public Report save(Report report) {
        return reportRepository.save(report);
    }

    // Löscht einen Report anhand der ID
    public void deleteById(Long id) {
        reportRepository.deleteById(id);
    }

    // Löscht einen Report
    public void delete(Report report) {
        reportRepository.delete(report);
    }

    // Findet alle Reports eines bestimmten Users
    public List<Report> findByUser(User user) {
        return reportRepository.findByCreatedBy(user);
    }

    // Findet alle Reports eines Users, sortiert nach Datum (neueste zuerst)
    public List<Report> findByUserSorted(User user) {
        return reportRepository.findByCreatedByOrderByCreatedAtDesc(user);
    }

    // Sucht Reports nach Titel
    public List<Report> searchByTitle(String title) {
        return reportRepository.findByTitleContainingIgnoreCase(title);
    }

    // Findet Reports, die nach einem bestimmten Datum erstellt wurden
    public List<Report> findCreatedAfter(LocalDateTime date) {
        return reportRepository.findByCreatedAtAfter(date);
    }

    // Erstellt einen neuen Report für einen User
    public Report createReport(String title, String description, User createdBy) {
        Report report = new Report(title, description, createdBy);
        return save(report);
    }

    // Zählt die Anzahl aller Reports
    public long count() {
        return reportRepository.count();
    }

    // Prüft, ob ein Report mit der ID existiert
    public boolean existsById(Long id) {
        return reportRepository.existsById(id);
    }
}
