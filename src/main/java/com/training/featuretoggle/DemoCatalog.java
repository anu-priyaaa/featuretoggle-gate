package com.training.featuretoggle;

import java.util.List;
import java.util.Optional;

/**
 * Definition of the agriculture problem modules and their features.
 * To add a feature: add a Def below. It is seeded, toggleable, listed and (if addable) gets an Add form.
 */
final class DemoCatalog {

    record Mod(String name, String description) { }

    /**
     * @param addable  farmers can add rows through a form
     * @param farmer   granted to FARMER users by default (MANAGER gets everything)
     */
    record Def(String code, String name, String module, boolean addable, boolean farmer,
               List<String> headers, List<List<String>> seed) { }

    private DemoCatalog() { }

    static final List<Mod> MODULES = List.of(
            new Mod("ADVISORY_MODULE", "Crop advisory"),
            new Mod("WEATHER_MODULE", "Weather and alerts"),
            new Mod("MARKET_MODULE", "Market prices"),
            new Mod("SOIL_MODULE", "Soil health"),
            new Mod("INPUTS_MODULE", "Inputs and inventory"),
            new Mod("IRRIGATION_MODULE", "Irrigation"),
            new Mod("SCHEMES_MODULE", "Schemes and finance"),
            new Mod("INSURANCE_MODULE", "Insurance and claims"),
            new Mod("MARKETPLACE_MODULE", "Produce marketplace"),
            new Mod("LEDGER_MODULE", "Expense and income ledger"),
            new Mod("PESTREPORT_MODULE", "Pest reporting"),
            new Mod("EQUIPMENT_MODULE", "Equipment sharing"));

