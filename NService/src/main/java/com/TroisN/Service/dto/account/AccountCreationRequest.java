package com.TroisN.Service.dto.account;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class AccountCreationRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;


    @Email
    @NotBlank
    private String emailAddress;


    @NotEmpty(message = "Au moins un email supplémentaire est requis")
    private List<@Email String> additionalEmails;

    @Pattern(
            regexp = "^\\+[1-9]\\d{1,2}\\d{6,12}$",
            message = "Numéro invalide (format invalide)"
    )
    private String phoneNumber;

    @Min(0)
    private Integer experienceYear;

    @NotBlank
    private String companyTitle;


    private String companyDescription;

    private String sector;

    @Min(1)
    private Integer nbEmployee;

    @Min(1)
    private Integer requestedAccounts;
}
