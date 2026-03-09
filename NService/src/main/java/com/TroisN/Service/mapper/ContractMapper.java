package com.TroisN.Service.mapper;

import com.TroisN.Service.dto.contract.ContractResponse;
import com.TroisN.Service.dto.offer.OfferCandidateResponse;
import com.TroisN.Service.entity.Contract;
import org.springframework.stereotype.Component;

@Component
public class ContractMapper {

    public ContractResponse toResponse(Contract contract) {

        OfferCandidateResponse candidateDto = new OfferCandidateResponse();

        candidateDto.setCandidateId(contract.getCandidate().getId());
        candidateDto.setFirstName(contract.getCandidate().getFirstName());
        candidateDto.setLastName(contract.getCandidate().getLastName());
        candidateDto.setSkills(contract.getCandidate().getSkills());
        candidateDto.setProfessional(contract.getCandidate().getProfessional());
        candidateDto.setStatus(
                contract.getCandidate().getStatus() != null
                        ? contract.getCandidate().getStatus().name()
                        : null
        );

        // ⚠️ si tu veux aussi remplir demandeProfilId & demandeProfilName,
        // il faut la relation OfferCandidate → DemandeProfil

        return new ContractResponse(
                contract.getId(),
                candidateDto,
                contract.getDemande().getId(),
                contract.getDemande().getReference(),
                contract.getStartDate(),
                contract.getEndDate(),
                contract.getOriginalFileName(),
                contract.getUploadedAt()
        );
    }
}