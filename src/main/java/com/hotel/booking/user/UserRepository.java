package com.hotel.booking.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.booking.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndPassword(String username, String password);
    Optional<User> findByUsername(String username);
}
