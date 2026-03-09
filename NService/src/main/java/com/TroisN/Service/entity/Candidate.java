package com.TroisN.Service.entity;

import com.TroisN.Service.enums.CanidateStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "candidates")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private String phoneNumber;

    @Email
    @Column(unique = true)
    private String emailAddress;

    @Min(0)
    private Integer experienceYear;

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime suspendedUntil;

    private String skills;

    private String professional;

    private String cin;

    private String cssNumber;

    private String cvPath;

    @Enumerated(EnumType.STRING)
    private CanidateStatus status;

    @Column(name = "next_available_date")
    private LocalDate nextAvailableDate;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimesheetMonth> timesheetMonths = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (status == null) {
            status = CanidateStatus.AVAILABLE;
        }

        if (status == CanidateStatus.AVAILABLE && nextAvailableDate == null) {
            nextAvailableDate = LocalDate.now();
        }
    }

    public boolean isTemporarilySuspended() {
        return suspendedUntil != null && suspendedUntil.isAfter(LocalDateTime.now());
    }
}