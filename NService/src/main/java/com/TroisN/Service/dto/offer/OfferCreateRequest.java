package com.TroisN.Service.dto.offer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OfferCreateRequest {

    @NotNull
    private Long demandeId;

    @NotEmpty
    private List<Long> candidateIds;
}
