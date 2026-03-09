package com.TroisN.Service.entity;

import com.TroisN.Service.enums.DemandeStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.UUID;
import java.time.Year;



@Getter
@Setter
@Entity
@Table(name = "demandes")
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer totalEmployeesNeeded;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientCompany client;

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DemandeProfil> profils = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;


    @OneToMany(
            mappedBy = "demande",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Offer> offers = new ArrayList<>();

    @Column(name = "reference", nullable = false, unique = true, updatable = false)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DemandeStatus status;

    @OneToMany(mappedBy = "demande", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Contract> contracts = new ArrayList<>();



    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.reference == null) {
            this.reference = generateReference();
        }

        if (this.status == null) {
            this.status = DemandeStatus.IN_PROGRESS;
        }
    }


    private String generateReference() {
        return "DEM-" + Year.now().getValue() + "-" +
                UUID.randomUUID().toString().substring(0, 8);
    }

}
