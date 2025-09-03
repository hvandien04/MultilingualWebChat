package com.example.identifyService.entity;

import com.example.identifyService.validator.PasswordConstraint;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    @PasswordConstraint(message = "INVALID_PASSWORD", min = 8)
    private String password;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "created_at", updatable = false, insertable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false)
    private Instant updatedAt;

    @Column(name = "role", nullable = false)
    private String role ;

    @Size(max = 255)
    @NotNull
    @Email(message = "INVALID_EMAIL")
    @Column(name = "email", nullable = false)
    private String email;

    @Size(max = 255)
    @Column(name = "google_id")
    private String googleId;

    @Lob
    @Column(name = "avatar_url")
    private String avatarUrl;

    @Size(max = 10)
    @Column(name = "locale", length = 10)
    @ColumnDefault("'US'")
    private String locale;

    @Column(name = "is_active")
    private Boolean isActive;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.isActive = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }


}