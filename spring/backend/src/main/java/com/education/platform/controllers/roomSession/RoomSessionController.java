package com.education.platform.controllers.roomSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.education.platform.entities.roomSession.*;
import com.education.platform.repositories.UserRepository;
import com.education.platform.services.interfaces.IAgoraTokenService;
import com.education.platform.services.interfaces.IRoomSessionService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/rooms")
public class RoomSessionController {

    private final IRoomSessionService roomSessionService;
    private final IAgoraTokenService agoraTokenService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @Value("${app.upload.directory:./uploads/recordings}")
    private String uploadDirectory;

    public RoomSessionController(
            IRoomSessionService roomSessionService,
            IAgoraTokenService agoraTokenService,
            SimpMessagingTemplate messagingTemplate,
            UserRepository userRepository) {
        this.roomSessionService = roomSessionService;
        this.agoraTokenService = agoraTokenService;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
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
        response.put("hostUserName", resolveHostUserName(room.getHostUserId()));
        response.put("status", room.getStatus().name());
        response.put("startTime", room.getStartTime());
        response.put("agoraChannelName", room.getAgoraChannelName());
        response.put("workspaceAccessBlocked", room.isWorkspaceAccessBlocked());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRoom(@PathVariable Long id) {
        return roomSessionService.getRoom(id).map(room -> {
            Map<String, Object> response = new HashMap<>();
            response.put("id", room.getId());
            response.put("name", room.getName());
            response.put("hostUserId", room.getHostUserId());
            response.put("hostUserName", resolveHostUserName(room.getHostUserId()));
            response.put("status", room.getStatus().name());
            response.put("startTime", room.getStartTime());
            response.put("endTime", room.getEndTime());
            response.put("agoraChannelName", room.getAgoraChannelName());
            response.put("workspaceAccessBlocked", room.isWorkspaceAccessBlocked());
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
            roomMap.put("hostUserName", resolveHostUserName(room.getHostUserId()));
            roomMap.put("status", room.getStatus().name());
            roomMap.put("startTime", room.getStartTime());
            roomMap.put("participantCount", roomSessionService.getActiveParticipants(room.getId()).size());
            roomMap.put("workspaceAccessBlocked", room.isWorkspaceAccessBlocked());
            response.add(roomMap);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/active/by-user/{userId}")
    public ResponseEntity<?> getActiveRoomsByUser(@PathVariable Long userId) {
        List<RoomSession> hostRooms = roomSessionService.getActiveRoomsForUserByRole(userId, SessionParticipant.ParticipantRole.HOST);
        List<RoomSession> participantRooms = roomSessionService.getActiveRoomsForUserByRole(userId, SessionParticipant.ParticipantRole.PARTICIPANT);

        Map<String, Object> response = new HashMap<>();
        response.put("hostRooms", mapRooms(hostRooms));
        response.put("participantRooms", mapRooms(participantRooms));
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

    @PostMapping("/join-by-name")
    public ResponseEntity<?> joinRoomByName(@RequestBody Map<String, String> request) {
        String roomName = Objects.toString(request.get("roomName"), "").trim();
        String userName = Objects.toString(request.get("userName"), "").trim();

        if (roomName.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Room name is required"));
        }

        try {
            Long userId = Long.parseLong(Objects.toString(request.get("userId"), "").trim());
            return roomSessionService.getActiveRoomByName(roomName).map(room -> {
                SessionParticipant participant = roomSessionService.joinRoom(room.getId(), userId, userName);

                messagingTemplate.convertAndSend("/topic/room/" + room.getId(), Map.of(
                        "type", "USER_JOINED",
                        "userId", participant.getUserId(),
                        "userName", participant.getUserName()
                ));

                Map<String, Object> response = new HashMap<>();
                response.put("roomId", room.getId());
                response.put("roomName", room.getName());
                response.put("hostUserId", room.getHostUserId());
                response.put("hostUserName", resolveHostUserName(room.getHostUserId()));
                response.put("workspaceAccessBlocked", room.isWorkspaceAccessBlocked());
                return ResponseEntity.ok(response);
            }).orElse(ResponseEntity.badRequest().body(Map.of("error", "No active room found with this name")));
        } catch (NumberFormatException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid userId"));
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

    @PostMapping("/{id}/workspace-access")
    public ResponseEntity<?> setWorkspaceAccess(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Long userId = parseLongField(request.get("userId"), "userId");
            boolean blocked = Boolean.parseBoolean(Objects.toString(request.get("blocked"), "false"));
            RoomSession room = roomSessionService.setWorkspaceAccessBlocked(id, userId, blocked);
            messagingTemplate.convertAndSend("/topic/room/" + id, Map.of(
                    "type", "ACCESS_LOCK_UPDATED",
                    "roomId", id,
                    "workspaceAccessBlocked", room.isWorkspaceAccessBlocked()
            ));
            return ResponseEntity.ok(Map.of(
                    "roomId", room.getId(),
                    "workspaceAccessBlocked", room.isWorkspaceAccessBlocked()
            ));
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
    public ResponseEntity<?> sendMessage(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Long senderId = parseLongField(request.get("senderId"), "senderId");
            String senderName = Objects.toString(request.get("senderName"), "").trim();
            String content = Objects.toString(request.get("content"), "").trim();

            if (content.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Message content is required"));
            }
            if (senderName.isEmpty()) {
                senderName = "User";
            }

            ChatMessage message = roomSessionService.addMessage(id, senderId, senderName, content);

            messagingTemplate.convertAndSend("/topic/room/" + id + "/chat", Map.of(
                    "id", message.getId(),
                    "senderId", senderId,
                    "senderName", senderName,
                    "content", content,
                    "timestamp", message.getTimestamp()
            ));

            Map<String, Object> response = new HashMap<>();
            response.put("id", message.getId());
            response.put("senderId", message.getSenderId());
            response.put("senderName", message.getSenderName());
            response.put("content", message.getContent());
            response.put("timestamp", message.getTimestamp());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Long parseLongField(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException("Missing required field: " + fieldName);
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String textValue = value.toString().trim();
        if (textValue.isEmpty()) {
            throw new IllegalArgumentException("Missing required field: " + fieldName);
        }
        try {
            return Long.parseLong(textValue);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid numeric field: " + fieldName);
        }
    }

    private String resolveHostUserName(Long hostUserId) {
        if (hostUserId == null) {
            return "Unknown";
        }
        return userRepository.findById(hostUserId)
                .map(user -> {
                    String username = user.getUsername();
                    return (username == null || username.isBlank()) ? "User #" + hostUserId : username;
                })
                .orElse("User #" + hostUserId);
    }

    private List<Map<String, Object>> mapRooms(List<RoomSession> rooms) {
        List<Map<String, Object>> mapped = new ArrayList<>();
        for (RoomSession room : rooms) {
            Map<String, Object> roomMap = new HashMap<>();
            roomMap.put("id", room.getId());
            roomMap.put("name", room.getName());
            roomMap.put("hostUserId", room.getHostUserId());
            roomMap.put("hostUserName", resolveHostUserName(room.getHostUserId()));
            roomMap.put("status", room.getStatus().name());
            roomMap.put("startTime", room.getStartTime());
            roomMap.put("participantCount", roomSessionService.getActiveParticipants(room.getId()).size());
            roomMap.put("workspaceAccessBlocked", room.isWorkspaceAccessBlocked());
            mapped.add(roomMap);
        }
        return mapped;
    }

    @PostMapping("/{id}/token")
    public ResponseEntity<?> getAgoraToken(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Long userId = Long.parseLong(request.get("userId"));

        return roomSessionService.getRoom(id).map(room -> {
            Map<String, String> tokenData = agoraTokenService.generateTokenForRoom(
                    room.getAgoraChannelName(), userId);
            String appId = tokenData.getOrDefault("appId", "").trim();
            String token = tokenData.getOrDefault("token", "").trim();
            if (appId.isEmpty() || token.isEmpty()) {
                return ResponseEntity.internalServerError().body(Map.of(
                        "error", "Agora configuration is invalid. Verify agora.app.id and agora.app.certificate in backend configuration."
                ));
            }
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