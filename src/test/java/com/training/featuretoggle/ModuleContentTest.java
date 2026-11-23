package com.training.featuretoggle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModuleContentTest {

    @Mock FarmRepository farms;
    @Mock DispatchRepository dispatches;
    @Mock TrainingRepository trainings;
    @Mock AppUserRepository users;
    @Mock ActivityLogRepository logs;
    @Mock DemoRecordRepository demoRecords;
    @InjectMocks ModuleContent content;

    private Feature feature(String code) {
        Feature f = new Feature();
        f.setCode(code);
        f.setName("Title " + code);
        return f;
    }

    private DemoRecord record(String code, String data) {
        DemoRecord r = new DemoRecord();
        r.setFeatureCode(code);
        r.setData(data);
        return r;
    }

    @Test
    void splitKeepsEmptyTrailingCells() {
        assertEquals(List.of("a", "", "c", ""), ModuleContent.split("a||c|"));
    }

    @Test
    void farmAreaIsSummedPerVillage() {
        Farm a = new Farm(); a.setVillage("Rampur"); a.setAreaAcres(3.0);
        Farm b = new Farm(); b.setVillage("Rampur"); b.setAreaAcres(2.0);
        Farm c = new Farm(); c.setVillage("Kondapur"); c.setAreaAcres(1.5);
        when(farms.findAll()).thenReturn(List.of(a, b, c));

        ModuleContent.Section s = content.build(feature("FARM_AREA"));

        assertEquals(List.of(List.of("Kondapur", "1.5"), List.of("Rampur", "5.0")), s.getRows());
    }

    @Test
    void pendingDeliveriesExcludeDelivered() {
        Dispatch done = new Dispatch(); done.setProduct("Seed"); done.setStatus("Delivered");
        Dispatch open = new Dispatch(); open.setProduct("Urea"); open.setStatus("In transit");
        when(dispatches.findAll()).thenReturn(List.of(done, open));

        ModuleContent.Section s = content.build(feature("TXN_PENDING"));

        assertEquals(1, s.getRows().size());
        assertEquals("Urea", s.getRows().get(0).get(0));
    }

    @Test
    void upcomingTrainingsOnlyScheduled() {
        Training a = new Training(); a.setTitle("A"); a.setStatus("Completed");
        Training b = new Training(); b.setTitle("B"); b.setStatus("Scheduled");
        when(trainings.findAll()).thenReturn(List.of(a, b));

        ModuleContent.Section s = content.build(feature("TRAIN_UPCOMING"));

        assertEquals(1, s.getRows().size());
        assertEquals("B", s.getRows().get(0).get(0));
    }

    @Test
    void unknownCodeGivesPlaceholder() {
        ModuleContent.Section s = content.build(feature("NOT_A_FEATURE"));
        assertEquals(List.of(List.of("No content defined")), s.getRows());
    }

    @Test
    void catalogFeatureShowsStoredRows() {
        when(demoRecords.findByFeatureCodeOrderByIdAsc("INP_STOCK"))
                .thenReturn(List.of(record("INP_STOCK", "Urea|Rampur|10|Low")));

        ModuleContent.Section s = content.build(feature("INP_STOCK"));

        assertEquals(List.of(List.of("Urea", "Rampur", "10", "Low")), s.getRows());
    }

    @Test
    void bestMarketPicksHighestPricePerCrop() {
        when(demoRecords.findByFeatureCodeOrderByIdAsc("MKT_PRICES")).thenReturn(List.of(
                record("MKT_PRICES", "Cotton|Guntur|7200|d"),
                record("MKT_PRICES", "Cotton|Adilabad|7350|d"),
                record("MKT_PRICES", "Paddy|Karimnagar|2300|d")));

        ModuleContent.Section s = content.build(feature("MKT_BEST"));

        assertEquals(List.of(List.of("Cotton", "Adilabad", "7350"),
                List.of("Paddy", "Karimnagar", "2300")), s.getRows());
    }

    @Test
    void ledgerSummaryComputesProfit() {
        when(demoRecords.findByFeatureCodeOrderByIdAsc("LED_ENTRIES")).thenReturn(List.of(
                record("LED_ENTRIES", "d|Income|Sale|1000"),
                record("LED_ENTRIES", "d|Expense|Seed|400"),
                record("LED_ENTRIES", "d|Expense|Bad number|abc")));

        ModuleContent.Section s = content.build(feature("LED_SUMMARY"));

        assertEquals(List.of("1000", "400", "600"), s.getRows().get(0));
    }

    @Test
    void pestMapFlagsVillagesWithManyReports() {
        when(demoRecords.findByFeatureCodeOrderByIdAsc("PEST_REPORTS")).thenReturn(List.of(
                record("PEST_REPORTS", "d|Kondapur|Cotton|a|x|y"),
                record("PEST_REPORTS", "d|Kondapur|Cotton|b|x|y"),
                record("PEST_REPORTS", "d|Kondapur|Cotton|c|x|y"),
                record("PEST_REPORTS", "d|Rampur|Paddy|d|x|y")));

        ModuleContent.Section s = content.build(feature("PEST_MAP"));

        assertEquals(List.of("Kondapur", "3", "HIGH"), s.getRows().get(0));
        assertEquals(List.of("Rampur", "1", "Watch"), s.getRows().get(1));
    }
}
