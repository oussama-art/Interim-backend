package com.TroisN.Service.dto.candidateNoAuth;

public record CandidateNoAuthResponse(
        Long id,
        String firstName,
        String lastName,
        String professional,
        String cvPath
) {}