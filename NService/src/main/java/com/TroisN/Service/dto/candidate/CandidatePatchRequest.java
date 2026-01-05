package com.TroisN.Service.dto.candidate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CandidatePatchRequest {

    // Champs User
    private String firstName;
    private String lastName;

    @Email
    private String emailAddress;

    private String phoneNumber;

    @Min(0)
    private Integer experienceYear;

    // Champs spécifiques à Candidate
    private String skills;
    private String professional;
    private String cin;
    private String cssNumber;

}
