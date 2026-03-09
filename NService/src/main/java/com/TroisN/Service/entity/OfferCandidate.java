package com.TroisN.Service.entity;

import com.TroisN.Service.enums.OfferCandidateStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "offer_candidates",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"offer_id", "candidate_id"}
        ))
public class OfferCandidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;



    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_profil_id", nullable = false)
    private DemandeProfil demandeProfil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferCandidateStatus status;

    @Column(nullable = false)
    private LocalDateTime proposedAt;

    @PrePersist
    protected void onCreate() {
        this.proposedAt = LocalDateTime.now();
        this.status = OfferCandidateStatus.PROPOSED;
    }

}
