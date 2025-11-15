package com.hotel.booking.controller;

import com.hotel.booking.entity.Report;
import com.hotel.booking.entity.User;
import com.hotel.booking.service.ReportService;
import com.hotel.booking.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/* Artur Derr
 * REST-Controller für Report-Verwaltung.
 * Stellt HTTP-Endpoints für CRUD-Operationen auf Report-Entitäten bereit. */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;

    public ReportController(ReportService reportService, UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }

    /* GET /api/reports
     * Gibt alle Reports zurück (sortiert nach Erstellungsdatum) */
    @GetMapping
    public ResponseEntity<List<Report>> getAllReports() {
        List<Report> reports = reportService.findAllSorted();
        return ResponseEntity.ok(reports);
    }

    /* GET /api/reports/{id}
     * Gibt einen Report anhand der ID zurück */
    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportById(@PathVariable Long id) {
        Optional<Report> report = reportService.findById(id);
        return report.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    /* POST /api/reports
     * Erstellt einen neuen Report
     * Request Body muss userId enthalten */
    @PostMapping
    public ResponseEntity<Report> createReport(@RequestBody ReportCreateRequest request) {
        // Validierung: Titel und Beschreibung müssen vorhanden sein
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // User muss existieren
        Optional<User> userOpt = userService.findById(request.getUserId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Report erstellen
        Report report = reportService.createReport(
            request.getTitle(),
            request.getDescription(),
            userOpt.get()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    /* PUT /api/reports/{id}
     * Aktualisiert einen existierenden Report */
    @PutMapping("/{id}")
    public ResponseEntity<Report> updateReport(@PathVariable Long id, @RequestBody ReportUpdateRequest request) {
        Optional<Report> reportOpt = reportService.findById(id);
        
        if (reportOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Report report = reportOpt.get();
        
        // Aktualisiere Felder
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            report.setTitle(request.getTitle());
        }
        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            report.setDescription(request.getDescription());
        }
        
        Report updatedReport = reportService.save(report);
        return ResponseEntity.ok(updatedReport);
    }

    /* DELETE /api/reports/{id}
     * Löscht einen Report anhand der ID */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        if (!reportService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        reportService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /* GET /api/reports/user/{userId}
     * Gibt alle Reports eines bestimmten Users zurück */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Report>> getReportsByUser(@PathVariable Long userId) {
        Optional<User> userOpt = userService.findById(userId);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<Report> reports = reportService.findByUserSorted(userOpt.get());
        return ResponseEntity.ok(reports);
    }

    /* GET /api/reports/search?title={title}
     * Sucht Reports nach Titel */
    @GetMapping("/search")
    public ResponseEntity<List<Report>> searchReportsByTitle(@RequestParam String title) {
        List<Report> reports = reportService.searchByTitle(title);
        return ResponseEntity.ok(reports);
    }

    /* DTO-Klasse für Report-Erstellung */
    public static class ReportCreateRequest {
        private String title;
        private String description;
        private Long userId;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }

    /* DTO-Klasse für Report-Aktualisierung */
    public static class ReportUpdateRequest {
        private String title;
        private String description;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
