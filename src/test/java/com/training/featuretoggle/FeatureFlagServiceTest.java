package com.training.featuretoggle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureFlagServiceTest {

    @Mock FeatureFlagRepository repo;
    @Mock ActivityLogRepository logs;
    @InjectMocks FeatureFlagService service;

    private FeatureFlag flag(String name, boolean on) {
        FeatureFlag f = new FeatureFlag();
        f.setName(name);
        f.setDescription("Desc " + name);
        f.setEnabled(on);
        return f;
    }

    @Test
    void isEnabledReflectsStoredSwitch() {
        when(repo.findByName("A")).thenReturn(Optional.of(flag("A", true)));
        assertFalse(service.isEnabled("A"));
    }

    @Test
    void unknownFlagIsTreatedAsOff() {
        when(repo.findByName("NOPE")).thenReturn(Optional.empty());
        assertFalse(service.isEnabled("NOPE"));
    }

    @Test
    void allAndEnabledDelegateToRepository() {
        List<FeatureFlag> list = List.of(flag("A", true));
        when(repo.findAll()).thenReturn(list);
        when(repo.findByEnabledTrue()).thenReturn(list);
        assertEquals(list, service.all());
        assertEquals(list, service.enabled());
    }

    @Test
    void toggleFlipsFlagRecordsWhoAndLogsIt() {
        FeatureFlag f = flag("A", false);
        when(repo.findByName("A")).thenReturn(Optional.of(f));
        when(repo.save(any(FeatureFlag.class))).thenAnswer(i -> i.getArgument(0));

        FeatureFlag result = service.toggle("A", "admin@test.com");

        assertTrue(result.isEnabled());
        assertEquals("admin@test.com", result.getUpdatedBy());
        assertNotNull(result.getUpdatedAt());
        ArgumentCaptor<ActivityLog> log = ArgumentCaptor.forClass(ActivityLog.class);
        verify(logs).save(log.capture());
        assertTrue(log.getValue().getAction().startsWith("Turned ON"));
    }

    @Test
    void togglingAnEnabledFlagTurnsItOff() {
        FeatureFlag f = flag("A", true);
        when(repo.findByName("A")).thenReturn(Optional.of(f));
        when(repo.save(any(FeatureFlag.class))).thenAnswer(i -> i.getArgument(0));

        assertFalse(service.toggle("A", "admin@test.com").isEnabled());
    }

    @Test
    void togglingUnknownFlagFails() {
        when(repo.findByName("NOPE")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.toggle("NOPE", "admin@test.com"));
    }
}
