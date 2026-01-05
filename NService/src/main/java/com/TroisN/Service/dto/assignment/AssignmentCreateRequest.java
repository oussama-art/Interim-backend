package com.TroisN.Service.dto.assignment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AssignmentCreateRequest {

    @NotNull
    private Long candidateId;

    @NotNull
    private Long demandeId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}
