package com.TroisN.Service.dto.contract;

import com.TroisN.Service.dto.offer.OfferCandidateResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ContractResponse(
        Long id,
        OfferCandidateResponse candidate,
        Long demandeId,
        String demandeReference,
        LocalDate startDate,
        LocalDate endDate,
        String originalFileName,
        LocalDateTime uploadedAt
) {}