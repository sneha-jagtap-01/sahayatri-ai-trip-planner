package com.sahayatri.sahayatribackend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/reel")
@CrossOrigin(origins = "*")
public class ReelController {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeReel(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "reelUrl", required = false) String reelUrl) {

        try {

            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Please upload a Reel screenshot."));
            }

            // Convert image to Base64
            String base64Image = Base64.getEncoder()
                    .encodeToString(image.getBytes());

            // Gemini API URL
            String url =
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent";

            // Prompt for AI
            String prompt = """
                    You are an AI travel location detector.
                    
                    Analyze this Instagram Reel screenshot/image carefully.
                    
                    Try to identify the tourist location shown in the image.
                    
                    Look at:
                    - Buildings
                    - Monuments
                    - Mountains
                    - Temples
                    - Roads
                    - Signs
                    - Text
                    - Natural landmarks
                    - Famous tourist attractions
                    
                    Return ONLY valid JSON in this exact format:
                    
                    {
                      "location": "location name",
                      "confidence": 85,
                      "reason": "short explanation"
                    }
                    
                    If you cannot identify the location, return:
                    
                    {
                      "location": "Unknown",
                      "confidence": 0,
                      "reason": "Location could not be identified from the image."
                    }
                    
                    Do not add markdown or any text outside JSON.
                    """;

            // Image part
            Map<String, Object> inlineData = new HashMap<>();
            inlineData.put("mimeType", image.getContentType());
            inlineData.put("data", base64Image);

            Map<String, Object> imagePart = new HashMap<>();
            imagePart.put("inlineData", inlineData);

            // Text part
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);

            // Parts
            List<Map<String, Object>> parts = new ArrayList<>();
            parts.add(textPart);
            parts.add(imagePart);

            // Content
            Map<String, Object> content = new HashMap<>();
            content.put("parts", parts);

            // Request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(content));

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiApiKey);

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(requestBody, headers);

            // Call Gemini
            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            request,
                            Map.class
                    );

            // Extract Gemini response
            Map responseBody = response.getBody();

            if (responseBody == null) {
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", "Empty response from Gemini."));
            }

            List candidates = (List) responseBody.get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", "Gemini could not analyze the image."));
            }

            Map candidate = (Map) candidates.get(0);
            Map contentResponse = (Map) candidate.get("content");
            List responseParts = (List) contentResponse.get("parts");

            Map firstPart = (Map) responseParts.get(0);

            String aiText = firstPart.get("text").toString();

            // Remove markdown if Gemini adds ```json
            aiText = aiText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            // Basic JSON parsing
            String location = extractJsonValue(aiText, "location");
            String confidence = extractJsonValue(aiText, "confidence");
            String reason = extractJsonValue(aiText, "reason");

            Map<String, Object> result = new HashMap<>();

            result.put("location", location);
            result.put("confidence", confidence);
            result.put("reason", reason);

            if (reelUrl != null && !reelUrl.isBlank()) {
                result.put("reelUrl", reelUrl);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error",
                            "AI analysis failed: " + e.getMessage()
                    ));
        }
    }

    private String extractJsonValue(String json, String key) {

        String search = "\"" + key + "\"";

        int keyIndex = json.indexOf(search);

        if (keyIndex == -1) {
            return "Unknown";
        }

        int colonIndex = json.indexOf(":", keyIndex);

        if (colonIndex == -1) {
            return "Unknown";
        }

        int start = colonIndex + 1;

        while (start < json.length()
                && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        // String value: location and reason
        if (start < json.length() && json.charAt(start) == '"') {

            int endQuote = json.indexOf("\"", start + 1);

            if (endQuote != -1) {
                return json.substring(start + 1, endQuote);
            }
        }

        // Numeric value: confidence
        int end = start;

        while (end < json.length()
                && (Character.isDigit(json.charAt(end))
                || json.charAt(end) == '.')) {
            end++;
        }

        if (start < end) {
            return json.substring(start, end);
        }

        return "Unknown";
    }
}