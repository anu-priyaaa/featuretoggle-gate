package com.training.featuretoggle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ModuleContent {

    @Data
    @AllArgsConstructor
    public static class Section {
        private String title;
        private List<String> headers;
        private List<List<String>> rows;
    }

    private final FarmRepository farms;
    private final DispatchRepository dispatches;
    private final TrainingRepository trainings;
    private final AppUserRepository users;
    private final ActivityLogRepository logs;
    private final DemoRecordRepository demoRecords;

    public Section build(Feature f) {
        String t = f.getName();
        return switch (f.getCode()) {

            case "FARM_ACTIVITY" -> new Section(t,
                    List.of("Farmer", "Village", "Crop", "Area (acres)", "Activity", "Date"),
                    farms.findAll().stream()
                            .map(x -> row(x.getFarmerName(), x.getVillage(), x.getCrop(),
                                    x.getAreaAcres(), x.getActivity(), x.getActivityDate()))
                            .toList());

            case "FARM_AREA" -> {
                Map<String, Double> byVillage = farms.findAll().stream()
                        .collect(Collectors.groupingBy(Farm::getVillage, TreeMap::new,
                                Collectors.summingDouble(Farm::getAreaAcres)));
                yield new Section(t, List.of("Village", "Total area (acres)"),
                        byVillage.entrySet().stream()
                                .map(e -> row(e.getKey(), String.format("%.1f", e.getValue())))
                                .toList());
            }

            case "MASTER_USERS" -> new Section(t, List.of("Email", "Role"),
                    users.findAll().stream().map(u -> row(u.getEmail(), u.getRole())).toList());

            case "MASTER_ACTIVITY" -> new Section(t, List.of("User", "Action", "Time"),
                    logs.findAllByOrderByOccurredAtDesc().stream()
                            .map(l -> row(l.getUsername(), l.getAction(), l.getOccurredAt()))
                            .toList());

            case "TXN_DISPATCHES" -> new Section(t,
                    List.of("Product", "Quantity (kg)", "Destination", "Status", "Date"),
                    dispatches.findAll().stream()
                            .map(d -> row(d.getProduct(), d.getQuantityKg(), d.getDestination(),
                                    d.getStatus(), d.getDispatchDate()))
                            .toList());

            case "TXN_PENDING" -> new Section(t,
                    List.of("Product", "Quantity (kg)", "Destination", "Status"),
                    dispatches.findAll().stream()
                            .filter(d -> !"Delivered".equals(d.getStatus()))
                            .map(d -> row(d.getProduct(), d.getQuantityKg(), d.getDestination(), d.getStatus()))
                            .toList());

            case "TRAIN_ALL" -> new Section(t,
                    List.of("Training", "Topic", "Farmer", "Trainer", "Date", "Status"),
                    trainings.findAll().stream()
                            .map(x -> row(x.getTitle(), x.getTopic(), x.getFarmerName(),
                                    x.getTrainer(), x.getTrainingDate(), x.getStatus()))
                            .toList());

            case "TRAIN_UPCOMING" -> new Section(t,
                    List.of("Training", "Farmer", "Trainer", "Date"),
                    trainings.findAll().stream()
                            .filter(x -> "Scheduled".equals(x.getStatus()))
                            .map(x -> row(x.getTitle(), x.getFarmerName(), x.getTrainer(), x.getTrainingDate()))
                            .toList());

            default -> demo(f, t);
        };
    }

    // ---- Catalog features (see DemoCatalog)

    static List<String> split(String data) {
        return Arrays.asList(data.split("\\|", -1));
    }

    private List<List<String>> records(String code) {
        return demoRecords.findByFeatureCodeOrderByIdAsc(code).stream()
                .map(r -> split(r.getData())).toList();
    }

    private static double number(List<String> cells, int i) {
        try {
            return cells.size() > i ? Double.parseDouble(cells.get(i).trim()) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Section demo(Feature f, String title) {
        DemoCatalog.Def d = DemoCatalog.find(f.getCode()).orElse(null);
        if (d == null) {
            return new Section(title, List.of("Info"), List.of(row("No content defined")));
        }
        return switch (d.code()) {

            case "MKT_BEST" -> {                       // highest price per crop
                Map<String, List<String>> best = new TreeMap<>();
                for (List<String> r : records("MKT_PRICES")) {
                    List<String> cur = best.get(r.get(0));
                    if (cur == null || number(r, 2) > number(cur, 2)) best.put(r.get(0), r);
                }
                yield new Section(title, d.headers(), best.values().stream()
                        .map(r -> row(r.get(0), r.get(1), r.get(2))).toList());
            }

            case "PEST_MAP" -> {                       // reports per village
                Map<String, Long> byVillage = records("PEST_REPORTS").stream()
                        .collect(Collectors.groupingBy(r -> r.get(1), TreeMap::new, Collectors.counting()));
                yield new Section(title, d.headers(), byVillage.entrySet().stream()
                        .map(e -> row(e.getKey(), e.getValue(), e.getValue() >= 3 ? "HIGH" : "Watch"))
                        .toList());
            }

            case "LED_SUMMARY" -> {                    // income - expense over all entries
                double income = 0, expense = 0;
                for (List<String> r : records("LED_ENTRIES")) {
                    String type = r.get(1).trim().toLowerCase();
                    if (type.startsWith("inc")) income += number(r, 3);
                    else if (type.startsWith("exp")) expense += number(r, 3);
                }
                yield new Section(title, d.headers(),
                        List.of(row(String.format("%.0f", income), String.format("%.0f", expense),
                                String.format("%.0f", income - expense))));
            }

            default -> new Section(title, d.headers(), records(d.code()));
        };
    }

    private List<String> row(Object... values) {
        return Arrays.stream(values).map(String::valueOf).toList();
    }
}