package com.training.featuretoggle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private static final int PAGE_SIZE = 5;

    private final FeatureFlagService flags;
    private final FeatureFlagRepository moduleRepo;
    private final FeatureRepository features;
    private final UserFeatureAccessRepository grants;
    private final AppUserRepository users;
    private final AccessService access;

    // Small classes just to carry data to the pages
    @Data @AllArgsConstructor
    public static class ModuleRow {
        private String name;
        private String description;
        private boolean enabled;
        private int featureCount;
        private long userCount;
    }

    @Data @AllArgsConstructor
    public static class FeatureRow {
        private String code;
        private String name;
        private boolean enabled;
        private int userCount;
    }

    @Data @AllArgsConstructor
    public static class UserRow {
        private Long id;
        private String email;
        private String role;
        private boolean granted;
        private String featureNames;
    }

    private Pageable pageable(int page) {
        return PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("email"));
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    // Screen 1: list of modules
    @GetMapping("/admin/features")
    String modules(Model model) {
        List<ModuleRow> rows = moduleRepo.findAll().stream().map(m -> {
            List<Feature> fs = features.findByModuleName(m.getName());
            List<String> codes = fs.stream().map(Feature::getCode).toList();
            long userCount = grants.findByFeatureCodeIn(codes).stream()
                    .map(UserFeatureAccess::getUserId).distinct().count();
            return new ModuleRow(m.getName(), m.getDescription(), m.isEnabled(), fs.size(), userCount);
        }).toList();
        model.addAttribute("modules", rows);
        return "admin-features";
    }

    @PostMapping("/admin/modules/{name}/toggle")
    String toggleModule(@PathVariable String name, Principal principal) {
        flags.toggle(name, principal.getName());
        return "redirect:/admin/features";
    }

    // Screen 2: features inside one module
    @GetMapping("/admin/modules/{module}")
    String moduleDetail(@PathVariable String module, Model model) {
        FeatureFlag m = moduleRepo.findByName(module).orElseThrow(this::notFound);
        List<FeatureRow> rows = features.findByModuleName(module).stream()
                .map(f -> new FeatureRow(f.getCode(), f.getName(), f.isEnabled(),
                        grants.findByFeatureCode(f.getCode()).size()))
                .toList();
        model.addAttribute("mod", m);
        model.addAttribute("featureRows", rows);
        return "admin-module";
    }

    @PostMapping("/admin/feature/{code}/toggle")
    String toggleFeature(@PathVariable String code, Principal principal) {
        Feature f = access.toggleFeature(code, principal.getName());
        return "redirect:/admin/modules/" + f.getModuleName();
    }

    // Screen 3: who can use this feature (search + pagination)
    @GetMapping("/admin/feature/{code}/access")
    String featureAccess(@PathVariable String code,
                         @RequestParam(defaultValue = "") String q,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        Feature f = features.findByCode(code).orElseThrow(this::notFound);
        Page<AppUser> p = users.findByEmailContainingIgnoreCase(q, pageable(page));
        Set<Long> granted = grants.findByFeatureCode(code).stream()
                .map(UserFeatureAccess::getUserId).collect(Collectors.toSet());

        List<UserRow> rows = p.getContent().stream()
                .map(u -> new UserRow(u.getId(), u.getEmail(), u.getRole(),
                        "ADMIN".equals(u.getRole()) || granted.contains(u.getId()), ""))
                .toList();

        model.addAttribute("feature", f);
        model.addAttribute("users", rows);
        model.addAttribute("pg", p);
        model.addAttribute("q", q);
        return "admin-access";
    }

    @PostMapping("/admin/feature/{code}/grant")
    String grant(@PathVariable String code, @RequestParam Long userId,
                 @RequestParam(defaultValue = "") String q,
                 @RequestParam(defaultValue = "0") int page,
                 Principal principal, RedirectAttributes ra) {
        access.grant(code, userId, principal.getName());
        ra.addAttribute("q", q).addAttribute("page", page);
        return "redirect:/admin/feature/" + code + "/access";
    }

    @PostMapping("/admin/feature/{code}/revoke")
    String revoke(@PathVariable String code, @RequestParam Long userId,
                  @RequestParam(defaultValue = "") String q,
                  @RequestParam(defaultValue = "0") int page,
                  Principal principal, RedirectAttributes ra) {
        access.revoke(code, userId, principal.getName());
        ra.addAttribute("q", q).addAttribute("page", page);
        return "redirect:/admin/feature/" + code + "/access";
    }

    // Screen 4: all users who have access to anything in this module
    @GetMapping("/admin/modules/{module}/users")
    String moduleUsers(@PathVariable String module,
                       @RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        FeatureFlag m = moduleRepo.findByName(module).orElseThrow(this::notFound);
        List<Feature> fs = features.findByModuleName(module);
        List<String> codes = fs.stream().map(Feature::getCode).toList();
        Map<String, String> names = fs.stream()
                .collect(Collectors.toMap(Feature::getCode, Feature::getName));

        Page<AppUser> p = users.searchWithAccess(q, codes, pageable(page));
        Map<Long, List<String>> byUser = grants.findByFeatureCodeIn(codes).stream()
                .collect(Collectors.groupingBy(UserFeatureAccess::getUserId,
                        Collectors.mapping(a -> names.get(a.getFeatureCode()), Collectors.toList())));

        List<UserRow> rows = p.getContent().stream()
                .map(u -> new UserRow(u.getId(), u.getEmail(), u.getRole(), true,
                        String.join(", ", byUser.getOrDefault(u.getId(), List.of()))))
                .toList();

        model.addAttribute("mod", m);
        model.addAttribute("users", rows);
        model.addAttribute("pg", p);
        model.addAttribute("q", q);
        return "admin-module-users";
    }
}