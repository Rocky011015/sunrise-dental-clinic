package com.sunrisedental.clinic.security;

import com.sunrisedental.clinic.domain.AppUser;
import com.sunrisedental.clinic.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    public DatabaseUserDetailsService(
            AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        String normalizedUsername =
                username == null ? "" : username.trim();

        AppUser appUser = userRepository
                .findByUsernameIgnoreCase(normalizedUsername)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No user found with username: "
                                + normalizedUsername
                ));

        return User.withUsername(appUser.getUsername())
                .password(appUser.getPasswordHash())
                .roles(appUser.getRole().name())
                .disabled(!appUser.isEnabled())
                .build();
    }
}