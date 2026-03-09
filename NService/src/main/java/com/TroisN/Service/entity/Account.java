package com.TroisN.Service.entity;

import com.TroisN.Service.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "account_creation_requests")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(nullable = false)
    private String emailAddress;

    private String phoneNumber;
    private Integer experienceYear;


    private String companyTitle;

    @Column(length = 2000)
    private String companyDescription;

    private String sector;
    private Integer nbEmployee;


    private Integer requestedAccounts;


    @ElementCollection
    @CollectionTable(
            name = "account_creation_request_emails",
            joinColumns = @JoinColumn(name = "account_request_id")
    )
    private List<AccountEmail> emails = new ArrayList<>();




    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;


    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime validatedAt;
    private LocalDateTime rejectedAt;
    private String rejectionReason;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = RequestStatus.PENDING;
    }
}
