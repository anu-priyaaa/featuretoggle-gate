package com.training.featuretoggle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessServiceTest {

    @Mock FeatureFlagRepository modules;
    @Mock FeatureRepository features;
    @Mock UserFeatureAccessRepository grants;
    @Mock AppUserRepository users;
    @Mock ActivityLogRepository logs;
    @InjectMocks AccessService service;

    private Authentication auth(String email, String role) {
        return new UsernamePasswordAuthenticationToken(email, "x",
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    private FeatureFlag module(boolean on) {
        FeatureFlag m = new FeatureFlag();
        m.setName("M");
        m.setEnabled(on);
        return m;
    }

    private Feature feature(String code, boolean on) {
        Feature f = new Feature();
        f.setCode(code);
        f.setName("Name " + code);
        f.setModuleName("M");
        f.setEnabled(on);
        return f;
    }

    private AppUser user(long id, String email) {
        AppUser u = new AppUser();
        u.setId(id);
        u.setEmail(email);
        return u;
    }

    @Test
    void adminDetectedByRole() {
        assertTrue(service.isAdmin(auth("a@test.com", "ADMIN")));
        assertFalse(service.isAdmin(auth("f@test.com", "FARMER")));
    }

    @Test
    void moduleOffMeansNothingUsable() {
        when(modules.findByName("M")).thenReturn(Optional.of(module(false)));
        assertTrue(service.usableFeatures("M", auth("a@test.com", "ADMIN")).isEmpty());
    }

    @Test
    void unknownModuleMeansNothingUsable() {
        when(modules.findByName("M")).thenReturn(Optional.empty());
        assertTrue(service.usableFeatures("M", auth("a@test.com", "ADMIN")).isEmpty());
    }

    @Test
    void adminSeesEveryEnabledFeatureWithoutGrants() {
        when(modules.findByName("M")).thenReturn(Optional.of(module(true)));
        when(users.findByEmail("a@test.com")).thenReturn(Optional.of(user(1, "a@test.com")));
        when(features.findByModuleName("M")).thenReturn(List.of(feature("F1", true), feature("F2", false)));

        List<Feature> usable = service.usableFeatures("M", auth("a@test.com", "ADMIN"));

        assertEquals(1, usable.size());
        assertEquals("F1", usable.get(0).getCode());
    }

    @Test
    void farmerSeesOnlyGrantedEnabledFeatures() {
        when(modules.findByName("M")).thenReturn(Optional.of(module(true)));
        when(users.findByEmail("f@test.com")).thenReturn(Optional.of(user(7, "f@test.com")));
        when(features.findByModuleName("M")).thenReturn(List.of(feature("F1", true), feature("F2", true)));
        when(grants.existsByUserIdAndFeatureCode(7L, "F1")).thenReturn(true);
        when(grants.existsByUserIdAndFeatureCode(7L, "F2")).thenReturn(false);

        List<Feature> usable = service.usableFeatures("M", auth("f@test.com", "FARMER"));

        assertEquals(List.of("F1"), usable.stream().map(Feature::getCode).toList());
    }

    @Test
    void toggleFeatureFlipsAndLogs() {
        Feature f = feature("F1", false);
        when(features.findByCode("F1")).thenReturn(Optional.of(f));

        Feature result = service.toggleFeature("F1", "admin@test.com");

        assertTrue(result.isEnabled());
        verify(features).save(f);
        verify(logs).save(any(ActivityLog.class));
    }

    @Test
    void grantCreatesAccessOnce() {
        when(grants.existsByUserIdAndFeatureCode(7L, "F1")).thenReturn(false);
        when(features.findByCode("F1")).thenReturn(Optional.of(feature("F1", true)));
        when(users.findById(7L)).thenReturn(Optional.of(user(7, "f@test.com")));

        service.grant("F1", 7L, "admin@test.com");

        verify(grants).save(any(UserFeatureAccess.class));
        verify(logs).save(any(ActivityLog.class));
    }

    @Test
    void grantIsSkippedWhenAlreadyGranted() {
        when(grants.existsByUserIdAndFeatureCode(7L, "F1")).thenReturn(true);

        service.grant("F1", 7L, "admin@test.com");

        verify(grants, never()).save(any(UserFeatureAccess.class));
    }

    @Test
    void revokeDeletesExistingGrant() {
        UserFeatureAccess a = new UserFeatureAccess();
        when(features.findByCode("F1")).thenReturn(Optional.of(feature("F1", true)));
        when(users.findById(7L)).thenReturn(Optional.of(user(7, "f@test.com")));
        when(grants.findByUserIdAndFeatureCode(7L, "F1")).thenReturn(Optional.of(a));

        service.revoke("F1", 7L, "admin@test.com");

        verify(grants).delete(a);
        verify(logs).save(any(ActivityLog.class));
    }

    @Test
    void revokeWithoutGrantDoesNothing() {
        when(features.findByCode("F1")).thenReturn(Optional.of(feature("F1", true)));
        when(users.findById(7L)).thenReturn(Optional.of(user(7, "f@test.com")));
        when(grants.findByUserIdAndFeatureCode(7L, "F1")).thenReturn(Optional.empty());

        service.revoke("F1", 7L, "admin@test.com");

        verify(grants, never()).delete(any(UserFeatureAccess.class));
    }
}
