package com.busbooking.bus_booking_system.config;

import com.busbooking.bus_booking_system.entity.Bus;
import com.busbooking.bus_booking_system.entity.Circuit;
import com.busbooking.bus_booking_system.entity.Role;
import com.busbooking.bus_booking_system.entity.RouteStop;
import com.busbooking.bus_booking_system.entity.Theme;
import com.busbooking.bus_booking_system.entity.User;
import com.busbooking.bus_booking_system.entity.YatraPoint;
import com.busbooking.bus_booking_system.repository.BusRepository;
import com.busbooking.bus_booking_system.repository.CircuitRepository;
import com.busbooking.bus_booking_system.repository.RouteStopRepository;
import com.busbooking.bus_booking_system.repository.ThemeRepository;
import com.busbooking.bus_booking_system.repository.UserRepository;
import com.busbooking.bus_booking_system.repository.YatraPointRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final BusRepository busRepository;
    private final CircuitRepository circuitRepository;
    private final RouteStopRepository routeStopRepository;
    private final ThemeRepository themeRepository;
    private final YatraPointRepository yatraPointRepository;
    private final CacheManager cacheManager;
    private final PasswordEncoder passwordEncoder;
    private final boolean bootstrapAdminEnabled;
    private final String bootstrapAdminEmail;
    private final String bootstrapAdminPassword;

    public DataInitializer(UserRepository userRepository,
                           BusRepository busRepository,
                           CircuitRepository circuitRepository,
                           RouteStopRepository routeStopRepository,
                           ThemeRepository themeRepository,
                           YatraPointRepository yatraPointRepository,
                           CacheManager cacheManager,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.bootstrap-admin.enabled:false}") boolean bootstrapAdminEnabled,
                           @Value("${app.bootstrap-admin.email:}") String bootstrapAdminEmail,
                           @Value("${app.bootstrap-admin.password:}") String bootstrapAdminPassword) {
        this.userRepository = userRepository;
        this.busRepository = busRepository;
        this.circuitRepository = circuitRepository;
        this.routeStopRepository = routeStopRepository;
        this.themeRepository = themeRepository;
        this.yatraPointRepository = yatraPointRepository;
        this.cacheManager = cacheManager;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapAdminEnabled = bootstrapAdminEnabled;
        this.bootstrapAdminEmail = bootstrapAdminEmail;
        this.bootstrapAdminPassword = bootstrapAdminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        ensureThemesExist(); 
        ensureYatraPointsExist(); 

        if (bootstrapAdminEnabled) {
            if (bootstrapAdminEmail == null || bootstrapAdminEmail.isBlank()
                    || bootstrapAdminPassword == null || bootstrapAdminPassword.isBlank()) {
                logger.warn("Admin bootstrap enabled but credentials are missing; skipping admin seed.");
            } else {
                userRepository.findByEmail(bootstrapAdminEmail)
                        .orElseGet(() -> {
                            User admin = new User();
                            admin.setName("Admin");
                            admin.setEmail(bootstrapAdminEmail);
                            admin.setPassword(passwordEncoder.encode(bootstrapAdminPassword));
                            admin.setRole(Role.ROLE_ADMIN);
                            logger.info("Bootstrapped admin user {}", bootstrapAdminEmail);
                            return userRepository.save(admin);
                        });
            }
        }

        // Seed initial bus if none exist
        if (busRepository.count() == 0) {

            Bus bus = new Bus();
            bus.setFromLocation("konark");
            bus.setToLocation("ramappa");
            bus.setDepartureTime(LocalDateTime.now().plusDays(1));
            bus.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(6));
            bus.setPrice(1100);

            // NEW FIELD (replaces availableSeats)
            bus.setTotalSeats(40);

            busRepository.save(bus);
        }

        ensureThemeCircuitsAndBusCoverage();
        ensureYatraPointThemeMappings();
    }

    // =========================================================