    static final List<Def> FEATURES = List.of(

            // ---- Crop advisory
            d("ADV_CALENDAR", "Crop calendar", "ADVISORY_MODULE", false, true,
                    List.of("Crop", "Stage", "Month", "What to do"),
                    List.of(r("Cotton", "Land preparation", "May", "Deep plough, add farmyard manure"),
                            r("Cotton", "Sowing", "June", "Sow 90 x 60 cm apart after first good rain"),
                            r("Cotton", "Vegetative", "July-Aug", "Weed twice, top-dress urea"),
                            r("Cotton", "Boll formation", "Sept-Oct", "Watch for bollworm, light irrigation"),
                            r("Cotton", "Harvest", "Nov-Dec", "Pick dry bolls, grade and store in shade"),
                            r("Paddy", "Transplanting", "July", "20-25 day seedlings, 2 per hill"),
                            r("Paddy", "Harvest", "Oct-Nov", "Harvest when 80% grains are golden"))),
            d("ADV_PEST", "Pest and disease guide", "ADVISORY_MODULE", false, true,
                    List.of("Pest / disease", "Crop", "Symptom", "Remedy"),
                    List.of(r("Pink bollworm", "Cotton", "Rosette flowers, damaged bolls", "Pheromone traps, neem spray"),
                            r("Whitefly", "Cotton", "Sticky leaves, yellowing", "Yellow sticky traps, neem oil"),
                            r("Stem borer", "Paddy", "Dead heart in young plants", "Remove affected tillers, light trap"),
                            r("Leaf blight", "Paddy", "Brown streaks on leaves", "Balanced nitrogen, recommended fungicide"))),

            // ---- Weather
            d("WX_FORECAST", "7-day forecast (demo data)", "WEATHER_MODULE", false, true,
                    List.of("Day", "Condition", "Temp (C)", "Rain (mm)"),
                    List.of(r("Mon", "Sunny", "32", "0"), r("Tue", "Cloudy", "30", "2"),
                            r("Wed", "Rain", "27", "18"), r("Thu", "Heavy rain", "26", "35"),
                            r("Fri", "Cloudy", "28", "5"), r("Sat", "Sunny", "31", "0"),
                            r("Sun", "Sunny", "33", "0"))),
            d("WX_ALERTS", "Weather alerts", "WEATHER_MODULE", false, true,
                    List.of("Date", "Alert", "Advice"),
                    List.of(r("2026-11-22", "Heavy rain expected", "Delay spraying and harvest, cover stored produce"),
                            r("2026-11-26", "Cold wave", "Protect nurseries and young plants overnight"))),

            // ---- Market prices
            d("MKT_PRICES", "Today's mandi prices", "MARKET_MODULE", false, true,
                    List.of("Crop", "Market", "Price (Rs/quintal)", "Date"),
                    List.of(r("Cotton", "Guntur", "7200", "2026-11-20"),
                            r("Cotton", "Adilabad", "7350", "2026-11-20"),
                            r("Cotton", "Warangal", "7100", "2026-11-20"),
                            r("Paddy", "Karimnagar", "2300", "2026-11-20"),
                            r("Paddy", "Nizamabad", "2250", "2026-11-20"),
                            r("Maize", "Warangal", "2150", "2026-11-20"))),
            d("MKT_BEST", "Best market per crop", "MARKET_MODULE", false, true,
                    List.of("Crop", "Best market", "Price (Rs/quintal)"), List.of()),

            // ---- Soil health
            d("SOIL_TESTS", "Soil test records", "SOIL_MODULE", true, true,
                    List.of("Farmer", "Plot", "pH", "Nitrogen", "Phosphorus", "Potassium"),
                    List.of(r("Ramesh Kumar", "Plot A", "7.2", "Low", "Medium", "High"),
                            r("Lakshmi Devi", "Plot B", "6.5", "Medium", "Low", "Medium"))),
            d("SOIL_RECO", "Fertilizer recommendation", "SOIL_MODULE", false, true,
                    List.of("Crop", "Nutrient low", "Fertilizer", "Dose per acre"),
                    List.of(r("Cotton", "Nitrogen", "Urea", "50 kg in 2 splits"),
                            r("Cotton", "Phosphorus", "DAP", "50 kg at sowing"),
                            r("Cotton", "Potassium", "MOP", "25 kg at flowering"),
                            r("Paddy", "Nitrogen", "Urea", "45 kg in 3 splits"))),

            // ---- Inputs and inventory
            d("INP_STOCK", "Depot stock", "INPUTS_MODULE", false, true,
                    List.of("Item", "Depot", "Stock (kg)", "Status"),
                    List.of(r("Cotton seed", "Kondapur depot", "900", "Available"),
                            r("Urea", "Rampur depot", "120", "Low stock"),
                            r("Pesticide", "Mallapur depot", "0", "Out of stock"))),
            d("INP_REQUESTS", "Input requests", "INPUTS_MODULE", true, true,
                    List.of("Farmer", "Item", "Quantity (kg)", "Status"),
                    List.of(r("Suresh Reddy", "Urea", "100", "Approved"))),

            // ---- Irrigation
            d("IRR_SCHEDULE", "Irrigation schedule", "IRRIGATION_MODULE", false, true,
                    List.of("Crop", "Growth stage", "Water every", "Method"),
                    List.of(r("Cotton", "Sowing", "10 days", "Drip"),
                            r("Cotton", "Flowering", "7 days", "Drip"),
                            r("Paddy", "Transplanting", "3 days", "Flood, 5 cm"))),
            d("IRR_USAGE", "Water usage log", "IRRIGATION_MODULE", true, true,
                    List.of("Date", "Plot", "Litres used", "Method"),
                    List.of(r("2026-11-10", "Plot A", "12000", "Drip"))),

            // ---- Schemes and finance
            d("SCH_LIST", "Government schemes", "SCHEMES_MODULE", false, true,
                    List.of("Scheme", "Benefit", "Who can apply", "Where"),
                    List.of(r("PM-KISAN", "Rs 6000 per year income support", "Small and marginal farmers", "Agriculture office / CSC"),
                            r("Kisan Credit Card", "Low interest crop loan", "All farmers", "Nearest bank"),
                            r("Drip subsidy", "Up to 90% subsidy on drip", "Small farmers", "Horticulture office"))),
            d("SCH_LOANS", "Loan tracker", "SCHEMES_MODULE", true, true,
                    List.of("Farmer", "Loan", "Amount (Rs)", "Due date", "Status"),
                    List.of(r("Mohan Lal", "Kisan Credit Card", "50000", "2027-03-31", "Active"))),

            // ---- Insurance
            d("INS_POLICIES", "Crop insurance policies", "INSURANCE_MODULE", false, true,
                    List.of("Policy", "Crop", "Premium (Rs/acre)", "Cover (Rs/acre)", "Last date"),
                    List.of(r("PMFBY Kharif", "Cotton", "800", "40000", "2026-07-31"),
                            r("PMFBY Rabi", "Paddy", "600", "35000", "2026-12-31"))),
            d("INS_CLAIMS", "Insurance claims", "INSURANCE_MODULE", true, true,
                    List.of("Farmer", "Crop", "Reason", "Claim amount (Rs)", "Status"),
                    List.of(r("Anitha Rao", "Cotton", "Hail damage", "18000", "Under review"))),

            // ---- Marketplace
            d("MP_LISTINGS", "Produce listings", "MARKETPLACE_MODULE", true, true,
                    List.of("Farmer", "Crop", "Quantity (quintal)", "Ask price (Rs/quintal)", "Status"),
                    List.of(r("Anitha Rao", "Cotton", "40", "7300", "Open"),
                            r("Ramesh Kumar", "Cotton", "25", "7250", "Open"))),
            d("MP_TRANSPORT", "Transport booking", "MARKETPLACE_MODULE", true, true,
                    List.of("Farmer", "From", "To", "Travel on", "Status"),
                    List.of(r("Suresh Reddy", "Kondapur", "Guntur ginning unit", "2026-11-28", "Confirmed"))),

            // ---- Ledger
            d("LED_ENTRIES", "Expense and income entries", "LEDGER_MODULE", true, true,
                    List.of("Date", "Type (Income/Expense)", "Item", "Amount (Rs)"),
                    List.of(r("2026-06-10", "Expense", "Cotton seed", "6000"),
                            r("2026-08-02", "Expense", "Fertilizer", "4500"),
                            r("2026-11-05", "Income", "Cotton sale", "52000"))),
            d("LED_SUMMARY", "Profit summary", "LEDGER_MODULE", false, true,
                    List.of("Total income (Rs)", "Total expense (Rs)", "Profit (Rs)"), List.of()),

            // ---- Pest reporting
            d("PEST_REPORTS", "Pest reports", "PESTREPORT_MODULE", true, true,
                    List.of("Date", "Village", "Crop", "Pest", "Description", "Reply"),
                    List.of(r("2026-11-12", "Kondapur", "Cotton", "Pink bollworm", "Holes in bolls", "Use pheromone traps"),
                            r("2026-11-14", "Kondapur", "Cotton", "Whitefly", "Sticky leaves", "Spray neem oil"),
                            r("2026-11-15", "Rampur", "Paddy", "Stem borer", "Dead hearts", "Awaiting expert"))),
            d("PEST_MAP", "Village outbreak summary", "PESTREPORT_MODULE", false, true,
                    List.of("Village", "Reports", "Level"), List.of()),

            // ---- Equipment sharing
            d("EQ_LIST", "Equipment for rent", "EQUIPMENT_MODULE", false, true,
                    List.of("Equipment", "Owner", "Village", "Rent (Rs/day)", "Available"),
                    List.of(r("Tractor", "Mohan Lal", "Rampur", "1500", "Yes"),
                            r("Power sprayer", "Ravi", "Kondapur", "300", "Yes"),
                            r("Rotavator", "Prakash", "Mallapur", "1200", "No"))),
            d("EQ_BOOK", "Equipment bookings", "EQUIPMENT_MODULE", true, true,
                    List.of("Farmer", "Equipment", "Needed on", "Status"),
                    List.of(r("Lakshmi Devi", "Tractor", "2026-11-25", "Confirmed"))));

    private static Def d(String code, String name, String module, boolean addable, boolean farmer,
                         List<String> headers, List<List<String>> seed) {
        return new Def(code, name, module, addable, farmer, headers, seed);
    }

    private static List<String> r(String... cells) {
        return List.of(cells);
    }

    static Optional<Def> find(String code) {
        return FEATURES.stream().filter(f -> f.code().equals(code)).findFirst();
    }

    /** Headers the user types in; Farmer, Date, Status and Reply are filled automatically. */
    static List<String> inputHeaders(Def d) {
        if (!d.addable()) return List.of();
        return d.headers().stream().filter(h -> !isAuto(h)).toList();
    }

    static boolean isAuto(String header) {
        return header.equals("Farmer") || header.equals("Date")
                || header.equals("Status") || header.equals("Reply");
    }
}
