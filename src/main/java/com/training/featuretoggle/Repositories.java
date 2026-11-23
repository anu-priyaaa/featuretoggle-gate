package com.training.featuretoggle;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

interface FarmRepository extends JpaRepository<Farm, Long> { }

interface DispatchRepository extends JpaRepository<Dispatch, Long> { }

interface TrainingRepository extends JpaRepository<Training, Long> { }

interface DemoRecordRepository extends JpaRepository<DemoRecord, Long> {
    List<DemoRecord> findByFeatureCodeOrderByIdAsc(String featureCode);
    boolean existsByFeatureCode(String featureCode);
}

interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findAllByOrderByOccurredAtDesc();
}