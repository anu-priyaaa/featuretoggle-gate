package com.training.featuretoggle;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessService {

    private final FeatureFlagRepository modules;
    private final FeatureRepository features;
    private final UserFeatureAccessRepository grants;
    private final AppUserRepository users;
    private final ActivityLogRepository logs;

    public boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // Features this user can use right now inside one module
    public List<Feature> usableFeatures(String moduleName, Authentication auth) {
        boolean moduleOn = modules.findByName(moduleName)
                .map(FeatureFlag::isEnabled).orElse(false);
        if (!moduleOn) return List.of();

        Long userId = users.findByEmail(auth.getName()).map(AppUser::getId).orElse(-1L);
        boolean admin = isAdmin(auth);

        return features.findByModuleName(moduleName).stream()
                .filter(Feature::isEnabled)
                .filter(f -> admin || grants.existsByUserIdAndFeatureCode(userId, f.getCode()))
                .toList();
    }

    @Transactional
    public Feature toggleFeature(String code, String by) {
        Feature f = features.findByCode(code).orElseThrow();
        f.setEnabled(!f.isEnabled());
        features.save(f);
        log(by, "Turned " + (f.isEnabled() ? "ON " : "OFF ") + "feature: " + f.getName());
        return f;
    }

    @Transactional
    public void grant(String code, Long userId, String by) {
        if (grants.existsByUserIdAndFeatureCode(userId, code)) return;
        Feature f = features.findByCode(code).orElseThrow();
        AppUser u = users.findById(userId).orElseThrow();

        UserFeatureAccess a = new UserFeatureAccess();
        a.setUserId(userId);
        a.setFeatureCode(code);
        a.setGrantedBy(by);
        a.setGrantedAt(LocalDateTime.now());
        grants.save(a);
        log(by, "Granted '" + f.getName() + "' to " + u.getEmail());
    }

    @Transactional
    public void revoke(String code, Long userId, String by) {
        Feature f = features.findByCode(code).orElseThrow();
        AppUser u = users.findById(userId).orElseThrow();
        grants.findByUserIdAndFeatureCode(userId, code).ifPresent(a -> {
            grants.delete(a);
            log(by, "Revoked '" + f.getName() + "' from " + u.getEmail());
        });
    }

    private void log(String username, String action) {
        ActivityLog l = new ActivityLog();
        l.setUsername(username);
        l.setAction(action);
        l.setOccurredAt(LocalDateTime.now());
        logs.save(l);
    }
}