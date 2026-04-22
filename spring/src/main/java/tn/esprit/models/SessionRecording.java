package tn.esprit.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "session_recordings")
public class SessionRecording {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private RoomSession session;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RecordingType type;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public enum RecordingType {
        VIDEO, WHITEBOARD, IDE
    }

    public SessionRecording() {
    }

    public SessionRecording(RoomSession session, String fileName, String filePath, RecordingType type) {
        this.session = session;
        this.fileName = fileName;
        this.filePath = filePath;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoomSession getSession() {
        return session;
    }

    public void setSession(RoomSession session) {
        this.session = session;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public RecordingType getType() {
        return type;
    }

    public void setType(RecordingType type) {
        this.type = type;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}