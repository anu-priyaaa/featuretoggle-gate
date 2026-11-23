package com.training.featuretoggle;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeatureFlagService {

    private final FeatureFlagRepository repo;
    private final ActivityLogRepository logs;

    public boolean isEnabled(String name) {
        return repo.findByName(name).map(FeatureFlag::isEnabled).orElse(false);
    }

    public List<FeatureFlag> all() { return repo.findAll(); }

    public List<FeatureFlag> enabled() { return repo.findByEnabledTrue(); }

    @Transactional
    public FeatureFlag toggle(String name, String changedBy) {
        FeatureFlag flag = repo.findByName(name).orElseThrow();
        flag.setEnabled(!flag.isEnabled());
        flag.setUpdatedBy(changedBy);
        flag.setUpdatedAt(LocalDateTime.now());

        ActivityLog log = new ActivityLog();
        log.setUsername(changedBy);
        log.setAction("Turned " + (flag.isEnabled() ? "ON " : "OFF ") + flag.getDescription());
        log.setOccurredAt(LocalDateTime.now());
        logs.save(log);

        return repo.save(flag);
    }
}