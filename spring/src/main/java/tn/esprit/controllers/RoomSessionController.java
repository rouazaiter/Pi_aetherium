package tn.esprit.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.models.*;
import tn.esprit.services.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/rooms")
public class RoomSessionController {

    private final RoomSessionService roomSessionService;
    private final AgoraTokenService agoraTokenService;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${app.upload.directory:./uploads/recordings}")
    private String uploadDirectory;

    public RoomSessionController(
            RoomSessionService roomSessionService,
            AgoraTokenService agoraTokenService,
            SimpMessagingTemplate messagingTemplate) {
        this.roomSessionService = roomSessionService;
        this.agoraTokenService = agoraTokenService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        Long hostUserId = Long.parseLong(request.get("hostUserId"));

        RoomSession room = roomSessionService.createRoom(name, hostUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("id", room.getId());
        response.put("name", room.getName());
        response.put("hostUserId", room.getHostUserId());
        response.put("status", room.getStatus().name());
        response.put("startTime", room.getStartTime());
        response.put("agoraChannelName", room.getAgoraChannelName());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRoom(@PathVariable Long id) {
        return roomSessionService.getRoom(id).map(room -> {
            Map<String, Object> response = new HashMap<>();
            response.put("id", room.getId());
            response.put("name", room.getName());
            response.put("hostUserId", room.getHostUserId());
            response.put("status", room.getStatus().name());
            response.put("startTime", room.getStartTime());
            response.put("endTime", room.getEndTime());
            response.put("agoraChannelName", room.getAgoraChannelName());
            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveRooms() {
        List<RoomSession> rooms = roomSessionService.getActiveRooms();
        List<Map<String, Object>> response = new ArrayList<>();

        for (RoomSession room : rooms) {
            Map<String, Object> roomMap = new HashMap<>();
            roomMap.put("id", room.getId());
            roomMap.put("name", room.getName());
            roomMap.put("hostUserId", room.getHostUserId());
            roomMap.put("status", room.getStatus().name());
            roomMap.put("startTime", room.getStartTime());
            roomMap.put("participantCount", roomSessionService.getActiveParticipants(room.getId()).size());
            response.add(roomMap);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinRoom(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Long userId = Long.parseLong(request.get("userId"));
        String userName = request.get("userName");

        try {
            SessionParticipant participant = roomSessionService.joinRoom(id, userId, userName);

            Map<String, Object> response = new HashMap<>();
            response.put("id", participant.getId());
            response.put("userId", participant.getUserId());
            response.put("userName", participant.getUserName());
            response.put("role", participant.getRole().name());
            response.put("joinedAt", participant.getJoinedAt());

            messagingTemplate.convertAndSend("/topic/room/" + id, Map.of(
                    "type", "USER_JOINED",
                    "userId", userId,
                    "userName", userName
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<?> leaveRoom(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Long userId = Long.parseLong(request.get("userId"));

        try {
            roomSessionService.leaveRoom(id, userId);

            messagingTemplate.convertAndSend("/topic/room/" + id, Map.of(
                    "type", "USER_LEFT",
                    "userId", userId
            ));

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<?> endRoom(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Long userId = Long.parseLong(request.get("userId"));

        try {
            RoomSession room = roomSessionService.endRoom(id, userId);

            messagingTemplate.convertAndSend("/topic/room/" + id, Map.of(
                    "type", "ROOM_ENDED",
                    "roomId", id
            ));

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "status", room.getStatus().name()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<?> getParticipants(@PathVariable Long id) {
        List<SessionParticipant> participants = roomSessionService.getParticipants(id);
        List<Map<String, Object>> response = new ArrayList<>();

        for (SessionParticipant p : participants) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("userId", p.getUserId());
            map.put("userName", p.getUserName());
            map.put("role", p.getRole().name());
            map.put("joinedAt", p.getJoinedAt());
            map.put("leftAt", p.getLeftAt());
            response.add(map);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<?> getMessages(@PathVariable Long id) {
        List<ChatMessage> messages = roomSessionService.getMessages(id);
        List<Map<String, Object>> response = new ArrayList<>();

        for (ChatMessage msg : messages) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", msg.getId());
            map.put("senderId", msg.getSenderId());
            map.put("senderName", msg.getSenderName());
            map.put("content", msg.getContent());
            map.put("timestamp", msg.getTimestamp());
            response.add(map);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<?> sendMessage(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Long senderId = Long.parseLong(request.get("senderId"));
        String senderName = request.get("senderName");
        String content = request.get("content");

        try {
            ChatMessage message = roomSessionService.addMessage(id, senderId, senderName, content);

            messagingTemplate.convertAndSend("/topic/room/" + id + "/chat", Map.of(
                    "id", message.getId(),
                    "senderId", senderId,
                    "senderName", senderName,
                    "content", content,
                    "timestamp", message.getTimestamp()
            ));

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/token")
    public ResponseEntity<?> getAgoraToken(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Long userId = Long.parseLong(request.get("userId"));

        return roomSessionService.getRoom(id).map(room -> {
            Map<String, String> tokenData = agoraTokenService.generateTokenForRoom(
                    room.getAgoraChannelName(), userId);
            return ResponseEntity.ok(tokenData);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/recordings")
    public ResponseEntity<?> uploadRecording(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {

        try {
            Path uploadPath = Paths.get(uploadDirectory, id.toString());
            Files.createDirectories(uploadPath);

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, file.getBytes());

            SessionRecording.RecordingType recordingType = SessionRecording.RecordingType.valueOf(type.toUpperCase());
            SessionRecording recording = roomSessionService.addRecording(
                    id,
                    file.getOriginalFilename(),
                    filePath.toString(),
                    file.getSize(),
                    file.getContentType(),
                    recordingType
            );

            return ResponseEntity.ok(Map.of(
                    "id", recording.getId(),
                    "fileName", recording.getFileName(),
                    "filePath", recording.getFilePath()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/recordings")
    public ResponseEntity<?> getRecordings(@PathVariable Long id) {
        List<SessionRecording> recordings = roomSessionService.getRecordings(id);
        List<Map<String, Object>> response = new ArrayList<>();

        for (SessionRecording r : recordings) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("fileName", r.getFileName());
            map.put("filePath", r.getFilePath());
            map.put("fileSize", r.getFileSize());
            map.put("contentType", r.getContentType());
            map.put("type", r.getType().name());
            map.put("durationSeconds", r.getDurationSeconds());
            map.put("createdAt", r.getCreatedAt());
            response.add(map);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/recordings/{recordingId}/download")
    public ResponseEntity<Resource> downloadRecording(@PathVariable Long recordingId) {

        var recordingOpt = roomSessionService.getRecording(recordingId);

        if (recordingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var recording = recordingOpt.get();

        try {
            Path filePath = Paths.get(recording.getFilePath());

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(
                            recording.getContentType() != null
                                    ? recording.getContentType()
                                    : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + recording.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}