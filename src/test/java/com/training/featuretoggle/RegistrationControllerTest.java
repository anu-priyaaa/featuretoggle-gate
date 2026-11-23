package com.training.featuretoggle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerTest {

    @Mock AppUserRepository users;
    @Mock UserFeatureAccessRepository grants;
    @Mock PasswordEncoder encoder;
    @InjectMocks RegistrationController controller;

    private String register(String name, String email, String village, String pw, String confirm, Model model) {
        return controller.register(name, email, village, pw, confirm, model);
    }

    @Test
    void formIsShown() {
        assertEquals("register", controller.form());
    }

    @Test
    void validRegistrationCreatesFarmerWithDefaultGrants() {
        when(users.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(encoder.encode("secret1")).thenReturn("HASH");
        when(users.save(any(AppUser.class))).thenAnswer(i -> {
            AppUser u = i.getArgument(0);
            u.setId(5L);
            return u;
        });

        String view = register("Test Farmer", " New@Test.com ", "Rampur", "secret1", "secret1", new ExtendedModelMap());

        assertEquals("redirect:/login?registered", view);
        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(users).save(saved.capture());
        assertEquals("new@test.com", saved.getValue().getEmail());
        assertEquals("FARMER", saved.getValue().getRole());
        assertEquals("HASH", saved.getValue().getPassword());
        verify(grants, atLeast(2)).save(any(UserFeatureAccess.class));
    }

    @Test
    void duplicateEmailIsRejected() {
        when(users.findByEmail("a@test.com")).thenReturn(Optional.of(new AppUser()));
        Model model = new ExtendedModelMap();

        assertEquals("register", register("A", "a@test.com", "V", "secret1", "secret1", model));
        assertEquals("This email is already registered", model.getAttribute("error"));
        verify(users, never()).save(any(AppUser.class));
    }

    @Test
    void shortPasswordIsRejected() {
        Model model = new ExtendedModelMap();
        assertEquals("register", register("A", "a@test.com", "V", "123", "123", model));
        assertEquals("Password must be at least 6 characters", model.getAttribute("error"));
    }

    @Test
    void mismatchedPasswordsAreRejected() {
        Model model = new ExtendedModelMap();
        assertEquals("register", register("A", "a@test.com", "V", "secret1", "secret2", model));
        assertEquals("Passwords do not match", model.getAttribute("error"));
    }

    @Test
    void invalidEmailIsRejected() {
        Model model = new ExtendedModelMap();
        assertEquals("register", register("A", "not-an-email", "V", "secret1", "secret1", model));
        assertEquals("Enter a valid email", model.getAttribute("error"));
    }

    @Test
    void blankNameOrVillageIsRejected() {
        Model model = new ExtendedModelMap();
        assertEquals("register", register(" ", "a@test.com", "V", "secret1", "secret1", model));
        assertEquals("Name and village are required", model.getAttribute("error"));
    }
}
