package com.hotel.booking.user;

import com.hotel.booking.entity.User;
import com.hotel.booking.security.UserRole;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    /** Demo-Daten, damit Login out-of-the-box funktioniert */
    @PostConstruct
    void initDemoUsers() {
        if (repo.count() == 0) {
            repo.save(new User("john.guest", "guest", UserRole.GUEST));
            repo.save(new User("sarah.receptionist", "reception", UserRole.RECEPTIONIST));
            repo.save(new User("david.manager", "manager", UserRole.MANAGER));
        }
    }

    public Optional<User> authenticate(String username, String password) {
        return repo.findByUsernameAndPassword(username, password);
    }

    public Optional<User> findByUsername(String username) {
        return repo.findByUsername(username);
    }
}
