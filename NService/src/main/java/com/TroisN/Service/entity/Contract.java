package com.TroisN.Service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.List;
import java.util.ArrayList;
@Getter
@Setter
@Entity
@Table(
        name = "contracts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_contract_candidate_demande",
                        columnNames = {"candidate_id", "demande_id"}
                )
        }
)
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_id", nullable = false)
    private Demande demande;

    // 🔥 AJOUT IMPORTANT
    @OneToMany(
            mappedBy = "contract",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    private List<TimesheetDay> timesheetDays = new ArrayList<>();

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, length = 500)
    private String filePath;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    public void prePersist() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }
}