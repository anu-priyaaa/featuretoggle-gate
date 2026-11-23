package com.training.featuretoggle;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class RegistrationController {

    // Features every new farmer gets by default (must exist in DataLoader)
    private static final List<String> DEFAULT_FARMER_FEATURES = List.of("FARM_ACTIVITY", "TRAIN_ALL");

    private final AppUserRepository users;
    private final UserFeatureAccessRepository grants;
    private final PasswordEncoder encoder;

    @GetMapping("/register")
    String form() { return "register"; }

    @PostMapping("/register")
    String register(@RequestParam String fullName,
                    @RequestParam String email,
                    @RequestParam String village,
                    @RequestParam String password,
                    @RequestParam String confirmPassword,
                    Model model) {

        String mail = email.trim().toLowerCase();
        String error = null;
        if (fullName.isBlank() || village.isBlank()) {
            error = "Name and village are required";
        } else if (!mail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            error = "Enter a valid email";
        } else if (password.length() < 6) {
            error = "Password must be at least 6 characters";
        } else if (!password.equals(confirmPassword)) {
            error = "Passwords do not match";
        } else if (users.findByEmail(mail).isPresent()) {
            error = "This email is already registered";
        }

        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("fullName", fullName);
            model.addAttribute("email", email);
            model.addAttribute("village", village);
            return "register";
        }

        AppUser u = new AppUser();
        u.setFullName(fullName.trim());
        u.setVillage(village.trim());
        u.setEmail(mail);
        u.setPassword(encoder.encode(password));
        u.setRole("FARMER");                      // self-registration is always FARMER
        users.save(u);

        List<String> codes = new java.util.ArrayList<>(DEFAULT_FARMER_FEATURES);
        DemoCatalog.FEATURES.stream().filter(DemoCatalog.Def::farmer)
                .forEach(d -> codes.add(d.code()));
        for (String code : codes) {
            UserFeatureAccess a = new UserFeatureAccess();
            a.setUserId(u.getId());
            a.setFeatureCode(code);
            a.setGrantedBy("system");
            a.setGrantedAt(LocalDateTime.now());
            grants.save(a);
        }
        return "redirect:/login?registered";
    }
}
