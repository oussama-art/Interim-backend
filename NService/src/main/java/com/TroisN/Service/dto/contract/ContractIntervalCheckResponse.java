package com.TroisN.Service.dto.contract;

import java.time.LocalDate;

public record ContractIntervalCheckResponse(
        Long candidateId,
        LocalDate startDate,
        LocalDate endDate,
        boolean overlaps,
        LocalDate availableAfter,
        String message
) {}