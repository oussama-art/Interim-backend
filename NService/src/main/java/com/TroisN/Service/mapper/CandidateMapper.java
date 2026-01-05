package com.TroisN.Service.mapper;

import com.TroisN.Service.dto.candidate.CandidateResponse;
import com.TroisN.Service.entity.Candidate;

public class CandidateMapper {

    public static CandidateResponse toResponseDTO(Candidate candidate) {

        if (candidate == null) return null;

        CandidateResponse dto = new CandidateResponse();

        dto.setId(candidate.getId());
        dto.setFirstName(candidate.getFirstName());
        dto.setLastName(candidate.getLastName());
        dto.setPhoneNumber(candidate.getPhoneNumber());
        dto.setEmailAddress(candidate.getEmailAddress());
        dto.setExperienceYear(candidate.getExperienceYear());

        dto.setSkills(candidate.getSkills());
        dto.setProfessional(candidate.getProfessional());
        dto.setCin(candidate.getCin());
        dto.setCssNumber(candidate.getCssNumber());

        dto.setCvPath(candidate.getCvPath());

        return dto;
    }
}
