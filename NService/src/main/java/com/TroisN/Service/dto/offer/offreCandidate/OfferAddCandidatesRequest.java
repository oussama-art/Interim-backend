package com.TroisN.Service.dto.offer.offreCandidate;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OfferAddCandidatesRequest {

    @NotNull
    private Long demandeProfilId;

    @NotEmpty
    private List<Long> candidateIds;
}