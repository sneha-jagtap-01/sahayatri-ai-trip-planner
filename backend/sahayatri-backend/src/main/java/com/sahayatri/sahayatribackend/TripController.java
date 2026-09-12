package com.sahayatri.sahayatribackend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class TripController {

    @Value("${google.maps.api.key}")
    private String googleApiKey;

    @Value("${google.maps.browser.key:}")
    private String browserMapsKey;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GEMINI_MODEL = "gemini-2.5-flash";

    // =========================================================
    // MAIN TRIP API
    // =========================================================

    @GetMapping("/trip")
    public Map<String, Object> generateTrip(
            @RequestParam String location,
            @RequestParam int days,
            @RequestParam double budget,
            @RequestParam String mood,
            @RequestParam(required = false, defaultValue = "") String preferences,
            @RequestParam(required = false, defaultValue = "0") long refresh) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("location", location);
        response.put("days", days);
        response.put("budget", budget);
        response.put("mood", mood);
        response.put("preferences", preferences);

        try {

            // -------------------------------------------------
            // 1. FIND STARTING LOCATION
            // -------------------------------------------------

            Map<String, Object> locationData =
                    findLocation(location);

            if (locationData == null) {
                response.put(
                        "error",
                        "Starting location could not be found."
                );
                return response;
            }

            Map<String, Object> startLocation =
                    (Map<String, Object>) locationData.get("location");

            double latitude =
                    ((Number) startLocation.get("latitude"))
                            .doubleValue();

            double longitude =
                    ((Number) startLocation.get("longitude"))
                            .doubleValue();

            // -------------------------------------------------
            // 2. FIND REAL TOURIST PLACES
            // -------------------------------------------------

            List<Map<String, Object>> places =
                    findTouristPlaces(
                            latitude,
                            longitude,
                            mood
                    );

            // -------------------------------------------------
            // 3. FILTER ACCORDING TO USER PREFERENCES
            // -------------------------------------------------

            List<Map<String, Object>> filteredPlaces =
                    filterPlacesByPreferences(
                            places,
                            mood,
                            preferences
                    );

            /*
             * If filtering gives very few places,
             * add original Google places as backup.
             */

            if (filteredPlaces.size() < Math.min(days * 3, 20)) {

                LinkedHashMap<String, Map<String, Object>> unique =
                        new LinkedHashMap<>();

                for (Map<String, Object> place : filteredPlaces) {
                    String key = getPlaceName(place);
                    unique.put(key, place);
                }

                for (Map<String, Object> place : places) {
                    String key = getPlaceName(place);
                    unique.putIfAbsent(key, place);
                }

                filteredPlaces =
                        new ArrayList<>(unique.values());
            }

            // -------------------------------------------------
            // 4. REGENERATE VARIATION
            // -------------------------------------------------

            if (!filteredPlaces.isEmpty()) {

                int rotation =
                        (int) (Math.abs(refresh)
                                % filteredPlaces.size());

                Collections.rotate(
                        filteredPlaces,
                        rotation
                );
            }

            // -------------------------------------------------
            // 5. SMART ITINERARY
            // -------------------------------------------------

            List<Map<String, Object>> itinerary =
                    buildItinerary(
                            filteredPlaces,
                            days,
                            mood,
                            location
                    );

            response.put(
                    "itinerary",
                    itinerary
            );

            // -------------------------------------------------
            // 6. COMPLETE ROUTE
            // -------------------------------------------------

            List<String> route =
                    buildRoute(itinerary);

            response.put(
                    "route",
                    route
            );

            // -------------------------------------------------
            // 7. GOOGLE MAP
            // -------------------------------------------------

            String mapUrl =
                    buildGoogleMapUrl(route);

            response.put(
                    "mapUrl",
                    mapUrl
            );

            // -------------------------------------------------
            // 8. LOCAL FOOD / RESTAURANTS
            // -------------------------------------------------

            List<Map<String, Object>> localFood =
                    findLocalFood(
                            latitude,
                            longitude
                    );

            response.put(
                    "localFood",
                    localFood
            );

            // -------------------------------------------------
            // 9. DYNAMIC BUDGET
            // -------------------------------------------------

            Map<String, Integer> budgetEstimate =
                    calculateBudget(
                            budget,
                            days,
                            mood
                    );

            response.put(
                    "budgetEstimate",
                    budgetEstimate
            );

            // -------------------------------------------------
            // 10. TRAVEL READINESS SCORE
            // -------------------------------------------------

            double readinessScore =
                    calculateTravelReadiness(
                            filteredPlaces,
                            localFood,
                            days,
                            mood
                    );

            response.put(
                    "safetyScore",
                    readinessScore
            );

            response.put(
                    "scoreLabel",
                    "Sahayatri Travel Readiness"
            );

            // -------------------------------------------------
            // 11. AI EXPLANATION
            // -------------------------------------------------

            String aiExplanation =
                    generateAIExplanation(
                            location,
                            days,
                            budget,
                            mood,
                            preferences,
                            route
                    );

            response.put(
                    "aiExplanation",
                    aiExplanation
            );

            // -------------------------------------------------
            // 12. AI TIPS
            // -------------------------------------------------

            String aiTips =
                    generateAITips(
                            location,
                            mood,
                            days,
                            budget
                    );

            response.put(
                    "aiTips",
                    aiTips
            );

            // -------------------------------------------------
            // 13. REAL PLACES
            // -------------------------------------------------

            response.put(
                    "places",
                    filteredPlaces
            );

            response.put(
                    "success",
                    true
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.put(
                    "success",
                    false
            );

            response.put(
                    "error",
                    "Unable to generate trip: "
                            + e.getMessage()
            );
        }

        return response;
    }

    // =========================================================
    // FIND LOCATION
    // =========================================================

    private Map<String, Object> findLocation(
            String location) {

        String url =
                "https://places.googleapis.com/v1/places:searchText";

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        headers.set(
                "X-Goog-Api-Key",
                googleApiKey
        );

        headers.set(
                "X-Goog-FieldMask",
                "places.location,places.displayName"
        );

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "textQuery",
                location + ", India"
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(
                        body,
                        headers
                );

        ResponseEntity<Map> result =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        request,
                        Map.class
                );

        if (result.getBody() == null) {
            return null;
        }

        List<Map<String, Object>> places =
                (List<Map<String, Object>>)
                        result.getBody().get("places");

        if (places == null
                || places.isEmpty()) {

            return null;
        }

        return places.get(0);
    }

    // =========================================================
    // FIND TOURIST PLACES
    // =========================================================

    private List<Map<String, Object>> findTouristPlaces(
            double latitude,
            double longitude,
            String mood) {

        String url =
                "https://places.googleapis.com/v1/places:searchNearby";

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        headers.set(
                "X-Goog-Api-Key",
                googleApiKey
        );

        headers.set(
                "X-Goog-FieldMask",
                "places.displayName,"
                        + "places.formattedAddress,"
                        + "places.location,"
                        + "places.googleMapsUri,"
                        + "places.primaryType"
        );

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "includedTypes",
                List.of("tourist_attraction")
        );

        body.put(
                "maxResultCount",
                20
        );

        body.put(
                "rankPreference",
                "POPULARITY"
        );

        Map<String, Object> center =
                new LinkedHashMap<>();

        center.put(
                "latitude",
                latitude
        );

        center.put(
                "longitude",
                longitude
        );

        Map<String, Object> circle =
                new LinkedHashMap<>();

        circle.put(
                "center",
                center
        );

        circle.put(
                "radius",
                50000.0
        );

        Map<String, Object> restriction =
                new LinkedHashMap<>();

        restriction.put(
                "circle",
                circle
        );

        body.put(
                "locationRestriction",
                restriction
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(
                        body,
                        headers
                );

        ResponseEntity<Map> result =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        request,
                        Map.class
                );

        if (result.getBody() == null) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> places =
                (List<Map<String, Object>>)
                        result.getBody().get("places");

        if (places == null) {
            return new ArrayList<>();
        }

        return places;
    }

    // =========================================================
    // FILTER PLACES
    // =========================================================

    private List<Map<String, Object>>
    filterPlacesByPreferences(
            List<Map<String, Object>> places,
            String mood,
            String preferences) {

        List<Map<String, Object>> filtered =
                new ArrayList<>();

        String pref =
                preferences == null
                        ? ""
                        : preferences.toLowerCase();

        String moodText =
                mood == null
                        ? ""
                        : mood.toLowerCase();

        for (Map<String, Object> place : places) {

            String type =
                    String.valueOf(
                            place.getOrDefault(
                                    "primaryType",
                                    ""
                            )
                    ).toLowerCase();

            String name =
                    getPlaceName(place)
                            .toLowerCase();

            boolean match = false;

            // NATURE
            if (pref.contains("nature")) {

                if (type.contains("park")
                        || type.contains("garden")
                        || type.contains("natural")
                        || name.contains("garden")
                        || name.contains("lake")
                        || name.contains("waterfall")) {

                    match = true;
                }
            }

            // TREKKING
            if (pref.contains("trekking")
                    || moodText.contains("adventure")) {

                if (type.contains("fort")
                        || type.contains("hiking")
                        || name.contains("fort")
                        || name.contains("hill")
                        || name.contains("trek")) {

                    match = true;
                }
            }

            // HISTORY
            if (pref.contains("history")) {

                if (type.contains("historical")
                        || type.contains("museum")
                        || type.contains("fort")
                        || name.contains("fort")
                        || name.contains("museum")
                        || name.contains("wada")) {

                    match = true;
                }
            }

            // PEACEFUL
            if (pref.contains("peaceful")
                    || moodText.contains("spiritual")) {

                if (type.contains("temple")
                        || type.contains("church")
                        || type.contains("mosque")
                        || type.contains("garden")
                        || name.contains("temple")
                        || name.contains("garden")) {

                    match = true;
                }
            }

            // PHOTOGRAPHY
            if (pref.contains("photography")) {

                if (type.contains("park")
                        || type.contains("tourist")
                        || name.contains("fort")
                        || name.contains("garden")
                        || name.contains("lake")) {

                    match = true;
                }
            }

            // CHILL
            if (moodText.contains("chill")) {

                if (type.contains("park")
                        || type.contains("garden")
                        || type.contains("lake")
                        || type.contains("spa")) {

                    match = true;
                }
            }

            // ROMANTIC
            if (moodText.contains("romantic")) {

                if (type.contains("park")
                        || type.contains("garden")
                        || type.contains("tourist")) {

                    match = true;
                }
            }

            if (match) {
                filtered.add(place);
            }
        }

        return filtered;
    }

    // =========================================================
    // SMART ITINERARY
    // =========================================================

    private List<Map<String, Object>> buildItinerary(
            List<Map<String, Object>> places,
            int days,
            String mood,
            String location) {

        List<Map<String, Object>> itinerary =
                new ArrayList<>();

        int placeIndex = 0;

        for (int i = 1; i <= days; i++) {

            Map<String, Object> day =
                    new LinkedHashMap<>();

            day.put(
                    "day",
                    "Day " + i
            );

            day.put(
                    "location",
                    location
            );

            List<Map<String, Object>> activities =
                    new ArrayList<>();

            // ---------------------------------------------
            // MORNING
            // ---------------------------------------------

            if (placeIndex < places.size()) {

                Map<String, Object> place =
                        places.get(placeIndex++);

                activities.add(
                        createActivity(
                                "09:00 AM",
                                place,
                                getMorningTitle(
                                        getPlaceName(place),
                                        mood
                                )
                        )
                );

            } else {

                activities.add(
                        createFallbackActivity(
                                "09:00 AM",
                                "Relaxed morning in " + location
                        )
                );
            }

            // ---------------------------------------------
            // AFTERNOON
            // ---------------------------------------------

            if (placeIndex < places.size()) {

                Map<String, Object> place =
                        places.get(placeIndex++);

                activities.add(
                        createActivity(
                                "12:30 PM",
                                place,
                                "Explore "
                                        + getPlaceName(place)
                        )
                );

            } else {

                activities.add(
                        createFallbackActivity(
                                "12:30 PM",
                                "Enjoy local food and explore the nearby area."
                        )
                );
            }

            // ---------------------------------------------
            // EVENING
            // ---------------------------------------------

            if (placeIndex < places.size()) {

                Map<String, Object> place =
                        places.get(placeIndex++);

                activities.add(
                        createActivity(
                                "05:00 PM",
                                place,
                                getEveningTitle(
                                        getPlaceName(place),
                                        mood
                                )
                        )
                );

            } else {

                activities.add(
                        createFallbackActivity(
                                "05:00 PM",
                                "Relax and enjoy the evening according to your "
                                        + mood
                                        + " mood."
                        )
                );
            }

            day.put(
                    "activities",
                    activities
            );

            itinerary.add(day);
        }

        return itinerary;
    }

    // =========================================================
    // CREATE ACTIVITY
    // =========================================================

    private Map<String, Object> createActivity(
            String time,
            Map<String, Object> place,
            String description) {

        Map<String, Object> activity =
                new LinkedHashMap<>();

        activity.put(
                "time",
                time
        );

        activity.put(
                "name",
                getPlaceName(place)
        );

        activity.put(
                "description",
                description
        );

        activity.put(
                "address",
                place.getOrDefault(
                        "formattedAddress",
                        ""
                )
        );

        activity.put(
                "mapsUri",
                place.getOrDefault(
                        "googleMapsUri",
                        ""
                )
        );

        activity.put(
                "primaryType",
                place.getOrDefault(
                        "primaryType",
                        ""
                )
        );

        return activity;
    }

    // =========================================================
    // FALLBACK ACTIVITY
    // =========================================================

    private Map<String, Object>
    createFallbackActivity(
            String time,
            String description) {

        Map<String, Object> activity =
                new LinkedHashMap<>();

        activity.put(
                "time",
                time
        );

        activity.put(
                "name",
                "Free Time"
        );

        activity.put(
                "description",
                description
        );

        activity.put(
                "address",
                ""
        );

        activity.put(
                "mapsUri",
                ""
        );

        return activity;
    }

    // =========================================================
    // MORNING TITLE
    // =========================================================

    private String getMorningTitle(
            String place,
            String mood) {

        if (mood.equalsIgnoreCase("Adventure")) {

            return place
                    + " exploration & adventure";

        } else if (mood.equalsIgnoreCase("Chill")) {

            return "Relax and explore "
                    + place;

        } else if (mood.equalsIgnoreCase("Spiritual")) {

            return "Peaceful visit to "
                    + place;

        } else if (mood.equalsIgnoreCase("Romantic")) {

            return "Romantic experience at "
                    + place;
        }

        return "Explore " + place;
    }

    // =========================================================
    // EVENING TITLE
    // =========================================================

    private String getEveningTitle(
            String place,
            String mood) {

        if (mood.equalsIgnoreCase("Adventure")) {

            return "Evening adventure at "
                    + place;

        } else if (mood.equalsIgnoreCase("Chill")) {

            return "Relaxing evening at "
                    + place;

        } else if (mood.equalsIgnoreCase("Spiritual")) {

            return "Peaceful evening at "
                    + place;

        } else if (mood.equalsIgnoreCase("Romantic")) {

            return "Romantic evening at "
                    + place;
        }

        return "Evening visit to "
                + place;
    }

    // =========================================================
    // BUILD COMPLETE ROUTE
    // =========================================================

    private List<String> buildRoute(
            List<Map<String, Object>> itinerary) {

        List<String> route =
                new ArrayList<>();

        Set<String> uniquePlaces =
                new LinkedHashSet<>();

        for (Map<String, Object> day : itinerary) {

            List<Map<String, Object>> activities =
                    (List<Map<String, Object>>)
                            day.get("activities");

            for (Map<String, Object> activity :
                    activities) {

                String name =
                        String.valueOf(
                                activity.getOrDefault(
                                        "name",
                                        ""
                                )
                        );

                if (!name.equals("Free Time")
                        && !name.isBlank()) {

                    uniquePlaces.add(name);
                }
            }
        }

        route.addAll(uniquePlaces);

        return route;
    }

    // =========================================================
    // GOOGLE MAP URL
    // =========================================================

    private String buildGoogleMapUrl(
            List<String> route) {

        if (browserMapsKey == null
                || browserMapsKey.isBlank()) {

            return "";
        }

        if (route.size() < 2) {
            return "";
        }

        String origin =
                encode(route.get(0));

        String destination =
                encode(
                        route.get(route.size() - 1)
                );

        StringBuilder url =
                new StringBuilder();

        url.append(
                "https://www.google.com/maps/embed/v1/directions"
        );

        url.append(
                "?key="
        );

        url.append(
                encode(browserMapsKey)
        );

        url.append(
                "&origin="
        );

        url.append(origin);

        url.append(
                "&destination="
        );

        url.append(destination);

        if (route.size() > 2) {

            url.append(
                    "&waypoints="
            );

            for (int i = 1;
                 i < route.size() - 1;
                 i++) {

                if (i > 1) {
                    url.append("|");
                }

                url.append(
                        encode(route.get(i))
                );
            }
        }

        url.append(
                "&mode=driving"
        );

        return url.toString();
    }

    // =========================================================
    // LOCAL FOOD / RESTAURANTS
    // =========================================================

    private List<Map<String, Object>>
    findLocalFood(
            double latitude,
            double longitude) {

        String url =
                "https://places.googleapis.com/v1/places:searchNearby";

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        headers.set(
                "X-Goog-Api-Key",
                googleApiKey
        );

        headers.set(
                "X-Goog-FieldMask",
                "places.displayName,"
                        + "places.formattedAddress,"
                        + "places.location,"
                        + "places.googleMapsUri,"
                        + "places.primaryType"
        );

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "includedTypes",
                List.of("restaurant")
        );

        body.put(
                "maxResultCount",
                6
        );

        body.put(
                "rankPreference",
                "POPULARITY"
        );

        Map<String, Object> center =
                new LinkedHashMap<>();

        center.put(
                "latitude",
                latitude
        );

        center.put(
                "longitude",
                longitude
        );

        Map<String, Object> circle =
                new LinkedHashMap<>();

        circle.put(
                "center",
                center
        );

        circle.put(
                "radius",
                10000.0
        );

        Map<String, Object> restriction =
                new LinkedHashMap<>();

        restriction.put(
                "circle",
                circle
        );

        body.put(
                "locationRestriction",
                restriction
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(
                        body,
                        headers
                );

        try {

            ResponseEntity<Map> result =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            request,
                            Map.class
                    );

            if (result.getBody() == null) {
                return new ArrayList<>();
            }

            List<Map<String, Object>> restaurants =
                    (List<Map<String, Object>>)
                            result.getBody().get("places");

            if (restaurants == null) {
                return new ArrayList<>();
            }

            return restaurants;

        } catch (Exception e) {

            System.out.println(
                    "Food API error: "
                            + e.getMessage()
            );

            return new ArrayList<>();
        }
    }

    // =========================================================
    // BUDGET CALCULATION
    // =========================================================

    private Map<String, Integer>
    calculateBudget(
            double budget,
            int days,
            String mood) {

        int total =
                (int) Math.round(budget);

        double accommodationPercent = 0.35;
        double foodPercent = 0.20;
        double transportPercent = 0.30;
        double activitiesPercent = 0.15;

        // Adventure usually has higher activity cost
        if (mood.equalsIgnoreCase("Adventure")) {

            accommodationPercent = 0.30;
            foodPercent = 0.20;
            transportPercent = 0.30;
            activitiesPercent = 0.20;
        }

        // Romantic trips may spend slightly more on stay
        if (mood.equalsIgnoreCase("Romantic")) {

            accommodationPercent = 0.40;
            foodPercent = 0.25;
            transportPercent = 0.20;
            activitiesPercent = 0.15;
        }

        Map<String, Integer> budgetMap =
                new LinkedHashMap<>();

        int accommodation =
                (int) Math.round(
                        total * accommodationPercent
                );

        int food =
                (int) Math.round(
                        total * foodPercent
                );

        int transport =
                (int) Math.round(
                        total * transportPercent
                );

        int activities =
                total
                        - accommodation
                        - food
                        - transport;

        budgetMap.put(
                "accommodation",
                accommodation
        );

        budgetMap.put(
                "food",
                food
        );

        budgetMap.put(
                "transport",
                transport
        );

        budgetMap.put(
                "activities",
                activities
        );

        return budgetMap;
    }

    // =========================================================
    // TRAVEL READINESS SCORE
    // =========================================================

    private double calculateTravelReadiness(
            List<Map<String, Object>> places,
            List<Map<String, Object>> food,
            int days,
            String mood) {

        double score = 7.0;

        if (places.size() >= days * 3) {
            score += 1.2;
        } else if (places.size() >= days * 2) {
            score += 0.7;
        } else {
            score += 0.3;
        }

        if (food.size() >= 3) {
            score += 0.4;
        }

        if (days <= 3) {
            score += 0.3;
        }

        if (mood.equalsIgnoreCase("Adventure")) {
            score -= 0.4;
        }

        if (mood.equalsIgnoreCase("Chill")) {
            score += 0.2;
        }

        score =
                Math.max(
                        5.0,
                        Math.min(
                                10.0,
                                score
                        )
                );

        return Math.round(score * 10.0) / 10.0;
    }

    // =========================================================
    // GEMINI AI EXPLANATION
    // =========================================================

    private String generateAIExplanation(
            String location,
            int days,
            double budget,
            String mood,
            String preferences,
            List<String> route) {

        if (geminiApiKey == null
                || geminiApiKey.isBlank()) {

            return createFallbackAIExplanation(
                    location,
                    days,
                    budget,
                    mood,
                    preferences
            );
        }

        String routeText =
                String.join(
                        " → ",
                        route
                );

        String prompt =
                """
                You are Sahayatri, an intelligent AI travel planner.

                Create a short and friendly travel insight for this trip.

                Starting location: %s
                Trip duration: %d days
                Budget: ₹%.0f
                Mood: %s
                Preferences: %s
                Planned route: %s

                Explain:
                1. Why this trip matches the user's mood.
                2. What makes the route useful.
                3. One practical travel suggestion.

                Keep the response between 80 and 120 words.
                Do not invent places that are not in the route.
                """.formatted(
                        location,
                        days,
                        budget,
                        mood,
                        preferences.isBlank()
                                ? "Not specified"
                                : preferences,
                        routeText
                );

        String aiResponse =
                callGemini(prompt);

        if (aiResponse == null
                || aiResponse.isBlank()) {

            return createFallbackAIExplanation(
                    location,
                    days,
                    budget,
                    mood,
                    preferences
            );
        }

        return aiResponse;
    }

    // =========================================================
    // GEMINI AI TIPS
    // =========================================================

    private String generateAITips(
            String location,
            String mood,
            int days,
            double budget) {

        if (geminiApiKey == null
                || geminiApiKey.isBlank()) {

            return "Start early, keep some budget for unexpected expenses, "
                    + "and check local conditions before travelling.";
        }

        String prompt =
                """
                Give 4 concise practical travel tips for a %s trip
                starting from %s.

                Duration: %d days
                Budget: ₹%.0f

                Return only 4 bullet points.
                Keep each bullet short.
                Do not invent specific facts.
                """.formatted(
                        mood,
                        location,
                        days,
                        budget
                );

        String result =
                callGemini(prompt);

        if (result == null
                || result.isBlank()) {

            return "• Start early\n"
                    + "• Keep emergency funds\n"
                    + "• Carry essentials\n"
                    + "• Check local conditions";
        }

        return result;
    }

    // =========================================================
    // GEMINI API CALL
    // =========================================================

    private String callGemini(
            String prompt) {

        try {

            String url =
                    "https://generativelanguage.googleapis.com/"
                            + "v1beta/models/"
                            + GEMINI_MODEL
                            + ":generateContent";

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_JSON
            );

            headers.set(
                    "x-goog-api-key",
                    geminiApiKey
            );

            Map<String, Object> part =
                    new LinkedHashMap<>();

            part.put(
                    "text",
                    prompt
            );

            Map<String, Object> content =
                    new LinkedHashMap<>();

            content.put(
                    "parts",
                    List.of(part)
            );

            Map<String, Object> body =
                    new LinkedHashMap<>();

            body.put(
                    "contents",
                    List.of(content)
            );

            Map<String, Object> generationConfig =
                    new LinkedHashMap<>();

            generationConfig.put(
                    "temperature",
                    0.7
            );

            generationConfig.put(
                    "maxOutputTokens",
                    500
            );

            body.put(
                    "generationConfig",
                    generationConfig
            );

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(
                            body,
                            headers
                    );

            ResponseEntity<Map> result =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            request,
                            Map.class
                    );

            if (result.getBody() == null) {
                return null;
            }

            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>)
                            result.getBody().get(
                                    "candidates"
                            );

            if (candidates == null
                    || candidates.isEmpty()) {

                return null;
            }

            Map<String, Object> candidate =
                    candidates.get(0);

            Map<String, Object> contentResponse =
                    (Map<String, Object>)
                            candidate.get(
                                    "content"
                            );

            if (contentResponse == null) {
                return null;
            }

            List<Map<String, Object>> parts =
                    (List<Map<String, Object>>)
                            contentResponse.get(
                                    "parts"
                            );

            if (parts == null
                    || parts.isEmpty()) {

                return null;
            }

            Object text =
                    parts.get(0).get("text");

            return text == null
                    ? null
                    : text.toString().trim();

        } catch (Exception e) {

            System.out.println(
                    "Gemini API error: "
                            + e.getMessage()
            );

            return null;
        }
    }

    // =========================================================
    // FALLBACK AI EXPLANATION
    // =========================================================

    private String createFallbackAIExplanation(
            String location,
            int days,
            double budget,
            String mood,
            String preferences) {

        String prefText =
                preferences == null
                        || preferences.isBlank()
                        ? "your travel preferences"
                        : preferences;

        return "Sahayatri created a "
                + days
                + "-day "
                + mood
                + " trip starting from "
                + location
                + " with a budget of ₹"
                + (int) budget
                + ". The plan is tailored around "
                + prefText
                + ". The itinerary combines real nearby places with "
                + "a practical day-wise route. Keep some budget aside "
                + "for unexpected travel expenses and check local "
                + "conditions before starting each day.";
    }

    // =========================================================
    // GET PLACE NAME
    // =========================================================

    private String getPlaceName(
            Map<String, Object> place) {

        Map<String, Object> displayName =
                (Map<String, Object>)
                        place.get("displayName");

        if (displayName == null) {
            return "Tourist Place";
        }

        Object text =
                displayName.get("text");

        return text == null
                ? "Tourist Place"
                : text.toString();
    }

    // =========================================================
    // URL ENCODE
    // =========================================================

    private String encode(
            String value) {

        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }
}