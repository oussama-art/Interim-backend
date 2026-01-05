package com.TroisN.Service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseUserRequest {

    private String firstName;
    private String lastName;
    private String phoneNumber;

    @Email
    private String emailAddress;
    private Integer experienceYear;
    @NotBlank
    private String password;

    @NotBlank
    private String confirmPassword;
}
