package com.hotel.booking.security;

import com.hotel.booking.entity.User;
import com.hotel.booking.entity.UserRole;
import com.hotel.booking.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service implementation for loading user details in Spring Security.
 * <p>
 * This class bridges the application's {@link User} entity to Spring Security's authentication
 * framework by implementing the {@link UserDetailsService} interface. It handles user lookup
 * by username or email address and converts the application user entity into Spring Security's
 * {@link UserDetails} format with appropriate roles and activation status.
 * </p>
 * <p>
 * The service is used by Spring Security's authentication provider to load user credentials
 * during the login process.
 * </p>
 * 
 * @author Artur Derr
 * @see UserDetailsService
 * @see com.hotel.booking.entity.User
 * @see com.hotel.booking.repository.UserRepository
 */
@Service
public class HotelUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public HotelUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrEmail));

        // Spring Security expects roles WITHOUT the ROLE_ prefix when using .roles(...)
        String role = user.getRole() != null ? user.getRole().name() : UserRole.GUEST.name();

        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(role)
                .disabled(!user.isActive())
                .build();
    }
}
