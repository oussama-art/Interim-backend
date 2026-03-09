package com.TroisN.Service.dto.assignment;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AssignmentResponse {

    private Long demandeId;

    private Long candidateId;
    private String candidateName;
    private String candidateStatus;

    private String demandeTitle;

    private String clientName;


    private LocalDate startDate;
    private LocalDate endDate;
}
