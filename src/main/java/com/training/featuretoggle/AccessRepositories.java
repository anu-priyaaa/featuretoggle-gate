package com.training.featuretoggle;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

interface FeatureRepository extends JpaRepository<Feature, Long> {
    List<Feature> findByModuleName(String moduleName);
    Optional<Feature> findByCode(String code);
}

interface UserFeatureAccessRepository extends JpaRepository<UserFeatureAccess, Long> {
    boolean existsByUserIdAndFeatureCode(Long userId, String featureCode);
    Optional<UserFeatureAccess> findByUserIdAndFeatureCode(Long userId, String featureCode);
    List<UserFeatureAccess> findByFeatureCode(String featureCode);
    List<UserFeatureAccess> findByFeatureCodeIn(Collection<String> codes);
}