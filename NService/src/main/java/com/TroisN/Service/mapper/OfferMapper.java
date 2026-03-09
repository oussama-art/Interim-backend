package com.TroisN.Service.mapper;

import com.TroisN.Service.dto.offer.*;
import com.TroisN.Service.entity.*;

import java.util.List;
import java.util.stream.Collectors;

public class OfferMapper {

    public static OfferResponse toOfferResponse(Offer offer) {
        OfferResponse response = new OfferResponse();

        response.setOfferId(offer.getId());
        response.setDemandeId(offer.getDemande().getId());
        response.setClientId(offer.getClient().getId());
        response.setCreatedAt(offer.getCreatedAt());

        List<OfferCandidateResponse> candidates = offer.getProposedCandidates()
                .stream()
                .map(OfferMapper::toOfferCandidateResponse)
                .collect(Collectors.toList());

        response.setProposedCandidates(candidates);

        return response;
    }

    public static OfferCandidateResponse toOfferCandidateResponse(OfferCandidate oc) {

        OfferCandidateResponse dto = new OfferCandidateResponse();

        Candidate c = oc.getCandidate();
        DemandeProfil p = oc.getDemandeProfil();

        dto.setCandidateId(c.getId());
        dto.setFirstName(c.getFirstName());
        dto.setLastName(c.getLastName());
        dto.setSkills(c.getSkills());
        dto.setProfessional(c.getProfessional());

        dto.setDemandeProfilId(p.getId());
        dto.setDemandeProfilName(p.getProfilName());

        dto.setStatus(oc.getStatus().name());

        return dto;
    }

}
