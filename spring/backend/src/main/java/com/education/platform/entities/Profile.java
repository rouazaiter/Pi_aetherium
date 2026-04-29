package com.education.platform.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "FirstName", length = 100)
    private String firstName;

    @Column(name = "LastName", length = 100)
    private String lastName;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "Intrests", joinColumns = @JoinColumn(name = "Profile_id"))
    @Column(name = "Intrest", length = 200)
    @Builder.Default
    private List<String> interests = new ArrayList<>();

    @Column(name = "Description", length = 2000)
    private String description;

    @Column(name = "profilepicture", length = 500)
    private String profilePicture;

    @Column(name = "LastPasswordchanged")
    private Instant lastPasswordChanged;

    @Column(name = "recuperationEmail", length = 255)
    private String recuperationEmail;

    @Column(name = "twoFactorEnabled", nullable = false)
    @Builder.Default
    private boolean twoFactorEnabled = false;

    @Column(name = "activeStatusVisible", nullable = false)
    @Builder.Default
    private boolean activeStatusVisible = true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "User_id", nullable = false, unique = true)
    private User user;
}
