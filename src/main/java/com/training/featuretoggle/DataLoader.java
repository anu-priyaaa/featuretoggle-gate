package com.training.featuretoggle;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final AppUserRepository users;
    private final FeatureFlagRepository flags;
    private final FeatureRepository features;
    private final UserFeatureAccessRepository grants;
    private final FarmRepository farms;
    private final DispatchRepository dispatches;
    private final TrainingRepository trainings;
    private final PasswordEncoder encoder;
    private final DemoRecordRepository demoRecords;

    @Override
    public void run(String... args) {
        if (users.count() == 0) {
            addUser("admin@test.com", "admin123", "ADMIN");
            addUser("manager@test.com", "manager123", "MANAGER");
            addUser("trainer@test.com", "trainer123", "TRAINER");
            for (String n : List.of("farmer", "ramesh", "lakshmi", "suresh", "anitha",
                    "mohan", "kavitha", "ravi", "sunita", "prakash")) {
                addUser(n + "@test.com", "farmer123", "FARMER");
            }
        }
        if (flags.count() == 0) {
            addModule("FARMER_MODULE", "Farmer details");
            addModule("MASTER_MODULE", "Master data");
            addModule("TRANSACTION_MODULE", "Transactions");
            addModule("TRAINING_TRACKING_MODULE", "Training and tracking");
        }
        if (features.count() == 0) {
            addFeature("FARM_ACTIVITY", "Farm activity", "FARMER_MODULE");
            addFeature("FARM_AREA", "Area by village", "FARMER_MODULE");
            addFeature("MASTER_USERS", "User list", "MASTER_MODULE");
            addFeature("MASTER_ACTIVITY", "Activity log", "MASTER_MODULE");
            addFeature("TXN_DISPATCHES", "Dispatches", "TRANSACTION_MODULE");
            addFeature("TXN_PENDING", "Pending deliveries", "TRANSACTION_MODULE");
            addFeature("TRAIN_ALL", "All trainings", "TRAINING_TRACKING_MODULE");
            addFeature("TRAIN_UPCOMING", "Upcoming trainings", "TRAINING_TRACKING_MODULE");
        }
        if (grants.count() == 0) {
            Map<String, List<String>> byRole = Map.of(
                    "FARMER", List.of("FARM_ACTIVITY", "TRAIN_ALL"),
                    "MANAGER", List.of("FARM_ACTIVITY", "FARM_AREA", "MASTER_USERS",
                            "MASTER_ACTIVITY", "TXN_DISPATCHES", "TXN_PENDING"),
                    "TRAINER", List.of("TRAIN_ALL", "TRAIN_UPCOMING"));
            for (AppUser u : users.findAll()) {
                for (String code : byRole.getOrDefault(u.getRole(), List.of())) {
                    UserFeatureAccess a = new UserFeatureAccess();
                    a.setUserId(u.getId());
                    a.setFeatureCode(code);
                    a.setGrantedBy("system");
                    a.setGrantedAt(LocalDateTime.now());
                    grants.save(a);
                }
            }
        }
        if (farms.count() == 0) {
            addFarm("Ramesh Kumar", "Kondapur", "Cotton", 4.5, "Sowing", "2026-06-10");
            addFarm("Lakshmi Devi", "Rampur", "Cotton", 3.0, "Irrigation", "2026-08-02");
            addFarm("Suresh Reddy", "Kondapur", "Cotton", 6.2, "Pest spraying", "2026-09-15");
            addFarm("Anitha Rao", "Mallapur", "Cotton", 2.8, "Harvest", "2026-10-20");
            addFarm("Mohan Lal", "Rampur", "Cotton", 5.0, "Weeding", "2026-07-18");
        }
        if (dispatches.count() == 0) {
            addDispatch("Cotton seed packets", 500, "Kondapur depot", "Delivered", "2026-05-28");
            addDispatch("Fertilizer (urea)", 1200, "Rampur depot", "Delivered", "2026-07-05");
            addDispatch("Pesticide", 300, "Mallapur depot", "In transit", "2026-09-10");
            addDispatch("Cotton bales", 8000, "Ginning unit, Guntur", "Dispatched", "2026-10-30");
        }
        if (trainings.count() == 0) {
            addTraining("Better cotton practices", "Soil preparation and sowing distance",
                    "Ramesh Kumar", "Dr. Priya", "2026-05-20", "Completed");
            addTraining("Pest management", "Identifying bollworm and safe spraying",
                    "Suresh Reddy", "Mr. Anand", "2026-08-25", "Completed");
            addTraining("Water saving", "Drip irrigation for cotton",
                    "Lakshmi Devi", "Dr. Priya", "2026-11-12", "Scheduled");
            addTraining("Harvest and storage", "Picking, grading and storing cotton",
                    "Anitha Rao", "Mr. Anand", "2026-11-20", "Scheduled");
        }
        seedCatalog();
    }

    // Agriculture problem modules (DemoCatalog). Safe to run repeatedly.
    private void seedCatalog() {
        for (DemoCatalog.Mod m : DemoCatalog.MODULES) {
            if (flags.findByName(m.name()).isEmpty()) addModule(m.name(), m.description());
        }
        for (DemoCatalog.Def d : DemoCatalog.FEATURES) {
            if (features.findByCode(d.code()).isEmpty()) addFeature(d.code(), d.name(), d.module());
            if (!demoRecords.existsByFeatureCode(d.code())) {
                for (List<String> row : d.seed()) {
                    DemoRecord r = new DemoRecord();
                    r.setFeatureCode(d.code());
                    r.setData(String.join("|", row));
                    r.setCreatedBy("system");
                    r.setCreatedAt(LocalDateTime.now());
                    demoRecords.save(r);
                }
            }
        }
        // FARMER gets the farmer features, MANAGER gets all (ADMIN always has full access)
        for (AppUser u : users.findAll()) {
            for (DemoCatalog.Def d : DemoCatalog.FEATURES) {
                boolean allowed = "MANAGER".equals(u.getRole()) || ("FARMER".equals(u.getRole()) && d.farmer());
                if (allowed && !grants.existsByUserIdAndFeatureCode(u.getId(), d.code())) {
                    UserFeatureAccess a = new UserFeatureAccess();
                    a.setUserId(u.getId());
                    a.setFeatureCode(d.code());
                    a.setGrantedBy("system");
                    a.setGrantedAt(LocalDateTime.now());
                    grants.save(a);
                }
            }
        }
    }

    private void addUser(String email, String rawPassword, String role) {
        AppUser u = new AppUser();
        u.setEmail(email);
        u.setPassword(encoder.encode(rawPassword));
        u.setRole(role);
        users.save(u);
    }

    private void addModule(String name, String description) {
        FeatureFlag f = new FeatureFlag();
        f.setName(name);
        f.setDescription(description);
        f.setEnabled(false);                 // modules start OFF
        flags.save(f);
    }

    private void addFeature(String code, String name, String moduleName) {
        Feature f = new Feature();
        f.setCode(code);
        f.setName(name);
        f.setModuleName(moduleName);
        f.setEnabled(false);                  // features start ON, so the module switch controls them
        features.save(f);
    }

    private void addFarm(String farmer, String village, String crop, double acres,
                         String activity, String date) {
        Farm f = new Farm();
        f.setFarmerName(farmer);
        f.setVillage(village);
        f.setCrop(crop);
        f.setAreaAcres(acres);
        f.setActivity(activity);
        f.setActivityDate(LocalDate.parse(date));
        farms.save(f);
    }

    private void addDispatch(String product, int kg, String destination,
                             String status, String date) {
        Dispatch d = new Dispatch();
        d.setProduct(product);
        d.setQuantityKg(kg);
        d.setDestination(destination);
        d.setStatus(status);
        d.setDispatchDate(LocalDate.parse(date));
        dispatches.save(d);
    }

    private void addTraining(String title, String topic, String farmer,
                             String trainer, String date, String status) {
        Training t = new Training();
        t.setTitle(title);
        t.setTopic(topic);
        t.setFarmerName(farmer);
        t.setTrainer(trainer);
        t.setTrainingDate(LocalDate.parse(date));
        t.setStatus(status);
        trainings.save(t);
    }
}