// 🔥 ENSURE THEMES EXIST (PRODUCTION SAFE)
// =========================================================
private void ensureThemesExist() {
    

    if (themeRepository.count() > 0) {
        return; // Already seeded
    }

    logger.info("Seeding initial civilizational themes...");

    List<String> themeNames = List.of(
            "Sacred Geography",
            "Temple Architecture",
            "Ancient Universities",
            "Indic Science & Mathematics",
            "Civilizational Capitals",
            "Bhakti Movement",
            "Maritime Civilizations",
            "Silk Route Bharat"
    );

    for (String name : themeNames) {
        Theme theme = new Theme();
        theme.setName(name);
        theme.setDescription(name + " dimension of the civilizational journey.");
        themeRepository.save(theme);
    }

    logger.info("Themes seeded successfully.");
}

    // =========================================================
// 🔥 ENSURE YATRA POINTS EXIST (PRODUCTION SAFE)
// =========================================================
private void ensureYatraPointsExist() {

    if (yatraPointRepository.count() > 0) {
        return; // Already seeded
    }

    logger.info("Seeding initial heritage nodes (YatraPoints)...");

    List<YatraPoint> points = List.of(

        buildPoint("kashi-vishwanath", "Kashi Vishwanath", 25.3109, 83.0107),
        buildPoint("ujjain-mahakal", "Mahakaleshwar Jyotirlinga", 23.1828, 75.7680),
        buildPoint("rameshwaram-temple", "Ramanathaswamy Temple", 9.2881, 79.3174),
        buildPoint("somnath-temple", "Somnath Temple", 20.8880, 70.4012),
        buildPoint("mahabalipuram-shore-temple", "Shore Temple", 12.6165, 80.1926),
        buildPoint("nalanda-mahavihara", "Nalanda Mahavihara", 25.1367, 85.4431),
        buildPoint("takshashila", "Takshashila University", 33.7456, 72.7867),
        buildPoint("pataliputra-patna", "Pataliputra", 25.5941, 85.1376)

);

    yatraPointRepository.saveAll(points);

    logger.info("YatraPoints seeded successfully.");
}

