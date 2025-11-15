// Datei: ReportRepository.java
package com.hotel.booking.repository;

import com.hotel.booking.entity.Report;
import com.hotel.booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/* Artur Derr
 * Repository für Report-Entitäten.
 * Bietet Datenbankzugriff und zusätzliche Query-Methoden für Reports. */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    // Findet alle Reports eines bestimmten Users
    List<Report> findByCreatedBy(User user);

    // Findet alle Reports, sortiert nach Erstellungsdatum absteigend
    List<Report> findAllByOrderByCreatedAtDesc();

    // Findet Reports nach Titel (case-insensitive)
    List<Report> findByTitleContainingIgnoreCase(String title);

    // Findet Reports, die nach einem bestimmten Datum erstellt wurden
    List<Report> findByCreatedAtAfter(LocalDateTime date);

    // Findet Reports eines Users, sortiert nach Datum absteigend
    List<Report> findByCreatedByOrderByCreatedAtDesc(User user);
}
