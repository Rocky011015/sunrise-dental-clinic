package com.sunrisedental.clinic.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String homePage() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboardPage(
            Authentication authentication,
            Model model) {

        String role = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority ->
                        authority.substring("ROLE_".length()))
                .findFirst()
                .orElse("USER");

        model.addAttribute("username", authentication.getName());
        model.addAttribute("role", role);
        model.addAttribute("isAdmin", "ADMIN".equals(role));

        return "dashboard";
    }
}