package com.TroisN.Service.dto.candidate;

import com.TroisN.Service.enums.CanidateStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CandidateResponse(
        Long id,
        String firstName,
        String lastName,
        LocalDateTime createdAt,
        String phoneNumber,
        String emailAddress,
        Integer experienceYear,
        boolean active,
        LocalDateTime suspendedUntil,
        String skills,
        String professional,
        String cin,
        String cssNumber,
        String cvPath,
        CanidateStatus status,
        LocalDate nextAvailableDate
) {
}