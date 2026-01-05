package com.TroisN.Service.dto.offer;

import lombok.Data;

@Data
public class OfferCandidateResponse {

    private Long candidateId;
    private String firstName;
    private String lastName;
    private String skills;
    private String professional;
    private String status;
}
