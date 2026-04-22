package tn.esprit.models.dto;

import java.time.LocalDateTime;

public class RoomDTO {
    private Long id;
    private String name;
    private Long hostUserId;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String agoraChannelName;
    private String agoraToken;

    public RoomDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getHostUserId() {
        return hostUserId;
    }

    public void setHostUserId(Long hostUserId) {
        this.hostUserId = hostUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getAgoraChannelName() {
        return agoraChannelName;
    }

    public void setAgoraChannelName(String agoraChannelName) {
        this.agoraChannelName = agoraChannelName;
    }

    public String getAgoraToken() {
        return agoraToken;
    }

    public void setAgoraToken(String agoraToken) {
        this.agoraToken = agoraToken;
    }
}