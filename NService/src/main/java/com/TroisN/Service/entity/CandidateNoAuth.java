package com.TroisN.Service.entity;

import com.TroisN.Service.enums.CanidateStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "candidates_no_auth")
public class CandidateNoAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String emailAddress;

    private Integer experienceYear;

    private String skills;

    private String professional;

    private String cin;

    private String cssNumber;

    private String cvPath;

    @Enumerated(EnumType.STRING)
    private CanidateStatus status;

    private LocalDate nextAvailableDate;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = CanidateStatus.AVAILABLE;
        }

        if (nextAvailableDate == null) {
            nextAvailableDate = LocalDate.now();
        }
    }
}