private YatraPoint buildPoint(
        String slug,
        String name,
        double latitude,
        double longitude
) {
    return YatraPoint.builder()
            .slug(slug)
            .name(name)
            .latitude(latitude)
            .longitude(longitude)
            .shortHistory(name + " – a civilizational heritage node.")
            .recommendedZoomLevel(6)
            .popularityScore(10)
            .culturalSignificanceScore(70)
            .metaDescription(name + " heritage site")
            .active(true)
            .build();
}

    private void ensureThemeCircuitsAndBusCoverage() {
        List<Theme> allThemes = themeRepository.findAll();
        if (allThemes.isEmpty()) {
            return;
        }

        Map<String, Theme> themesByName = allThemes.stream()
                .collect(Collectors.toMap(
                        t -> t.getName().trim().toLowerCase(),
                        t -> t,
                        (a, b) -> a
                ));

        List<CircuitSpec> specs = List.of(
                new CircuitSpec(
                        "sacred-river-jyotirlinga-circuit",
                        "Sacred River & Jyotirlinga Circuit",
                        "Sacred geography across Jyotirlinga and tirtha spaces.",
                        1,
                        List.of("Sacred Geography", "Bhakti Movement", "Temple Architecture"),
                        "Varanasi",
                        "Rameshwaram",
                        List.of("kashi-vishwanath", "ujjain-mahakal", "rameshwaram-temple")
                ),
                new CircuitSpec(
                        "temple-architecture-spine",
                        "Temple Architecture Spine",
                        "Civilizational temple architecture across regions.",
                        2,
                        List.of("Temple Architecture", "Sacred Geography"),
                        "Konark",
                        "Mahabalipuram",
                        List.of("somnath-temple", "mahabalipuram-shore-temple", "rameshwaram-temple")
                ),
                new CircuitSpec(
                        "knowledge-heritage-circuit",
                        "Knowledge Heritage Circuit",
                        "Ancient universities and knowledge centers of Bharat.",
                        3,
                        List.of("Ancient Universities", "Indic Science & Mathematics", "Civilizational Capitals"),
                        "Nalanda",
                        "Takshashila",
                        List.of("nalanda-mahavihara", "pataliputra-patna", "takshashila")
                ),
                new CircuitSpec(
                        "maritime-trade-circuit",
                        "Maritime & Trade Circuit",
                        "Maritime ports and trade corridors of civilizational Bharat.",
                        4,
                        List.of("Maritime Civilizations", "Silk Route Bharat", "Temple Architecture"),
                        "Somnath",
                        "Mahabalipuram",
                        List.of("somnath-temple", "mahabalipuram-shore-temple")
                )
        );
        Map<String, CircuitSpec> specsBySlug = specs.stream()
                .collect(Collectors.toMap(CircuitSpec::slug, s -> s));

        Map<String, Circuit> circuitsBySlug = new HashMap<>();
        boolean circuitsChanged = false;

        for (CircuitSpec spec : specs) {
            Circuit circuit = circuitRepository.findBySlug(spec.slug()).orElseGet(Circuit::new);
            boolean mutated = false;

            boolean isNew = circuit.getId() == null;
            if (isNew) {
                circuit.setSlug(spec.slug());
                mutated = true;
            }

            if (!Objects.equals(circuit.getName(), spec.name())) {
                circuit.setName(spec.name());
                mutated = true;
            }
            if (!Objects.equals(circuit.getDescription(), spec.description())) {
                circuit.setDescription(spec.description());
                mutated = true;
            }
            if (!Objects.equals(circuit.getDisplayOrder(), spec.displayOrder())) {
                circuit.setDisplayOrder(spec.displayOrder());
                mutated = true;
            }
            if (!Boolean.TRUE.equals(circuit.getActive())) {
                circuit.setActive(true);
                mutated = true;
            }

            Set<Theme> desiredThemes = spec.themeNames().stream()
                    .map(String::toLowerCase)
                    .map(themesByName::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (!desiredThemes.isEmpty() && !desiredThemes.equals(circuit.getThemes())) {
                circuit.setThemes(desiredThemes);
                mutated = true;
            }

            if (mutated) {
                circuit = circuitRepository.save(circuit);
                circuitsChanged = true;
            }
            if (circuit.getId() == null) {
                circuit = circuitRepository.save(circuit);
                circuitsChanged = true;
            }
            circuitsBySlug.put(spec.slug(), circuit);
        }

        List<Bus> buses = busRepository.findAll();
        boolean busesChanged = false;

        for (Bus bus : buses) {
            if (bus.getCircuit() != null) {
                continue;
            }

            Circuit resolved = resolveCircuitForBus(bus, circuitsBySlug);
            if (resolved != null) {
                bus.setCircuit(resolved);
                busRepository.save(bus);
                busesChanged = true;
            }
        }

        int seedIndex = 0;
        for (CircuitSpec spec : specs) {
            Circuit circuit = circuitsBySlug.get(spec.slug());
            if (circuit == null) {
                continue;
            }

            boolean hasBus = busRepository.findAll().stream()
                    .anyMatch(b -> b.getCircuit() != null && Objects.equals(b.getCircuit().getId(), circuit.getId()));

            if (hasBus) {
                continue;
            }

            Bus seed = new Bus();
            seed.setCircuit(circuit);
            seed.setFromLocation(spec.seedFrom());
            seed.setToLocation(spec.seedTo());
            seed.setDepartureTime(LocalDateTime.now().plusDays(7 + seedIndex));
            seed.setArrivalTime(LocalDateTime.now().plusDays(7 + seedIndex).plusHours(8));
            seed.setPrice(1500 + (seedIndex * 200));
            seed.setTotalSeats(40);
            busRepository.save(seed);
            seedIndex++;
            busesChanged = true;
        }

        for (Bus bus : busRepository.findAll()) {
            if (bus.getCircuit() == null || bus.getCircuit().getSlug() == null) {
                continue;
            }
            CircuitSpec spec = specsBySlug.get(bus.getCircuit().getSlug());
            if (spec == null) {
                continue;
            }
            boolean busMutated = false;
            String from = bus.getFromLocation() == null ? "" : bus.getFromLocation().trim().toLowerCase();
            String to = bus.getToLocation() == null ? "" : bus.getToLocation().trim().toLowerCase();
            if (from.startsWith("theme hub") || from.startsWith("test-")) {
                bus.setFromLocation(spec.seedFrom());
                busMutated = true;
            }
            if (to.startsWith("civilizational node") || to.startsWith("test-")) {
                bus.setToLocation(spec.seedTo());
                busMutated = true;
            }
            if (busMutated) {
                busRepository.save(bus);
                busesChanged = true;
            }
        }

        boolean routeStopsChanged = ensureRouteStops(specsBySlug);
        if (circuitsChanged || busesChanged) {
            clearBusCaches();
            logger.info("Reconciled circuits/themes and bus coverage for all theme clusters.");
        }
        if (routeStopsChanged) {
            clearBusCaches();
            logger.info("Seeded route stops for thematic circuits.");
        }
    }

    private boolean ensureRouteStops(Map<String, CircuitSpec> specsBySlug) {
        List<YatraPoint> points = yatraPointRepository.findAll();
        if (points.isEmpty()) {
            return false;
        }
        Map<String, YatraPoint> pointsBySlug = points.stream()
                .filter(p -> p.getSlug() != null)
                .collect(Collectors.toMap(
                        p -> p.getSlug().trim().toLowerCase(),
                        p -> p,
                        (a, b) -> a
                ));

        boolean changed = false;
        for (Bus bus : busRepository.findAll()) {
            if (bus.getCircuit() == null || bus.getCircuit().getSlug() == null) {
                continue;
            }
            CircuitSpec spec = specsBySlug.get(bus.getCircuit().getSlug());
            if (spec == null) {
                continue;
            }

            List<YatraPoint> desired = spec.routeStopSlugs().stream()
                    .map(String::toLowerCase)
                    .map(pointsBySlug::get)
                    .filter(Objects::nonNull)
                    .toList();
            if (desired.isEmpty()) {
                continue;
            }

            List<RouteStop> existing = new ArrayList<>(bus.getRouteStops());
            List<String> existingSlugs = existing.stream()
                    .sorted(Comparator.comparing(RouteStop::getSequenceOrder))
                    .map(RouteStop::getYatraPoint)
                    .filter(Objects::nonNull)
                    .map(YatraPoint::getSlug)
                    .filter(Objects::nonNull)
                    .map(s -> s.trim().toLowerCase())
                    .toList();
            List<String> desiredSlugs = desired.stream()
                    .map(YatraPoint::getSlug)
                    .filter(Objects::nonNull)
                    .map(s -> s.trim().toLowerCase())
                    .toList();

            if (existingSlugs.equals(desiredSlugs)) {
                continue;
            }

            routeStopRepository.deleteAll(existing);
            bus.getRouteStops().clear();

            int seq = 1;
            for (YatraPoint point : desired) {
                RouteStop stop = RouteStop.builder()
                        .bus(bus)
                        .yatraPoint(point)
                        .sequenceOrder(seq)
                        .travelMinutesFromPrevious(seq == 1 ? 0 : 180)
                        .distanceKmFromPrevious(seq == 1 ? 0.0 : 220.0)
                        .build();
                bus.getRouteStops().add(stop);
                seq++;
            }
            busRepository.save(bus);
            changed = true;
        }
        return changed;
    }

    private void ensureYatraPointThemeMappings() {

        List<YatraPoint> points = yatraPointRepository.findAll();
        List<Theme> themes = themeRepository.findAll();

        if (points.isEmpty() || themes.isEmpty()) {
            return;
        }

        Map<String, Theme> themesByName = themes.stream()
                .collect(Collectors.toMap(
                        t -> t.getName().trim().toLowerCase(),
                        t -> t,
                        (a, b) -> a
                ));

        Theme sacred = themesByName.get("sacred geography");
        Theme temple = themesByName.get("temple architecture");
        Theme universities = themesByName.get("ancient universities");
        Theme science = themesByName.get("indic science & mathematics");
        Theme capitals = themesByName.get("civilizational capitals");
        Theme bhakti = themesByName.get("bhakti movement");
        Theme maritime = themesByName.get("maritime civilizations");
        Theme silkRoute = themesByName.get("silk route bharat");

        int updatedPoints = 0;
        int linked = 0;
        for (YatraPoint point : points) {
            String slug = point.getSlug() == null ? "" : point.getSlug().toLowerCase();
            Set<Theme> assigned = new HashSet<>();

            if (slug.contains("kashi") || slug.contains("mahakal") || slug.contains("rameshwaram")) {
                addIfPresent(assigned, sacred, temple, bhakti);
            } else if (slug.contains("somnath") || slug.contains("mahabalipuram")) {
                addIfPresent(assigned, temple, maritime, sacred);
            } else if (slug.contains("nalanda") || slug.contains("takshashila")) {
                addIfPresent(assigned, universities, science, silkRoute);
            } else if (slug.contains("pataliputra")) {
                addIfPresent(assigned, capitals, universities, science);
            } else {
                addIfPresent(assigned, sacred);
            }

            if (assigned.isEmpty()) {
                if (point.getThemes() != null && !point.getThemes().isEmpty()) {
                    assigned.addAll(point.getThemes());
                } else {
                    assigned.add(themes.get(0));
                }
            }

            Set<Theme> current = point.getThemes() == null
                    ? new HashSet<>()
                    : new HashSet<>(point.getThemes());

            if (!current.containsAll(assigned) || current.isEmpty()) {
                current.addAll(assigned);
                point.setThemes(current);
                yatraPointRepository.save(point);
                updatedPoints++;
                linked += current.size();
            }
        }

        if (updatedPoints > 0) {
            clearTrailCache();
            logger.info(
                    "Auto-reconciled yatra themes for {} points ({} links).",
                    updatedPoints,
                    linked
            );
        }
    }

    private void addIfPresent(Set<Theme> target, Theme... candidates) {
        for (Theme candidate : candidates) {
            if (candidate != null) {
                target.add(candidate);
            }
        }
    }

    private void clearTrailCache() {
        Cache cache = cacheManager.getCache("generated_trails");
        if (cache != null) {
            cache.clear();
        }
    }

    private void clearBusCaches() {
        clearTrailCache();
        Cache busCache = cacheManager.getCache("bus");
        if (busCache != null) {
            busCache.clear();
        }
        Cache routeMapCache = cacheManager.getCache("route-map");
        if (routeMapCache != null) {
            routeMapCache.clear();
        }
    }

    private Circuit resolveCircuitForBus(Bus bus, Map<String, Circuit> circuitsBySlug) {
        String route = ((bus.getFromLocation() == null ? "" : bus.getFromLocation()) + " "
                + (bus.getToLocation() == null ? "" : bus.getToLocation())).toLowerCase();

        if (route.contains("rameshwaram") || route.contains("varanasi")
                || route.contains("kashi") || route.contains("ujjain")) {
            return circuitsBySlug.get("sacred-river-jyotirlinga-circuit");
        }
        if (route.contains("nalanda") || route.contains("takshashila")
                || route.contains("patna") || route.contains("pataliputra")) {
            return circuitsBySlug.get("knowledge-heritage-circuit");
        }
        if (route.contains("somnath") || route.contains("mahabalipuram")
                || route.contains("coast") || route.contains("port")) {
            return circuitsBySlug.get("maritime-trade-circuit");
        }

        return circuitsBySlug.get("temple-architecture-spine");
    }

    private record CircuitSpec(
            String slug,
            String name,
            String description,
            int displayOrder,
            List<String> themeNames,
            String seedFrom,
            String seedTo,
            List<String> routeStopSlugs
    ) {}
}
