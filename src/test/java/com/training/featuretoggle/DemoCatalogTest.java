package com.training.featuretoggle;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DemoCatalogTest {

    @Test
    void featureCodesAreUnique() {
        Set<String> seen = new HashSet<>();
        for (DemoCatalog.Def d : DemoCatalog.FEATURES) {
            assertTrue(seen.add(d.code()), "duplicate code " + d.code());
        }
    }

    @Test
    void everyFeatureBelongsToADeclaredModule() {
        Set<String> modules = new HashSet<>();
        DemoCatalog.MODULES.forEach(m -> modules.add(m.name()));
        for (DemoCatalog.Def d : DemoCatalog.FEATURES) {
            assertTrue(modules.contains(d.module()), d.code() + " has unknown module " + d.module());
        }
    }

    @Test
    void seedRowsMatchHeaderCount() {
        for (DemoCatalog.Def d : DemoCatalog.FEATURES) {
            for (List<String> row : d.seed()) {
                assertEquals(d.headers().size(), row.size(), "bad seed row in " + d.code());
            }
        }
    }

    @Test
    void noCellContainsTheSeparator() {
        for (DemoCatalog.Def d : DemoCatalog.FEATURES) {
            d.seed().forEach(row -> row.forEach(c -> assertFalse(c.contains("|"), d.code())));
        }
    }

    @Test
    void findReturnsFeatureOrEmpty() {
        assertTrue(DemoCatalog.find("LED_ENTRIES").isPresent());
        assertTrue(DemoCatalog.find("NOPE").isEmpty());
    }

    @Test
    void inputHeadersSkipAutoFilledColumns() {
        DemoCatalog.Def claims = DemoCatalog.find("INS_CLAIMS").orElseThrow();
        assertEquals(List.of("Crop", "Reason", "Claim amount (Rs)"), DemoCatalog.inputHeaders(claims));
    }

    @Test
    void readOnlyFeaturesHaveNoInputFields() {
        DemoCatalog.Def prices = DemoCatalog.find("MKT_PRICES").orElseThrow();
        assertTrue(DemoCatalog.inputHeaders(prices).isEmpty());
    }

    @Test
    void autoColumnsAreRecognised() {
        assertTrue(DemoCatalog.isAuto("Farmer"));
        assertTrue(DemoCatalog.isAuto("Date"));
        assertFalse(DemoCatalog.isAuto("Crop"));
    }
}
