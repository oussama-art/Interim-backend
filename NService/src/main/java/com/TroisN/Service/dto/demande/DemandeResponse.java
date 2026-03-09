package com.TroisN.Service.dto.demande;

import com.TroisN.Service.enums.DemandeStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DemandeResponse {

    private Long id;


    private String reference;

    private String title;
    private String description;
    private Integer totalEmployeesNeeded;

    private Long clientId;

    private LocalDate startDate;
    private LocalDate endDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private DemandeStatus status;


    private List<DemandeProfilResponse> profils;
}
