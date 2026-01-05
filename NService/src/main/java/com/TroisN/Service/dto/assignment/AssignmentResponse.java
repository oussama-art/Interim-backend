package com.TroisN.Service.dto.assignment;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AssignmentResponse {

    private String candidateName;
    private String candidateStatus;

    private String demandeTitle;

    private String clientName;


    private LocalDate startDate;
    private LocalDate endDate;
}
