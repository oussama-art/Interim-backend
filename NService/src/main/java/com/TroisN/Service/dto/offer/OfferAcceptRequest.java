package com.TroisN.Service.dto.offer;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OfferAcceptRequest {

    @NotNull
    private Long candidateId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}
