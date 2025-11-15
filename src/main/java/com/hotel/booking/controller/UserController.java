package com.hotel.booking.controller;

import com.hotel.booking.entity.User;
import com.hotel.booking.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/* Artur Derr
 * REST-Controller für User-Verwaltung.
 * Stellt HTTP-Endpoints für CRUD-Operationen auf User-Entitäten bereit. */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /* GET /api/users
     * Gibt alle Users zurück */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    /* GET /api/users/{id}
     * Gibt einen User anhand der ID zurück */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        return user.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    /* POST /api/users
     * Erstellt einen neuen User */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // Validierung: Username muss vorhanden sein
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Prüfen ob Username bereits existiert
        if (userService.existsByUsername(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        User savedUser = userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    /* PUT /api/users/{id}
     * Aktualisiert einen existierenden User */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> userOpt = userService.findById(id);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        
        // Aktualisiere Felder
        if (userDetails.getUsername() != null) {
            user.setUsername(userDetails.getUsername());
        }
        if (userDetails.getPassword() != null) {
            user.setPassword(userDetails.getPassword());
        }
        if (userDetails.getEmail() != null) {
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getFirstName() != null) {
            user.setFirstName(userDetails.getFirstName());
        }
        if (userDetails.getLastName() != null) {
            user.setLastName(userDetails.getLastName());
        }
        if (userDetails.getBirthdate() != null) {
            user.setBirthdate(userDetails.getBirthdate());
        }
        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }
        
        User updatedUser = userService.save(user);
        return ResponseEntity.ok(updatedUser);
    }

    /* DELETE /api/users/{id}
     * Löscht einen User anhand der ID */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /* GET /api/users/username/{username}
     * Sucht einen User anhand des Usernames */
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userService.findByUsername(username);
        return user.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    /* GET /api/users/email/{email}
     * Sucht einen User anhand der E-Mail */
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.findByEmail(email);
        return user.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
}
