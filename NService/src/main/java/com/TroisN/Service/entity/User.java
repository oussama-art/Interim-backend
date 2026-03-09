package com.TroisN.Service.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@MappedSuperclass
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(name = "keycloak_user_id", nullable = false, unique = true, length = 36)
    protected String keycloakUserId;

    protected String firstName;
    protected String lastName;

    @Column(nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    protected String phoneNumber;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    protected String emailAddress;

    @Min(0)
    protected Integer experienceYear;



    @Column(nullable = false)
    protected boolean active = true;

    protected LocalDateTime suspendedUntil;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public boolean isTemporarilySuspended() {
        return suspendedUntil != null &&
                suspendedUntil.isAfter(LocalDateTime.now());
    }
}
