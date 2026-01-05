package com.TroisN.Service.dto.offer;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OfferResponse {

    private Long offerId;
    private Long demandeId;
    private Long clientId;
    private LocalDateTime createdAt;

    private List<OfferCandidateResponse> proposedCandidates;
}
