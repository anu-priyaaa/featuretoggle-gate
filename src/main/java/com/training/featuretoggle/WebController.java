package com.training.featuretoggle;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final FeatureFlagService flags;
    private final FeatureFlagRepository moduleRepo;
    private final AccessService access;
    private final ModuleContent content;
    private final AppUserRepository users;
    private final DemoRecordRepository demoRecords;

    @GetMapping("/")
    String home() { return "redirect:/dashboard"; }

    @GetMapping("/login")
    String login() { return "login"; }

    // Modules that are ON and where this user can use at least one feature
    @GetMapping("/dashboard")
    String dashboard(Authentication auth, Model model) {
        List<FeatureFlag> visible = flags.enabled().stream()
                .filter(m -> !access.usableFeatures(m.getName(), auth).isEmpty())
                .toList();
        model.addAttribute("features", visible);
        model.addAttribute("user", auth.getName());
        return "dashboard";
    }

    @GetMapping("/modules/{name}")
    String module(@PathVariable String name, Authentication auth, Model model) {
        if (!flags.isEnabled(name)) {                          // module OFF
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        List<Feature> usable = access.usableFeatures(name, auth);
        if (usable.isEmpty()) {                                // nothing granted or all features OFF
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        model.addAttribute("title",
                moduleRepo.findByName(name).map(FeatureFlag::getDescription).orElse(name));
        model.addAttribute("sections", usable.stream().map(content::build).toList());
        model.addAttribute("module", name);
        model.addAttribute("codes", usable.stream().map(Feature::getCode).toList());
        model.addAttribute("fields", usable.stream()
                .map(f -> DemoCatalog.find(f.getCode()).map(DemoCatalog::inputHeaders).orElse(List.<String>of()))
                .toList());
        return "module";
    }

    // Add a row to a catalog feature (only if the user may use that feature)
    @PostMapping("/modules/{name}/features/{code}/add")
    String add(@PathVariable String name, @PathVariable String code,
               HttpServletRequest req, Authentication auth) {
        if (!flags.isEnabled(name)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        boolean allowed = access.usableFeatures(name, auth).stream()
                .anyMatch(f -> f.getCode().equals(code));
        DemoCatalog.Def def = DemoCatalog.find(code).orElse(null);
        if (!allowed || def == null || !def.addable()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        String who = users.findByEmail(auth.getName()).map(u -> u.getFullName() != null && !u.getFullName().isBlank()
                ? u.getFullName() : u.getEmail()).orElse(auth.getName());

        List<String> cells = new ArrayList<>();
        int i = 0;
        for (String h : def.headers()) {
            String v;
            if (!DemoCatalog.isAuto(h)) {
                v = clean(req.getParameter("f" + i++));
            } else {
                v = switch (h) {
                    case "Farmer" -> who;
                    case "Date" -> LocalDate.now().toString();
                    case "Status" -> "Pending";
                    default -> "Awaiting expert";          // Reply
                };
            }
            cells.add(v);
        }
        DemoRecord r = new DemoRecord();
        r.setFeatureCode(code);
        r.setData(String.join("|", cells));
        r.setCreatedBy(auth.getName());
        r.setCreatedAt(LocalDateTime.now());
        demoRecords.save(r);
        return "redirect:/modules/" + name;
    }

    private static String clean(String s) {
        if (s == null) return "";
        s = s.replace("|", "/").trim();
        return s.length() > 200 ? s.substring(0, 200) : s;
    }
}