package com.education.platform.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String path; // chemin physique sur disque

    private long size;

    private String type;

    private long duration;

    private String thumbnailPath; // chemin vers la miniature JPG générée par FFmpeg

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private PrivateDrive drive;
}