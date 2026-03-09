package com.TroisN.Service.dto.contract;

import java.time.LocalDate;

public record CandidateAvailabilityResponse(
        Long candidateId,
        LocalDate availableAfter // null si aucun contrat
) {}