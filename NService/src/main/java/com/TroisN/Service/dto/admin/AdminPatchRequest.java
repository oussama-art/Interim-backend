package com.TroisN.Service.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminPatchRequest
{

    private String firstName;
    private String lastName;

    @Email
    private String emailAddress;

    private String phoneNumber;

    @Min(0)
    private Integer experienceYear;


    @Min(0)
    private Integer adminLevel;
}
