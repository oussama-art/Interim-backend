package com.TroisN.Service.dto.contract;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ContractCreateRequest(
        @NotNull Long demandeId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {}