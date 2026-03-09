package com.TroisN.Service.dto.candidate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CandidateCreateRequest {

    private String firstName;

    private String lastName;

    @Pattern(
            regexp = "^\\+[1-9]\\d{1,2}\\d{6,12}$",
            message = "Numéro invalide (format invalide)"
    )
    private String phoneNumber;

    @Email(message = "Email invalide")
    private String emailAddress;

    @Min(value = 0, message = "L'expérience ne peut pas être négative")
    private Integer experienceYear;

    private String skills;

    private String professional;

    private String cin;

    private String cssNumber;

    private Boolean active;
}