package com.TroisN.Service.dto.offer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OfferCreateRequest {

    @NotNull
    private Long demandeId;


    @NotEmpty
    private Map<Long, List<Long>> profilsCandidates;
}
