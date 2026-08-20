package com.sunrisedental.clinic.security;

import com.sunrisedental.clinic.domain.AppUser;
import com.sunrisedental.clinic.domain.UserRole;
import com.sunrisedental.clinic.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseUserDetailsServiceTest {

    @Mock
    private AppUserRepository userRepository;

    private DatabaseUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new DatabaseUserDetailsService(userRepository);
    }

    @Test
    void shouldLoadEnabledAdminFromDatabase() {
        AppUser admin = new AppUser(
                "admin",
                "$2a$10$exampleHash",
                "Clinic Administrator",
                UserRole.ADMIN,
                true
        );

        when(userRepository.findByUsernameIgnoreCase("admin"))
                .thenReturn(Optional.of(admin));

        UserDetails result =
                userDetailsService.loadUserByUsername("admin");

        assertEquals("admin", result.getUsername());
        assertEquals("$2a$10$exampleHash", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")));

        verify(userRepository).findByUsernameIgnoreCase("admin");
    }

    @Test
    void shouldRejectUnknownUsername() {
        when(userRepository.findByUsernameIgnoreCase("missing"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing")
        );
    }

    @Test
    void shouldKeepDisabledStaffAccountDisabled() {
        AppUser staff = new AppUser(
                "staff",
                "$2a$10$exampleHash",
                "Clinic Staff",
                UserRole.STAFF,
                false
        );

        when(userRepository.findByUsernameIgnoreCase("staff"))
                .thenReturn(Optional.of(staff));

        UserDetails result =
                userDetailsService.loadUserByUsername("staff");

        assertFalse(result.isEnabled());
    }
}