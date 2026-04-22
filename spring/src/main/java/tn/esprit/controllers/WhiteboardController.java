package tn.esprit.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/whiteboard")
public class WhiteboardController {
    private static final String NETLESS_API = "https://api.netless.link";

    @Value("${whiteboard.ak:}")
    private String whiteboardAk;

    @Value("${whiteboard.app-identifier:${whiteboard.ak:}}")
    private String whiteboardAppIdentifier;

    @Value("${whiteboard.sk:}")
    private String whiteboardSk;

    @Value("${whiteboard.sdk-token:}")
    private String whiteboardSdkToken;

    @Value("${whiteboard.region:global}")
    private String whiteboardRegion;

    private final RestTemplate restTemplate = new RestTemplate();

    // In-memory room mapping: appRoomId -> whiteboardUuid
    private final Map<Long, String> roomMapping = new HashMap<>();

    @GetMapping("/config")
    public ResponseEntity<?> getConfig() {
        Optional<ResponseEntity<Map<String, String>>> configError = validateWhiteboardConfig();
        if (configError.isPresent()) {
            return configError.get();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("appIdentifier", whiteboardAppIdentifier);
        result.put("region", whiteboardRegion);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/create-room")
    public ResponseEntity<?> createRoom(@RequestBody Map<String, String> request) {
        Optional<ResponseEntity<Map<String, String>>> configError = validateWhiteboardConfig();
        if (configError.isPresent()) {
            return configError.get();
        }

        try {
            String sdkToken = generateSdkToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("token", sdkToken);
            headers.set("region", whiteboardRegion);

            Map<String, Object> body = new HashMap<>();
            body.put("isRecord", false);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    NETLESS_API + "/v5/rooms",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> roomData = response.getBody();
                if (roomData == null) {
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                            .body(Map.of("error", "Empty response from whiteboard service"));
                }

                String uuid = (String) roomData.get("uuid");
                if (uuid == null || uuid.isBlank()) {
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                            .body(Map.of("error", "Whiteboard service did not return room uuid"));
                }

                Map<String, Object> result = new HashMap<>();
                result.put("uuid", uuid);
                result.put("room", roomData);

                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Failed to create whiteboard room"));
            }

        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(Map.of("error", "Whiteboard provider rejected credentials (401). Verify whiteboard.app-identifier and either whiteboard.sdk-token or whiteboard.ak/whiteboard.sk."));
            }
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Whiteboard provider error: " + e.getStatusCode() + " " + e.getResponseBodyAsString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/room-token")
    public ResponseEntity<?> getRoomToken(@RequestBody Map<String, Object> request) {
        Optional<ResponseEntity<Map<String, String>>> configError = validateWhiteboardConfig();
        if (configError.isPresent()) {
            return configError.get();
        }

        String uuid = (String) request.get("uuid");
        String role = (String) request.getOrDefault("role", "writer");
        long lifespan = request.containsKey("lifespan")
                ? ((Number) request.get("lifespan")).longValue() 
                : 3600000L;

        if (uuid == null || uuid.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "uuid is required"));
        }

        try {
            String sdkToken = generateSdkToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("token", sdkToken);
            headers.set("region", whiteboardRegion);

            Map<String, Object> body = new HashMap<>();
            body.put("lifespan", lifespan);
            body.put("role", role);

            String tokenUrl = UriComponentsBuilder
                    .fromHttpUrl(NETLESS_API + "/v5/tokens/rooms/{uuid}")
                    .buildAndExpand(uuid)
                    .toUriString();

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> tokenResponse = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(Map.of("error", "Failed to generate room token from whiteboard provider"));
            }

            String roomToken = normalizeRoomToken(tokenResponse.getBody());
            if (roomToken == null || roomToken.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(Map.of("error", "Whiteboard provider returned empty room token"));
            }

            long expireAt = Instant.now().plusMillis(lifespan).toEpochMilli();

            Map<String, Object> result = new HashMap<>();
            result.put("roomToken", roomToken);
            result.put("expireAt", expireAt);

            return ResponseEntity.ok(result);

        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(Map.of("error", "Whiteboard provider rejected credentials (401) while creating room token."));
            }
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Whiteboard provider token API error: " + e.getStatusCode() + " " + e.getResponseBodyAsString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/refresh-room-token")
    public ResponseEntity<?> refreshRoomToken(@RequestBody Map<String, Object> request) {
        String uuid = (String) request.get("uuid");
        if (uuid == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "uuid is required"));
        }
        return getRoomToken(Map.of("uuid", uuid, "role", "writer", "lifespan", 3600000L));
    }

    @GetMapping("/map/{appRoomId}")
    public ResponseEntity<?> getMappedRoom(@PathVariable Long appRoomId) {
        String wbUuid = roomMapping.get(appRoomId);
        if (wbUuid != null) {
            return ResponseEntity.ok(Map.of("uuid", wbUuid));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/map/{appRoomId}")
    public ResponseEntity<?> mapRoom(@PathVariable Long appRoomId, @RequestBody Map<String, String> request) {
        String wbUuid = request.get("uuid");
        if (wbUuid != null) {
            roomMapping.put(appRoomId, wbUuid);
            return ResponseEntity.ok(Map.of("success", true, "uuid", wbUuid));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "uuid is required"));
    }

    // Generate SDK token using AK/SK for Whiteboard API calls
    private String generateSdkToken() {
        if (whiteboardSdkToken != null && !whiteboardSdkToken.isBlank()) {
            return whiteboardSdkToken;
        }

        long timestamp = Instant.now().toEpochMilli() / 1000;
        String payload = whiteboardAk + timestamp;
        
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(whiteboardSk.getBytes(), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes());
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            
            return whiteboardAk + "_" + timestamp + "_" + sb;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate SDK token", e);
        }
    }

    private String normalizeRoomToken(String rawBody) {
        String trimmed = rawBody == null ? "" : rawBody.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private Optional<ResponseEntity<Map<String, String>>> validateWhiteboardConfig() {
        if (whiteboardAppIdentifier == null || whiteboardAppIdentifier.isBlank()) {
            return Optional.of(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Whiteboard app identifier is not configured (whiteboard.app-identifier)")));
        }

        boolean hasSdkToken = whiteboardSdkToken != null && !whiteboardSdkToken.isBlank();
        boolean hasAkSk = whiteboardAk != null && !whiteboardAk.isBlank() && whiteboardSk != null && !whiteboardSk.isBlank();
        if (!hasSdkToken && !hasAkSk) {
            return Optional.of(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Whiteboard credentials are not configured. Set whiteboard.sdk-token or whiteboard.ak + whiteboard.sk.")));
        }

        return Optional.empty();
    }
}