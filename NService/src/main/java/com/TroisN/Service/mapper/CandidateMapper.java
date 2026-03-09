package com.TroisN.Service.mapper;

import com.TroisN.Service.dto.candidate.CandidateResponse;
import com.TroisN.Service.entity.Candidate;

public class CandidateMapper {

    private CandidateMapper() {}

    public static CandidateResponse toResponseDTO(Candidate candidate) {

        if (candidate == null) {
            return null;
        }

        return new CandidateResponse(
                candidate.getId(),
                candidate.getFirstName(),
                candidate.getLastName(),
                candidate.getCreatedAt(),
                candidate.getPhoneNumber(),
                candidate.getEmailAddress(),
                candidate.getExperienceYear(),
                candidate.isActive(),
                candidate.getSuspendedUntil(),
                candidate.getSkills(),
                candidate.getProfessional(),
                candidate.getCin(),
                candidate.getCssNumber(),
                candidate.getCvPath(),
                candidate.getStatus(),
                candidate.getNextAvailableDate()
        );
    }
}